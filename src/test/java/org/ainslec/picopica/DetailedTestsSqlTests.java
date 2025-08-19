package org.ainslec.picopica;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DetailedTestsSqlTests {

    @Test
    void testSqlExcludeIf() {
        String input = """
            -- @exclude-if FOO
            select * from user;
            -- @end
            """;
        String expectedNoKey = "select * from user;\n";
        String expectedWithFoo = "";

        assertEquals(expectedNoKey,
            PicoPica.input(input).languageMode(LanguageMode.SQL).exec());
        assertEquals(expectedWithFoo,
            PicoPica.input(input).keys("FOO").languageMode(LanguageMode.SQL).exec());
    }

    @Test
    void testSqlIncludeIf() {
        String input = """
                -- @include-if FOO
                select * from user;
                -- @end
                """;
        String expectedWithFoo = "select * from user;\n";
        String expectedNoKey = "";

        assertEquals(expectedNoKey,
            PicoPica.input(input).languageMode(LanguageMode.SQL).exec());
        assertEquals(expectedWithFoo,
            PicoPica.input(input).keys("FOO").languageMode(LanguageMode.SQL).exec());
    }
    
    @Test
    void testSqlIncludeIfTestIgnoreRegularMultiline() {
        String input = """
                /* @include-if FOO */
                select * from user;
                /* @end */
                """;
        
        String expectedWithFoo = "select * from user;\n";
        String expectedNoKey = "";

        assertEquals(expectedNoKey,
            PicoPica.input(input).languageMode(LanguageMode.SQL).exec());
        assertEquals(expectedWithFoo,
            PicoPica.input(input).keys("FOO").languageMode(LanguageMode.SQL).exec());
    }

    @Test
    void testSqlFileExcludeIf() {
        String input = "# @file-exclude-if FOO\nselect * from user;";
        String expectedNoKey = input;
        String expectedWithFoo = input;

        assertEquals(expectedNoKey, PicoPica.input(input).languageMode(LanguageMode.SQL).exec());
        assertEquals(expectedWithFoo, PicoPica.input(input).keys("FOO").languageMode(LanguageMode.SQL).exec());
    }

    @Test
    void testSqlRejectsSpanningDirectives() {
        String input = """
            /* @exclude-if FOO */
            select * from user;
            /* @end */
            """;

        String expected = "select * from user;\n";
        assertEquals(expected,PicoPica.input(input).languageMode(LanguageMode.SQL).exec());
    }
    
    @Test
    void testError() {
        String input = """
            -- @exclude-if FOO hello
            select * from user;
            -- @end
            """;
        
        // TODO :: Should be error as nothing should be allowed after a key in single line mode
    }    
}
