package org.orange.parser.parser;

import org.orange.parser.connection.ConnectionAgent;

import java.io.IOException;

public abstract class AbstractParser<T> implements Parser<T> {
	protected ConnectionAgent mConnectionAgent;
	protected ParseListener mParseListener = new ParseAdapter();

	@Override
	public Parser<T> setConnectionAgent(ConnectionAgent connectionAgent) {
		mConnectionAgent = connectionAgent;
		return this;
	}

	public void setParseListener(ParseListener listener) {
		if(listener != null)
			mParseListener = listener;
		else
			mParseListener = new ParseAdapter();
	}

	@Override
	public T parse() throws IOException {
		if(mConnectionAgent == null)
			throw new IllegalStateException("Must set ReadPageHelper before parse()");
		return null;
		//TODO 确保子类实现
	}
}
