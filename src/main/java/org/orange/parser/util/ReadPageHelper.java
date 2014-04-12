package org.orange.parser.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.orange.parser.parser.Constant;
import org.orange.parser.reader.Cookie;

public class ReadPageHelper implements Cloneable{
	private static final String DEFAULT_CHARSET = "GB2312";
	private OnReadPageListener listener = null;
	
	/**网络连接的超时时间，单位milliseconds*/
	private int timeout;
	private String userName, password, charset, charsetForParsePostsFromSCCE;
	private boolean isNewUser;
	private Cookie teachingAffairsSession, teachingAffairsToken, SCCESession;
	/**保留的session的过期时间，单位milliseconds*/
	private int expire;

	public ReadPageHelper(){
		this.timeout = 12000;
		this.userName = this.password = "";
		this.charset = null;
		this.charsetForParsePostsFromSCCE = null;
		this.isNewUser = false;
		this.teachingAffairsSession = this.SCCESession = null;
		this.expire = 15 * 60 *1000;//	15 minutes
	}
	public ReadPageHelper(String userName, String password){
		this();
		setUser(userName, password);
	}
	public ReadPageHelper(String userName, String password, String pageCharset){
		this(userName, password);
		this.charset = pageCharset;
	}
	public ReadPageHelper(String userName, String password, String pageCharset, int timeout){
		this(userName, password, pageCharset);
		setTimeout(timeout);
	}
	
