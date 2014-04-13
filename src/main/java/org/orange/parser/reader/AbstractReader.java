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
	public void url(String url) {
		mReadConnection.url(url);
	}

	@Override
	public Document read() throws IOException {
		if(mReadConnection.request().url().toString().equals(Constant.url.DEFAULT_PAGE)) {
			System.err.println(
					"WARNING: you read default page: " + Constant.url.DEFAULT_PAGE);
		}
		return mReadConnection.get();
	}

	@Override
	public Document read(String url) throws IOException {
		url(url);
		return read();
	}
}