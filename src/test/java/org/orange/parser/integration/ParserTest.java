package org.orange.parser.integration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.orange.parser.parser.Parser;
import org.orange.parser.parser.PersonalInformationParser;
import org.orange.parser.parser.PersonalInformationParserTest;
import org.orange.parser.reader.LoginReader;
import org.orange.parser.reader.SSFWWebsiteReader;

import java.io.IOException;
import java.util.Map;

@RunWith(JUnit4.class)
public class ParserTest {

	@Test(expected = IllegalStateException.class)
	public void testParserWithoutSettings() throws IOException {
		LoginReader reader = new SSFWWebsiteReader();
		Parser parser = new PersonalInformationParser();
		parser.setReader(reader);
		System.out.println(parser.parse());
	}

	@Test
	public void testParserWithoutAccountSettings() throws IOException {
		LoginReader reader = new SSFWWebsiteReader();
		Assert.assertFalse("login without account information", reader.login());
	}

	@Test
	public void testParser() throws IOException {
		LoginReader reader = new SSFWWebsiteReader();
		reader.setAccount("20106173", "20106173");
		Assert.assertTrue("login", reader.login());
		Parser<Map<String, Map<String, String>>> parser = new PersonalInformationParser();
		parser.setReader(reader);
		Map<String, Map<String, String>> result = parser.parse();
		System.out.println(parser.parse());
		PersonalInformationParserTest.validatePersonalInformation(result);
	}

}
