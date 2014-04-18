package org.orange.parser.connection;

import org.jsoup.Connection;

import java.io.IOException;

public interface LoginConnectionAgent extends ConnectionAgent {
    public LoginConnectionAgent setAccount(String accountName, String password);
    public Connection getLoginConnection();
    public boolean login() throws IOException;

    @Override
    LoginConnectionAgent url(String url);
}
