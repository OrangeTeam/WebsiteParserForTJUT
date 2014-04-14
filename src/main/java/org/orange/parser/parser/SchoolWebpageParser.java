package org.orange.parser.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.orange.parser.entity.Post;
import org.orange.parser.util.ReadPageHelper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SchoolWebpageParser {
	private ParseListener listener;
	private ReadPageHelper autoReadHelper;
	private ReadPageHelper readHelper;

	/**
	 * 无参构造方法
	 */
	public SchoolWebpageParser() {
		super();
		listener = new ParseAdapter();
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
	public SchoolWebpageParser(ParseListener listener) throws CloneNotSupportedException {
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
	public SchoolWebpageParser(ParseListener listener, String userName, String password) throws CloneNotSupportedException {
		this(listener);
		setUser(userName, password);
	}
	/**
	 * 设置监听器和读取网页帮助类
	 * @param listener
	 * @param readHelper
	 * @throws CloneNotSupportedException
	 */
	public SchoolWebpageParser(ParseListener listener, ReadPageHelper readHelper) throws CloneNotSupportedException{
		this(listener);
		setReadHelper(readHelper);
		autoReadHelper.setUser(readHelper.getUserName(), readHelper.getPassword());
	}

	/**
	 * @return the listener
	 */
	public ParseListener getListener() {
		return listener;
	}
	/**
	 * @param listener the listener to set
	 * @throws CloneNotSupportedException
	 */
	public void setListener(ParseListener listener) throws CloneNotSupportedException {
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
	 * {@link SchoolWebpageParser#setListener(ParseListener) setListener(ParserListener)}
	 * 设置的ReadPageHelper（用于登录，读取页面等）。对之的修改会应用到本对象。
	 * @return 用户最近设置的ReadPageHelper，若您之前没有设置过则返回null
	 */
	public ReadPageHelper getReadHelper() {
		return readHelper;
	}
	/**
	 * 取得 本对象自动设置的ReadPageHelper（用于登录，读取页面等），这个helper只在您没有自行通过
	 * {@link SchoolWebpageParser#setListener(ParseListener) setListener(ParserListener)}
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
	 * @throws ParseException 登录失败
	 * @throws IOException doLogin进行post请求时遇到error
	 */
	public ReadPageHelper getCurrentHelperAfterLogin() throws ParseException, IOException{
		ReadPageHelper readHelper = getCurrentHelper();
		try{
			if(!readHelper.doLogin()){
				this.listener.onError(ParseListener.ERROR_CANNOT_LOGIN, "登录失败。");
				throw new ParseException("Cannot login website.");
			}
		}catch(IOException e){
			listener.onError(ParseListener.ERROR_IO, "遇到IO异常，无法登录。 "+e.getMessage());
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
			listener.onError(ParseListener.ERROR_UNSUPPORTED_ENCODING, "遇到编码异常，解释教务处信息失败。 "+e.getMessage());
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
				} catch (java.text.ParseException e) {
					listener.onWarn(ParseListener.WARNING_CANNOT_PARSE_DATE, "解析教务处通知/文章的日期失败。 "+e.getMessage());
				}
				doc = readHelper.getWithDocument(url+i);
				result.addAll(parsePostsFromTeachingAffairs(aCategory, start, end, max-result.size(), doc));
			}
		} catch (IOException e) {
			listener.onError(ParseListener.ERROR_IO, "遇到IO异常，无法打开页面，解析教务处信息失败。 "+e.getMessage());
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
			} catch (java.text.ParseException e) {
				listener.onWarn(ParseListener.WARNING_CANNOT_PARSE_DATE, "解析教务处通知/文章的日期失败。 "+e.getMessage());
			}
			aPost.setCategory(aCategory).setTitle(link.text()).setUrl(link.attr("abs:href"));
			aPost.setSource(Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS);
			result.add(aPost);
		}
		return result;
	}

	/**
	 * 根据categories、start、end、max、baseURL等条件，利用readHelper，从SCCE解析posts
	 * @param categories 类别，例如“学生通知”
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
				listener.onWarn(ParseListener.WARNING_CANNOT_PARSE_PAGE_NUMBER, "不能从计算机学院网站解析页数，现仅解析第一页内容。");
			result.addAll(parsePostsFromSCCE(categories, start, end ,max ,doc));

			for(int i = 2;i<=page;i++){
				if(max>=0 && result.size()>=max)
					break;
				try {
					if(start!=null && Post.convertToDate(ReadPageHelper.deleteSpace(doc
							.select("form table table").first().getElementsByTag("tr").last()
							.getElementsByTag("td").get(3).text())).before(start))
						break;
				} catch (java.text.ParseException e) {
					listener.onWarn(ParseListener.WARNING_CANNOT_PARSE_DATE, "解析计算机学院网站通知/文章的日期失败。 "+e.getMessage());
				}
				doc = readHelper.getWithDocumentForParsePostsFromSCCE(baseURL+i);
				result.addAll(parsePostsFromSCCE(categories, start, end, max-result.size(), doc));
			}
		} catch (IOException e) {
			listener.onError(ParseListener.ERROR_IO, "遇到IO异常，无法打开页面，解析计算机学院网站信息失败。 "+e.getMessage());
			throw e;
		}
		return result;
	}
	/**
	 * 以categories、start、end、max为限制条件，从计算机学院页面doc中解析posts
	 * @param categories 类别，例如“学生通知”
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
			} catch (java.text.ParseException e) {
				listener.onWarn(ParseListener.WARNING_CANNOT_PARSE_DATE, "解析计算机学院网站通知/文章的日期失败。 "+e.getMessage());
			}
			//设置 title、url、author、source
			post.setTitle(cols.get(0).text().trim());
			matcher = pattern.matcher(cols.get(0).getElementsByTag("a").attr("onclick"));
			if(matcher.find())
				post.setUrl("http://59.67.152.3/"+matcher.group(1));
			else
				listener.onWarn(ParseListener.WARNING_CANNOT_PARSE_URL, "解析计算机学院网站通知/文章的URL失败。 ");
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
				listener.onWarn(ParseListener.WARNING_CANNOT_PARSE_PAGE_NUMBER, "不能从计算机学院学生网站解析页数，现仅解析第一页内容。");

			for(int i = 2;i<=page;i++){
				if(max>=0 && result.size()>=max)
					break;
				try {
					if(start!=null && Post.convertToDate(doc.select(".oright .orbg ul li").last()
							.getElementsByClass("date").first().text().trim().substring(1, 11)).before(start))
						break;
				} catch (java.text.ParseException e) {
					listener.onWarn(ParseListener.WARNING_CANNOT_PARSE_DATE, "解析计算机学院学生网站通知/文章的日期失败。 "+e.getMessage());
				}
				doc = readHelper.getWithDocumentForParsePostsFromSCCE(url+i);
				result.addAll(parsePostsFromSCCEStudent(aCategory, start, end, max-result.size(), doc));
			}
		} catch (IOException e) {
			listener.onError(ParseListener.ERROR_IO, "遇到IO异常，无法打开页面，解析计算机学院学生网站信息失败。 "+e.getMessage());
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
			} catch (java.text.ParseException e) {
				listener.onWarn(ParseListener.WARNING_CANNOT_PARSE_DATE, "解析计算机学院学生网站通知/文章的日期失败。 "+e.getMessage());
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
	 * @throws ParseException Post的来源或者URL无效
	 * @throws IOException 可能是网络异常
	 */
	public Post parsePostMainBody(Post target) throws ParseException, IOException{
		if(target.getSource()==Post.SOURCES.UNKNOWN_SOURCE || target.getUrl()==null){
			this.listener.onError(ParseListener.ERROR_INSUFFICIENT_INFORMATION, "Post的来源或者URL为null，无法解析正文。");
			throw new ParseException("Post的来源或者URL为null，无法解析正文。\tPost:"+target.toString());
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
			this.listener.onError(ParseListener.ERROR_IO, "遇到IO。请检查网络连接。详情："+e.getMessage());
			throw e;
		}
		rawMainBody = String.format(format, doc.baseUri(), target.getTitle(), rawMainBody);
		target.setMainBody(rawMainBody);
		return target;
	}

}
