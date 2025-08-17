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
 * We use this to eliminate emitted lines that are blank because they uniquely contained directives
 */
public class LineOmitter {
	StringBuilder outputBuffer;
	StringBuilder lineBuffer = new StringBuilder();
	boolean lineHasContent = false;
	boolean isDirectiveLine = false;
	public LineOmitter(StringBuilder sb) {
		LineOmitter.this.outputBuffer = sb;
	}
	public void setDirectiveLine(boolean directiveLine) {
		this.isDirectiveLine = directiveLine;
	}
	public void appendEol(CharSequence cs) {
		// Emit the line unless it is a directive-only line (no content)
		if (!isDirectiveLine || lineHasContent) {
		    outputBuffer.append(lineBuffer);
		    outputBuffer.append(cs);
		}
		lineBuffer.setLength(0);
		isDirectiveLine = false;
		lineHasContent = false;
	}
	public void flush() {
		// At EOF (no trailing EOL), emit the buffered line unless it is a directive-only line.
		if (!(isDirectiveLine && !lineHasContent)) {
			outputBuffer.append(lineBuffer);
		}
		lineBuffer.setLength(0);
		isDirectiveLine = false;
		lineHasContent = false;
	}
	public void appendNonEol(CharSequence cs) {
		int len = cs.length();
		if (len > 0) {
			for (int i=0; i < len; i++) {
				char c = cs.charAt(i);
				if (c != ' ' && c != '\t' ) {
					lineHasContent = true;
					break;
				}
			}
			lineBuffer.append(cs);
		}
	}
	public void clearLineBufferIfJustWhitespace() {
		if (!lineHasContent) {
			lineBuffer.setLength(0);
			isDirectiveLine = false;
			lineHasContent = false;
			
		}
	}
}