package util.webpage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.BitOperate.BitOperateException;
import util.webpage.Course.CourseException;
import util.webpage.Course.TimeAndAddress;
import util.webpage.Course.TimeAndAddress.TimeAndAddressException;

public class SchoolWebpageParser {
	/** 数字范围的模式字符串。其实例如：1-6, 4, 5 3 23 */
	private static final String NUＭBER_RANGE_PATTERN_STRING = "\\d[\\d\\s,\\-]*";
	/** 周次字符串的模式，其group 1是数字部分。其实例如：1,4周, 3周 5 ,1-6周,9-12周(单) 1-6,4,5 周(单)，
	 * 它们的group 1分别为：<br/>1,4<br/>3<br/>5 ,1-6<br/>9-12<br/>1-6,4,5 */
	private static final Pattern WEEK_PATTERN =
			Pattern.compile("(" + NUＭBER_RANGE_PATTERN_STRING + ")" + "周[\\(\\)单双]*");
	/** 节次（or课时or第几节课）字符串的模式，其group 1是数字部分。类似{@link #WEEK_PATTERN} */
	private static final Pattern PERIOD_PATTERN =
			Pattern.compile("(" + NUＭBER_RANGE_PATTERN_STRING + ")" + "节");
	/** 星期的模式。其实例如：周二至周五 , 周二 至 周五 周四 周日 */
	private static final Pattern DAY_OF_WEEK_PATTERN =
			Pattern.compile("周[一二三四五六日]([\\s,周日一二三四五六至到]*[一二三四五六日])?");
	/** 学年学期（{@link Headings#COURSE_ACADEMIC_YEAR_AND_SEMESTER}）的模式，有3个捕获组。
	 * 例如2013-2014学年第二学期的group1、2、3分别为2013、2014、二。 */
	private static final Pattern ACADEMIC_YEAR_AND_SEMESTER_PATTERN =
			Pattern.compile("(\\d{4})-(\\d{4})学年第?([一二三四五])学期");

	private ParserListener listener;
	private ReadPageHelper autoReadHelper;
	private ReadPageHelper readHelper;

	private static final Headings HEADINGS = new Headings();
	
	/**
	 * 无参构造方法
	 */
	public SchoolWebpageParser() {
		super();
		listener = new ParserListenerAdapter();
		autoReadHelper = new ReadPageHelper();
		autoReadHelper.setCharset("UTF-8");
		autoReadHelper.setCharsetForParsePostsFromSCCE("UTF-8");
		readHelper = null;
	}
	/**
	 * 仅设置监听器
	 * @param listener
	 * @throws CloneNotSupportedException
	 */
	public SchoolWebpageParser(ParserListener listener) throws CloneNotSupportedException {
		this();
		setListener(listener);
	}
	/**
	 * 仅设置监听器以及用户名、密码（设置到自动readPageHelper里）
	 * @param listener
	 * @param userName
	 * @param password
	 * @throws CloneNotSupportedException
	 */
	public SchoolWebpageParser(ParserListener listener, String userName, String password) throws CloneNotSupportedException {
		this(listener);
		setUser(userName, password);
	}
	/**
	 * 设置监听器和读取网页帮助类
	 * @param listener
	 * @param readHelper
	 * @throws CloneNotSupportedException
	 */
	public SchoolWebpageParser(ParserListener listener, ReadPageHelper readHelper) throws CloneNotSupportedException{
		this(listener);
		setReadHelper(readHelper);
		autoReadHelper.setUser(readHelper.getUserName(), readHelper.getPassword());
	}
	
