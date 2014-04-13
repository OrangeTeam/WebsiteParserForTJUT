package org.orange.parser.reader;

import org.jsoup.Connection;

import java.io.IOException;

public interface LoginReader extends Reader {
	public LoginReader setAccount(String accountName, String password);
	public Connection getLoginConnection();
	public boolean login() throws IOException;

	@Override
	LoginReader url(String url);
}
