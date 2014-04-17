package org.orange.parser.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.orange.parser.entity.Course;
import org.orange.parser.util.BitOperate;
import org.orange.parser.util.ReadPageHelper;
import org.orange.parser.util.StringUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectedCourseParser extends BaseCourseParser {
	/** 数字范围的模式字符串。其实例如：1-6, 4, 5 3 23 */
	private static final String NUMBER_RANGE_PATTERN_STRING = "\\d[\\d\\s,\\-]*";
	/** 周次字符串的模式，其group 1是数字部分。其实例如：1,4周, 3周 5 ,1-6周,9-12周(单) 1-6,4,5 周(单)，
	 * 它们的group 1分别为：<br/>1,4<br/>3<br/>5 ,1-6<br/>9-12<br/>1-6,4,5 */
	private static final Pattern WEEK_PATTERN =
			Pattern.compile("(" + NUMBER_RANGE_PATTERN_STRING + ")" + "周[\\(\\)单双]*");
	/** 节次（or课时or第几节课）字符串的模式，其group 1是数字部分。类似{@link #WEEK_PATTERN} */
	private static final Pattern PERIOD_PATTERN =
			Pattern.compile("(" + NUMBER_RANGE_PATTERN_STRING + ")" + "节");
	/** 星期的模式。其实例如：周二至周五 , 周二 至 周五 周四 周日 */
	private static final Pattern DAY_OF_WEEK_PATTERN =
			Pattern.compile("周[一二三四五六日]([\\s,周日一二三四五六至到]*[一二三四五六日])?");

	@Override
	public List<Course> parse() throws IOException {
		super.parse();
		try{
			List<Course> result = parse1();
			if(result.isEmpty())
				mParseListener.onWarn(ParseListener.WARNING_RESULT_IS_EMPTY, "结果为空，可能已选课程等页面已失效。");
			return result;
		} catch(IOException e) {
			mParseListener.onError(ParseListener.ERROR_IO, "遇到IO异常，无法打开页面，解析课程信息失败。 "+e.getMessage());
			throw e;
		}
	}

	List<Course> parse0(Document document) {
		List<Course> result = new LinkedList<>();
		for(Element table : document.select("div.ks-tabs-panel table.ui_table"))
			result.addAll(readCourseTable(table));
		return result;
	}
	List<Course> parse1() throws IOException {
		mConnectionAgent.getConnection().request().data().clear();
		//TODO 检测是否超过30000个课程
		mConnectionAgent.getConnection().data("iDisplayLength", "30000");
		Document doc = mConnectionAgent.url(Constant.url.LEARNING_COURSES).post();
		return parse0(doc);
	}

	@Override
	protected boolean parseTeachers(Course out, String rawData) {
		out.addTeacher(StringUtils.trim(rawData));
		return true;
	}

	@Override
	protected boolean parseTimeAndAddress(Course out, String rawTime, String rawAddress)
	throws BitOperate.BitOperateException, Course.TimeAndAddress.TimeAndAddressException {
		if(rawTime==null)
			throw new NullPointerException("Error: rawTime == null");
		if(rawAddress==null)
			throw new NullPointerException("Error: rawAddress == null");
		String[] times, addresses;
		times = splitRawTime(rawTime);
		addresses = splitRawAddress(rawAddress);
		if(times.length != addresses.length)
			throw new AssertionError("times.length != addresses.length");
		for(int index = 0 ; index<times.length ; index++){
			out.addTimeAndAddress(parseTime(times[index]).setAddress(addresses[index]));
		}
		return true;
	}
	private Course.TimeAndAddress parseTime(String time) throws BitOperate.BitOperateException, Course.TimeAndAddress.TimeAndAddressException {
		Course.TimeAndAddress result = new Course.TimeAndAddress();
		Matcher matcher = WEEK_PATTERN.matcher(time);
		while(matcher.find())
			addWeek(result, matcher.group(), matcher.group(1));
		matcher = DAY_OF_WEEK_PATTERN.matcher(matcher.replaceAll(""));
		while(matcher.find())
			result.addDays(matcher.group());
		matcher = PERIOD_PATTERN.matcher(matcher.replaceAll(""));
		while(matcher.find())
			result.addPeriods(matcher.group(1));
		String remnants = matcher.replaceAll("").replace(",", "").trim();
		if(remnants.length() != 0)
			mParseListener.onWarn(ParseListener.WARNINT_CANNOT_PARSE_TIME_AND_ADDRESS, "部分时间地点信息无法解析："+remnants);
		return result;
	}
	/**
	 * 把weekToken加入指定的{@link Course.TimeAndAddress}
	 * @param taa 把weekToken加入此{@link Course.TimeAndAddress}
	 * @param weekToken 周次字符串，如：1-6,9,11 周(单)
	 * @param weekNumber weekToken的数字部分，如：1-6,9,11
	 * @throws BitOperate.BitOperateException 当 参数非法（非以上格式，无法解析，或者超出0-20的范围）时
	 * @see Course.TimeAndAddress#addWeeks(String, Boolean)
	 */
	private static void addWeek(Course.TimeAndAddress taa, String weekToken, String weekNumber) throws BitOperate.BitOperateException {
		Boolean oddOrEven = null;
		if(weekToken.contains("单"))
			oddOrEven = true;
		else if (weekToken.contains("双"))
			oddOrEven = false;
		taa.addWeeks(weekNumber, oddOrEven);
	}

	private static String normalizeTime(String time) {
		time = time.replaceAll("[－\u2013\u2014\u2015]", "-");
		time = time.replaceAll("[\\s\u00a0\u3000]+", " ");
		time = time.replaceAll(";；，", ",");
		time = time.replaceAll("（", "(");
		time = time.replaceAll("）", ")");
		time = time.replaceAll("\\s+\\(\\s", "(");	// 去掉'('前后的空格
		time = time.replaceAll("\\s+\\)", ")");	// 去掉')'前的空格
		time = time.replaceAll("星期", "周");
		time = time.replaceAll("单\\s*周", "单");
		time = time.replaceAll("双\\s*周", "双");
		time = time.replaceAll("到", "至");
		return time;
	}
	/**
	 * 拆分成多组，每组对应一个{@link Course.TimeAndAddress}。
	 * @param rawTime 类似 ”1-5周&lt;br /&gt;8-14周 星期五 3-4节&lt;br /&gt;1-6周(单)&lt;br /&gt;9-12周(单) 星期二 7-8节“
	 * @return 类似 ["1-5周,8-14周 星期五 3-4节","1-6周(单),9-12周(单) 星期二 7-8节"]
	 */
	private static String[] splitRawTime(String rawTime) {
		rawTime = normalizeTime(rawTime);
		LinkedList<String> result = new LinkedList<>();
		String[] token = rawTime.split("<[^><]*>");
		StringBuilder buffer = new StringBuilder();
		for(String s:token){
			s = ReadPageHelper.trim(s);
			if(s.length() == 0)
				continue;
			if(WEEK_PATTERN.matcher(s).matches())
				buffer.append(s).append(",");
			else {
				result.add(buffer.toString() + s);
				buffer.setLength(0);
			}
		}
		return result.toArray(new String[result.size()]);
	}
	/**
	 * 拆分成多组，每组对应一个{@link Course.TimeAndAddress}。
	 */
	private static String[] splitRawAddress(String rawAddress){
		String[] first;
		LinkedList<String> second = new LinkedList<>();
		first = rawAddress.split("<[^><]*>");
		for(String s:first){
			s = s.trim();
			if(s.length()>0)
				second.add(s);
		}
		return second.toArray(new String[second.size()]);
	}
}
