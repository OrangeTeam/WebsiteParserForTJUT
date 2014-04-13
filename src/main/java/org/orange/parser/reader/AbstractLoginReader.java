package org.orange.parser.reader;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.orange.parser.parser.Constant;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public abstract class AbstractLoginReader extends AbstractReader implements LoginReader {
	/** 最近一次成功登录的时间，null表示尚没有成功登录 */
	protected Date mRecentLoginTime;
	protected Connection mLoginConnection = Jsoup.connect(Constant.url.DEFAULT_PAGE);

	@Override
	public Connection getLoginConnection() {
		return mLoginConnection;
	}

	@Override
	public void setAccount(String accountName, String password) {
		if(accountName == null || password == null) {
			throw new IllegalArgumentException(
					"accountName == null || password == null",
					new NullPointerException());
		}
	}

	@Override
	public boolean login() throws IOException {
		if(onLogin()) {
			mRecentLoginTime = new Date();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Document read() throws IOException {
		if(mRecentLoginTime == null)
			throw new IllegalStateException("Must login before read.");
		return super.read();
	}

	protected abstract boolean onLogin() throws IOException;

	protected void setCookiesFromResponse(Connection.Response response) {
		setCookiesFromResponse(mReadConnection, response);
	}

	protected static void setCookiesFromResponse(Connection connection, Connection.Response response) {
		Map<String, String> cookies = response.cookies();
		if(cookies.isEmpty())
			System.err.printf("WARNING: no cookie in response [%s]", response);
		connection.cookies(response.cookies());
	}
}