package org.orange.parser.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.orange.parser.connection.LoginConnectionAgent;
import org.orange.parser.connection.SSFWWebsiteConnectionAgent;
import org.orange.parser.parser.PersonalInformationParser;
import org.orange.parser.parser.PersonalInformationParserTest;
import org.orange.parser.parser.ScoreParser;
import org.orange.parser.parser.SelectedCourseParser;

import java.io.IOException;
import java.util.Map;

@RunWith(JUnit4.class)
public class ParserTest {

	@Test(expected = IllegalStateException.class)
	public void testParserWithoutSettings() throws IOException {
		System.out.println(new PersonalInformationParser().setConnectionAgent(new SSFWWebsiteConnectionAgent()).parse());
	}

	@Test
	public void testParser() throws IOException {
		LoginConnectionAgent connectionAgent = new SSFWWebsiteConnectionAgent().setAccount("20106173", "20106173");
		Map<String, Map<String, String>> personalInformation =
				new PersonalInformationParser().setConnectionAgent(connectionAgent).parse();
		PersonalInformationParserTest.validatePersonalInformation(personalInformation);
		System.out.printf("-------------------- %s --------------------%n", "个人信息");
		System.out.println(personalInformation);
		System.out.printf("-------------------- %s --------------------%n", "已选课程");
		System.out.println(new SelectedCourseParser().setConnectionAgent(connectionAgent).parse());
		System.out.printf("-------------------- %s --------------------%n", "成绩表");
		ScoreParser scoreParser = new ScoreParser();
		for(int i = 2010 ; i <= 2013 ; i++) {
			scoreParser.addAcademicYearAndSemester(i, 1);
			scoreParser.addAcademicYearAndSemester(i, 2);
		}
		System.out.println(scoreParser.setConnectionAgent(connectionAgent).parse());
	}

}
