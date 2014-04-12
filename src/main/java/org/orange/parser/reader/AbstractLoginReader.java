package org.orange.parser.reader;

import org.jsoup.Connection;

import java.util.Map;

public abstract class AbstractLoginReader extends AbstractReader implements LoginReader {
	protected Connection mLoginConnection;
	protected Cookie mAuthenticationCookie;

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

	protected void setCookiesFromResponse(Connection.Response response) {
		setCookiesFromResponse(mReadConnection, response);
	}

	public static void setCookiesFromResponse(Connection connection, Connection.Response response) {
		Map<String, String> cookies = response.cookies();
		if(cookies.isEmpty())
			System.err.printf("WARNING: no cookie in response [%s]", response);
		connection.cookies(response.cookies());
	}
}
