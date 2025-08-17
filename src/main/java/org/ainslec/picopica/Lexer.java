/*
 * Copyright 2025 Chris Ainsley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ainslec.picopica;

import static org.ainslec.picopica.TokenType.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lexer for the preprocessor file format.
 * Responsibilities:
 *  - Emit TEXT chunks for non-comment code/text.
 *  - Recognize block comments:  /* ... *​/  and Javadoc: /** ... *​/
 *  - Recognize HTML comments inside Javadoc: <!-- ... -->
 *  - Recognize Python-style single-line comments (#...) if enabled.
 *  - Inside any comment body, recognize directive atoms: @include-if, @exclude-if, @end, and the KEY token.
 *  - DO NOT PARSE structure / nesting. This is only a tokenizer.
 *
 * Notes:
 *  - String and char literals are respected; comment openers inside them are treated as text.
 *  - End-of-lines are now emitted as TOKEN_EOL; a single EOL token represents \r\n, \n\r, lone \n, or lone \r (verbatim).
 */
public class Lexer {

    private boolean lineHasNonWhitespace = false;


    private boolean isWhitespaceInt(int c) {
        return c == 32   // space
            || c == 9    // tab
            || c == 10   // \n
            || c == 13;  // \r
    }

    protected List<Token> tokenize(LanguageMode languageMode, String resourceUrl, String sourceCode) {
        ArrayList<Token> out = new ArrayList<>();
        LexerCursor cursor = new LexerCursor(sourceCode, LexerState.DEFAULT, resourceUrl);
        cursor.setLineNumberBase1(cursor.getLineNumberBase1());
        final StringBuilder buf = new StringBuilder();
        LexerState state = LexerState.DEFAULT;  
        cursor.replaceState(state); 
        while (true) {
            int c = cursor.peekChar(0);
            if (c == -1) {
                flushTextToken(cursor, buf, out);
                out.add(new Token(cursor.getLineNumberBase1(), cursor.getColumnNumberBase1(),
                        -1, -1, "", TOKEN_EOF, cursor.getLexicalState()));
                break;
            }
            switch (state) {
                case DEFAULT: {
                    if (c == '\r' || c == '\n') {
                        flushTextToken(cursor, buf, out);
                        emitEol(cursor, out);
                        continue;
                    }
                    if (c == '\"') {
                        consumeStringLiteral(cursor, buf, '\"');
                        continue;
                    } else if (c == '\'') {
                        consumeCharLiteral(cursor, buf);
                        continue;
                    }
                    if (languageMode.isParseLeadingSingleHash() && c == '#' && lineHasNonWhitespace == false) {
                        flushTextToken(cursor, buf, out);
                        int line = cursor.getLineNumberBase1();
                        int col  = cursor.getColumnNumberBase1();
                        consume(cursor); 
                        StringBuilder commentBuf = new StringBuilder();
                        while (true) {
                            int cc = cursor.peekChar(0);
                            if (cc == -1 || cc == '\r' || cc == '\n') break;
                            commentBuf.append((char) consume(cursor));
                        }
                        String text = commentBuf.toString().trim();
                        if (text.startsWith("@")) {
                            LexerCursor subCursor = new LexerCursor(text, LexerState.IN_BLOCK_COMMENT, resourceUrl);
                            boolean isWasComplex = lexDirectiveAtom(subCursor, out, () -> {
                                emitFixedToken(cursor, out, "#", TOKEN_BLOCK_OPEN, line, col);
                            });
                            if (isWasComplex) {
                            	emitFixedToken(cursor, out, "", TOKEN_BLOCK_CLOSE);
                            }
                        } else {
                            out.add(new Token(line, col, line, col + 1 + commentBuf.length(),
                                    "#" + commentBuf.toString(), TOKEN_TEXT, cursor.getLexicalState()));
                        }
                        continue;
                    }
                    
                    if (languageMode.isParseLeadingDoubleHyphen() 
                            && c == '-' 
                            && cursor.peekChar(1) == '-' 
                            && lineHasNonWhitespace == false) {
                        flushTextToken(cursor, buf, out);
                        int line = cursor.getLineNumberBase1();
                        int col  = cursor.getColumnNumberBase1();
                        consume(cursor); // first '-'
                        consume(cursor); // second '-'
                        StringBuilder commentBuf = new StringBuilder();
                        while (true) {
                            int cc = cursor.peekChar(0);
                            if (cc == -1 || cc == '\r' || cc == '\n') break;
                            commentBuf.append((char) consume(cursor));
                        }
                        String text = commentBuf.toString().trim();
                        if (text.startsWith("@")) {
                            LexerCursor subCursor = new LexerCursor(text, LexerState.IN_BLOCK_COMMENT, resourceUrl);
                            boolean isWasComplex = lexDirectiveAtom(subCursor, out, () -> {
                                emitFixedToken(cursor, out, "--", TOKEN_BLOCK_OPEN, line, col);
                            });
                            if (isWasComplex) {
                                emitFixedToken(cursor, out, "", TOKEN_BLOCK_CLOSE);
                            }
                        } else {
                            out.add(new Token(line, col, line, col + 2 + commentBuf.length(),
                                    "--" + commentBuf.toString(), TOKEN_TEXT, cursor.getLexicalState()));
                        }
                        continue;
                    }
                    if (languageMode.isParseCLikeMultiLineComments() && c == '/') {
                        int c1 = cursor.peekChar(1);
                        int c2 = cursor.peekChar(2);
                        if (c1 != -1 && c1 == '*' && c2 != -1 && c2 == '*') {
                            flushTextToken(cursor, buf, out);
                            int c3 = cursor.peekChar(3);
                            if (c3 != -1 && c3 == '/') {
                                int line = cursor.getLineNumberBase1();
                                int col  = cursor.getColumnNumberBase1();
                                out.add(new Token(line, col, line, col + 4, "/**/", TOKEN_TEXT, cursor.getLexicalState()));
                                consume(cursor); consume(cursor); consume(cursor); consume(cursor);
                                continue;
                            }
                            emitFixedToken(cursor, out, "/**", TOKEN_JAVADOC_OPEN);
                            consume(cursor); consume(cursor); consume(cursor);
                            state = LexerState.IN_BLOCK_COMMENT;
                            cursor.replaceState(state);
                            continue;
                        } else if (c1 != -1 && c1 == '*') {
                            flushTextToken(cursor, buf, out);
                            emitFixedToken(cursor, out, "/*", TOKEN_BLOCK_OPEN);
                            consume(cursor); consume(cursor);
                            state = LexerState.IN_BLOCK_COMMENT;
                            cursor.replaceState(state);
                            continue;
                        }
                    }
                    buf.append((char) consume(cursor));
                    break;
                }
                case IN_BLOCK_COMMENT: {
                    int c0e = cursor.peekChar(0);
                    if (c0e == '\r' || c0e == '\n') {
                        flushTextToken(cursor, buf, out);
                        emitEol(cursor, out);
                        continue;
                    }
                    int c0 = cursor.peekChar(0);
                    if (c0 == '*') {
                        int c1 = cursor.peekChar(1);
                        if (c1 != -1 && c1 == '/') {
                            flushTextToken(cursor, buf, out);
                            emitFixedToken(cursor, out, "*/", TOKEN_BLOCK_CLOSE);
                            consume(cursor); consume(cursor);
                            state = LexerState.DEFAULT;
                            cursor.replaceState(state);
                            continue;
                        }
                    }
                    if (languageMode.isParseHtmlBlockComments() && c0 == '<' && matchAhead(cursor, "<!--")) {
                        flushTextToken(cursor, buf, out);
                        emitFixedToken(cursor, out, "<!--", TOKEN_HTML_OPEN);
                        advance(cursor, 4);
                        state = LexerState.IN_HTML_COMMENT;
                        cursor.replaceState(state);
                        continue;
                    }
                    if (c0 == '@') {
                        flushTextToken(cursor, buf, out);
                        lexDirectiveAtom(cursor, out, null);
                        continue;
                    }
                    buf.append((char) consume(cursor));
                    break;
                }
                case IN_HTML_COMMENT: {
                    int c0e = cursor.peekChar(0);
                    if (c0e == '\r' || c0e == '\n') {
                        flushTextToken(cursor, buf, out);
                        emitEol(cursor, out);
                        continue;
                    }
                    if (matchAhead(cursor, "-->")) {
                        flushTextToken(cursor, buf, out);
                        emitFixedToken(cursor, out, "-->", TOKEN_HTML_CLOSE);
                        advance(cursor, 3);
                        state = LexerState.IN_BLOCK_COMMENT;
                        cursor.replaceState(state);
                        continue;
                    }
                    int c0 = cursor.peekChar(0);
                    if (c0 == '@') {
                        flushTextToken(cursor, buf, out);
                        lexDirectiveAtom(cursor, out, null);
                        continue;
                    }
                    buf.append((char) consume(cursor));
                    break;
                }
            }
        }
        return out;
    }

