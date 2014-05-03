package org.orange.parser.connection;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.orange.parser.parser.Constant;

import java.io.IOException;

public abstract class AbstractConnectionAgent implements ConnectionAgent {
    protected static final int DEFAULT_RETRY_COUNT = 5;

    protected int mRetryCount = DEFAULT_RETRY_COUNT;
    protected Connection mReadConnection = Jsoup.connect(Constant.url.DEFAULT_PAGE);

    @Override
    public Connection getConnection() {
        return mReadConnection;
    }

    @Override
    public ConnectionAgent retryCount(int numberOfRetries) {
        if(numberOfRetries <= 0) throw new IllegalArgumentException("numberOfRetries <= 0");
        mRetryCount = numberOfRetries;
        return this;
    }

    @Override
    public ConnectionAgent url(String url) {
        mReadConnection.url(url);
        return this;
    }

    protected void beforeExecute() throws IOException {
        if(mReadConnection.request().url().toString().equals(Constant.url.DEFAULT_PAGE)) {
            System.err.println(
                    "WARNING: you read default page: " + Constant.url.DEFAULT_PAGE);
        }
    }

    @Override
    public final Document get() throws IOException {
        mReadConnection.method(Connection.Method.GET);
        return execute().parse();
    }
    @Override
    public final Document post() throws IOException {
        mReadConnection.method(Connection.Method.POST);
        return execute().parse();
    }
    private Connection.Response execute() throws IOException {
        for(int counter = mRetryCount ; counter >= 1 ; counter--) { // 遇到IOException时重试
            try {
                beforeExecute();
                return mReadConnection.execute();
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

}
