package org.orange.parser.reader;

import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.orange.parser.parser.Constant;

import java.io.IOException;

@RunWith(JUnit4.class)
public class SSFWWebsiteReaderTest {

	@Test
	public void readTest() throws IOException {
		SSFWWebsiteReader reader = new SSFWWebsiteReader();
		reader.setAccount("20106173", "20106173");
		reader.doLogin();
		reader.url(Constant.url.PERSONAL_INFORMATION);
		Document document = reader.read();
		String html = document.outerHtml();
		Assert.assertTrue(html.contains("基本信息"));
		Assert.assertTrue(html.contains("学籍信息"));
		Assert.assertTrue(html.contains("20106173"));
		Assert.assertTrue(html.contains("柏杰"));
		Assert.assertTrue(html.contains("15620906177"));
		Assert.assertTrue(html.contains("baijie1991@gmail.com"));
		Assert.assertTrue("邮编", html.contains("050207"));
		Assert.assertTrue("考生号", html.contains("10130103150617"));
		System.out.println("结果：");
		System.out.println(html);
	}
}