    private void emitEol(LexerCursor cursor, List<Token> out) {
        int line = cursor.getLineNumberBase1();
        int col  = cursor.getColumnNumberBase1();
        int c0 = cursor.peekChar(0);
        String lexeme;
        if (c0 == '\r') {
            int c1 = cursor.peekChar(1);
            if (c1 == '\n') {
                lexeme = "\r\n";
                consume(cursor); consume(cursor);
            } else {
                lexeme = "\r";
                consume(cursor);
            }
        } else {
            int c1 = cursor.peekChar(1);
            if (c1 == '\r') {
                lexeme = "\n\r";
                consume(cursor); consume(cursor);
            } else {
                lexeme = "\n";
                consume(cursor);
            }
        }
        out.add(new Token(line, col, line, col + lexeme.length(), lexeme, TOKEN_EOL, cursor.getLexicalState()));
        resetLineFlags();
    }

    private boolean lexDirectiveAtom(LexerCursor cursor, List<Token> out, Runnable onDetectDirective) {
        int line = cursor.getLineNumberBase1();
        int col  = cursor.getColumnNumberBase1();
        LexerState ls = cursor.getLexicalState();
        String word = readIdent(cursor);
        int type;
        int wordLength = word.length();
		if ("@include-if".equals(word)) {
            type = TOKEN_AT_INCLUDE_IF;
        } else if ("@exclude-if".equals(word)) {
            type = TOKEN_AT_EXCLUDE_IF;
        } else if ("@file-exclude-if".equals(word)) {
            type = TokenType.TOKEN_FILE_EXCLUDE_IF;
        } else if ("@end".equals(word)) {
            type = TOKEN_AT_END;
        } else if ("@else".equals(word)) {
            type = TokenType.TOKEN_AT_ELSE;
        } else if ("@license".equals(word)) {
            type = TokenType.TOKEN_AT_LICENSE;
        } else {
            out.add(new Token(line, col, line, col + wordLength, word, TOKEN_TEXT, ls));
            return false;
        }
		if (onDetectDirective != null) {
			onDetectDirective.run();
		}
        out.add(new Token(line, col, line, col + wordLength, word, type, ls));
        if (type == TOKEN_AT_INCLUDE_IF || type == TOKEN_AT_EXCLUDE_IF || type == TokenType.TOKEN_FILE_EXCLUDE_IF) {
            int wsStartCol = cursor.getColumnNumberBase1();
            StringBuilder ws = new StringBuilder();
            int p = cursor.peekChar(0);
            while (p != -1 && (p == ' ' || p == '\t')) {
                ws.append((char) consume(cursor));
                p = cursor.peekChar(0);
            }
            if (ws.length() > 0) {
                out.add(new Token(line, wsStartCol, line, wsStartCol + ws.length(), ws.toString(), TOKEN_TEXT, ls));
            }
            String key = readKeyToken(cursor);
            if (!key.isEmpty()) {
                int kStartCol = cursor.getColumnNumberBase1() - key.length();
                out.add(new Token(line, kStartCol, line, kStartCol + key.length(), key, TOKEN_KEY, ls));
            }
        }
        return true;
    }

