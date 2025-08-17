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

/** Classification of a single HTML comment found inside Javadoc. */
final class HtmlDirective {
	
	final HtmlKind kind;
	final String key;

	HtmlDirective(HtmlKind k, String key) {
		this.kind = k;
		this.key = key;
	}

	static HtmlDirective plain() {
		return new HtmlDirective(HtmlKind.PLAIN, null);
	}
}