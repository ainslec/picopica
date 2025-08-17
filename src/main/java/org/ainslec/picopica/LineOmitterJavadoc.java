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
 * Small helper that buffers one logical line of Javadoc body and decides
 * whether to emit it (or drop it if it's just a leading "* " and whitespace).
 */
final class LineOmitterJavadoc {
	
	private final LineOmitter out;
	private final StringBuilder line = new StringBuilder();

	LineOmitterJavadoc(LineOmitter out) {
		this.out = out;
	}

	void append(String s) {
		if (s != null && !s.isEmpty()) {
			line.append(s);
		}
	}

	void emitEol(String eolLexeme) {
		if (!isStarOnlyLine(line)) {
			out.appendNonEol(line);
			out.appendEol(eolLexeme);
		}
		line.setLength(0);
	}

	void flushAtEnd() {
		if (line.length() == 0)
			return;
		if (!isStarOnlyLine(line)) {
			out.appendNonEol(line);
		}
		line.setLength(0);
	}

	private static boolean isStarOnlyLine(CharSequence cs) {
		int n = cs.length();
		int i = 0;
		// skip spaces/tabs
		while (i < n) {
			char c = cs.charAt(i);
			if (c == ' ' || c == '\t') {
				i++;
			} else {
				break;
			}
		}
		
		if (i + 1 >= n) {
			return false; // too short to be "* "
		}
		
		if (cs.charAt(i) != '*') {
			return false; // not starting with *
		}
		
		if (cs.charAt(i + 1) != ' ') {
			return false; // must be "* " exactly
		}
		
		int j = i + 2;
		
		// the rest must be only spaces/tabs
		while (j < n) {
			char c = cs.charAt(j);
			if (c != ' ' && c != '\t') {
				return false;
			}
			j++;
		}
		
		return true; // matches: optional ws + "* " + only ws
	}
}