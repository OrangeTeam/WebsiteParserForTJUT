package org.orange.parser.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

@RunWith(JUnit4.class)
public class PersonalInformationParserTest {

	@Test(expected = IllegalStateException.class)
	public void testParseException() throws Exception {
		new PersonalInformationParser().parse();
	}

	@Test
	public void testParse1() throws IOException {
		Document doc = Jsoup.parse(
				getFile("personal_information.html"),
				"utf-8", Constant.url.PERSONAL_INFORMATION);
		PersonalInformationParser parser = new PersonalInformationParser();
		Map<String, Map<String, String>> result = parser.parse1(doc);
		System.out.println("结果：");
		System.out.println(result);
		Assert.assertTrue("基本信息", result.containsKey("基本信息"));
		Assert.assertTrue("学籍信息", result.containsKey("学籍信息"));
		Assert.assertTrue("入学信息", result.containsKey("入学信息"));
		Assert.assertTrue("联系方式", result.containsKey("联系方式"));
		Assert.assertFalse("毕业信息", result.containsKey("毕业信息")); //样例html文档中毕业信息里无内容
		Assert.assertEquals("姓名", "柏杰", result.get("基本信息").get("姓名"));
		Assert.assertEquals("出生日期", "1991-03-08", result.get("基本信息").get("出生日期"));
		Assert.assertEquals("年级", "2010", result.get("学籍信息").get("年级"));
		Assert.assertEquals("学制", "4", result.get("学籍信息").get("学制"));
		Assert.assertEquals("入学日期", "2010-09-12", result.get("入学信息").get("入学日期"));
		Assert.assertEquals("入学年级", "2010", result.get("入学信息").get("入学年级"));
		Assert.assertEquals("个人邮箱", "baijie1991@gmail.com", result.get("联系方式").get("个人邮箱"));
		Assert.assertEquals("家长电话", "13081106100", result.get("联系方式").get("家长电话"));
	}

	public File getFile(String resourceName) {
		try {
			File file = new File(getClass().getResource("/htmltests/" + resourceName).toURI());
			return file;
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

}
