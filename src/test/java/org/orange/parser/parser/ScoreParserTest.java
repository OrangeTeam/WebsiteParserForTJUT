package org.orange.parser.parser;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.orange.parser.connection.LoginConnectionAgent;
import org.orange.parser.connection.SSFWWebsiteConnectionAgent;
import org.orange.parser.entity.Course;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class ScoreParserTest {

    @Test(expected = IllegalStateException.class)
    public void testParseException() throws Exception {
        new ScoreParser().parse();
    }

    @Test
    public void testScoreParser() throws IOException {
        LoginConnectionAgent connectionAgent = new SSFWWebsiteConnectionAgent()
                .setAccount("20106173", "20106173");
        ScoreParser scoreParser = new ScoreParser();
        for (int i = 2010; i <= 2013; i++) {
            scoreParser.addAcademicYearAndSemester(i, 1);
            scoreParser.addAcademicYearAndSemester(i, 2);
        }
        final int TOTAL = 8;
        final boolean[] completedProgress = new boolean[TOTAL + 1];
        Arrays.fill(completedProgress, false);
        scoreParser.setParseListener(new ParseAdapter() {
            @Override
            public void onProgressChange(int current, int total) {
                Assert.assertEquals(TOTAL, total);
                completedProgress[current] = true;
            }
        });
        List<Course> result = scoreParser.setConnectionAgent(connectionAgent).parse();
        checkScores(result);
        for (boolean progress : completedProgress) {
            Assert.assertTrue(progress);
        }
    }

    public static void checkScores(List<Course> courses) {
        final double delta = 0.01;
        for (Course course : courses) {
            switch (course.getCode()) {
                case "1590046":
                    Assert.assertEquals("线性代数", course.getName());
                    Assert.assertEquals("必修课", course.getKind());
                    Assert.assertEquals(3, course.getCredit().intValue());
                    Assert.assertEquals(2010, course.getYear().intValue());
                    Assert.assertEquals(1, course.getSemester().intValue());
                    Assert.assertEquals(100, course.getTotalScore(), delta);
                    break;
                case "1590316":
                    Assert.assertEquals("大学物理实验AⅠ", course.getName());
                    Assert.assertEquals("必修课", course.getKind());
                    Assert.assertEquals(1, course.getCredit().intValue());
                    Assert.assertEquals(2010, course.getYear().intValue());
                    Assert.assertEquals(2, course.getSemester().intValue());
                    Assert.assertEquals(80, course.getTotalScore(), delta);
                    break;
                case "3111211":
                    Assert.assertEquals("计算机组装与维护技术", course.getName());
                    Assert.assertEquals("选修课", course.getKind());
                    Assert.assertEquals(2, course.getCredit().intValue());
                    Assert.assertEquals(2011, course.getYear().intValue());
                    Assert.assertEquals(1, course.getSemester().intValue());
                    Assert.assertEquals(90, course.getTotalScore(), delta);
                    break;
                case "3112071":
                    Assert.assertEquals("影视动画视听语言", course.getName());
                    Assert.assertEquals("选修课", course.getKind());
                    Assert.assertEquals(2, course.getCredit().intValue());
                    Assert.assertEquals(2011, course.getYear().intValue());
                    Assert.assertEquals(2, course.getSemester().intValue());
                    Assert.assertEquals(89, course.getTotalScore(), delta);
                    break;
                case "0666206":
                    Assert.assertEquals("网络体系结构与程序设计", course.getName());
                    Assert.assertEquals("必修课", course.getKind());
                    Assert.assertEquals(3, course.getCredit().intValue());
                    Assert.assertEquals(2012, course.getYear().intValue());
                    Assert.assertEquals(1, course.getSemester().intValue());
                    Assert.assertEquals(94.8, course.getTotalScore(), delta);
                    break;
                case "0666146":
                    Assert.assertEquals("对象设计与建模", course.getName());
                    Assert.assertEquals("必修课", course.getKind());
                    Assert.assertEquals(3, course.getCredit().intValue());
                    Assert.assertEquals(2012, course.getYear().intValue());
                    Assert.assertEquals(2, course.getSemester().intValue());
                    Assert.assertEquals(80.2, course.getTotalScore(), delta);
                    break;
                case "0686026":
                    Assert.assertEquals("市场调研", course.getName());
                    Assert.assertEquals("必修课", course.getKind());
                    Assert.assertEquals(2, course.getCredit().intValue());
                    Assert.assertEquals(2013, course.getYear().intValue());
                    Assert.assertEquals(1, course.getSemester().intValue());
                    Assert.assertEquals(75, course.getTotalScore(), delta);
                    break;
            }
        }
    }
}