    private String readIdent(LexerCursor cursor) {
        StringBuilder b = new StringBuilder();
        int c = consume(cursor);
        if (c != '@') return "";
        b.append('@');
        int p = cursor.peekChar(0);
        while (p != -1 && isIdentChar((char) p)) {
            b.append((char) consume(cursor));
            p = cursor.peekChar(0);
            if (p == '-') {
                b.append((char) consume(cursor));
                p = cursor.peekChar(0);
            }
        }
        return b.toString();
    }

    private String readKeyToken(LexerCursor cursor) {
        StringBuilder b = new StringBuilder();
        int p = cursor.peekChar(0);
        while (p != -1 && !isBoundaryInComment((char) p)) {
            b.append((char) consume(cursor));
            p = cursor.peekChar(0);
        }
        return b.toString().trim();
    }

    private boolean isIdentChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isBoundaryInComment(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n'
                || c == '*' || c == '/' || c == '<' || c == '!'
                || c == '-' || c == '>';
    }

    private void consumeStringLiteral(LexerCursor cursor, StringBuilder buf, char quote) {
        buf.append((char) consume(cursor));
        while (cursor.peekChar(0) != -1) {
            char c = (char) consume(cursor);
            buf.append(c);
            if (c == '\\') {
                int e = cursor.peekChar(0);
                if (e != -1) buf.append((char) consume(cursor));
                continue;
            }
            if (c == quote) break;
        }
    }

    private void consumeCharLiteral(LexerCursor cursor, StringBuilder buf) {
        consumeStringLiteral(cursor, buf, '\'');
    }

    private void flushTextToken(LexerCursor cursor, StringBuilder buf, List<Token> out) {
        if (buf.length() == 0) return;
        int len = buf.length();
        int endCol = cursor.getColumnNumberBase1();
        int startCol = endCol - len;
        out.add(new Token(cursor.getLineNumberBase1(), startCol,
                cursor.getLineNumberBase1(), endCol,
                buf.toString(), TOKEN_TEXT, cursor.getLexicalState()));
        buf.setLength(0);
    }

    private void emitFixedToken(LexerCursor cursor, List<Token> out, String lexeme, int type) {
        int line = cursor.getLineNumberBase1();
        int col  = cursor.getColumnNumberBase1();
        emitFixedToken(cursor, out, lexeme, type, line, col);
    }

	private void emitFixedToken(LexerCursor cursor, List<Token> out, String lexeme, int type, int line, int col) {
		out.add(new Token(line, col, line, col + lexeme.length(), lexeme, type, cursor.getLexicalState()));
	}

    private boolean matchAhead(LexerCursor cursor, String s) {
        for (int i = 0; i < s.length(); i++) {
            int c = cursor.peekChar(i);
            if (c == -1 || c != s.charAt(i)) return false;
        }
        return true;
    }

    private void advance(LexerCursor cursor, int n) {
        for (int i = 0; i < n; i++) {
        	consume(cursor);
        }
    }


    private int consume(LexerCursor cursor) {
        int c = cursor.consumeChar();
        if (c != -1 && !isWhitespaceInt(c)) {
            lineHasNonWhitespace = true;
        }
        return c;
    }

    private void resetLineFlags() {
        lineHasNonWhitespace = false;
    }
}