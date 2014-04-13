package org.orange.parser.reader;

import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.orange.parser.parser.Constant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@RunWith(JUnit4.class)
public class SSFWWebsiteReaderTest {

	@Test(expected = IllegalArgumentException.class)
	public void testSetAccountWithNullAccount1() throws IOException {
		new SSFWWebsiteReader().setAccount(null, "20106173");
	}
	@Test(expected = IllegalArgumentException.class)
	public void testSetAccountWithNullAccount2() throws IOException {
		new SSFWWebsiteReader().setAccount("20106173", null);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testSetAccountWithNullAccount3() throws IOException {
		new SSFWWebsiteReader().setAccount(null, null);
	}

	@Test(expected = IllegalStateException.class)
	public void testLoginWithoutAccountSet() throws IOException {
		new SSFWWebsiteReader().login();
	}

	@Test
	public void testLoginWithWrongAccount() throws IOException {
		SSFWWebsiteReader reader = new SSFWWebsiteReader();
		reader.setAccount("20106173", "wrong password");
		Assert.assertFalse("login with wrong password", reader.login());
		Assert.assertNull(reader.mRecentLoginTime);
	}

	@Test
	public void testReadWithoutUrlSetting() throws IOException {
		final String WARNING_MESSAGE = "WARNING: you read default page: " + Constant.url.DEFAULT_PAGE;

		ByteArrayOutputStream errorLog = new ByteArrayOutputStream();
		PrintStream originErr = System.err;
		System.setErr(new PrintStream(errorLog, true, "UTF-8"));

		SSFWWebsiteReader reader = new SSFWWebsiteReader();
		reader.setAccount("20106173", "20106173");
		Assert.assertTrue("login", reader.login());
		Assert.assertEquals(Constant.url.DEFAULT_PAGE, reader.mReadConnection.request().url().toString());
		reader.read();
		Assert.assertTrue("log", errorLog.toString("UTF-8").contains(WARNING_MESSAGE));

		System.setErr(originErr);
	}

	@Test
	public void testRead() throws IOException {
		SSFWWebsiteReader reader = new SSFWWebsiteReader();
		reader.setAccount("20106173", "20106173");
		reader.login();
		reader.url(Constant.url.PERSONAL_INFORMATION);
		Document document = reader.read();
		validatePersonalInformationDocument(document);
	}
	@Test
	public void testRead2() throws IOException {
		SSFWWebsiteReader reader = new SSFWWebsiteReader();
		reader.setAccount("20106173", "20106173");
		Document document = reader.read(Constant.url.PERSONAL_INFORMATION);
		validatePersonalInformationDocument(document);
		System.out.println(document);
	}

	private void validatePersonalInformationDocument(Document document) {
		String html = document.outerHtml();
		Assert.assertTrue(html.contains("基本信息"));
		Assert.assertTrue(html.contains("学籍信息"));
		Assert.assertTrue(html.contains("20106173"));
		Assert.assertTrue(html.contains("柏杰"));
		Assert.assertTrue(html.contains("15620906177"));
		Assert.assertTrue(html.contains("baijie1991@gmail.com"));
		Assert.assertTrue("邮编", html.contains("050207"));
		Assert.assertTrue("考生号", html.contains("10130103150617"));
	}
}