	/**
	 * @return the listener
	 */
	public ParserListener getListener() {
		return listener;
	}
	/**
	 * @param listener the listener to set
	 * @throws CloneNotSupportedException 
	 */
	public void setListener(ParserListener listener) throws CloneNotSupportedException {
		this.listener = listener.clone();
	}
	/**
	 * 设置ReadPageHelper的监听器。总是设置内部自动生成的helper，若已自定义helper，也会同时设置
	 * @param listener
	 */
	public void setOnReadPageListener(ReadPageHelper.OnReadPageListener listener){
		autoReadHelper.setListener(listener);
		if(readHelper != null)
			readHelper.setListener(listener);
	}
	/**
	 * 取得 用户最近通过
	 * {@link SchoolWebpageParser#setListener(ParserListener) setListener(ParserListener)}
	 * 设置的ReadPageHelper（用于登录，读取页面等）。对之的修改会应用到本对象。
	 * @return 用户最近设置的ReadPageHelper，若您之前没有设置过则返回null
	 */
	public ReadPageHelper getReadHelper() {
		return readHelper;
	}
	/**
	 * 取得 本对象自动设置的ReadPageHelper（用于登录，读取页面等），这个helper只在您没有自行通过
	 * {@link SchoolWebpageParser#setListener(ParserListener) setListener(ParserListener)}
	 * 设置过ReadHelper时使用。对之的修改会应用到本对象自动生成的ReadHelper上。
	 * @return 本对象自动设置的ReadPageHelper
	 */
	public ReadPageHelper getAutoReadHelper() {
		return autoReadHelper;
	}
	/**
	 * 返回当前使用的ReadPageHelper
	 * @return 如果您自定义过ReadPageHelper，返回您上次定义的ReadPageHelper；否则返回本对象自动生成的ReadPageHelper
	 */
	public ReadPageHelper getCurrentHelper(){
		return readHelper!=null?readHelper:autoReadHelper;
	}
	/**
	 * 登录后，再返回当前使用的ReadPageHelper
	 * @return 如果您自定义过ReadPageHelper，返回您上次定义的ReadPageHelper；否则返回本对象自动生成的ReadPageHelper
	 * @throws ParserException 登录失败
	 * @throws IOException doLogin进行post请求时遇到error
	 */
	public ReadPageHelper getCurrentHelperAfterLogin() throws ParserException, IOException{
		ReadPageHelper readHelper = getCurrentHelper();
		try{
			if(!readHelper.doLogin()){
				this.listener.onError(ParserListener.ERROR_CANNOT_LOGIN, "登录失败。");
				throw new ParserException("Cannot login website.");
			}
		}catch(IOException e){
			listener.onError(ParserListener.ERROR_IO, "遇到IO异常，无法登录。 "+e.getMessage());
			throw e;
		}
		return readHelper;
	}
	/**
	 * 设置ReadPageHelper，它用于登录，读取页面等工作。
	 * @param readHelper 用于读取网页的ReadPageHelper，您可以在readHelper中设置timeout、charset等
	 * @throws CloneNotSupportedException 用{@link ReadPageHelper#clone()}复制readHelper时遇到此异常
	 */
	public void setReadHelper(ReadPageHelper readHelper) throws CloneNotSupportedException {
		this.readHelper = readHelper.clone();
	}
	/**
	 * 设置用户名和密码。
	 * @param userName 用户名
	 * @param password 密码
	 * @throws NullPointerException
	 */
	public void setUser(String userName, String password){
		if(readHelper != null)
			readHelper.setUser(userName, password);
		autoReadHelper.setUser(userName, password);
	}
	/**
	 * 设置ReadPageHelper的超时时间，单位milliseconds
	 * @param timeout 超时时间，单位milliseconds
	 * @return 参数大于0返回真；参数小于等于0忽略本次调用，忽略此次调用，返回假
	 */
	public boolean setTimeout(int timeout) {
		if(readHelper != null)
			readHelper.setTimeout(timeout);
		return autoReadHelper.setTimeout(timeout);
	}
	/**
	 * 从给定来源，在指定的categories类别中，解析通知等文章
	 * @param postSource 来源，类似Post.CATEGORYS.TEACHING_AFFAIRS_WEBSITE
	 * @param categories 指定类别范围，是String[]，内容类似Post.CATEGORYS.TEACHING_AFFAIRS_NOTICES。空数组或首项为null时不分类别全解析
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @return 符合条件的posts；如果postSource不可识别，返回空ArrayList
	 * @throws IOException 链接超时等IO异常
	 * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
	 */
    public List<Post> parsePosts(byte postSource, String[] categories, Date start, 
    		Date end, int max) throws UnsupportedEncodingException, IOException{
    	LinkedList<Post> result = new LinkedList<Post>();
    	switch(postSource){
    	case Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS:
    		if(categories==null || categories.length==0 || categories[0]==null)
    			categories = Post.CATEGORYS.IN_TEACHING_AFFAIRS_WEBSITE;
    		for(String aCategory:categories){
    			if(max<0 || result.size()<max)
    				result.addAll(parsePostsFromTeachingAffairs(aCategory, start , end, max-result.size()));
    		}
    	break;
    	case Post.SOURCES.WEBSITE_OF_SCCE:
    		if(categories==null || categories.length==0 || categories[0]==null){
	    		result.addAll(parsePostsFromSCCE(null, start, end, max, Post.SOURCES.NOTICES_IN_SCCE_URL));
	    		if(max<0 || result.size()<max)
	    			result.addAll(parsePostsFromSCCE(null, start, end, max-result.size(), Post.SOURCES.NEWS_IN_SCCE_URL));
    		}else{
    			LinkedList<String> categoriesInNotices = new LinkedList<String>();
    			LinkedList<String> categoriesInNews = new LinkedList<String>();
    			for(String aCategory:categories){
    				if(aCategory.matches(".*通知.*"))
    					categoriesInNotices.add(aCategory);
    				else if(aCategory.matches(".*新闻.*"))
    					categoriesInNews.add(aCategory);
    			}
    			if(!categoriesInNotices.isEmpty() && (max<0 || result.size()<max))
    				result.addAll(parsePostsFromSCCE(categoriesInNotices.toArray(new String[0]), 
    						start, end, max, Post.SOURCES.NOTICES_IN_SCCE_URL));
    			if(!categoriesInNews.isEmpty() && (max<0 || result.size()<max))
    				result.addAll(parsePostsFromSCCE(categoriesInNews.toArray(new String[0]), start,
    						end, max-result.size(), Post.SOURCES.NEWS_IN_SCCE_URL));
    		}
    	break;
    	case Post.SOURCES.STUDENT_WEBSITE_OF_SCCE:
    		if(categories==null || categories.length==0 || categories[0]==null)
    			categories = Post.CATEGORYS.IN_STUDENT_WEBSITE_OF_SCCE;
    		for(String aCategory:categories){
    			if(max<0 || result.size()<max)
    				result.addAll(parsePostsFromSCCEStudent(aCategory, start, end, max-result.size()));
    		}
    	break;
    	case Post.SOURCES.UNKNOWN_SOURCE:
    		if(categories==null || categories.length==0 || categories[0]==null){
				if(max>=0 && result.size()>=max)
    				break;
    			result.addAll(parsePosts(Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS,categories, start, end, max));
    			if(max>=0 && result.size()>=max)
    				break;
    			result.addAll(parsePosts(Post.SOURCES.WEBSITE_OF_SCCE, categories , start, end, max-result.size()));
    			if(max>=0 && result.size()>=max)
    				break;
    			result.addAll(parsePosts(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE, categories, start, end, max-result.size()));
    		}
    		else{
    			for(String tested:categories){
    				if(tested==null) continue;
    				if(max>=0 && result.size()>=max)
        				break;
    				for(String aCategory:Post.CATEGORYS.IN_TEACHING_AFFAIRS_WEBSITE)
    					if(aCategory.equals(tested)){
    						result.addAll(parsePostsFromTeachingAffairs(tested, start, end, max));
    						break;
    					}
    				if(max>=0 && result.size()>=max)
        				break;
    				for(String aCategory:Post.CATEGORYS.IN_SCCE)
    					if(aCategory.equals(tested)){
    						result.addAll(parsePostsFromSCCE(new String[]{tested}, start, end, max-result.size(), (String)null));
    						break;
    					}
    				if(max>=0 && result.size()>=max)
        				break;
    				for(String aCategory:Post.CATEGORYS.IN_STUDENT_WEBSITE_OF_SCCE)
    					if(aCategory.equals(tested)){
    						result.addAll(parsePostsFromSCCEStudent(tested, start, end, max-result.size()));
    						break;
    					}
    			}
    		}
    	break;
    	default:
    	}
    	return result;
    }
    /**
     * 在指定的categories类别中，解析通知等文章
     * @param categories 指定类别范围，是String[]，内容类似Post.CATEGORYS.TEACHING_AFFAIRS_NOTICES。空数组或首项为null时不分类别全解析
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
     * @return 符合条件的posts
     * @throws IOException 链接超时等IO异常
     * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
     */
    public List<Post> parsePosts(String[] categories, Date start, Date end, int max) throws UnsupportedEncodingException, IOException{
    	return parsePosts(Post.SOURCES.UNKNOWN_SOURCE, categories, start, end, max);
    }
    /**
     * 在指定的category类别中，解析通知等文章
     * @param aCategory 某具体类别，类似Post.CATEGORYS.TEACHING_AFFAIRS_NOTICES等
     * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
     * @return 符合条件的posts
     * @throws IOException 链接超时等IO异常
     * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
     */
    public List<Post> parsePosts(String aCategory, Date start, Date end, int max) throws UnsupportedEncodingException, IOException{
    	return parsePosts(Post.SOURCES.UNKNOWN_SOURCE, aCategory, start, end, max);
    }
    /**
     * 从所有可处理来源中，解析通知等文章
     * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
     * @return 符合条件的posts
     * @throws IOException 链接超时等IO异常
     * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
     */
    public List<Post> parsePosts(Date start, Date end, int max) throws UnsupportedEncodingException, IOException{
    	return parsePosts(Post.SOURCES.UNKNOWN_SOURCE, start, end, max);
    }
    /**
     * 从所有可处理来源的常用类别中，解析通知等文章
     * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
     * @return 符合条件的posts
     * @throws IOException 链接超时等IO异常
     * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
     */
    public List<Post> parseCommonPosts(Date start, Date end, int max) throws UnsupportedEncodingException, IOException{
    	LinkedList<Post> result = new LinkedList<Post>();
    	if(max>=0 && result.size()>=max)
			return result;
		result.addAll(parsePosts(Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS,Post.CATEGORYS.IN_TEACHING_AFFAIRS_WEBSITE_COMMON, start, end, max));
		if(max>=0 && result.size()>=max)
			return result;
		result.addAll(parsePosts(Post.SOURCES.WEBSITE_OF_SCCE, (String[])null , start, end, max-result.size()));
		if(max>=0 && result.size()>=max)
			return result;
		result.addAll(parsePosts(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE,Post.CATEGORYS.IN_STUDENT_WEBSITE_OF_SCCE_COMMON, start, end, max-result.size()));
		return result;
    }
    /**
	 * 从给定来源，解析通知等文章
	 * @param postSource 来源，类似Post.CATEGORYS.TEACHING_AFFAIRS_WEBSITE
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @return 符合条件的posts；如果postSource不可识别，返回null
     * @throws IOException 
     * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
	 */
    public List<Post> parsePosts(byte postSource, Date start, 
    		Date end, int max) throws UnsupportedEncodingException, IOException{
    	String[] categories = null;
    	return parsePosts(postSource, categories, start, end, max);
    }
    /**
     * 从给定来源，根据指定的类别等条件，解析通知等文章
     * @param postSource 来源，类似Post.CATEGORYS.TEACHING_AFFAIRS_WEBSITE
     * @param aCategory 某具体类别，类似Post.CATEGORYS.TEACHING_AFFAIRS_NOTICES等
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @return 符合条件的posts；如果postSource不可识别，返回null
     * @throws IOException 
     * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
     */
	public List<Post> parsePosts(byte postSource, String aCategory, Date start, Date end, 
			int max) throws UnsupportedEncodingException, IOException {
		String[] categories = new String[]{aCategory};
		return parsePosts(postSource, categories, start, end, max);
	}
	
