package util.webpage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.BitOperate.BitOperateException;
import util.webpage.Course.CourseException;
import util.webpage.Course.TimeAndAddress.TimeAndAddressException;

public class SchoolWebpageParser {

    private static final int SEQUENCE_NUMBER = 0;
    private static final int COURSE_CODE = 1;
    private static final int COURSE_NAME = 2;
    private static final int CLASS_NUMBER = 3;
    private static final int COURSE_TEACHER = 4;
    private static final int COURSE_CREDIT = 5;
    private static final int COURSE_TIME = 6;
    private static final int COURSE_ADDRESS = 7;
    
    private static final int COURSE_TEST_SCORE = 8;
    private static final int COURSE_TOTAL_SCORE = 9;
    private static final int COURSE_ACADEMIC_YEAR = 10;
    private static final int COURSE_SEMESTER = 11;

	
	/**
	 * 从给定来源，解析通知等文章
	 * @param postSource 来源，类似Post.CATEGORYS.TEACHING_AFFAIRS_WEBSITE
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @param readHelper 用于读取网页，你可以在readHelper中设置timeout、charset等
	 * @return 符合条件的Posts
	 */
    public static ArrayList<Post> parsePosts(int postSource, Date start, Date end, int max, 
    		ReadPageHelper readHelper){
    	ArrayList<Post> result = new ArrayList<Post>();
    	switch(postSource){
    	case Post.SOURCES.TEACHING_AFFAIRS_WEBSITE:
    		for(String aCategory:Post.CATEGORYS.CATEGORYS_IN_TEACHING_AFFAIRS_WEBSITE){
    			if(max>0 && result.size()>=max)
    				break;
    			result.addAll(parsePosts(postSource, aCategory, start , end, max-result.size(), readHelper));
    		}
    		return result;
    	default:return null;
    	}
    }
    /**
     * 从给定来源，根据指定的类别等条件，解析通知等文章
     * @param postSource 来源，类似Post.CATEGORYS.TEACHING_AFFAIRS_WEBSITE
     * @param aCategory 某具体类别，类似Post.CATEGORYS。CATEGORYS_IN_TEACHING_AFFAIRS_WEBSITE中的“重要通知”等
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @param readHelper 用于读取网页，你可以在readHelper中设置timeout、charset等
	 * @return 符合条件的Posts
     */
	public static ArrayList<Post> parsePosts(int postSource, String aCategory, Date start, Date end, 
			int max, ReadPageHelper readHelper) {
		switch(postSource){
		case Post.SOURCES.TEACHING_AFFAIRS_WEBSITE:
			return parsePostsFromTeachingAffairs(aCategory, start ,end ,max, readHelper);
		}
		return null;
	}
	/**
	 * 根据指定的类别等条件，从教务处网站解析通知等文章
	 * @param aCategory  某具体类别，类似Post.CATEGORYS。CATEGORYS_IN_TEACHING_AFFAIRS_WEBSITE中的“重要通知”等
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @param readHelper 用于读取网页，你可以在readHelper中设置timeout、charset等
	 * @return 符合条件的Posts
	 */
	public static ArrayList<Post> parsePostsFromTeachingAffairs(String aCategory, Date start, Date end, 
			int max, ReadPageHelper readHelper) {
		String url = null;
		Document doc = null;
		int page = 0;
		ArrayList<Post> result = new ArrayList<Post>();
		try {
			url = "http://59.67.148.66:8080/getRecords.jsp?url=list.jsp&pageSize=100&name=" 
					+ URLEncoder.encode(aCategory, "GB2312") + "&currentPage=";
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		try {
			doc = readHelper.getWithDocument(url+"1");
			page = Integer.parseInt( doc.body().select("table table table table")
					.get(1).select("tr td form font:eq(1)").text() );
			result.addAll(parsePostsFromTeachingAffairs(aCategory, start, end, max, doc));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		for(int i = 2;i<=page;i++){
			if(max>0 && result.size()>=max)
				break;
			try {
				doc = readHelper.getWithDocument(url+i);
				result.addAll(parsePostsFromTeachingAffairs(aCategory, start, end, max-result.size(), doc));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	/**
	 * 利用类别等信息，从指定的某教务处网页的Document文档对象，解析通知等文章
	 * @param aCategory  某具体类别，类似Post.CATEGORYS。CATEGORYS_IN_TEACHING_AFFAIRS_WEBSITE中的“重要通知”等
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @param doc 要解析的网页的Document文档对象模型
	 * @return 符合条件的Posts
	 */
	public static ArrayList<Post> parsePostsFromTeachingAffairs(String aCategory, Date start, Date end, 
			int max, Document doc) {
		ArrayList<Post> result = new ArrayList<Post>();
		Elements posts = doc.body().select("table table table table").get(0).getElementsByTag("tr");
		Element link = null;
		Post aPost = null;
		for(int i = 1;i<posts.size();i++){
			if(max>0 && result.size()>max)
				break;
			link = posts.get(i).getElementsByTag("a").first();
			aPost = new Post();
			try {
				aPost.setDate(link.nextSibling().outerHtml().trim().substring(1, 11));
				if(start!=null && aPost.getDate().before(start))
					continue;
				if(end!=null && aPost.getDate().after(end))
					continue;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			aPost.setCategory(aCategory).setTitle(link.text()).setUrl(link.attr("abs:href"));
			aPost.setSource(Post.SOURCES.TEACHING_AFFAIRS_WEBSITE);
			result.add(aPost);
		}
		return result;
	}
	/**
	 * 暂不可用
	 * @param url
	 * @param readPageHelper
	 * @param studentInfoToReturn
	 * @return
	 * @throws ParserException
	 * @throws IOException
	 */
	public static ArrayList<Course> parseCourse(String url, 
			ReadPageHelper readPageHelper, Student studentInfoToReturn) throws ParserException, IOException{
		Document doc = readPageHelper.getWithDocument(url);
		//student
		
		//courses
		return readCourseTable(doc.getElementsByTag("table").get(0), false);
	}
	/**
	 * 从URL指定的页面，使用指定的网络连接方法（readPageHelper），解析课程信息
	 * @param url 要读取的页面地址
	 * @param readPageHelper 使用它做网络连接，您可以在这设置用户名、密码、超时时间等
	 * @return 满足条件的课程信息
	 * @throws ParserException 不能正确读取课程表表头时
	 * @throws IOException 网络连接出现异常
	 */
	public static ArrayList<Course> parseCourse(String url, 
			ReadPageHelper readPageHelper) throws ParserException, IOException{
		return parseCourse(url, readPageHelper, null);
	}
	/**
	 * 暂不可用
	 * @param url
	 * @param readPageHelper
	 * @param studentInfoToReturn
	 * @return
	 * @throws ParserException
	 * @throws IOException
	 */
	public static ArrayList<Course> parseScores(String url, 
			ReadPageHelper readPageHelper, Student studentInfoToReturn) throws ParserException, IOException{
		Document doc = readPageHelper.getWithDocument(url);
		//student
		
		//courses
		return readCourseTable(doc.getElementsByTag("table").get(0), true);
	}
	/**
	 * 从URL指定的页面，使用指定的网络连接方法（readPageHelper），解析成绩
	 * @param url 要读取的页面地址
	 * @param readPageHelper 使用它做网络连接，您可以在这设置用户名、密码、超时时间等
	 * @return 满足条件的包含成绩信息的课程类
	 * @throws ParserException 不能正确读取课程表表头时
	 * @throws IOException 网络连接出现异常
	 */
	public static ArrayList<Course> parseScores(String url, 
			ReadPageHelper readPageHelper) throws ParserException, IOException{
		return parseScores(url, readPageHelper, null);
	}
	private static ArrayList<Course> readCourseTable(Element table, boolean hasScores) throws ParserException {
	    ArrayList<Course> result = new ArrayList<Course>();
	    Elements courses = table.getElementsByTag("tr");
	    
	    HashMap<Integer, Integer> headingMap = getHeading(courses.first());
	    if(headingMap == null)
	    	throw new ParserException("Can't getHeading because result is null.");
	    int size = hasScores?courses.size():courses.size()-1;
	    for(int i=1;i<size;i++)
			try {
				result.add(readCourse(courses.get(i), headingMap));
			} catch (Exception e) {
				System.out.println("Can't parse \""+courses.get(i).html()+"\".");
				e.printStackTrace();
			}
	    result.trimToSize();
	    if(!hasScores){
	    	int totalCredit = 0, totalCreditCalculated = 0;
	    	totalCredit = Integer.parseInt( courses.last().text().replaceAll("\\D+", "") );
		    
		    for(Course c:result)
		    	totalCreditCalculated += c.getCredit();
		    if(totalCredit != totalCreditCalculated)
		    	System.out.println("Warning: TotalCreditCalculated doesn't match " +
		    			"with totalCredit fetched from page .");
		    	//throw new ParserException(
		    	//		"TotalCreditCalculated doesn't match with totalCredit fetched from page.");
	    }
		return result;
	}
	private static Course readCourse(Element course, HashMap<Integer, Integer> headingMap){
		String rawTime = null, rawAddress = null;
		Course result = new Course();
		Elements cols = course.getElementsByTag("td");
		int i;
		Integer fieldCode;
		for(i = 0;i<cols.size();i++){
			fieldCode = headingMap.get(i);
			if(fieldCode == null)
				continue;
			switch(fieldCode){
			case SEQUENCE_NUMBER:continue;
			case COURSE_CODE:result.setCode(cols.get(i).text().trim());break;
			case COURSE_NAME:result.setName(ReadPageHelper.deleteSpace(cols.get(i).text()));break;
			case CLASS_NUMBER:result.setClassNumber(cols.get(i).text().trim());break;
			case COURSE_TEACHER:
				try {
					result.addTeacher(cols.get(i).text().trim());
				} catch (CourseException e) {
					System.out.println("Can't add teacher normally. Because " + e.getMessage());
				}
				break;
			case COURSE_CREDIT:
				try{
					result.setCredit(Byte.parseByte(cols.get(i).text()));
				}catch(Exception e){
					System.out.println("Can't parse credit normally. Because " + e.getMessage());
				}
				break;
			case COURSE_TIME:rawTime= cols.get(i).getElementsByTag("font").get(0).html();break;
			case COURSE_ADDRESS:rawAddress=cols.get(i).getElementsByTag("font").get(0).html();break;
			//成绩表：
			case COURSE_TEST_SCORE:
				try {
					result.setTestScore(Short.parseShort(cols.get(i).text()));
				} catch (NumberFormatException e) {
					System.out.println("Can't parse test score because can't parse to short.");
					e.printStackTrace();
				} catch (CourseException e) {
					System.out.println("Can't parse test score normally. Because " + e.getMessage());
					e.printStackTrace();
				}
				break;
			case COURSE_TOTAL_SCORE:
				try {
					result.setTotalScore(Short.parseShort(cols.get(i).text()));
				} catch (NumberFormatException e) {
					System.out.println("Can't parse total score because can't parse to short.");
					e.printStackTrace();
				} catch (CourseException e) {
					System.out.println("Can't parse total score normally. Because " + e.getMessage());
					e.printStackTrace();
				}
				break;
			case COURSE_ACADEMIC_YEAR:
				try {
					result.setYear(Short.parseShort(cols.get(i).text()));
				} catch (NumberFormatException e) {
					System.out.println("Can't parse academic year because can't parse to short.");
					e.printStackTrace();
				} catch (CourseException e) {
					System.out.println("Can't parse academic year normally. Because " + e.getMessage());
					e.printStackTrace();
				}
				break;
			case COURSE_SEMESTER:
				try{
					switch(Integer.parseInt(cols.get(i).text())){
					case 1:result.isFirstSemester(true);break;
					case 2:result.isFirstSemester(false);break;
					default:result.isFirstSemester(null);break;
					}
				}catch(NumberFormatException e){
					System.out.println("Can't parse semester because can't parse to int.");
					e.printStackTrace();
				}
				break;
			default:System.out.println("Unknown column.");break;
			}
		}
		if(rawTime!=null || rawAddress!=null){
			try{
				readTimeAndAddress(result, rawTime, rawAddress);
			}catch(Exception e){
				System.out.println(
						"Can't parse time&address normally. Because " + e.getMessage());
			}
		}
		return result;
	}
	private static void readTimeAndAddress(Course result, String rawTime, String rawAddress) 
			throws ParserException, TimeAndAddressException, BitOperateException {
		if(rawTime==null)
			throw new ParserException("Error: rawTime == null");
		if(rawAddress==null)
			throw new ParserException("Error: rawAddress == null");
		String[] times, addresses ,timesSplited;
		times = splitTimeOrAddress(rawTime);
		addresses = splitTimeOrAddress(rawAddress);
		if(times.length != addresses.length)
			throw new ParserException("Error: times.length != addresses.length");
		for(int index = 0;index<times.length;index++){
			timesSplited = splitTime(times[index]);
			result.addTimeAndAddress(timesSplited[0], timesSplited[1], 
					timesSplited[2], addresses[index]);
		}
	}
	private static String[] splitTime(String time) throws ParserException{
		int counter = 0;
		String[] result = new String[3];
		Pattern pattern = Pattern.compile(
				"(\\d[\\d\\s\u00a0\u3000;；,，\\-－\u2013\u2014\u2015]*\\d)|" +
				"((星期|周)[一二三四五六日]([\\s\u00a0\u3000;；,，星期周日一二三四五六至到]*[一二三四五六日])?)");
		Matcher matcher = pattern.matcher(time);
		while(matcher.find())
			if(counter<3){
				result[counter] = matcher.group();
				counter++;
			}
			else
				throw new ParserException("Unexpected time String.");
		if(counter != 3)
			throw new ParserException("Unexpected time String.");
		return result;
	}
	/*
	private static String[] splitTime(String time) throws ParserException{
		int counter = 0;
		String[] result = new String[3];
		Pattern numberPattern = Pattern.compile(
				"\\d[\\d\\s\u00a0\u3000;；,，\\-－\u2013\u2014\u2015]*\\d");
		Pattern dayOfWeekPattern = Pattern.compile(
				"(星期|周)[一二三四五六日]([\\s\u00a0\u3000;；,，星期周日一二三四五六至到]*[一二三四五六日])?");
		Matcher numberMatcher = numberPattern.matcher(time);
		Matcher dayOfWeekMatcher = dayOfWeekPattern.matcher(time);
		while(numberMatcher.find()){
			counter++;
			switch(counter){
			case 1:result[0] = numberMatcher.group();break;
			case 2:result[2] = numberMatcher.group();break;
			default:throw new ParserException("Unexpected time String.");
			}
		}
		if(dayOfWeekMatcher.find())
			result[1] = dayOfWeekMatcher.group();
		return result;
	}*/
	/*
	private String[] splitTime(String time) throws ParserException{
		time = time.trim();
		if(!time.matches(".*节"))
			throw new ParserException("Can't match the time.");
		ArrayList<String> result = new ArrayList<String>(3);
		String[] splited = time.split("周");
		result.add(splited[0].trim());
		splited = splited[1].split("[\\s\u00a0\u3000]");
		int lastWeekString = 0,i;
		for(i = 0;i<splited.length;i++)
			if(splited[i].matches(".*[星期周一二三四五六日].*") && i>lastWeekString)
				lastWeekString = i;
		String temp = "";
		for(i = 0;i<=lastWeekString;i++){
			splited[i] = splited[i].trim();
			if(splited[i].length()>0)
				temp += splited[i] + " ";
		}
		result.add(temp.trim());
		temp = "";
		for(i = lastWeekString;i<splited.length;i++){
			splited[i] = splited[i].trim();
			if(splited[i].length()>0){
				if(!splited[i].matches(".*节.*"))
					temp += splited[i] + " ";
				else if(splited[i].matches(".*节"))
					temp += splited[i].replace("节", "");
			}
		}
	}*/
	private static String[] splitTimeOrAddress(String timeOrAddress){
		String[] first;
		ArrayList<String> second = new ArrayList<String>();
		first = timeOrAddress.split("<[^><]*>");
		for(String s:first){
			s.trim();
			if(s.length()>0)
				second.add(s);
		}
		return (String[])second.toArray(new String[0]);
	}
	private static HashMap<Integer, Integer> getHeading(Element heading) throws ParserException {
		String colName = null;
		HashMap<Integer, Integer> headMap = new HashMap<Integer, Integer>(8);
		Elements cols = heading.getElementsByTag("td");
		if(cols == null || cols.isEmpty())
			throw new ParserException("Can't getHeading because no td tag in first line.");
		for(int i=0;i<cols.size();i++){
			colName = ReadPageHelper.deleteSpace(cols.get(i).text());
			//assert colName != null;
			if("序号".equals(colName))
				headMap.put(i, SEQUENCE_NUMBER);
			else if("课程代码".equals(colName))
				headMap.put(i, COURSE_CODE);
			else if("课程编码".equals(colName))
				headMap.put(i, COURSE_CODE);
			else if("课程名称".equals(colName))
				headMap.put(i, COURSE_NAME);
			else if("教学班号".equals(colName))
				headMap.put(i, CLASS_NUMBER);
			else if("教师".equals(colName))
				headMap.put(i, COURSE_TEACHER);
			else if("学分".equals(colName))
				headMap.put(i, COURSE_CREDIT);
			else if("时间".equals(colName))
				headMap.put(i, COURSE_TIME);
			else if("地点".equals(colName))
				headMap.put(i, COURSE_ADDRESS);
			else if("结课考核成绩".equals(colName))
				headMap.put(i, COURSE_TEST_SCORE);
			else if("期末总评成绩".equals(colName))
				headMap.put(i, COURSE_TOTAL_SCORE);
			else if("学年".equals(colName))
				headMap.put(i, COURSE_ACADEMIC_YEAR);
			else if("学期".equals(colName))
				headMap.put(i, COURSE_SEMESTER);
		}
		return headMap;
	}
	
	public static class ParserException extends Exception{
		private static final long serialVersionUID = 3737828070910029299L;
		public ParserException(String message){
			super(message + " @SchoolWebpageParser");
		}
		public ParserException(){
			this("encounter Exception when parse school page.");
		}
	}
}
