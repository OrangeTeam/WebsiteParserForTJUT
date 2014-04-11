package org.orange.parser.parser;

import org.orange.parser.util.ReadPageHelper;

import java.io.IOException;

public abstract class AbstractParser<T> implements Parser<T> {
	protected ReadPageHelper mReadPageHelper;
	protected ParseListener mParseListener = new ParseAdapter();

	public void setReadPageHelper(ReadPageHelper readPageHelper) {
		mReadPageHelper = readPageHelper;
	}

	public void setParseListener(ParseListener listener) {
		if(listener != null)
			mParseListener = listener;
		else
			mParseListener = new ParseAdapter();
	}

	@Override
	public T parse() throws IOException {
		if(mReadPageHelper == null)
			throw new IllegalStateException("Must set ReadPageHelper before parse()");
		return null;
	}
}
