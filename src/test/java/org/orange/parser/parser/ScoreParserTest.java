package org.orange.parser.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ScoreParserTest {
    @Test(expected = IllegalStateException.class)
    public void testParseException() throws Exception {
        new ScoreParser().parse();
    }
}
