package org.orange.parser.parser;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.orange.parser.entity.Course;
import org.orange.parser.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseCourseParser extends AbstractParser<List<Course>> {
	/** 学年学期（{@link Course.Property#ACADEMIC_YEAR_AND_SEMESTER}）的模式，有3个捕获组。
	 * 例如2013-2014学年第二学期的group1、2、3分别为2013、2014、二。 */
	private static final Pattern ACADEMIC_YEAR_AND_SEMESTER_PATTERN =
			Pattern.compile("(\\d{4})-(\\d{4})学年第?([一二三四五])学期");

	protected List<Course> readCourseTable(Element table) {
		LinkedList<Course> result = new LinkedList<>();
		Elements courses = table.getElementsByTag("tr");

		Map<Integer, Course.Property> headingMap = null;
		for(Element course:courses){
			if(course.text().trim().length()==0)
				continue;
			Elements cols = course.getElementsByTag("td");
			if(cols.size() > 1)
				result.add(readCourse(course, headingMap));
			else if(cols.isEmpty())
				headingMap = readHeading(course.getElementsByTag("th"));
			else		// have and only one column
				mParseListener.onInformation(ParseListener.INFO_SKIP, "Skip: "+course.text());
		}
		return result;
	}

	protected Course readCourse(
			Element tableRow, Map<Integer, Course.Property> headingMap) {
		if(headingMap == null){
			mParseListener.onError(ParseListener.NULL_POINTER, "headingMap是null，无法解析课程表格。");
			throw new NullPointerException("headingMap is null.");
		}
		String rawTime = null, rawAddress = null;
		Course result = new Course();
		Elements cols = tableRow.getElementsByTag("td");
		for(int i = 0 ; i < cols.size() ; i++) {
			Element col = cols.get(i);
			Course.Property property = headingMap.get(i);
			if(property == null) {
				mParseListener.onWarn(ParseListener.NULL_POINTER, "headingMap检查字段内容时，返回null。");
				throw new NullPointerException("Cannot find field from headingMap");
			}
			switch(property) {
				//TODO 旧教务处网站
				case CODE: case NAME: case TEACHING_CLASS/*旧教务处网站*/: case KIND:
					parseStringProperty(result, col.text(), property);
					break;
				case CREDIT: case TEST_SCORE: case TOTAL_SCORE:
				case ACADEMIC_YEAR: case SEMESTER: //TODO 这两个用于旧教务处网站
					parseNumberProperty(result, col.text(), property);
					break;
				case ACADEMIC_YEAR_AND_SEMESTER: // 新师生服务网站
					parseAcademicYearAndSemester(result, col.text(), property);
					break;
				case TEACHER:
					parseTeachers(result, col.text());
					break;
				case TIME:rawTime= col.getElementsByTag("span").html();break;
				case ADDRESS:rawAddress=col.getElementsByTag("span").html();break;
				// 忽略以下属性
				case TEACHING_MATERIAL: case GRADE_POINT:
					mParseListener.onInformation(ParseListener.INFO_SKIP, "忽略" + property + "："+col.text().trim());
					break;
				// 未知属性
				case UNKNOWN_COL: default:
					mParseListener.onWarn(ParseListener.WARNING_UNKNOWN_COLUMN, "未知列: "+col.text());break;
			}
		}

		if(rawTime!=null || rawAddress!=null){
			try{
				parseTimeAndAddress(result, rawTime, rawAddress);
			}catch(Exception e){
				mParseListener.onWarn(ParseListener.WARNINT_CANNOT_PARSE_TIME_AND_ADDRESS, "解析时间地点失败。因为：" + e.getMessage());
				e.printStackTrace();
			}
		}
		return result;
	}

	protected abstract boolean parseTeachers(Course out, String rawData);
	protected abstract boolean parseTimeAndAddress(Course out, String rawTime, String rawAddress) throws Exception;

	private boolean parseNumberProperty(Course out, String rawData, Course.Property property) {
		String temp = normalizeString(property, rawData);
		if(temp == null)
			return false;
		temp = temp.replaceAll("\\.0*$", ""); //删除多余的".0"，如师生服务网站查成绩时，学分用浮点数表示
		try {
			switch(property) {
				case CREDIT: out.setCredit(Integer.valueOf(temp)); break;
				case TEST_SCORE: out.setTestScore(Double.valueOf(temp)); break;
				case TOTAL_SCORE: out.setTotalScore(Double.valueOf(temp)); break;
				//TODO 下边两个旧教务处网站
				case ACADEMIC_YEAR: out.setYear(Integer.valueOf(temp)); break;
				case SEMESTER: out.setSemester(Integer.valueOf(temp)); break;
				default: throw new AssertionError("Shouldn't go here");
			}
		} catch (NumberFormatException e) {
			mParseListener.onWarn(ParseListener.WARNING_CANNOT_PARSE_NUMBER_DATA,
					"不能把字符串数据转换为数字，解析数字数据项" + property +"失败。详情：" + e.getMessage());
			return false;
		}
		return true;
	}
	private boolean parseStringProperty(Course out, String rawData, Course.Property property) {
		String result = normalizeString(property, rawData);
		if(result == null)
			return false;
		switch(property) {
			case CODE: out.setCode(result); break;
			case NAME: out.setName(result); break;
			case KIND: out.setKind(result); break;
			default: throw new AssertionError("Shouldn't go here");
		}
		return true;
	}

	private boolean parseAcademicYearAndSemester(Course out, String rawData, Course.Property property) {
		rawData = normalizeString(property, rawData);
		if(rawData == null)
			return false;
		Matcher matcher = ACADEMIC_YEAR_AND_SEMESTER_PATTERN.matcher(rawData);
		if(!matcher.matches()) {
			mParseListener.onWarn(ParseListener.WARNINT_CANNOT_PARSE_ACADEMIC_YEAR_AND_SEMESTER,
					"未知的学年学期格式" + rawData + "，解析学年学期失败。");
			return false;
		}
		boolean allGreen = true;
		// 学年
		try {
			int year1 = Integer.parseInt(matcher.group(1));
			int year2 = Integer.parseInt(matcher.group(2));
			if(year2 - year1 == 1) {
				out.setYear(year1);
			} else {
				mParseListener.onWarn(ParseListener.WARNINT_CANNOT_PARSE_ACADEMIC_YEAR_AND_SEMESTER,
						"非预期的学年："+rawData+"不是一年，解析学年失败。");
				allGreen = false;
			}
		} catch (NumberFormatException e) {
			mParseListener.onWarn(ParseListener.WARNING_CANNOT_PARSE_NUMBER_DATA,
					"不能把字符串数据转换为数字，解析数字数据项"+ property +"失败。详情："+e.getMessage());
			allGreen = false;
		}
		// 学期
		try {
			out.setSemester(valueOfChineseNumber(matcher.group(3)));
		} catch (UnsupportedOperationException e) {
			mParseListener.onWarn(ParseListener.WARNING_CANNOT_PARSE_NUMBER_DATA,
					"不能把字符串数据转换为数字，解析数字数据项"+ property +"失败。详情："+e.getMessage());
			allGreen = false;
		}
		return allGreen;
	}
	/**
	 * @param chineseNumber 一~五
	 * @return 与chineseNumber对应的数字
	 */
	private int valueOfChineseNumber(String chineseNumber) {
		switch(chineseNumber) {
			case "一":return 1;
			case "二":return 2;
			case "三":return 3;
			case "四":return 4;
			case "五":return 5;
			default: throw new UnsupportedOperationException("Unsupported Number: " + chineseNumber);
		}
	}

	private String normalizeString(Course.Property property, String rawData){
		if(property == Course.Property.NAME || property == Course.Property.TEACHER)
			rawData = StringUtils.trim(rawData);
		else
			rawData = StringUtils.deleteSpace(rawData);
		if(rawData == null || rawData.length() == 0){
			mParseListener.onWarn(ParseListener.WARNING_CANNOT_PARSE_STRING_DATA, "数据"+ property +"为空，解析失败。");
			return null;
		}
		else
			return rawData;
	}

	/**
	 * 解析课程表头，取得课程表列顺序
	 * @param tableHeaders 课程表头。{@code <th> cells}列表
	 * @return 列序号到字段意义的映射，用于查看每列的意义。
	 */
	protected Map<Integer, Course.Property> readHeading(Elements tableHeaders) {
		if(tableHeaders.isEmpty()) {
			mParseListener.onError(ParseListener.ERROR_CANNOT_PARSE_TABLE_HEADING, "课程表头为空,无法解析表头。");
			throw new IllegalArgumentException("encounter empty table headers");
		}
		int index = 0;
		Map<Integer, Course.Property> headMap = new HashMap<>();
		for(Element th:tableHeaders) {
			headMap.put(index++, Course.Property.fromString(StringUtils.deleteSpace(th.text())));
		}
		return headMap;
	}



}
