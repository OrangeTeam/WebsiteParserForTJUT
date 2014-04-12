package org.orange.parser.reader;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;

public abstract class AbstractReader implements Reader {
	protected Connection mReadConnection;

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
		return mReadConnection.get();
	}
}
