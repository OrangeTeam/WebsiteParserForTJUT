package org.orange.parser.connection;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.orange.parser.parser.Constant;

import java.io.IOException;
import java.net.HttpURLConnection;

public class SSFWWebsiteConnectionAgent extends AbstractLoginConnectionAgent {

    public SSFWWebsiteConnectionAgent() {
        mLoginConnection.followRedirects(false).ignoreHttpErrors(true);
    }

    private void setupAccount() {
        mLoginConnection.request().data().clear();
        mLoginConnection.data("Login.Token1", mAccountName, "Login.Token2", mAccountPassword);
    }

    @Override
    public boolean onLogin() throws IOException {
        setupAccount();
        Connection.Response response =  mLoginConnection
                .url(Constant.url.LOGIN_PAGE)
                .method(Connection.Method.POST)
                .execute();
        String body = response.body();
        int status = response.statusCode();
        if(status == HttpURLConnection.HTTP_OK) {
            if(body.matches("(?si:.*(success|succeed).*)")) { //成功登录
                setCookiesFromResponse(response);
                setCookiesFromResponse(mLoginConnection, response);
                fetchSessionCookie();
                return true;
            } else { //用户名或密码错误
                return false;
            }
        } else {
            throw new HttpStatusException(
                    String.format("收到未知的服务器响应，请确认与教务处网站的连通性。\nHeader:\n%s\nBody:\n%s",
                            response.headers(), body),
                    status, mLoginConnection.request().url().toString());
        }
    }

    /**
     * 取得教务处网站（含课程信息等）的会话cookie
     * <p>前置条件：已{@link #onLogin()}</p>
     * @throws IOException
     */
    private void fetchSessionCookie() throws IOException {
        Connection.Response response = mLoginConnection
                .url(Constant.url.TEACHING_AFFAIRS_SESSION_PAGE)
                .method(Connection.Method.GET)
                .execute();
        setCookiesFromResponse(response);
    }
}
