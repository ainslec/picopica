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
 * Encapsulates the parsed classification of a block comment directive.
 * <p>
 * Stores the directive kind, the associated key (if any),
 * and token indices for directive body processing.
 * </p>
 */
final class DirectiveKind {
	final BKind kind;
	final String key;
	final int includeEndIndex;
	final int afterKeyIndex;

	DirectiveKind(BKind k, String key, int includeEndIndex, int afterKeyIndex) {
		this.kind = k;
		this.key = key;
		this.includeEndIndex = includeEndIndex;
		this.afterKeyIndex = afterKeyIndex;
	}

	static DirectiveKind plain() {
		return new DirectiveKind(BKind.PLAIN, null, -1, -1);
	}
}