package org.orange.parser.reader;

import org.jsoup.Connection;

import java.io.IOException;

public interface LoginReader extends Reader {
	public void setAccount(String accountName, String password);
	public Connection getLoginConnection();
	public boolean doLogin() throws IOException;
}
