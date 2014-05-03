package org.orange.parser.connection;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.orange.parser.parser.Constant;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public abstract class AbstractLoginConnectionAgent extends AbstractConnectionAgent implements LoginConnectionAgent {
    /** 最近一次成功登录的时间，null表示尚没有成功登录 */
    protected Date mRecentLoginTime;
    protected Connection mLoginConnection = Jsoup.connect(Constant.url.DEFAULT_PAGE);

    protected String mAccountName;
    protected String mAccountPassword;
    private boolean hasSetAccount = false;

    @Override
    public Connection getLoginConnection() {
        return mLoginConnection;
    }

    @Override
    public final LoginConnectionAgent setAccount(String accountName, String password) {
        if(accountName == null || password == null) {
            throw new IllegalArgumentException(
                    "accountName == null || password == null",
                    new NullPointerException());
        }
        mAccountName = accountName;
        mAccountPassword = password;
        hasSetAccount = true;
        return this;
    }

    @Override
    public final boolean login() throws IOException {
        if(!hasSetAccount)
            throw new IllegalStateException("Please set account before login");
        for(int counter = mRetryCount ; counter >= 1 ; counter--) { // 遇到IOException时重试
            try {
                if (onLogin()) {
                    mRecentLoginTime = new Date();
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                if (counter > 1) {
                    System.out.println("Encounter IOException(" + e + "), try again. : " + counter);
                } else {
                    throw e;
                }
            }
        }
        throw new AssertionError("Shouldn't go here.");
    }

    @Override
    public LoginConnectionAgent url(String url) {
        super.url(url);
        return this;
    }

    @Override
    public void beforeExecute() throws IOException {
        if(mRecentLoginTime == null ||
                System.currentTimeMillis() - mRecentLoginTime.getTime()
                        >= Constant.PERIOD_OF_SESSION_COOKIE) {
            if(!login())
                throw new IllegalStateException("Login failed");
        }
        super.beforeExecute();
    }

    protected abstract boolean onLogin() throws IOException;

    protected void setCookiesFromResponse(Connection.Response response) {
        setCookiesFromResponse(mReadConnection, response);
    }

    protected static void setCookiesFromResponse(Connection connection, Connection.Response response) {
        Map<String, String> cookies = response.cookies();
        if(cookies.isEmpty())
            System.err.printf("WARNING: no cookie in response [%s]", response);
        connection.cookies(cookies);
    }
}
