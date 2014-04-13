package org.orange.parser.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
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
		System.out.println(new PersonalInformationParser().setReader(new SSFWWebsiteReader()).parse());
	}

	@Test
	public void testParser() throws IOException {
		LoginReader reader = new SSFWWebsiteReader().setAccount("20106173", "20106173");
		Map<String, Map<String, String>> result =
				new PersonalInformationParser().setReader(reader).parse();
		PersonalInformationParserTest.validatePersonalInformation(result);
		System.out.println(result);
	}

}
