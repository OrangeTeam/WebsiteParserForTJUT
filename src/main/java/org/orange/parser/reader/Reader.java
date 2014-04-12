package org.orange.parser.reader;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;

public interface Reader {
	public Connection getConnection();
	public void url(String url);
	public Document read() throws IOException;
}
