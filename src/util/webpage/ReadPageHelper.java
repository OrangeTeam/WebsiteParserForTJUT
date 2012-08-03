package util.webpage;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;

public class ReadPageHelper {
	/**网络连接的超时时间，单位milliseconds*/
	private int timeout;
	private String userName, password, charset;
	private boolean isNewUser;
	private Cookie teachingAffairsSession, SCCESession;
	/**保留的session的过期时间，单位milliseconds*/
	private int expire;

	public ReadPageHelper(){
		this.timeout = 12000;
		this.userName = this.password = "";
		this.charset = "GB2312";
		this.isNewUser = false;
		this.teachingAffairsSession = this.SCCESession = null;
		this.expire = 60 * 60 *1000;//	1 hour 
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
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}
	/**
	 * @param timeout the timeout to set
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
	 * do login<br />登录
	 * @param loginPageURL send login request to this page 向此网址发送登录请求 
	 * @return true for success, false for failure
	 * @throws IOException
	 */
	public boolean doLogin(String loginPageURL) throws IOException{
		if(!isNewUser && teachingAffairsSession!=null 
				&& teachingAffairsSession.isModifiedWithIn(expire))
			return true;
		Connection conn = Jsoup.connect(loginPageURL).timeout(timeout)
				.data("name",userName,"pswd", password).followRedirects(false);
		conn.post();
		if(conn.response().statusCode() != 302)
			return false;
		this.teachingAffairsSession = getCookie1FromMap(conn.response().cookies());
		if(this.teachingAffairsSession == null)
			return false;
		return true;
	}
	/**
	 * do login with default login page<br />登录,使用默认登录页
	 * @return true for success, false for failure
	 * @throws IOException
	 */
	public boolean doLogin() throws IOException{
		return doLogin(Constant.url.LOGIN_PAGE);
	}
	public boolean prepareToGetPostsFromSCCE(String preparePageURL) throws IOException{
		if(SCCESession!=null && SCCESession.isModifiedWithIn(expire))
			return true;
		Connection conn = Jsoup.connect(preparePageURL).timeout(timeout).followRedirects(false);
		conn.get();
		this.SCCESession = getCookie1FromMap(conn.response().cookies());
		if(this.SCCESession == null)
			return false;
		return true;
	}
	public boolean prepareToGetPostsFromSCCE() throws IOException{
		return prepareToGetPostsFromSCCE(Constant.url.PREPARE_PAGE_FOR_GET_POSTS_FROM_SCCE);
	}
	/**
	 * 取出cookies中的第一个Cookie
	 * @return 成功返回Cookie；失败返回null
	 */
	private Cookie getCookie1FromMap(Map<String, String> cookies){
		if(cookies == null)
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
	public Document getWithDocument(String url) throws IOException{
		Connection conn = Jsoup.connect(url).timeout(timeout).followRedirects(false);
		if(teachingAffairsSession != null && !teachingAffairsSession.isEmpty())
			conn.cookie(teachingAffairsSession.cookieKey, teachingAffairsSession.cookieValue);
		if(SCCESession != null && !SCCESession.isEmpty())
			conn.cookie(SCCESession.cookieKey, SCCESession.cookieValue);
		return ((HttpConnection)conn).get(charset);
	}
	/**
	 * read page by POST method
	 * @param url page's URL you want to read from
	 * @return web page's content in String
	 * @throws IOException
	 */
	public String post(String url) throws IOException{
		Connection conn = Jsoup.connect(url).timeout(timeout);
		if(teachingAffairsSession != null && !teachingAffairsSession.isEmpty())
			conn.cookie(teachingAffairsSession.cookieKey, teachingAffairsSession.cookieValue);
		if(SCCESession != null && !SCCESession.isEmpty())
			conn.cookie(SCCESession.cookieKey, SCCESession.cookieValue);
		return ((HttpConnection)conn).post(charset).outerHtml();
	}
	
	
	public static String deleteSpace(String src){
    	if(src!=null)
    		return src.replaceAll("\\s|\u00a0|\u3000", "");
    		//	ideographic space	0x3000	&#12288(HTML);
    	else
    		return null;
    }
	/**
	 * 打开连接，并打开输入流。用getInputStream()取得输入流
	 * 注意：用完输入流后，要关闭输入流，并且getConnection().close();
	 * @return "OK" for success, or the information about failure.成功返回“OK”，否则返回失败原因。
	 * @author Bai Jie
	 */
	protected String openInputStream(){
		return "";
	}
	
	public static class Cookie {
		private String cookieKey;
		private String cookieValue;
		private Date modifiedTime;
		
		public Cookie(){
			cookieKey = cookieValue = "";
			modifiedTime = new Date(0);
		}
		public Cookie(String cookieKey, String cookieValue) {
			this();
			setCookie(cookieKey, cookieValue);
		}
		/**
		 * @return the cookieKey
		 */
		public String getCookieKey() {
			return cookieKey;
		}
		/**
		 * @return the cookieValue
		 */
		public String getCookieValue() {
			return cookieValue;
		}
		/**
		 * @param cookieKey the cookie's key to set
		 * @param cookieValue the cookie's value to set
		 */
		public void setCookie(String cookieKey, String cookieValue) {
			this.cookieKey = cookieKey;
			this.cookieValue = cookieValue;
			modifiedTime.setTime(System.currentTimeMillis());
		}
		/**
		 * @return the modifiedTime
		 */
		public Date getModifiedTime() {
			return (Date)modifiedTime.clone();
		}
		public String getModifiedTimeString(){
			return DateFormat.getInstance().format(modifiedTime);
		}
		/**
		 * 上次修改时间是否在距现在指定时间（毫秒）内
		 * @param milliseconds 测试标准。单位：毫秒
		 * @return 在milliseconds毫秒内，返回true；在milliseconds毫秒外，返回false
		 */
		public boolean isModifiedWithIn(long milliseconds){
			return (System.currentTimeMillis()-modifiedTime.getTime() <= milliseconds);
		}
		public boolean isEmpty(){
			return (cookieKey.length() == 0&&cookieValue.length() == 0);
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Cookie [cookieKey=" + cookieKey + ", cookieValue="
					+ cookieValue +"modifiedTime="+getModifiedTimeString()+ "]";
		}
	}
}
