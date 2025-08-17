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

/**
 * Represents the current lexical analysis state of the {@link Lexer}.
 * <p>
 * The state determines how incoming characters are interpreted
 * (e.g., whether the lexer is in normal text, inside a block comment,
 * or inside an HTML comment).
 * </p>
 */
public enum LexerState {

    /**
     * Default state for normal source code and text.
     * <p>
     * In this state, the lexer treats comment delimiters
     * (<code>&#47;&#42;</code>, <code>&#47;&#42;&#42;</code>) as comment starts
     * and string/char literals as opaque sequences.
     * </p>
     */
    DEFAULT("Default", false /* ignore Whitespace */),

    /**
     * State for being inside a block comment (<code>&#47;&#42; ... &#42;&#47;</code>)
     * or a Javadoc comment (<code>&#47;&#42;&#42; ... &#42;&#47;</code>).
     * <p>
     * In this state, the lexer looks for block comment close markers,
     * HTML comment openers, and directive atoms.
     * </p>
     */
    IN_BLOCK_COMMENT("BlockComment", false),

    /**
     * State for being inside an HTML comment (<code>&lt;!-- ... --&gt;</code>)
     * that itself appears inside a block or Javadoc comment.
     * <p>
     * In this state, the lexer scans for HTML comment close markers
     * and directive atoms.
     * </p>
     */
    IN_HTML_COMMENT("HtmlComment", false);

    /** Whether whitespace should be ignored in this state. */
    private final boolean ignoreWhitespace;

    /** Human-readable state name. */
    private final String name;

    /**
     * Creates a new lexer state.
     *
     * @param name              human-readable name for the state
     * @param ignoreWhitespace  whether whitespace should be ignored in this state
     */
    private LexerState(String name, boolean ignoreWhitespace) {
        this.name = name;
        this.ignoreWhitespace = ignoreWhitespace;
    }

    /**
     * @return {@code true} if whitespace should be ignored in this state
     */
    public boolean isIgnoreWhitespace() {
        return ignoreWhitespace;
    }

    /**
     * @return the human-readable name of this state
     */
    public String getName() {
        return name;
    }
}
