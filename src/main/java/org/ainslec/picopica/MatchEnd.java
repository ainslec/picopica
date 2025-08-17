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
 * Represents the token index boundaries around a matched directive span.
 * beforeEnd/afterEnd always point to the &at;end pair that closes the span.
 * If an &at; else exists at the same nesting depth, beforeElse/afterElse are set; otherwise -1.
 */
final class MatchEnd {
	
    final int beforeEnd;
    final int afterEnd;
    final int beforeElse;
    final int afterElse;

    MatchEnd(int beforeEnd, int afterEnd) {
        this(beforeEnd, afterEnd, -1, -1);
    }
    
    MatchEnd(int beforeEnd, int afterEnd, int beforeElse, int afterElse) {
        this.beforeEnd = beforeEnd;
        this.afterEnd = afterEnd;
        this.beforeElse = beforeElse;
        this.afterElse = afterElse;
    }
    
}