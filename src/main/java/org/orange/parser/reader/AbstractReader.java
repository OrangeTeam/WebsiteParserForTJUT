package org.orange.parser.reader;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.orange.parser.parser.Constant;

import java.io.IOException;

public abstract class AbstractReader implements Reader {
	protected Connection mReadConnection = Jsoup.connect(Constant.url.DEFAULT_PAGE);

	@Override
	public Connection getConnection() {
		return mReadConnection;
	}

	@Override
	public Reader url(String url) {
		mReadConnection.url(url);
		return this;
	}

	protected void beforeRead() throws IOException  {
		if(mReadConnection.request().url().toString().equals(Constant.url.DEFAULT_PAGE)) {
			System.err.println(
					"WARNING: you read default page: " + Constant.url.DEFAULT_PAGE);
		}
	}

	@Override
	public Document get() throws IOException {
		beforeRead();
		return mReadConnection.get();
	}
	@Override
	public Document post() throws IOException {
		beforeRead();
		return mReadConnection.post();
	}

}
