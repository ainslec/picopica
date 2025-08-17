package org.ainslec.picopica;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Map;

public class DetailedTestsPython {


    @Test
    void testPythonIncludeIf() {
        String input = """
            # @include-if FOO
            print("foo")
            # @end""";
        String expectedNoKey = "";
        String expectedWithFoo = "print(\"foo\")\n";
        assertEquals(expectedNoKey, PicoPica.input(input).languageMode(LanguageMode.PYTHON).exec());
        assertEquals(expectedWithFoo, PicoPica.input(input).keys("FOO").languageMode(LanguageMode.PYTHON).exec());
    }

    @Test
    void testPythonExcludeIf() {
        String input = """
            # @exclude-if FOO
            print("bar")
            # @end""";
        String expectedNoKey = "print(\"bar\")\n";
        String expectedWithFoo = "";
        assertEquals(expectedNoKey, PicoPica.input(input).languageMode(LanguageMode.PYTHON).exec());
        assertEquals(expectedWithFoo, PicoPica.input(input).keys("FOO").languageMode(LanguageMode.PYTHON).exec());
    }

    
    @Test
    void testSqlIncludeIfTestIgnoreRegularMultiline() {
        String input = """
                /* @include-if FOO */
                select * from user;
                /* @end */
                """;
        
        String expectedWithFoo = input;
        String expectedNoKey = input;

        assertEquals(expectedNoKey,
            PicoPica.input(input).languageMode(LanguageMode.PYTHON).exec());
        assertEquals(expectedWithFoo,
            PicoPica.input(input).keys("FOO").languageMode(LanguageMode.PYTHON).exec());
    }
    
    @Test
    void testPythonElseBranch() {
        String input = """
            # @include-if FOO
            print("foo")
            # @else
            print("not foo")
            # @end
            """;
        String expectedNoKey = """
            print("not foo")
            """;
        String expectedWithFoo = """
            print("foo")
            """;
        assertEquals(expectedNoKey, PicoPica.input(input).languageMode(LanguageMode.PYTHON).exec());
        assertEquals(expectedWithFoo, PicoPica.input(input).keys("FOO").languageMode(LanguageMode.PYTHON).exec());
    }

    @Test
    void testInlineIncludeIf() {
        String input = """
            print(1 # @include-if FOO , 2 # @end )
            """;
        
        String expectedNoKey = input;
        String expectedWithFoo = input;
        assertEquals(expectedNoKey, PicoPica.input(input).languageMode(LanguageMode.PYTHON).exec());
        assertEquals(expectedWithFoo, PicoPica.input(input).keys("FOO").languageMode(LanguageMode.PYTHON).exec());
    }

    @Test
    void testInlineExcludeIf() {
        String input = """
            print( # @exclude-if FOO hello # @end )
            """;
        String expectedNoKey = input;
        String expectedWithFoo = input;
        assertEquals(expectedNoKey, PicoPica.input(input).languageMode(LanguageMode.PYTHON).exec());
        assertEquals(expectedWithFoo, PicoPica.input(input).keys("FOO").languageMode(LanguageMode.PYTHON).exec());
    }

    @Test
    void testNonDirectiveHashComment() {
        String input = """
            # Just a regular comment
            print("hi")
            """;
        String expected = """
            # Just a regular comment
            print("hi")
            """;
        assertEquals(expected, PicoPica.input(input).languageMode(LanguageMode.PYTHON).exec());
    }

    @Test
    void testFileExcludeIf() {
        String input = """
            # @file-exclude-if FOO
            print("hello")
            """;
        String expectedNoKey = """
            print("hello")
            """;
        String expectedWithFoo = """
            """;
        assertEquals(expectedNoKey, PicoPica.input(input).languageMode(LanguageMode.PYTHON).exec());
        assertEquals(expectedWithFoo, PicoPica.input(input).keys("FOO").languageMode(LanguageMode.PYTHON).exec());
    }

    @Test
    void testLicenseInsertionDefault() {
        String input = """
            # @license
            print("hello")
            """;
        String expected = """
            # default license
            print("hello")
            """;
        String result = PicoPica.input(input)
            .licenseMap(Map.of(
                PicoPica.DEFAULT_LICENSE_KEY, "# default license\n",
                "OSS", "# oss license\n"
            ))
            .languageMode(LanguageMode.PYTHON)
            .exec();
        assertEquals(expected, result);
    }

    @Test
    void testLicenseInsertionWithKey() {
        String input = """
            # @license
            print("hello")
            """;
        String expected = """
            # oss license
            print("hello")
            """;
        String result = PicoPica.input(input)
            .licenseMap(Map.of(
                PicoPica.DEFAULT_LICENSE_KEY, "# default license\n",
                "OSS", "# oss license\n"
            ))
            .languageMode(LanguageMode.PYTHON)
            .keys("OSS")
            .exec();
        assertEquals(expected, result);
    }
}
