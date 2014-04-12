package org.orange.parser.reader;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Cookie implements Cloneable {
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
	public static List<Cookie> fromMap(Map<String, String> cookies) {
		List<Cookie> result = new LinkedList<Cookie>();
		for(Map.Entry<String, String> entry : cookies.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if(key == null || value == null)
				throw new IllegalArgumentException("encounter null cookie key or value");
			result.add(new Cookie(key, value));
		}
		return result;
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
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Cookie clone() throws CloneNotSupportedException {
		Cookie clone = (Cookie) super.clone();
		clone.modifiedTime = (Date) this.modifiedTime.clone();
		return clone;
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