	/**
	 * 根据指定的类别等条件，从教务处网站解析通知等文章
	 * @param aCategory  某具体类别，类似Post.CATEGORYS.TEACHING_AFFAIRS_NOTICES等
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @return 符合条件的posts
	 * @throws UnsupportedEncodingException 设置URL时，GB2312编码异常
	 * @throws IOException
	 */
	public List<Post> parsePostsFromTeachingAffairs(String aCategory, Date start, Date end, int max) throws UnsupportedEncodingException, IOException {
		ReadPageHelper readHelper = getCurrentHelper();
		if(aCategory == null)
			return parsePosts(Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS, start, end, max);
		String url = null;
		Document doc = null;
		int page = 0;
		LinkedList<Post> result = new LinkedList<Post>();
		if(max == 0) return result;
		try {
			url = "http://59.67.148.66:8080/getRecords.jsp?url=list.jsp&pageSize=100&name=" 
					+ URLEncoder.encode(aCategory, "GB2312") + "&currentPage=";
		} catch (UnsupportedEncodingException e) {
			listener.onError(ParserListener.ERROR_UNSUPPORTED_ENCODING, "遇到编码异常，解释教务处信息失败。 "+e.getMessage());
			throw e;
		}
		try {
			doc = readHelper.getWithDocument(url+"1");
			result.addAll(parsePostsFromTeachingAffairs(aCategory, start, end, max, doc));
			page = Integer.parseInt( doc.body().select("table table table table")
					.get(1).select("tr td form font:eq(1)").text() );
			
			for(int i = 2;i<=page;i++){
				if(max>=0 && result.size()>=max)
					break;
				try {
					if(start!=null && Post.convertToDate(doc.body().select("table table table table")
							.get(0).getElementsByTag("tr").last().getElementsByTag("a").first()
							.nextSibling().outerHtml().trim().substring(1, 11)).before(start))
						break;
				} catch (ParseException e) {
					listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_DATE, "解析教务处通知/文章的日期失败。 "+e.getMessage());
				}
				doc = readHelper.getWithDocument(url+i);
				result.addAll(parsePostsFromTeachingAffairs(aCategory, start, end, max-result.size(), doc));
			}
		} catch (IOException e) {
			listener.onError(ParserListener.ERROR_IO, "遇到IO异常，无法打开页面，解析教务处信息失败。 "+e.getMessage());
			throw e;
		}
		return result;
	}
	/**
	 * 利用类别等信息，从指定的某教务处网页的Document文档对象，解析通知等文章
	 * @param aCategory  某具体类别，类似Post.CATEGORYS.TEACHING_AFFAIRS_NOTICES等
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @param doc 要解析的网页的Document文档对象模型
	 * @return 符合条件的posts
	 */
	public List<Post> parsePostsFromTeachingAffairs(String aCategory, Date start, Date end, 
			int max, Document doc) {
		LinkedList<Post> result = new LinkedList<Post>();
		Elements posts = doc.body().select("table table table table").get(0).getElementsByTag("tr");
		Element link = null;
		Post aPost = null;
		for(int i = 1;i<posts.size();i++){
			if(max>=0 && result.size()>=max)
				break;
			link = posts.get(i).getElementsByTag("a").first();
			aPost = new Post();
			try {
				//解析日期
				aPost.setDate(link.nextSibling().outerHtml().trim().substring(1, 11));
				if(end!=null && aPost.getDate().after(end))
					continue;
				if(start!=null && aPost.getDate().before(start))
					break;
			} catch (ParseException e) {
				listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_DATE, "解析教务处通知/文章的日期失败。 "+e.getMessage());
			}
			aPost.setCategory(aCategory).setTitle(link.text()).setUrl(link.attr("abs:href"));
			aPost.setSource(Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS);
			result.add(aPost);
		}
		return result;
	}
	
	/**
	 * 根据aCategory、start、end、max、baseURL等条件，利用readHelper，从SCCE解析posts
	 * @param aCategory 类别，例如“学生通知”
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @param baseURL 指定解析的基础URL，类似Post.SOURCES.NOTICES_IN_SCCE_URL
	 * @return 符合条件的posts
	 * @throws IOException 
	 */
	public List<Post> parsePostsFromSCCE(String[] categories, Date start, Date end, 
			int max, String baseURL) throws IOException{
		ReadPageHelper readHelper = getCurrentHelper();
		Document doc = null;
		int page = 0;
		LinkedList<Post> result = new LinkedList<Post>();
		if(max == 0) return result;
		if(baseURL == null){
			if(categories == null)
				return parsePosts(Post.SOURCES.WEBSITE_OF_SCCE, start, end, max);
			boolean hasNew = false, hasNotice = false;
			for(String aCategory:categories){
				if(aCategory.matches(".*通知.*"))
					hasNotice = true;
				else if(aCategory.matches(".*新闻.*"))
					hasNew = true;
			}
			if(hasNotice && !hasNew)
				baseURL = Post.SOURCES.NOTICES_IN_SCCE_URL;
			else if(!hasNotice && hasNew)
				baseURL = Post.SOURCES.NEWS_IN_SCCE_URL;
			else if(hasNotice && hasNew)
				return parsePosts(Post.SOURCES.WEBSITE_OF_SCCE, categories, start, end, max);
			else
				return result;
		}	
		baseURL += "?page=";
		try {
			readHelper.prepareToParsePostsFromSCCE();
			doc = readHelper.getWithDocumentForParsePostsFromSCCE(baseURL+"1");
			Matcher matcher = Pattern.compile("\\?page=(\\d+)").matcher(doc.body()
					.select("a[href*=more.aspx?page=]").last().attr("href"));
			if(matcher.find())
				page = Integer.parseInt(matcher.group(1));
			else
				listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_PAGE_NUMBER, "不能从计算机学院网站解析页数，现仅解析第一页内容。");
			result.addAll(parsePostsFromSCCE(categories, start, end ,max ,doc));

			for(int i = 2;i<=page;i++){
				if(max>=0 && result.size()>=max)
					break;
				try {
					if(start!=null && Post.convertToDate(ReadPageHelper.deleteSpace(doc
							.select("form table table").first().getElementsByTag("tr").last()
							.getElementsByTag("td").get(3).text())).before(start))
						break;
				} catch (ParseException e) {
					listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_DATE, "解析计算机学院网站通知/文章的日期失败。 "+e.getMessage());
				}
				doc = readHelper.getWithDocumentForParsePostsFromSCCE(baseURL+i);
				result.addAll(parsePostsFromSCCE(categories, start, end, max-result.size(), doc));
			}
		} catch (IOException e) {
			listener.onError(ParserListener.ERROR_IO, "遇到IO异常，无法打开页面，解析计算机学院网站信息失败。 "+e.getMessage());
			throw e;
		}
		return result;
	}
	/**
	 * 以aCategory、start、end、max为限制条件，从计算机学院页面doc中解析posts
	 * @param aCategory 类别，例如“学生通知”
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @param doc 包含post列表的 某计算机学院网页的 Document
	 * @return 符合条件的posts
	 */
	public List<Post> parsePostsFromSCCE(
			String[] categories, Date start, Date end, int max, Document doc) {
		Post post;
		Elements cols = null;
		Pattern pattern = Pattern.compile("openwin\\('(.*)'\\)");
		Matcher matcher = null;
		LinkedList<Post> result = new LinkedList<Post>();
		Elements posts = doc.select("form table table").first().getElementsByTag("tr");
		posts.remove(0);
		for(Element postTr:posts){
			if(max>=0 && result.size()>=max)
				break;
			cols = postTr.getElementsByTag("td");
			//TODO 因为期中期末时间地点通对象也是教师，为显示期中期末通知，暂去掉下边的检测
//			//验证通知对象
//			String temp = ReadPageHelper.deleteSpace(cols.get(4).text());
//			if(temp.equals("教师") || temp.equals("全体教师"))
//				continue;
			
			post = new Post();
			//验证Category
			post.setCategory(ReadPageHelper.deleteSpace(cols.get(1).text()));
			if(categories!=null){
				boolean isContained = false;
				for(String aCategory:categories)
					if(aCategory.equals(post.getCategory()))
						isContained = true;
				if(!isContained)
					continue;
			}
			//验证日期
			try {
				post.setDate(ReadPageHelper.deleteSpace(cols.get(3).text()));
				if(end!=null && post.getDate().after(end))
					continue;
				if(start!=null && post.getDate().before(start))
					break;
			} catch (ParseException e) {
				listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_DATE, "解析计算机学院网站通知/文章的日期失败。 "+e.getMessage());
			}
			//设置 title、url、author、source
			post.setTitle(cols.get(0).text().trim());
			matcher = pattern.matcher(cols.get(0).getElementsByTag("a").attr("onclick"));
			if(matcher.find())
				post.setUrl("http://59.67.152.3/"+matcher.group(1));
			else
				listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_URL, "解析计算机学院网站通知/文章的URL失败。 ");
			post.setAuthor(ReadPageHelper.deleteSpace(cols.get(2).text()));
			post.setSource(Post.SOURCES.WEBSITE_OF_SCCE);
			result.add(post);
		}
		return result;
	}
	
	/**
	 * 根据aCategory、start、end、max等条件，利用readHelper，从SCCE学生网站解析posts
	 * @param aCategory 类别，例如Post.CATEGORYS.SCCE_STUDENT_NOTICES
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @return 符合条件的posts；如果aCategory不可识别，返回空List
	 * @throws IOException 
	 */
	public List<Post> parsePostsFromSCCEStudent(String aCategory, Date start, Date end, int max) throws IOException{
		ReadPageHelper readHelper = getCurrentHelper();
		if(aCategory == null)
			return parsePosts(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE, start, end, max);
		int page = 0;
		Document doc = null;
		String url = "http://59.67.152.6/Channels/";
		LinkedList<Post> result = new LinkedList<Post>();
		if(max == 0) return result;
		if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_NEWS))
			url += "7";
		else if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_NOTICES))
			url += "9";
		else if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_UNION))
			url += "45";
		else if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_EMPLOYMENT))
			url += "43";
		else if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_YOUTH_LEAGUE))
			url += "29";
		else if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_DOWNLOADS))
			url += "16";
		else if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_JOBS))
			url += "55";
		else
			return result;
		url += "?page=";
		
		try {
			doc = readHelper.getWithDocumentForParsePostsFromSCCE(url+"1");
			result.addAll(parsePostsFromSCCEStudent(aCategory, start, end, max, doc));
			Matcher matcher = Pattern.compile("共(\\d+)页").matcher(doc.select(".oright .page").first().text());
			if(matcher.find())
				page = Integer.parseInt(matcher.group(1));
			else
				listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_PAGE_NUMBER, "不能从计算机学院学生网站解析页数，现仅解析第一页内容。");
			
			for(int i = 2;i<=page;i++){
				if(max>=0 && result.size()>=max)
					break;
				try {
					if(start!=null && Post.convertToDate(doc.select(".oright .orbg ul li").last()
							.getElementsByClass("date").first().text().trim().substring(1, 11)).before(start))
						break;
				} catch (ParseException e) {
					listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_DATE, "解析计算机学院学生网站通知/文章的日期失败。 "+e.getMessage());
				}
				doc = readHelper.getWithDocumentForParsePostsFromSCCE(url+i);
				result.addAll(parsePostsFromSCCEStudent(aCategory, start, end, max-result.size(), doc));
			}
		} catch (IOException e) {
			listener.onError(ParserListener.ERROR_IO, "遇到IO异常，无法打开页面，解析计算机学院学生网站信息失败。 "+e.getMessage());
			throw e;
		}
		
		return result;
	}
	/**
	 * 以start、end、max为限制条件，从计算机学院学生网站页面doc中解析posts
	 * @param aCategory 类别，例如Post.CATEGORYS.SCCE_STUDENT_NOTICES
	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
	 * @param doc 包含post列表的 某计算机学院学生网站网页的 Document
	 * @return 符合条件的posts
	 */
	public List<Post> parsePostsFromSCCEStudent(
			String aCategory, Date start, Date end, int max, Document doc){
		Post post = null;
		Element link = null;
		LinkedList<Post> result = new LinkedList<Post>();
		Elements posts = doc.select(".oright .orbg ul li");
		for(Element postLi:posts){
			if(max>=0 && result.size()>=max)
				break;
			post = new Post();
			try {
				post.setDate(postLi.getElementsByClass("date").first().text().substring(1,11));
				if(end!=null && post.getDate().after(end))
					continue;
				if(start!=null && post.getDate().before(start))
					break;
			} catch (ParseException e) {
				listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_DATE, "解析计算机学院学生网站通知/文章的日期失败。 "+e.getMessage());
			}
			link = postLi.getElementsByTag("a").first();
			post.setTitle(link.text().trim());
			post.setUrl(link.attr("abs:href"));
			post.setSource(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE).setCategory(aCategory);
			result.add(post);
		}
		return result;
	}
	/**
	 * 解析通知正文，解析结果为完整的HTML文档，保存在target中。根据target中来源、URL、标题解析。<br />
	 * <strong>注：</strong>target中的来源和URL必须有效。
	 * @param target 要解析的Post
	 * @return 为方便使用，返回target
	 * @throws ParserException Post的来源或者URL无效
	 * @throws IOException 可能是网络异常
	 */
	public Post parsePostMainBody(Post target) throws ParserException, IOException{
		if(target.getSource()==Post.SOURCES.UNKNOWN_SOURCE || target.getUrl()==null){
			this.listener.onError(ParserListener.ERROR_INSUFFICIENT_INFORMATION, "Post的来源或者URL为null，无法解析正文。");
			throw new ParserException("Post的来源或者URL为null，无法解析正文。\tPost:"+target.toString());
		}
		ReadPageHelper helper = getCurrentHelper();
		Document doc = null;
		String rawMainBody;
		final String format = 
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
				"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
				"<html xmlns='http://www.w3.org/1999/xhtml'>\n\t<head>\n" +
				"\t\t<meta  http-equiv='Content-Type' content='text/html; charset=UTF-16' />\n" +
				"\t\t<base href='%s' />\n" +
				"\t\t<title>%s</title>\n" +
				"\t</head>\n" +
				"\t<body>\n" +
				"\t\t%s\n" +
				"\t</body>\n" +
				"</html>";
		try{
			switch(target.getSource()){
			case Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS:
				doc = helper.getWithDocument(target.getUrl());
				rawMainBody = doc.select("table tr:eq(2) table td:eq(1) table table tr:eq(4) td").html();
				break;
			case Post.SOURCES.WEBSITE_OF_SCCE:
				doc = helper.getWithDocumentForParsePostsFromSCCE(target.getUrl());
				rawMainBody = doc.select("#Label2").html();
				break;
			case Post.SOURCES.STUDENT_WEBSITE_OF_SCCE:
				doc = helper.getWithDocumentForParsePostsFromSCCE(target.getUrl());
				rawMainBody = doc.select(".content div").html();
				break;
			default:
				rawMainBody = "未知来源。若看到此信息说明程序异常，请联系开发组。";
				break;
			}
		}catch(IOException e){
			this.listener.onError(ParserListener.ERROR_IO, "遇到IO。请检查网络连接。详情："+e.getMessage());
			throw e;
		}
		rawMainBody = String.format(format, doc.baseUri(), target.getTitle(), rawMainBody);
		target.setMainBody(rawMainBody);
		return target;
	}
	//TODO 实现它
	public Student parseStudentInformation(String url) throws IOException, ParserException {
		throw new UnsupportedOperationException("尚未实现");
	}
	/**
	 * 从URL指定的页面，使用指定的网络连接方法（readPageHelper），解析课程信息
	 * @param url 要读取的页面地址
	 * @return 满足条件的课程信息
	 * @throws ParserException 不能正确读取课程表表头时
	 * @throws IOException 网络连接出现异常
	 */
	public List<Course> parseCourse(String url) throws ParserException, IOException{
		try{
			Map<String, String> data = new HashMap<>(1);
			data.put("iDisplayLength", "30000"); //TODO 检测是否超过30000个课程
			ReadPageHelper helper = this.getCurrentHelperAfterLogin();
			Document doc = helper.request(url, Connection.Method.POST, helper.getCharset(), data).parse();
			//courses
			List<Course> result = new LinkedList<Course>();
			for(Element table : doc.select("div.ks-tabs-panel table.ui_table"))
				result.addAll(readCourseTable(table));
			if(result.isEmpty())
				listener.onWarn(ParserListener.WARNING_RESULT_IS_EMPTY, "结果为空，可能已选课程等页面已失效。");
			return result;
		}catch(IOException e){
			listener.onError(ParserListener.ERROR_IO, "遇到IO异常，无法打开页面，解析课程信息失败。 "+e.getMessage());
			throw e;
		}
	}
	/**
	 * {@link #parseScores(int, int)}的包装方法，解析多个学期的成绩
	 * <p>本方法的参数是学年学期表，如需解析m个学年，n个学期的成绩，则此表格式为int[m][n]，
	 * 其中每行的第0个元素是学年，第1~n个元素为学期号。</p><p>示例：</p><pre>
+------+---+---+
| 2010 | 1 | 2 |
+------+---+---+---+
| 2011 | 1 | 2 | 3 |
+------+---+---+---+
| 2012 | 1 |
+------+---+
	 * </pre><p><strong>提示</strong>：
	 * 允许{@code academicYearAndSemesters[i].length != academicYearAndSemesters[j].length}</p>
	 * @param academicYearAndSemesters 学年学期表
	 * @return 指定学年学期课程的成绩，其外层{@link Map}的{@code key}为学年，内层的{@code key}为学期
	 * <p>示例：{@code result.get(2011).get(1)}</p>
	 * @see #parseScores(int, int)
	 */
	public Map<Integer, Map<Integer, List<Course>>>
	parseScores(int[][] academicYearAndSemesters) throws ParserException, IOException {
		Map<Integer, Map<Integer, List<Course>>> result = new HashMap<>(academicYearAndSemesters.length);
		for(int[] year : academicYearAndSemesters) {
			Map<Integer, List<Course>> coursesInTheYear = new HashMap<>(year.length);
			result.put(year[0], coursesInTheYear);
			for(int index = 1 ; index < year.length ; index++) {
				int semester = year[index];
				for(int counter = 5 ; counter >= 1 ; counter--) {
					try {
						coursesInTheYear.put(semester, parseScores(year[0], semester));
						break; //成功，不用再重试
					} catch (IOException e) {
						if(counter > 1)
							continue; //再重试一遍 TODO 加日志
						else
							throw e; //最后一次重试失败，直接重抛异常
					}
				}
			}
		}
		return result;
	}
	/**
	 * 使用指定的网络连接方法（readPageHelper），解析成绩
	 * @param academicYear 学年
	 * @param semester 学期
	 * @return 满足条件的包含成绩信息的课程类
	 * @throws ParserException 不能正确读取课程表表头时
	 * @throws IOException 网络连接出现异常
	 */
	public List<Course> parseScores(int academicYear, int semester) throws ParserException, IOException{
		try{
			Map<String, String> data = new HashMap<>();
			data.put("qXndm_ys", academicYear + "-" + (academicYear+1));
			data.put("qXqdm_ys", String.valueOf(semester));
			ReadPageHelper helper = this.getCurrentHelperAfterLogin();
			Document doc = helper.request(Constant.url.ALL_PERSONAL_GRADES, Connection.Method.POST, helper.getCharset(), data).parse();
			//courses
			List<Course> result = readCourseTable(doc.select("div#tab01 [tabid~=0*1] table.ui_table").first()); //TODO tabid的设置
			if(result.isEmpty())
				listener.onWarn(ParserListener.WARNING_RESULT_IS_EMPTY, "结果为空，可能最新成绩等页面已失效。");
			return result;
		}catch(IOException e){
			listener.onError(ParserListener.ERROR_IO, "遇到IO异常，无法打开页面，解析成绩信息失败。 "+e.getMessage());
			throw e;
		}
	}

	private List<Course> readCourseTable(Element table) {
	    LinkedList<Course> result = new LinkedList<Course>();
	    Elements courses = table.getElementsByTag("tr");
	    
	    HashMap<Integer, Integer> headingMap = null;
	    for(Element course:courses){
	    	if(course.text().trim().length()==0)
	    		continue;
			Elements cols = course.getElementsByTag("td");
			if(cols.size() > 1)
				result.add(readCourse(course, headingMap));
			else if(cols.isEmpty())
				headingMap = getHeading(course.getElementsByTag("th"));
			else		// have and only one column
	    		listener.onInformation(ParserListener.INFO_SKIP, "Skip: "+course.text());
	    }
		return result;
	}
	private Course readCourse(Element course, HashMap<Integer, Integer> headingMap){
		if(headingMap == null){
			listener.onError(ParserListener.NULL_POINTER, "headingMap是null，无法解析课程表格。");
			throw new NullPointerException("headingMap is null.");
		}
		String rawTime = null, rawAddress = null;
		Course result = new Course();
		Elements cols = course.getElementsByTag("td");
		int i;
		Integer fieldCode;
		for(i = 0;i<cols.size();i++){
			fieldCode = headingMap.get(i);
			if(fieldCode == null){
				listener.onWarn(ParserListener.NULL_POINTER, "headingMap检查字段内容时，返回null。");
				continue;
			}
			switch(fieldCode.intValue()){
			case Headings.COURSE_CODE: case Headings.COURSE_NAME: case Headings.CLASS_NUMBER: 
			case Headings.COURSE_TEACHER: case Headings.COURSE_KIND:
			case Headings.COURSE_ACADEMIC_YEAR_AND_SEMESTER:
				parseAsString(result, fieldCode.intValue(), cols.get(i).text());
				break;
			case Headings.COURSE_CREDIT: case Headings.COURSE_TEST_SCORE: case Headings.COURSE_TOTAL_SCORE: 
			case Headings.COURSE_ACADEMIC_YEAR: case Headings.COURSE_SEMESTER:
				parseAsNumber(result, fieldCode.intValue(), cols.get(i).text());
				break;
			case Headings.COURSE_TIME:rawTime= cols.get(i).getElementsByTag("span").html();break;
			case Headings.COURSE_ADDRESS:rawAddress=cols.get(i).getElementsByTag("span").html();break;
			case Headings.SEQUENCE_NUMBER: case Headings.COURSE_TEACHING_MATERIAL: case Headings.COURSE_GRADE_POINT:
				listener.onInformation(ParserListener.INFO_SKIP, "忽略"+HEADINGS.getString(fieldCode)+"："+cols.get(i).text().trim());
				break;
			case Headings.UNKNOWN_COL:
			default:listener.onWarn(ParserListener.WARNING_UNKNOWN_COLUMN, "未知列: "+cols.get(i).text());break;
			}
		}
		if(rawTime!=null || rawAddress!=null){
			try{
				readTimeAndAddress(result, rawTime, rawAddress);
			}catch(Exception e){
				listener.onWarn(ParserListener.WARNINT_CANNOT_PARSE_TIME_AND_ADDRESS, "解析时间地点失败。因为：" + e.getMessage());
				e.printStackTrace();
			}
		}
		return result;
	}
	private String pretreatmentString(int colContent, String rawData){
		String temp = null;
		if(colContent == Headings.COURSE_NAME || colContent == Headings.COURSE_TEACHER)
			temp = ReadPageHelper.trim(rawData);
		else
			temp = ReadPageHelper.deleteSpace(rawData);
		if(temp.length() == 0){
			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_STRING_DATA, "数据"+HEADINGS.getString(colContent)+"为空，解析失败。");
			return null;
		}
		else
			return temp;
	}
	private void parseAsString(Course result, int colContent, String rawData){
		String temp = pretreatmentString(colContent, rawData);
		if(temp == null)
			return;
		switch(colContent){
		case Headings.COURSE_CODE:result.setCode(temp);break;
		case Headings.COURSE_NAME:result.setName(temp);break;
		case Headings.CLASS_NUMBER:result.setClassNumber(temp);break;
		case Headings.COURSE_TEACHER:result.addTeacher(temp);break;
		case Headings.COURSE_KIND:result.setKind(temp);break;
		case Headings.COURSE_ACADEMIC_YEAR_AND_SEMESTER:
			parseAcademicYearAndSemester(result, colContent, temp);
			break;
		default:listener.onWarn(ParserListener.WARNING_UNKNOWN_COLUMN, 
				"未知的字符串数据项"+colContent+"("+HEADINGS.getString(colContent)+")。");
		}
	}
	private void parseAcademicYearAndSemester(Course result, int colContent, String rawYearAndSemester) {
		Matcher matcher = ACADEMIC_YEAR_AND_SEMESTER_PATTERN.matcher(rawYearAndSemester);
		if(!matcher.matches()) {
			listener.onWarn(ParserListener.WARNINT_CANNOT_PARSE_ACADEMIC_YEAR_AND_SEMESTER,
					"未知的学年学期格式"+rawYearAndSemester+"，解析学年学期失败。");
		}
		// 学年
		try {
			int year1 = Integer.parseInt(matcher.group(1));
			int year2 = Integer.parseInt(matcher.group(2));
			if(year2 - year1 == 1) {
				result.setYear(year1);
			} else {
				listener.onWarn(ParserListener.WARNINT_CANNOT_PARSE_ACADEMIC_YEAR_AND_SEMESTER,
						"非预期的学年："+rawYearAndSemester+"不是一年，解析学年失败。");
			}
		} catch (NumberFormatException e) {
			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_NUMBER_DATA,
					"不能把字符串数据转换为数字，解析数字数据项"+HEADINGS.getString(colContent)+"失败。详情："+e.getMessage() );
		} catch (CourseException e) {
			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_NUMBER_DATA,
					"数据超过合理范围，解析数字数据项"+HEADINGS.getString(colContent)+"失败.详情："+e.getMessage() );
		}
		// 学期
		try {
			result.setSemester((byte) valueOfChineseNumber(matcher.group(3)));
		} catch (CourseException e) {
			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_NUMBER_DATA,
					"数据超过合理范围，解析数字数据项"+HEADINGS.getString(colContent)+"失败.详情："+e.getMessage() );
		}
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
	private void parseAsNumber(Course result, int colContent, String rawData){
		String temp = pretreatmentString(colContent, rawData);
		if(temp == null)
			return;
		temp = temp.replaceAll("\\.0*$", ""); //在师生服务网站查成绩时，学分用浮点数表示，这里删除多余的".0"
		try{
			switch(colContent){
			case Headings.COURSE_CREDIT:result.setCredit(Integer.parseInt(temp));break;
			case Headings.COURSE_TEST_SCORE:result.setTestScore(Float.parseFloat(temp));break;
			case Headings.COURSE_TOTAL_SCORE:result.setTotalScore(Float.parseFloat(temp));break;
			case Headings.COURSE_ACADEMIC_YEAR:result.setYear(Integer.parseInt(temp));break;
			case Headings.COURSE_SEMESTER:result.setSemester(Byte.valueOf(temp));break;
			default:listener.onWarn(ParserListener.WARNING_UNKNOWN_COLUMN, 
					"未知的数字数据项"+colContent+"("+HEADINGS.getString(colContent)+")。");
			}
		}catch(NumberFormatException e){
			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_NUMBER_DATA, 
					"不能把字符串数据转换为数字，解析数字数据项"+HEADINGS.getString(colContent)+"失败。详情："+e.getMessage() );
		}
		catch (CourseException e) {
			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_NUMBER_DATA, 
					"数据超过合理范围，解析数字数据项"+HEADINGS.getString(colContent)+"失败.详情："+e.getMessage() );
		}
	}
	private void readTimeAndAddress(Course result, String rawTime, String rawAddress) 
			throws ParserException, TimeAndAddressException, BitOperateException {
		if(rawTime==null)
			throw new NullPointerException("Error: rawTime == null");
		if(rawAddress==null)
			throw new NullPointerException("Error: rawAddress == null");
		String[] times, addresses;
		times = splitRawTime(rawTime);
		addresses = splitRawAddress(rawAddress);
		if(times.length != addresses.length)
			throw new ParserException("Error: times.length != addresses.length");
		for(int index = 0;index<times.length;index++){
			result.addTimeAndAddress(parseTime(times[index]).setAddress(addresses[index]));
		}
	}

	private TimeAndAddress parseTime(String time) throws ParserException, BitOperateException, TimeAndAddressException{
		TimeAndAddress result = new TimeAndAddress();
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
			listener.onWarn(ParserListener.WARNINT_CANNOT_PARSE_TIME_AND_ADDRESS, "部分时间地点信息无法解析："+remnants);
		return result;
	}
	/**
	 * 把weekToken加入指定的{@link TimeAndAddress}
	 * @param taa 把weekToken加入此{@link TimeAndAddress}
	 * @param weekToken 周次字符串，如：1-6,9,11 周(单)
	 * @param weekNumber weekToken的数字部分，如：1-6,9,11
	 * @throws BitOperateException 当 参数非法（非以上格式，无法解析，或者超出0-20的范围）时
	 * @see TimeAndAddress#addWeeks(String, Boolean)
	 */
	private static void addWeek(TimeAndAddress taa, String weekToken, String weekNumber) throws BitOperateException {
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
		LinkedList<String> result = new LinkedList<String>();
		String[] token = rawTime.split("<[^><]*>");
		StringBuilder buffer = new StringBuilder();
		for(String s:token){
			s = ReadPageHelper.trim(s);
			if(s.length() == 0)
				continue;
			if(WEEK_PATTERN.matcher(s).matches())
				buffer.append(s + ",");
			else {
				result.add(buffer.toString() + s);
				buffer.setLength(0);
			}
		}
		return result.toArray(new String[0]);
	}
	/**
	 * 拆分成多组，每组对应一个{@link Course.TimeAndAddress}。
	 * @param rawAddress
	 * @return
	 */
	private static String[] splitRawAddress(String rawAddress){
		String[] first;
		LinkedList<String> second = new LinkedList<String>();
		first = rawAddress.split("<[^><]*>");
		for(String s:first){
			s.trim();
			if(s.length()>0)
				second.add(s);
		}
		return second.toArray(new String[0]);
	}
	/**
	 * 解析课程表头，取得课程表列顺序
	 * @param tableHeaders 课程表头。&lt;th&gt; cells列表
	 * @return 列序号到字段意义的映射，用于查看每列的意义。
	 */
	private HashMap<Integer, Integer> getHeading(Elements tableHeaders) {
		if(tableHeaders.isEmpty()) {
			listener.onError(ParserListener.ERROR_CANNOT_PARSE_TABLE_HEADING, "课程表头为空,无法解析表头。");
			throw new IllegalArgumentException("encounter empty table headers");
		}
		int index = 0;
		HashMap<Integer, Integer> headMap = new HashMap<Integer, Integer>(15);
		for(Element th:tableHeaders) {
			headMap.put(index++, HEADINGS.getCode(ReadPageHelper.deleteSpace(th.text())));
		}
		return headMap;
	}
	public static class ParserException extends Exception{
		private static final long serialVersionUID = 3737828070910029299L;
		public ParserException(String message){
			super(message + " @SchoolWebpageParser");
		}
		public ParserException(){
			super("encounter Exception when parse school page.");
		}
		public ParserException(String message, Throwable cause) {
			super(message + " @SchoolWebpageParser", cause);
		}
		public ParserException(Throwable cause){
			super("encounter Exception when parse school page.", cause);
		}
	}

	private static final class Headings{
		//TODO 以下改为enmu
		private static final int UNKNOWN_COL = -1;
		/** 课表（表格）序号，无语义（忽略） */
	    private static final int SEQUENCE_NUMBER = 0;
	    private static final int COURSE_CODE = 1;
	    private static final int COURSE_NAME = 2;
	    private static final int CLASS_NUMBER = 3;
	    private static final int COURSE_TEACHER = 4;
	    private static final int COURSE_CREDIT = 5;
	    private static final int COURSE_TIME = 6;
	    private static final int COURSE_ADDRESS = 7;
	    private static final int COURSE_TEACHING_MATERIAL = 8;
		private static final int COURSE_ACADEMIC_YEAR_AND_SEMESTER = 9;
	    //SCORE
	    private static final int COURSE_TEST_SCORE = 10;
	    private static final int COURSE_TOTAL_SCORE = 11;
	    private static final int COURSE_ACADEMIC_YEAR = 12;
	    private static final int COURSE_SEMESTER = 13;
	    private static final int COURSE_KIND = 14;
	    private static final int COURSE_GRADE_POINT = 15;
		
	    private static final HashMap<String, Integer> HEADING_STRING2INTEGER = new HashMap<String, Integer>();
	    public Headings(){
	    	HEADING_STRING2INTEGER.put("序号",	SEQUENCE_NUMBER);
			HEADING_STRING2INTEGER.put("课程号",	COURSE_CODE);
	    	HEADING_STRING2INTEGER.put("课程代码",	COURSE_CODE);
	    	HEADING_STRING2INTEGER.put("课程编码",	COURSE_CODE);
	    	HEADING_STRING2INTEGER.put("课程名称",	COURSE_NAME);
	    	HEADING_STRING2INTEGER.put("教学班号",	CLASS_NUMBER);
			HEADING_STRING2INTEGER.put("上课教师",	COURSE_TEACHER);
	    	HEADING_STRING2INTEGER.put("教师",	COURSE_TEACHER);
	    	HEADING_STRING2INTEGER.put("学分",	COURSE_CREDIT);
			HEADING_STRING2INTEGER.put("上课时间",	COURSE_TIME);
	    	HEADING_STRING2INTEGER.put("时间",	COURSE_TIME);
			HEADING_STRING2INTEGER.put("上课地点",	COURSE_ADDRESS);
	    	HEADING_STRING2INTEGER.put("地点",	COURSE_ADDRESS);
	    	HEADING_STRING2INTEGER.put("参考教材",	COURSE_TEACHING_MATERIAL);
			HEADING_STRING2INTEGER.put("学年学期",	COURSE_ACADEMIC_YEAR_AND_SEMESTER);
	    	//Scores
	    	HEADING_STRING2INTEGER.put("结课考核成绩",	COURSE_TEST_SCORE);
	    	HEADING_STRING2INTEGER.put("期末总评成绩",	COURSE_TOTAL_SCORE);
	    	HEADING_STRING2INTEGER.put("成绩",		COURSE_TOTAL_SCORE);
	    	HEADING_STRING2INTEGER.put("学年",		COURSE_ACADEMIC_YEAR);
	    	HEADING_STRING2INTEGER.put("学期",		COURSE_SEMESTER);
	    	HEADING_STRING2INTEGER.put("课程性质",		COURSE_KIND);
	    	HEADING_STRING2INTEGER.put("绩点",		COURSE_GRADE_POINT);
	    	HEADING_STRING2INTEGER.put("<UNKNOWN_COL>", UNKNOWN_COL);
	    }
	    public int getCode(String heading){
	    	Integer temp = HEADING_STRING2INTEGER.get(heading);
	    	return temp!=null ? temp:UNKNOWN_COL;
	    }
	    public String getString(int headingCode){
	    	Iterator<Map.Entry<String, Integer>> iter = HEADING_STRING2INTEGER.entrySet().iterator();
	    	while (iter.hasNext()) {
	    	    Map.Entry<String, Integer> entry = iter.next();
	    	    if (entry.getValue().intValue() == headingCode) {
	    	        return entry.getKey();
	    	    }
	    	}
	    	return "Unkown Code";
	    }
	}
	
	/**
	 * 解析监听器，用于返回状体信息，解析进度等。
	 * @author Bai Jie
	 */
	public static interface ParserListener extends Cloneable{
		public static final int NULL_POINTER = 1;
		public static final int ERROR_CANNOT_LOGIN = 2;
		public static final int ERROR_IO = 3;
		public static final int ERROR_UNSUPPORTED_ENCODING = 4;
		/**不能读取网页中的课程（成绩）表头，无法继续解析。*/
		public static final int ERROR_CANNOT_PARSE_TABLE_HEADING = 5;
		/**Post的来源或者URL不明，无法解析Post正文。*/
		public static final int ERROR_INSUFFICIENT_INFORMATION = 6;
		/**不能读取总页面数，这样的话可能仅读取第一页*/
		public static final int WARNING_CANNOT_PARSE_PAGE_NUMBER = 10;
		/**结果为空，可能最新成绩、已选课程等页面已失效*/
		public static final int WARNING_RESULT_IS_EMPTY = 11;
		/**课程（成绩）表中遇到未知的列（数据项）。*/
		public static final int WARNING_UNKNOWN_COLUMN = 12;
		/**解析时间失败。*/
		public static final int WARNING_CANNOT_PARSE_DATE = 13;
		public static final int WARNING_CANNOT_PARSE_URL = 14;
		public static final int WARNING_CANNOT_PARSE_STRING_DATA = 15;
		public static final int WARNING_CANNOT_PARSE_NUMBER_DATA = 16;
		public static final int WARNINT_CANNOT_PARSE_TIME_AND_ADDRESS = 17;
		public static final int WARNINT_CANNOT_PARSE_SEMESTER = 18;
		public static final int WARNINT_CANNOT_PARSE_ACADEMIC_YEAR_AND_SEMESTER = 19;
		public static final int WARNING_CANNOT_PARSE_STUDENT_INFO = 20;
		public static final int WARNING_CANNOT_PARSE_STUDENT_BIRTHDAY = 21;
		public static final int WARNING_CANNOT_PARSE_STUDENT_ADMISSION_TIME = 22;
		public static final int WARNING_CANNOT_PARSE_STUDENT_ACADEMIC_PERIOD = 23;
		/**跳过/忽略某信息。*/
		public static final int INFO_SKIP = 30;
		/**
		 * 当遇到错误时，调用此方法。错误意味着解释失败，停止解析。很可能此调用后解析方法抛出异常
		 * @param code 错误代码
		 * @param message 错误信息
		 */
		public void onError(int code, String message);
		/**
		 * 当遇到警告信息时，调用此方法。警告意味着可能有错误的解析结果，需要用户检查正误，解析方法本身会正常返回结果（可能包含错误信息）。
		 * @param code 警告代码
		 * @param message 警告信息
		 */
		public void onWarn(int code, String message);
		/**
		 * 当返回消息信息时，调用此方法。这并不代表程序不正常，只是用于返回一些关于当前状态的消息。
		 * @param code 消息代码
		 * @param message 消息信息
		 */
		public void onInformation(int code, String message);
		/**
		 * 当解析进度改变时。用于报告解析进度。用current/total表示，0<=current<=total，current==total时解析完成。
		 * @param current 当前进度。
		 * @param total 总进度。
		 */
		public void onProgressChange(float current, float total);
		/**
		 * 实现深克隆
		 * @see java.lang.Object#clone()
		 */
		public ParserListener clone() throws CloneNotSupportedException;
	}
	/**
	 * 解析监听器适配器，若无说明，默认方法是空方法。您可以继承此类，只重写需要定义的方法。<br />
	 * <strong>注：</strong>本适配器的onError调用System.err.println返回错误信息，方便调试。您可以重写此方法（如用空方法替代）<br />
	 * <strong>注：</strong>本适配器的clone()方法仅调用默认的super.clone()方法，如果您有非安全对象字段，这并不适合您，请重写clone
	 * @author Bai Jie
	 */
	public static class ParserListenerAdapter implements ParserListener{	
		public ParserListenerAdapter() {
			super();
		}
		@Override
		public void onError(int code, String message) {
			System.err.println("Error "+code+": "+message);
		}
		@Override
		public void onWarn(int code, String message) {			
		}
		@Override
		public void onInformation(int code, String message) {
		}
		@Override
		public void onProgressChange(float current, float total) {			
		}
		@Override
		public ParserListener clone() throws CloneNotSupportedException{
			return (ParserListener) super.clone();
		}
	}
}
