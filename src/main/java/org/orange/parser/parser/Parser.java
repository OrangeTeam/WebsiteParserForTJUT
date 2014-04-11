package org.orange.parser.parser;

import java.io.IOException;

public interface Parser<T> {
	/**
	 * 执行解析
	 * @return 解析结果
	 * @throws IOException 网络连接出现异常时
	 */
	public T parse() throws IOException;
}
