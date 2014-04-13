package org.orange.parser.parser;

import org.orange.parser.reader.Reader;

import java.io.IOException;

public abstract class AbstractParser<T> implements Parser<T> {
	protected Reader mReader;
	protected ParseListener mParseListener = new ParseAdapter();

	@Override
	public Parser<T> setReader(Reader reader) {
		mReader = reader;
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
		if(mReader == null)
			throw new IllegalStateException("Must set ReadPageHelper before parse()");
		return null;
	}
}
