package org.orange.parser.connection;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;

public interface ConnectionAgent {
	public Connection getConnection();
	public ConnectionAgent url(String url);
	public Document get() throws IOException;
	public Document post() throws IOException;
}
