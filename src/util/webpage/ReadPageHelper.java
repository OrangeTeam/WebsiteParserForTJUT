package util.webpage;

import java.io.IOException;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;

public class ReadPageHelper {
	private static final int DEFAULT_TIMEOUT = 12000;	/* milliseconds */
	private static final String DEFAULT_CHARSET = "GB2312";
	private int timeout; /* milliseconds */
	private String userName, password, charset;
	private Cookie sessionCookie;
	
	public ReadPageHelper(String userName, String password, 
			String pageCharset, int timeout){
		this.userName = userName;
		this.password = password;
		this.charset = pageCharset;
		if(timeout>0)
			this.timeout = timeout;
		else
			this.timeout = DEFAULT_TIMEOUT;
	}
	public ReadPageHelper(String userName, String password, String pageCharset){
		this(userName, password, pageCharset, DEFAULT_TIMEOUT);
	}
	public ReadPageHelper(String userName, String password){
		this(userName, password, DEFAULT_CHARSET);
	}
	public ReadPageHelper(){
		this(null ,null);
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
	 * @param userName the user name to set
	 * @param password this password to set
	 */
	public void setUserName(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}
	/**
	 * @return the sessionCookie
	 */
	public Cookie getSessionCookie() {
		return sessionCookie;
	}
	/**
	 * @param sessionCookie the sessionCookie to set
	 */
	public void setSessionCookie(String sessionCookieKey, String sessionCookieValue) {
		this.sessionCookie = new Cookie(sessionCookieKey, sessionCookieValue);
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
		Connection conn = 
				Jsoup.connect(loginPageURL).timeout(timeout).data("name",userName,"pswd", password).followRedirects(false);
		conn.post();
		if(conn.response().statusCode() != 302)
			return false;
		Map<String, String> cookies = conn.response().cookies();
		if(cookies == null)
			return false;
		String key = (String)cookies.keySet().toArray()[0];
		if(key == null || key.length() == 0)
			return false;
		String value = cookies.get(key);
		if(value == null || value.length() == 0)
			return false;
		sessionCookie = new Cookie(key, value);
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
		Connection conn = Jsoup.connect(url).timeout(timeout);
		if(sessionCookie != null && !sessionCookie.isEmpty())
			conn.cookie(sessionCookie.cookieKey, sessionCookie.cookieValue);
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
		if(sessionCookie != null && !sessionCookie.isEmpty())
			conn.cookie(sessionCookie.cookieKey, sessionCookie.cookieValue);
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
		public String cookieKey;
		public String cookieValue;
		
		public Cookie(String cookieKey, String cookieValue) {
			this.cookieKey = cookieKey;
			this.cookieValue = cookieValue;
		}
		/**
		 * @return the cookieKey
		 */
		public String getCookieKey() {
			return cookieKey;
		}
		/**
		 * @param cookieKey the cookieKey to set
		 */
		public void setCookieKey(String cookieKey) {
			this.cookieKey = cookieKey;
		}
		/**
		 * @return the cookieValue
		 */
		public String getCookieValue() {
			return cookieValue;
		}
		/**
		 * @param cookieValue the cookieValue to set
		 */
		public void setCookieValue(String cookieValue) {
			this.cookieValue = cookieValue;
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
					+ cookieValue + "]";
		}
	}
}