	/**
	 * @return the listener
	 */
	public OnReadPageListener getListener() {
		return listener;
	}
	/**
	 * @param listener the listener to set
	 */
	public void setListener(OnReadPageListener listener) {
		this.listener = listener;
	}
	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}
	/**
	 * 设置 网络连接的超时时间，单位milliseconds
	 * @param timeout 网络连接的超时时间，单位milliseconds
	 * @return 参数大于0返回真；参数小于等于0忽略本次调用，直接返回假
	 */
	public boolean setTimeout(int timeout) {
		if(timeout<=0)
			return false;
		this.timeout = timeout;
		return true;
	}
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * 设置用户名、密码<br />
	 * <strong>注意：</strong>参数不能设为null，否则抛出NullPointerException
	 * @param userName the user name to set
	 * @param password the password to set
	 */
	public void setUser(String userName, String password) {
		if(userName==null || password == null)
			throw new NullPointerException("Encounter null when set user.");
		this.userName = userName;
		this.password = password;
		this.isNewUser = true;
	}
	/**
	 * @return the teachingAffairsSession
	 */
	public Cookie getTeachingAffairsSession() {
		return teachingAffairsSession;
	}
	/**
	 * @param teachingAffairsSession the teachingAffairsSession to set
	 */
	public void setTeachingAffairsSession(String sessionCookieKey, String sessionCookieValue) {
		this.teachingAffairsSession = new Cookie(sessionCookieKey, sessionCookieValue);
	}
	/**
	 * @return the charset
	 */
	public String getCharset() {
		return charset;
	}
	/**
	 * @param charset the charset to set
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}
	/**
	 * @return the charsetForParsePostsFromSCCE
	 */
	public String getCharsetForParsePostsFromSCCE() {
		return charsetForParsePostsFromSCCE;
	}
	/**
	 * @param charsetForParsePostsFromSCCE the charsetForParsePostsFromSCCE to set
	 */
	public void setCharsetForParsePostsFromSCCE(String charsetForParsePostsFromSCCE) {
		this.charsetForParsePostsFromSCCE = charsetForParsePostsFromSCCE;
	}
	/**
	 * do login<br />登录
	 * @param loginPageURL send login request to this page 向此网址发送登录请求 
	 * @return true for success, false for failure 成功登录返回真，失败返回假
	 * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
	 * @throws HttpStatusException 若响应状态不可识别，可能与教务处网站的连通性出现问题，或教务网站改版。
	 * @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
	 * @throws java.net.SocketTimeoutException if the connection times out
	 * @throws IOException encounter error when make post request 进行post请求时遇到error
	 */
	public boolean doLogin(String loginPageURL) throws IOException{
		//是旧用户且上次认证未过期
		if(!isNewUser && teachingAffairsSession!=null 
				&& teachingAffairsSession.isModifiedWithIn(expire))
			return true;
		//用户名或密码为空
		if(userName.length()==0 || password.length()==0)
			return false;
		Response res = Jsoup
				.connect(loginPageURL).timeout(timeout).followRedirects(false).ignoreHttpErrors(true)
				.data("Login.Token1",userName,"Login.Token2", password)
				.method(Method.POST).execute();
		if(listener != null)
			listener.onRequest(loginPageURL, res.statusCode(), res.statusMessage(), res.bodyAsBytes().length);
		String body = new String(res.bodyAsBytes(), charset!=null?charset: DEFAULT_CHARSET);
		int status = res.statusCode();
		if(status == HttpURLConnection.HTTP_OK) {
			if(body.matches("(?si:.*(success|succeed).*)")) { //成功登录
				this.teachingAffairsToken = getCookie1FromMap(res.cookies());
				fetchTeachingAffairsSession();
				return true;
			} else { //用户名或密码错误
				return false;
			}
		} else{
			throw new HttpStatusException("收到未知的服务器响应，请确认与教务处网站的连通性。Header:\n"+res.headers()+"\nBody:\n"+body, status, loginPageURL);
		}
	}
	/**
	 * do login with default login page<br />登录,使用默认登录页
	 * @return true for success, false for failure
	 * @throws IOException
	 */
	public boolean doLogin() throws IOException{
		return doLogin(Constant.url.LOGIN_PAGE);
	}
	/**
	 * 取得教务处网站（含课程信息等）的会话cookie
	 * @throws IOException
	 * @precondition 已设置teachingAffairsToken
	 */
	private void fetchTeachingAffairsSession() throws IOException {
		Response res = Jsoup
				.connect(Constant.url.TEACHING_AFFAIRS_SESSION_PAGE)
				.timeout(timeout).followRedirects(false).ignoreHttpErrors(true)
				.cookie(teachingAffairsToken.getCookieKey(), teachingAffairsToken.getCookieValue())
				.method(Method.GET).execute();
		teachingAffairsSession = getCookie1FromMap(res.cookies());
	}
	/**
	 * 准备
	 * @param preparePageURL
	 * @param charset
	 * @return
	 * @throws IOException
	 * @see {@link #request(String, Method, String)}
	 */
	public boolean prepareToParsePostsFromSCCE(String preparePageURL) throws IOException{
		if(SCCESession!=null && SCCESession.isModifiedWithIn(expire))
			return true;
		this.SCCESession = getCookie1FromMap(request(preparePageURL, Method.GET, null).cookies());
		if(this.SCCESession == null)
			return false;
		return true;
	}
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean prepareToParsePostsFromSCCE() throws IOException{
		return prepareToParsePostsFromSCCE(Constant.url.PREPARE_PAGE_FOR_GET_POSTS_FROM_SCCE);
	}
	/**
	 * 取出cookies中的第一个Cookie
	 * @return 成功返回Cookie；失败返回null
	 */
	private Cookie getCookie1FromMap(Map<String, String> cookies){
		if(cookies == null || cookies.isEmpty())
			return null;
		String key = (String)cookies.keySet().toArray()[0];
		if(key == null || key.length() == 0)
			return null;
		String value = cookies.get(key);
		if(value == null || value.length() == 0)
			return null;
		return new Cookie(key, value);
	}
	/**
	 * read page by GET method
	 * @param url page's URL you want to read from
	 * @return web page's content in String
	 * @throws IOException
	 */
	public String get(String url) throws IOException{
		return getWithDocument(url).outerHtml();
	}
	/**
	 * @see {@link #request(String, Method, String)}
	 */
	public Document getWithDocument(String url) throws IOException{
		return getWithDocument(url, this.charset);
	}
	/**
	 * @see {@link #request(String, Method, String)}
	 */
	public Document getWithDocumentForParsePostsFromSCCE(String url) throws IOException{
		return getWithDocument(url, this.charsetForParsePostsFromSCCE);
	}
	/**
	 * @see {@link #request(String, Method, String)}
	 */
	public Document getWithDocument(String url, String charset) throws IOException{
		return request(url, Method.GET, charset).parse();
	}
	/**
	 * read page by POST method
	 * @param url page's URL you want to read from
	 * @return web page's content in String
	 * @throws IOException
	 * @see {@link #request(String, Method, String)}
	 */
	public String post(String url) throws IOException{
		return request(url, Method.POST, null).parse().outerHtml();
	}
	/**
	 * @see #request(String, Method, String, Map)
	 */
	public Response request(String url, Method method, String charset) throws IOException{
		return request(url, method, charset, (String[])null);
	}
	/**
	 * Execute the request.
	 * @param data map of request data parameters
	 * @see #request(String, Method, String, String...)
	 */
	public Response request(String url, Method method, String charset, Map<String, String> data) throws IOException{
		String[] dataArray = null;
		if(data != null) {
			dataArray = new String[data.entrySet().size() * 2];
			int counter = 0;
			for(Map.Entry<String, String> entry : data.entrySet()) {
				dataArray[counter++] = entry.getKey();
				dataArray[counter++] = entry.getValue();
			}
		}
		return request(url, method, charset, dataArray);
	}
	/**
	 * Execute the request.
	 * @param url URL to connect to
	 * @param method {@link Method}
	 * @param charset set character set to be used
	 * @param data a set of key value pairs. see {@link Connection#data(String...)}
	 * @return a response object
	 * @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
	 * @throws org.jsoup.HttpStatusException if the response is not OK and HTTP response errors are not ignored
	 * @throws org.jsoup.UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
	 * @throws java.net.SocketTimeoutException if the connection times out
	 * @throws IOException on error
	 * @see Connection
	 */
	public Response request(String url, Method method, String charset, String... data) throws IOException{
		Connection conn = Jsoup.connect(url).timeout(timeout).followRedirects(false);
		if(teachingAffairsSession != null && !teachingAffairsSession.isEmpty())
			conn.cookie(teachingAffairsSession.getCookieKey(), teachingAffairsSession.getCookieValue());
		if(teachingAffairsToken != null && !teachingAffairsToken.isEmpty())
			conn.cookie(teachingAffairsToken.getCookieKey(), teachingAffairsToken.getCookieValue());
		if(SCCESession != null && !SCCESession.isEmpty())
			conn.cookie(SCCESession.getCookieKey(), SCCESession.getCookieValue());

		if(data != null)
			conn.data(data);
		Response response = conn.method(method).execute();
//		if(charset != null)
//			response.charset(charset);
		if(listener != null){
			listener.onRequest(url, response.statusCode(), response.statusMessage(), response.bodyAsBytes().length);
		}
		return response;
	}
	//TODO 移到工具包
	/** 修剪头尾不可见符，包括\s\u00a0\u3000 */
	public static String trim(String src){
		if(src!=null)
			return src.replaceAll("(^[\\s\u00a0\u3000])|([\\s\u00a0\u3000]$)", "");
		else
			return null;
	}
	/** 删除所有不可见字符，包括\s\u00a0\u3000 */
	public static String deleteSpace(String src){
    	if(src!=null)
    		return src.replaceAll("[\\s\u00a0\u3000]", "");
    		//	ideographic space	0x3000	&#12288(HTML);
    	else
    		return null;
    }
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ReadPageHelper clone() throws CloneNotSupportedException {
		ReadPageHelper clone = (ReadPageHelper) super.clone();
		if(this.SCCESession != null)
			clone.SCCESession = this.SCCESession.clone();
		else
			clone.SCCESession = null;
		if(this.teachingAffairsSession != null)
			clone.teachingAffairsSession = this.teachingAffairsSession.clone();
		else
			clone.teachingAffairsSession = null;
		if(this.teachingAffairsToken != null)
			clone.teachingAffairsToken = this.teachingAffairsToken.clone();
		else
			clone.teachingAffairsToken = null;
		return clone;
	}

	public static interface OnReadPageListener{
		/**
		 * 当（用Get或Post等方法）读取一个页面后
		 * @param url
		 * @param statusCode
		 * @param statusMessage
		 * @param pageSize 单位：字节
		 */
		public void onRequest(String url, int statusCode, String statusMessage, int pageSize);
	}
}
