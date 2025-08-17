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
 * Represents the location of a matching HTML directive terminator
 * {@code <!-- @end -->}.
 */
final class HtmlMatchEnd {
    final int beforeEndHtmlOpen;
    final int endHtmlCloseIndex;
    final int afterEndHtmlClose;

    final int beforeElseHtmlOpen;    // -1 if none
    final int elseHtmlCloseIndex;    // -1 if none
    final int afterElseHtmlClose;    // -1 if none

    HtmlMatchEnd(int beforeOpen, int endClose, int afterClose) {
        this(beforeOpen, endClose, afterClose, -1, -1, -1);
    }
    HtmlMatchEnd(int beforeOpen, int endClose, int afterClose,
                 int beforeElseHtmlOpen, int elseHtmlCloseIndex, int afterElseHtmlClose) {
        this.beforeEndHtmlOpen = beforeOpen;
        this.endHtmlCloseIndex = endClose;
        this.afterEndHtmlClose = afterClose;
        this.beforeElseHtmlOpen = beforeElseHtmlOpen;
        this.elseHtmlCloseIndex = elseHtmlCloseIndex;
        this.afterElseHtmlClose = afterElseHtmlClose;
    }
}