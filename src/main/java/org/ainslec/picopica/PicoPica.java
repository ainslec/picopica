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

import static org.ainslec.picopica.TokenType.TOKEN_AT_END;
import static org.ainslec.picopica.TokenType.TOKEN_AT_EXCLUDE_IF;
import static org.ainslec.picopica.TokenType.TOKEN_AT_INCLUDE_IF;
import static org.ainslec.picopica.TokenType.TOKEN_BLOCK_CLOSE;
import static org.ainslec.picopica.TokenType.TOKEN_BLOCK_OPEN;
import static org.ainslec.picopica.TokenType.TOKEN_EOL;
import static org.ainslec.picopica.TokenType.TOKEN_HTML_CLOSE;
import static org.ainslec.picopica.TokenType.TOKEN_HTML_OPEN;
import static org.ainslec.picopica.TokenType.TOKEN_JAVADOC_OPEN;
import static org.ainslec.picopica.TokenType.TOKEN_KEY;
import static org.ainslec.picopica.TokenType.TOKEN_AT_ELSE;
import static org.ainslec.picopica.TokenType.TOKEN_TEXT;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Preprocessor that tokenizes with {@link Lexer} and performs a lightweight
 * parse to apply include/exclude and other preprocessing directives found in comments.
 *
 * <p>
 * <strong>Comment syntaxes recognized</strong> (shown with safe escapes):
 * </p>
 * <ul>
 *   <li>Block comments: <code>&#47;&#42; ... &#42;&#47;</code></li>
 *   <li>Javadoc comments: <code>&#47;&#42;&#42; ... &#42;&#47;</code></li>
 *   <li>HTML comments inside Javadoc: <code>&lt;!-- ... --&gt;</code></li>
 * </ul>
 *
 * <p>
 * <strong>Supported directives</strong> (appear inside comment bodies):
 * </p>
 * <ul>
 *   <li><code>&#64;include-if KEY</code> — include the following block only if {@code KEY} matches.</li>
 *   <li><code>&#64;exclude-if KEY</code> — exclude the following block if {@code KEY} matches.</li>
 *   <li><code>&#64;else</code> — alternate branch within an <code>@include-if</code> or <code>@exclude-if</code> block.</li>
 *   <li><code>&#64;end</code> — closes an <code>@include-if</code> or <code>@exclude-if</code> block.</li>
 *   <li><code>&#64;license [KEY]</code> — inserts a license header from a provided license map (optional key).</li>
 * </ul>
 *
 * <p>
 * <strong>Forms</strong>
 * </p>
 * <ul>
 *   <li>
 *     <em>Single-comment include/exclude</em> (inline body between directive and <code>@end</code>):
 *     <pre><code>
 * &#47;&#42; &#64;include-if FOO ... &#64;end &#42;&#47;
 * &#47;&#42; &#64;exclude-if BAR ... &#64;end &#42;&#47;
 *     </code></pre>
 *   </li>
 *   <li>
 *     <em>Spanning include/exclude</em> (directive and <code>@end</code> in separate comments):
 *     <pre><code>
 * &#47;&#42; &#64;include-if FOO &#42;&#47;
 *    ... content ...
 * &#47;&#42; &#64;end &#42;&#47;
 *
 * &#47;&#42; &#64;exclude-if BAR &#42;&#47;
 *    ... content ...
 * &#47;&#42; &#64;end &#42;&#47;
 *     </code></pre>
 *   </li>
 *   <li>
 *     <em>Else branch</em>:
 *     <pre><code>
 * &#47;&#42; &#64;include-if FOO &#42;&#47;
 *    ... content if FOO ...
 * &#47;&#42; &#64;else &#42;&#47;
 *    ... content otherwise ...
 * &#47;&#42; &#64;end &#42;&#47;
 *     </code></pre>
 *   </li>
 *   <li>
 *     <em>License insertion</em>:
 *     <pre><code>
 * &#47;&#42; &#64;license &#42;&#47;
 *     </code></pre>
 *     Looks up license text from provided license map using DEFAULT if no keys provided or a single key.
 *   </li>
 * </ul>
 *
 * <p>
 * <strong>Javadoc HTML comment directives</strong>:
 * </p>
 * <ul>
 *   <li><code>&lt;!-- &#64;include-if KEY --&gt;</code></li>
 *   <li><code>&lt;!-- &#64;exclude-if KEY --&gt;</code></li>
 *   <li><code>&lt;!-- &#64;else --&gt;</code></li>
 *   <li><code>&lt;!-- &#64;end --&gt;</code></li>
 * </ul>
 * These work identically to their block comment counterparts, but are placed within
 * HTML comment markers inside Javadoc bodies.
 *
 * <p>
 * All non-directive text is preserved verbatim. Newlines are preserved.
 * </p>
 * <p>
 * Provides convenience methods to configure and execute conditional
 * compilation directly on source strings. Most usage flows through
 * {@link #input(String, String...)} for fluent configuration, but
 * {@link #exec(String, String...)} is available for quick, one-off
 * preprocessing.
 * </p>
 */
public class PicoPica {
	
	
	 /**
     * Reserved key name for the default license mapping.
     * <p>
     * Cannot be used as a user-specified active key. Represents the
     * absence of other keys when performing license lookups.
     * </p>
     */
    public static final String DEFAULT_RESERVED = "DEFAULT";
    
    
    public static final String DEFAULT_LICENSE_KEY = DEFAULT_RESERVED;
    
    private static final class EmptyAllOutput extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
    

    /**
     * Creates a new {@link Api} builder instance with the given
     * source text and optional active keys.
     * <p>
     * This is the primary entry point for configuring preprocessing.
     * You may further chain methods on the returned {@link Api} to set
     * resource URLs, license maps, language modes, and other options
     * before calling {@link Api#exec()}.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * String output = PicoPica.input(src, "FOO").exec();
     * }</pre>
     *
     * @param src  the source text to preprocess
     * @param keys optional active keys to control conditional inclusion/exclusion
     * @return a new {@link Api} builder instance
     */
    public static Api input(String src, String ... keys) {
        return new Api().load(src, keys);
    }
    
    /**
     * Quickly runs the preprocessor on the given source text using
     * the specified active keys.
     * <p>
     * This is a shorthand for:
     * <pre>{@code
     * PicoPica.input(src, keys).exec();
     * }</pre>
     * Use this when no additional configuration (e.g. license mapping,
     * resource URLs) is needed. Keys are optional comma seperated strings in the above example.
     * </p>
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * String stableOnly = PicoPica.exec(input, "RELEASE");
     * }</pre>
     *
     * @param src  the source text to preprocess
     * @param keys optional active keys to control conditional inclusion/exclusion
     * @return the preprocessed source text with directives applied
     * @throws IllegalStateException if the input is missing or invalid
     * @throws IllegalArgumentException if invalid keys are provided (e.g. {@code DEFAULT})
     */
    public static String exec(String src, String ... keys) {
        return new Api().load(src, keys).exec();
    }

	protected static String choose(String resourceUrl, String src, Set<String> keys, Map<String, String> licenseMap, boolean isAutoAddLicenseToHeader, LanguageMode languageMode) {
		Lexer lx = new Lexer();
		List<Token> toks = lx.tokenize(languageMode, resourceUrl, src);

		StringBuilder out = new StringBuilder(src.length());
		
		if (keys != null && keys.contains(DEFAULT_RESERVED)) {
			throw new IllegalArgumentException("Not allowed to call a key DEFAULT (reserved token name), reserved for absense of other keys.");
		}
		
		boolean isLicenseLookupModeEnabled = isAutoAddLicenseToHeader || (licenseMap != null && licenseMap.size() > 0);
		
		if (isLicenseLookupModeEnabled && keys != null && keys.size() > 1) {
			throw new IllegalArgumentException("Cannot provide multiple keys if license mode is enabled (would lead to ambiguity).");
		}
		
		LineOmitter appender = new LineOmitter(out);
		
		// If this mode is set then we will automatically lookup license header, and append here
		// This can be convenient so that license headers don't need to be added to a file or files manually
		if (isAutoAddLicenseToHeader) {
			appendLicense(appender, licenseMap, keys);
		}
		
	    try {
	        processRange(resourceUrl, toks, 0, toks.size(), keys, appender, true /* topLevel */, licenseMap, languageMode);
	        appender.flush();
			String result = out.toString();
			return result;
	    } catch (EmptyAllOutput e) {
	        // If @empty_if matched anywhere, nothing is emitted.
	        return "";
	    }

	}
	
	/* ===================== Parser / Processor ===================== */
   private static void processRange(String resourceUrl, List<Token> toks, int from, int to, Set<String> keys, LineOmitter appender, boolean topLevel, Map<String, String> licenseMap, LanguageMode languageMode) {
        int i = from;
        while (i < to) {
            Token t = toks.get(i);
            int tt = t.getType();
            if (tt == TOKEN_EOL) {
                appender.appendEol(t.getToken());
                i++;
                continue;
            }
            if (tt == TOKEN_TEXT) {
                appender.appendNonEol(t.getToken());
                i++;
                continue;
            }
            if (tt == TOKEN_BLOCK_OPEN) {
                int close = findMatching(toks, i, TOKEN_BLOCK_CLOSE);
                if (close < 0) {
                    throw new IllegalStateException("Unterminated block comment");
                }
                DirectiveKind kind = classifyBlockDirective(toks, i + 1, close);
                switch (kind.kind) {
                    case EXCLUDE_IF: {
                        String key = kind.key;
                        int afterClose = close + 1;
                        MatchEnd me = findMatchingSpanEnd(toks, afterClose);
                        if (me == null) {
                            throw new IllegalStateException("Unclosed @exclude-if " + key);
                        }
                        appender.setDirectiveLine(true);

                        // compute first part and else part ranges
                        int part1From = afterClose;
                        int part1To   = (me.beforeElse >= 0) ? me.beforeElse : me.beforeEnd; // exclusive
                        int part2From = (me.afterElse >= 0) ? me.afterElse : -1;
                        int part2To   = me.beforeEnd;

                        if (keyMatches(keys, key)) {
                            // exclude when matches -> take else part if present, otherwise drop
                            if (part2From >= 0) {
                                int left = part2From, rightExclusive = part2To;
                                while (left < rightExclusive && isWhitespaceOnlyText(toks.get(left))) left++;
                                while (rightExclusive > left && isWhitespaceOnlyText(toks.get(rightExclusive - 1))) rightExclusive--;
                                processRange(resourceUrl, toks, left, rightExclusive, keys, appender, false, licenseMap, languageMode);
                            }
                            i = me.afterEnd;
                        } else {
                            // keep first part
                            int left = part1From, rightExclusive = part1To;
                            while (left < rightExclusive && isWhitespaceOnlyText(toks.get(left))) left++;
                            while (rightExclusive > left && isWhitespaceOnlyText(toks.get(rightExclusive - 1))) rightExclusive--;
                            processRange(resourceUrl, toks, left, rightExclusive, keys, appender, false, licenseMap, languageMode);
                            i = me.afterEnd;
                        }
                        if (topLevel && i < to) {
                            boolean isEol = (isEol(toks.get(i)) || isNewlineText(toks.get(i)));
                            if (isEol) { appender.clearLineBufferIfJustWhitespace(); i++; }
                        }
                        break;
                    }
                    case INCLUDE_IF: {
                        String key = kind.key;
                        int afterClose = close + 1;
                        MatchEnd me = findMatchingSpanEnd(toks, afterClose);
                        if (me == null) {
                            throw new IllegalStateException("Unclosed @include-if " + key);
                        }
                        appender.setDirectiveLine(true);

                        int part1From = afterClose;
                        int part1To   = (me.beforeElse >= 0) ? me.beforeElse : me.beforeEnd;
                        int part2From = (me.afterElse >= 0) ? me.afterElse : -1;
                        int part2To   = me.beforeEnd;

                        if (keyMatches(keys, key)) {
                            // take first part
                            int left = part1From, rightExclusive = part1To;
                            while (left < rightExclusive && isWhitespaceOnlyText(toks.get(left))) left++;
                            while (rightExclusive > left && isWhitespaceOnlyText(toks.get(rightExclusive - 1))) rightExclusive--;
                            processRange(resourceUrl, toks, left, rightExclusive, keys, appender, false, licenseMap, languageMode);
                        } else if (part2From >= 0) {
                            // take else part if present
                            int left = part2From, rightExclusive = part2To;
                            while (left < rightExclusive && isWhitespaceOnlyText(toks.get(left))) left++;
                            while (rightExclusive > left && isWhitespaceOnlyText(toks.get(rightExclusive - 1))) rightExclusive--;
                            processRange(resourceUrl, toks, left, rightExclusive, keys, appender, false, licenseMap, languageMode);
                        }
                        i = me.afterEnd;
                        if (topLevel && i < to) {
                            boolean isEol = (isEol(toks.get(i)) || isNewlineText(toks.get(i)));
                            if (isEol) { appender.clearLineBufferIfJustWhitespace(); i++; }
                        }
                        break;
                    }
                    case INCLUDE_SINGLE_COMMENT: {
                        appender.setDirectiveLine(true);
                        if (kind.key == null) {
                            throw new IllegalStateException("Missing key for @include-if");
                        }
                        if (kind.includeEndIndex < 0) {
                            throw new IllegalStateException("Missing @end for @include-if " + kind.key);
                        }
                        int bodyFrom = kind.afterKeyIndex;
                        int bodyToExclusive = kind.includeEndIndex;
                        String inner = tokensToString(toks, bodyFrom, bodyToExclusive);
                        if (keyMatches(keys, kind.key)) {
                            inner = stripOneOuterSpace(inner);
                
                            String innerProcessed = choose(resourceUrl, inner, keys, null /* licenseMap */, false /* isAutoAddLicense */, languageMode);

                            handleInnerProcessedForInlineDirectives(appender, innerProcessed);
                     
                        } else {
                            // drop inactive inline include entirely
                        }
                        i = close + 1;
                        break;
                    }
                    case EXCLUDE_SINGLE_COMMENT: {
                        appender.setDirectiveLine(true);
                        if (kind.key == null) {
                            throw new IllegalStateException("Missing key for @exclude-if");
                        }
                        if (kind.includeEndIndex < 0) {
                            throw new IllegalStateException("Missing @end for @exclude-if " + kind.key);
                        }
                        int bodyFrom = kind.afterKeyIndex;
                        int bodyToExclusive = kind.includeEndIndex;
                        String inner = tokensToString(toks, bodyFrom, bodyToExclusive);
                        
                        if (!keyMatches(keys, kind.key)) {
                            inner = stripOneOuterSpace(inner);
                          
                            String innerProcessed = choose(resourceUrl, inner, keys, null /* licenseMap */, false /* isAutoAddLicense */, languageMode);
                            handleInnerProcessedForInlineDirectives(appender, innerProcessed);
                        } else {
                            // drop when key IS present
                        }
                        
                        i = close + 1;
                        break;
                    }
                    case LICENSE: {
                        appender.setDirectiveLine(true);
                        appendLicense(appender, licenseMap, keys);
                        i = close + 1;
                        if (topLevel && i < to) {
                            boolean isEol = (isEol(toks.get(i)) || isNewlineText(toks.get(i)));
                            if (isEol) { appender.clearLineBufferIfJustWhitespace(); i++; }
                        }
                        break;
                    }
                    case FILE_EXCLUDE_IF: {
                        String key = kind.key;
                        appender.setDirectiveLine(true);

                        // If it matches, abort *everything* and return empty output.
                        if (emptyIfMatches(keys, key)) {
                            throw new EmptyAllOutput();
                        }

                        // If it does not match, we simply drop the directive comment itself.
                        i = close + 1;

                        // If top-level and the next token is an EOL (or newline TEXT), remove the blank line.
                        if (topLevel && i < to) {
                            boolean isEol = (isEol(toks.get(i)) || isNewlineText(toks.get(i)));
                            if (isEol) { appender.clearLineBufferIfJustWhitespace(); i++; }
                        }
                        break;
                    }
                    case END: {
                        appender.setDirectiveLine(true);
                        appendTokensVerbatim(toks, i, close, appender);
                        i = close + 1;
                        break;
                    }
                    case ELSE: {
                        appender.setDirectiveLine(true);
                        appendTokensVerbatim(toks, i, close, appender);
                        i = close + 1;
                        break;
                    }
                    case PLAIN: {
                        appendTokensVerbatim(toks, i, close, appender);
                        i = close + 1;
                        break;
                    }
                }
                continue;
            }
            if (tt == TOKEN_JAVADOC_OPEN) {
                int close = findMatching(toks, i, TOKEN_BLOCK_CLOSE);
                if (close < 0) {
                    throw new IllegalStateException("Unterminated Javadoc comment");
                }
                String currentTok = toks.get(i).getToken();
                appender.appendNonEol(currentTok); // "/**"
                LineOmitterJavadoc lo = new LineOmitterJavadoc(appender);
                processJavadocBody(toks, i + 1, close, keys, lo);
                lo.flushAtEnd();
                String closeToken = toks.get(close).getToken();
                appender.appendNonEol(closeToken); // "*/"
                i = close + 1;
                continue;
            }
            appender.appendNonEol(t.getToken());
            i++;
        }
    }

	private static void handleInnerProcessedForInlineDirectives(LineOmitter appender, String innerProcessed) {
		char[] charArray = innerProcessed.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (char c : charArray) {
			if (c == '\n' || c == '\r') {
				if (sb.length() > 0) {
					appender.appendNonEol(sb.toString());
				}
				appender.appendEol(String.valueOf(c));
				sb.setLength(0);
				
			} else {
				sb.append(c);
				
			}
		}

		if (sb.length() > 0) {
			appender.appendNonEol(sb.toString());
		}
		
		appender.setDirectiveLine(true);

	}

	private static void appendLicense(LineOmitter appender, Map<String, String> licenseMap, Set<String> keys) {
		if (licenseMap == null || licenseMap.isEmpty()) {
		    throw new IllegalStateException("Cannot use @license directive unless one or more license files has been provided.");
		}
		
		String currentKey = keys == null || keys.size() == 0 ? DEFAULT_LICENSE_KEY : keys.iterator().next();
		
		String license = licenseMap.get(currentKey);
		
		if (license == null) {
			throw new IllegalStateException("Cannot find license for '"+currentKey+"' key.");
		} else {
			appender.appendNonEol(license);
		}
	}

	/* ===================== Block comment helpers ===================== */
	private static int findMatching(List<Token> toks, int openIndex, int closeType) {
		for (int i = openIndex + 1; i < toks.size(); i++) {
			if (toks.get(i).getType() == closeType) {
				return i;
			}
		}
		return -1;
	}

	private static boolean isWhitespaceText(Token t) {
		return t.getType() == TOKEN_TEXT && t.getToken().trim().isEmpty();
	}

	private static boolean isWhitespaceOnlyText(Token t) {
		return t.getType() == TOKEN_TEXT && t.getToken().length() > 0 && t.getToken().trim().isEmpty();
	}

	private static DirectiveKind classifyBlockDirective(List<Token> toks, int bodyFrom, int bodyToExclusive) {
		int i = bodyFrom;
		
		while (i < bodyToExclusive && isWhitespaceText(toks.get(i))) {
			i++;
		}
		
		if (i >= bodyToExclusive) {
			return DirectiveKind.plain();
		}
		
		Token first = toks.get(i);
		int ft = first.getType();
		
		if (ft == TOKEN_AT_END) {
			for (int k = i + 1; k < bodyToExclusive; k++) {
				if (!isWhitespaceText(toks.get(k))) {
					return DirectiveKind.plain();
				}
			}
			return new DirectiveKind(BKind.END, null, -1, -1);
		}
		
		if (ft == TOKEN_AT_EXCLUDE_IF) {
		    int k = i + 1;
		    while (k < bodyToExclusive && isWhitespaceText(toks.get(k))) k++;
		    if (k >= bodyToExclusive || toks.get(k).getType() != TOKEN_KEY) {
		        return DirectiveKind.plain();
		    }
		    String key = toks.get(k).getToken();
		    int afterKey = k + 1;

		    int endIdx = -1;
		    boolean onlyWsAfterKey = true;
		    for (int m = afterKey; m < bodyToExclusive; m++) {
		        if (toks.get(m).getType() == TOKEN_AT_END) { endIdx = m; break; }
		        if (!isWhitespaceText(toks.get(m))) { onlyWsAfterKey = false; }
		    }

		    if (endIdx >= 0) {
		        // inline exclude form: /* @exclude-if KEY ... @end */
		        return new DirectiveKind(BKind.EXCLUDE_SINGLE_COMMENT, key, endIdx, afterKey);
		    }
		    if (onlyWsAfterKey) {
		        // spanning form: /* @exclude-if KEY */ ... /* @end */
		        return new DirectiveKind(BKind.EXCLUDE_IF, key, -1, -1);
		    }
		    // Text after key but no @end => treat as error (mirrors include)
		    throw new IllegalStateException("Missing @end for @exclude-if " + key);
		}
		if (ft == TOKEN_AT_INCLUDE_IF) {
			int k = i + 1;
			while (k < bodyToExclusive && isWhitespaceText(toks.get(k))) {
				k++;
			}
			if (k >= bodyToExclusive || toks.get(k).getType() != TOKEN_KEY) {
				throw new IllegalStateException("Missing key for @include-if");
			}
			String key = toks.get(k).getToken();
			int afterKey = k + 1;
			int endIdx = -1;
			boolean onlyWsAfterKey = true;
			for (int m = afterKey; m < bodyToExclusive; m++) {
				if (toks.get(m).getType() == TOKEN_AT_END) {
					endIdx = m;
					break;
				}
				if (!isWhitespaceText(toks.get(m))) {
					onlyWsAfterKey = false;
				}
			}
			if (endIdx >= 0) {
				return new DirectiveKind(BKind.INCLUDE_SINGLE_COMMENT, key, endIdx, afterKey);
			}
			if (onlyWsAfterKey) {
				return new DirectiveKind(BKind.INCLUDE_IF, key, -1, -1);
			}
			throw new IllegalStateException("Missing @end for @include-if " + key);
		}
		
		if (ft == TokenType.TOKEN_FILE_EXCLUDE_IF) {
		    int k = i + 1;
		    while (k < bodyToExclusive && isWhitespaceText(toks.get(k))) k++;
		    if (k >= bodyToExclusive || toks.get(k).getType() != TOKEN_KEY) {
		        throw new IllegalStateException("Missing key for @empty_if");
		    }
		    String key = toks.get(k).getToken();
		    int afterKey = k + 1;

		    // After the key there must be only whitespace in this single-line directive.
		    for (int m = afterKey; m < bodyToExclusive; m++) {
		        if (!isWhitespaceText(toks.get(m))) {
		            return DirectiveKind.plain(); // treat as plain if anything else appears
		        }
		    }
		    // Single-line directive inside this comment
		    return new DirectiveKind(BKind.FILE_EXCLUDE_IF, key, -1, afterKey);
		}
        if (ft == TokenType.TOKEN_AT_ELSE) {
            for (int k = i + 1; k < bodyToExclusive; k++) {
                if (!isWhitespaceText(toks.get(k))) {
                    return DirectiveKind.plain();
                }
            }
            return new DirectiveKind(BKind.ELSE, null, -1, -1);
        }
	    if (ft == TokenType.TOKEN_AT_LICENSE) {
	        int k = i + 1;
	        while (k < bodyToExclusive && isWhitespaceText(toks.get(k))) k++;
	        String key = null;
	        int afterKey = k;
	        
	        if (k < bodyToExclusive && toks.get(k).getType() == TOKEN_KEY) {
	            key = toks.get(k).getToken();
	            afterKey = k + 1;
	        }
	        
	        return new DirectiveKind(BKind.LICENSE, key, -1, afterKey);
	    }
	    
	    
		return DirectiveKind.plain();
	}

    private static MatchEnd findMatchingSpanEnd(List<Token> toks, int start) {
    	
        int depth = 1;
        int i = start;
        int beforeElse = -1;
        int afterElse  = -1;
        
        while (i < toks.size()) {
            Token t = toks.get(i);
            int tt = t.getType();
            if (tt == TOKEN_BLOCK_OPEN) {
            	
                int close = findMatching(toks, i, TOKEN_BLOCK_CLOSE);
                
                if (close < 0) {
                    throw new IllegalStateException("Unterminated block comment");
                }
                
                DirectiveKind kind = classifyBlockDirective(toks, i + 1, close);
                
                if (kind.kind == BKind.EXCLUDE_IF || kind.kind == BKind.INCLUDE_IF) {
                    depth++;
                } else if (kind.kind == BKind.END) {
                    depth--;
                    if (depth == 0) {
                        return new MatchEnd(/* beforeEnd= */i, /* afterEnd= */close + 1, beforeElse, afterElse);
                    }
                } else if (kind.kind == BKind.ELSE) {
                    if (depth == 1 && beforeElse < 0) {
                        beforeElse = i;
                        afterElse = close + 1;
                    }
                }
                
                i = close + 1;
                continue;
                
            }
            
            if (tt == TOKEN_JAVADOC_OPEN) {
                int close = findMatching(toks, i, TOKEN_BLOCK_CLOSE);
                if (close < 0) {
                    throw new IllegalStateException("Unterminated Javadoc");
                }
                i = close + 1;
                continue;
            }
            i++;
        }
        return null;
    }


	/* ===================== Javadoc (HTML directives) ===================== */

	/**
	 * Processes the body of a Javadoc comment, applying HTML-comment directives,
	 * and then omitting any lines that start with "* " and contain no other
	 * content.
	 */
    private static void processJavadocBody(List<Token> toks, int from, int toExclusive, Set<String> keys,
            LineOmitterJavadoc lo) {
        int i = from;
        while (i < toExclusive) {
            Token t = toks.get(i);
            int tt = t.getType();
            if (tt == TOKEN_TEXT) {
                lo.append(t.getToken());
                i++;
                continue;
            }
            if (tt == TOKEN_EOL) {
                lo.emitEol(t.getToken());
                i++;
                continue;
            }
            if (tt == TOKEN_HTML_OPEN) {
                int htmlClose = findMatching(toks, i, TOKEN_HTML_CLOSE);
                if (htmlClose < 0) {
                    throw new IllegalStateException("Unclosed HTML comment in Javadoc");
                }
                HtmlDirective d = classifyHtmlDirective(toks, i + 1, htmlClose);
                if (d.kind == HtmlKind.PLAIN) {
                    lo.append(tokensToString(toks, i, htmlClose + 1));
                    i = htmlClose + 1;
                    continue;
                }
                if (d.kind == HtmlKind.EXCLUDE_IF) {
                    HtmlMatchEnd me = findHtmlMatchingEnd(toks, htmlClose + 1);
                    if (me == null) {
                        throw new IllegalStateException("Unclosed @exclude-if " + d.key + " in Javadoc");
                    }
                    int p1From = htmlClose + 1;
                    int p1To   = (me.beforeElseHtmlOpen >= 0) ? me.beforeElseHtmlOpen : me.beforeEndHtmlOpen;
                    int p2From = (me.afterElseHtmlClose >= 0) ? me.afterElseHtmlClose : -1;
                    int p2To   = me.beforeEndHtmlOpen;

                    if (!keyMatches(keys, d.key)) {
                        processJavadocBody(toks, p1From, p1To, keys, lo);
                    } else if (p2From >= 0) {
                        processJavadocBody(toks, p2From, p2To, keys, lo);
                    }
                    i = me.afterEndHtmlClose;
                    continue;
                }
                if (d.kind == HtmlKind.INCLUDE_IF) {
                    HtmlMatchEnd me = findHtmlMatchingEnd(toks, htmlClose + 1);
                    if (me == null) {
                        throw new IllegalStateException("Missing @end for @include-if " + d.key + " in Javadoc");
                    }
                    int p1From = htmlClose + 1;
                    int p1To   = (me.beforeElseHtmlOpen >= 0) ? me.beforeElseHtmlOpen : me.beforeEndHtmlOpen;
                    int p2From = (me.afterElseHtmlClose >= 0) ? me.afterElseHtmlClose : -1;
                    int p2To   = me.beforeEndHtmlOpen;

                    if (keyMatches(keys, d.key)) {
                        processJavadocBody(toks, p1From, p1To, keys, lo);
                    } else if (p2From >= 0) {
                        processJavadocBody(toks, p2From, p2To, keys, lo);
                    }
                    i = me.afterEndHtmlClose;
                    continue;
                }
                // HtmlKind.ELSE alone inside Javadoc body is treated as plain
                lo.append(tokensToString(toks, i, htmlClose + 1));
                i = htmlClose + 1;
                continue;
            }
            lo.append(t.getToken());
            i++;
        }
    }

    private static HtmlDirective classifyHtmlDirective(List<Token> toks, int from, int toExclusive) {
        int i = from;
        while (i < toExclusive && isWhitespaceText(toks.get(i))) {
            i++;
        }
        if (i >= toExclusive) {
            return HtmlDirective.plain();
        }
        Token first = toks.get(i);
        int ft = first.getType();
        if (ft == TOKEN_AT_INCLUDE_IF || ft == TOKEN_AT_EXCLUDE_IF) {
            int k = i + 1;
            while (k < toExclusive && isWhitespaceText(toks.get(k))) {
                k++;
            }
            if (k >= toExclusive || toks.get(k).getType() != TOKEN_KEY) {
                return HtmlDirective.plain();
            }
            String key = toks.get(k).getToken();
            return new HtmlDirective(ft == TOKEN_AT_INCLUDE_IF ? HtmlKind.INCLUDE_IF : HtmlKind.EXCLUDE_IF, key);
        }
        if (ft == TOKEN_AT_ELSE) {
            return new HtmlDirective(HtmlKind.ELSE, null);
        }
        
        if (ft == TOKEN_AT_END) {
            return HtmlDirective.plain();
        }
        return HtmlDirective.plain();
    }

	private static HtmlMatchEnd findHtmlMatchingEnd(List<Token> toks, int start) {
        int depth = 1;
        int i = start;
        int beforeElse = -1, elseClose = -1, afterElse = -1;
        while (i < toks.size()) {
            Token t = toks.get(i);
            int tt = t.getType();
            if (tt == TOKEN_HTML_OPEN) {
                int htmlClose = findMatching(toks, i, TOKEN_HTML_CLOSE);
                if (htmlClose < 0) {
                    throw new IllegalStateException("Unclosed HTML comment in Javadoc");
                }
                HtmlDirective d = classifyHtmlDirective(toks, i + 1, htmlClose);
                if (d.kind == HtmlKind.INCLUDE_IF || d.kind == HtmlKind.EXCLUDE_IF) {
                    depth++;
                } else {
                    int j = i + 1;
                    while (j < htmlClose && isWhitespaceText(toks.get(j))) j++;
                    if (j < htmlClose && toks.get(j).getType() == TOKEN_AT_END) {
                        depth--;
                        if (depth == 0) {
                            return new HtmlMatchEnd(/* beforeEndHtmlOpen= */i,
                                                    /* endHtmlCloseIndex= */htmlClose,
                                                    /* afterEndHtmlClose= */htmlClose + 1,
                                                    beforeElse, elseClose, afterElse);
                        }
                    } else if (j < htmlClose && toks.get(j).getType() == TOKEN_AT_ELSE) {
                        if (depth == 1 && beforeElse < 0) {
                            beforeElse = i;
                            elseClose = htmlClose;
                            afterElse = htmlClose + 1;
                        }
                    }
                }
                i = htmlClose + 1;
                continue;
            }
            i++;
        }
        return null;
    }

	private static void appendTokensVerbatim(List<Token> toks, int fromInclusive, int toInclusive, LineOmitter out) {
		for (int i = fromInclusive; i <= toInclusive; i++) {
			Token token = toks.get(i);
			String tokenString = token.getToken();
			out.appendNonEol(tokenString);
		}
	}

	private static String tokensToString(List<Token> toks, int fromInclusive, int toExclusive) {
		StringBuilder b = new StringBuilder();
		for (int i = fromInclusive; i < toExclusive; i++) {
			b.append(toks.get(i).getToken());
		}
		return b.toString();
	}

	private static boolean isNewlineText(Token t) {
		if (t.getType() != TOKEN_TEXT) {
			return false;
		}
		String s = t.getToken();
		return s.trim().isEmpty() && s.indexOf('\n') >= 0; // whitespace containing at least one LF
	}

	/**
	 * A key expression can be a single key ("FOO") or a pipe-separated OR like
	 * "FOO|BAR|BAZ". Whitespace around parts is ignored; empty parts are ignored.
	 */
	private static boolean keyMatches(Set<String> keys, String keyExpr) {
		if (keyExpr == null) {
			return false;
		}
		
		final String expr = keyExpr.trim();
		
		if (expr.indexOf('|') < 0) {
			return keys.contains(expr);
		}
		
		int start = 0;
		int n = expr.length();
		
		for (int i = 0; i <= n; i++) {
			if (i == n || expr.charAt(i) == '|') {
				String part = expr.substring(start, i).trim();
				if (!part.isEmpty() && keys.contains(part)) {
					return true; // OR semantics
				}
				start = i + 1;
			}
		}
		return false;
	}
	
	/**
	 * Will match on supplied keys, or if DEFAULT is the value, will match on no keys
	 */
	private static boolean emptyIfMatches(Set<String> keys, String keyExpr) {
	    if (keyExpr == null) return false;
	    String expr = keyExpr.trim();
	    if (DEFAULT_RESERVED.equals(expr)) {
	        return keys == null || keys.isEmpty();
	    }
	    // otherwise fall back to normal OR semantics
	    return keyMatches(keys, expr);
	}

	private static String stripOneOuterSpace(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}
		
		int start = 0;
		int end = s.length();
		
		if (s.charAt(0) == ' ') {
			start = 1; // remove exactly one leading space
		}
		
		if (end - start > 0 && s.charAt(end - 1) == ' ') {
			end--; // remove exactly one trailing space
		}
		return s.substring(start, end);
	}


	  
	/** True if token is an explicit end-of-line emitted by the lexer. */
	private static boolean isEol(Token t) {
		return t.getType() == TOKEN_EOL;
	}
}
