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

import java.util.HashMap;

/**
 * Defines token type constants and names used by the {@link Lexer} and {@link Token}.
 * <p>
 * Token types represent both structural tokens (e.g., comment delimiters)
 * and content tokens (e.g., text, directive keywords).
 * </p>
 */
public class TokenType {

    /** Free-form text outside comments, or non-directive text inside comments. */
    public static final int TOKEN_TEXT = 0;

    /** Opening of a standard block comment: <code>&#47;&#42;</code>. */
    public static final int TOKEN_BLOCK_OPEN = 1;

    /** Closing of a block comment: <code>&#42;&#47;</code>. */
    public static final int TOKEN_BLOCK_CLOSE = 2;

    /** Opening of a Javadoc comment: <code>&#47;&#42;&#42;</code>. */
    public static final int TOKEN_JAVADOC_OPEN = 3;

    /** Opening of an HTML comment: <code>&lt;!--</code>. */
    public static final int TOKEN_HTML_OPEN = 4;

    /** Closing of an HTML comment: <code>--&gt;</code>. */
    public static final int TOKEN_HTML_CLOSE = 5;

    /** Directive atom: <code>&#64;include-if</code>. */
    public static final int TOKEN_AT_INCLUDE_IF = 6;

    /** Directive atom: <code>&#64;exclude-if</code>. */
    public static final int TOKEN_AT_EXCLUDE_IF = 7;
    
    /** Directive atom: <code>&#64;license</code>. */
    public static final int TOKEN_AT_LICENSE = 8;
    
    /** Directive atom: <code>&#64;empty-if DEFAULT | KEY</code>. */
    public static final int TOKEN_FILE_EXCLUDE_IF = 9;
    
    /** Directive atom: <code>&#64;else</code>. */
    public static final int TOKEN_AT_ELSE = 10;
    
    /** Directive atom: <code>&#64;end</code>. */
    public static final int TOKEN_AT_END = 11;

    /** Directive key token (single token after <code>&#64;include-if</code> / <code>&#64;exclude-if</code>). */
    public static final int TOKEN_KEY = 12;

    /** End-of-line marker: \n, \r, \r\n, or \n\r (preserved verbatim). */
    public static final int TOKEN_EOL = 13;
    
    /** Runs of horizontal whitespace (spaces/tabs/formfeeds) in DEFAULT state only. */
    public static final int TOKEN_WS = 14;

    /** End-of-file marker. */
    public static final int TOKEN_EOF = 15;

    /**
     * Mapping from token type constants to human-readable names.
     * Used for debugging and {@link Token#toString()}.
     */
    public static final String[] LEX_TOKENS = new String[] {
        "TEXT",
        "BLOCK_OPEN",
        "BLOCK_CLOSE",
        "JAVADOC_OPEN",
        "HTML_OPEN",
        "HTML_CLOSE",
        "AT_INCLUDE",
        "AT_EXCLUDE_IF",
        "AT_LICENSE",
        "AT_EMPTY_IF",
        "AT_ELSE",
        "AT_END",
        "KEY",
        "EOL",
        "WS",
        "EOF",
         
    };

    /**
     * Optional map for looking up token type constants by keyword text.
     * Not currently populated by default.
     */
    static HashMap<String, Integer> KEYWORD_TO_TOKEN_MAP = new HashMap<>();
}
