package org.ainslec.picopica;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LanguageModeLexerTests {
    private final Lexer lexer = new Lexer();

    private List<Token> tokenize(LanguageMode mode, String src) {
        return lexer.tokenize(mode, "test", src);
    }

    @Test
    void testCLikeCommentsRespected() {
        String code = "int x = 1; /* comment */ int y = 2;";

        // In JAVA, comments are parsed
        List<Token> javaTokens = tokenize(LanguageMode.JAVA, code);
        assertTrue(javaTokens.stream().anyMatch(t -> t.getType() == TokenType.TOKEN_BLOCK_OPEN));
        assertTrue(javaTokens.stream().anyMatch(t -> t.getType() == TokenType.TOKEN_BLOCK_CLOSE));

        // In PYTHON, C-like block comments should NOT be parsed
        List<Token> pyTokens = tokenize(LanguageMode.PYTHON, code);
        assertFalse(pyTokens.stream().anyMatch(t -> t.getType() == TokenType.TOKEN_BLOCK_OPEN));
        assertFalse(pyTokens.stream().anyMatch(t -> t.getType() == TokenType.TOKEN_BLOCK_CLOSE));
    }

    @Test
    void testHtmlCommentsRespected() {
        String code = "/* before <!-- html --> after */";

        // In JAVA (html parsing enabled), should detect HTML open/close
        List<Token> javaTokens = tokenize(LanguageMode.JAVA, code);
        assertTrue(javaTokens.stream().anyMatch(t -> t.getType() == TokenType.TOKEN_HTML_OPEN));
        assertTrue(javaTokens.stream().anyMatch(t -> t.getType() == TokenType.TOKEN_HTML_CLOSE));

        // In PYTHON (html parsing disabled), no html tokens
        List<Token> pyTokens = tokenize(LanguageMode.PYTHON, code);
        assertFalse(pyTokens.stream().anyMatch(t -> t.getType() == TokenType.TOKEN_HTML_OPEN));
        assertFalse(pyTokens.stream().anyMatch(t -> t.getType() == TokenType.TOKEN_HTML_CLOSE));
    }

    @Test
    void testPythonHashCommentsRespected() {
        String code = "print('hi')\n# just a comment\n#@include-if FOO\nprint('bye')";

        // In PYTHON, '#' should be treated as comment
        List<Token> pyTokens = tokenize(LanguageMode.PYTHON, code);
        assertTrue(pyTokens.stream().anyMatch(t -> t.getToken().startsWith("# just a comment")));
        assertTrue(pyTokens.stream().anyMatch(t -> t.getType() == TokenType.TOKEN_AT_INCLUDE_IF));

        // In JAVA, '#' should just be TEXT
        List<Token> javaTokens = tokenize(LanguageMode.JAVA, code);
        assertTrue(javaTokens.stream().anyMatch(t -> t.getToken().contains("# just a comment")));
        assertFalse(javaTokens.stream().anyMatch(t -> t.getType() == TokenType.TOKEN_AT_INCLUDE_IF));
    }

    @Test
    void testEndOfFileAlwaysProduced() {
        List<Token> tokens = tokenize(LanguageMode.JAVA, "int a;");
        assertEquals(TokenType.TOKEN_EOF,
                tokens.get(tokens.size() - 1).getType(),
                "EOF token should always be last");
    }
}
