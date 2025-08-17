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

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Cursor for traversing source text while tracking line/column position and lexer state.
 * Provides character-level access, lookahead, and lexical state stack management.
 */
public final class LexerCursor {
   private final String sourceCode;
   private final String resourceUrl;

   private LexerState lexicalState;
   private final Deque<LexerState> lexStateStack = new ArrayDeque<>();

   private int positionBaseZero = 0;
   private int lineNumberBase1 = 1;
   private int columnNumberBase1 = 1;

   /**
    * Creates a new lexer cursor.
    *
    * @param sourceCode   the text to traverse
    * @param lexicalState the initial lexical state
    * @param resourceUrl  optional URL or identifier for the source
    */
   public LexerCursor(String sourceCode, LexerState lexicalState, String resourceUrl) {
      this.sourceCode = sourceCode;
      this.lexicalState = lexicalState;
      this.resourceUrl = resourceUrl;
   }

   /** @return the current 0-based character index in the source */
   public int getPositionBaseZero() { return positionBaseZero; }

   /** @return the current 1-based line number */
   public int getLineNumberBase1() { return lineNumberBase1; }

   /**
    * Sets the current 1-based line number.
    * @param v the new line number
    */
   public void setLineNumberBase1(int v) { lineNumberBase1 = v; }

   /** @return the current 1-based column number */
   public int getColumnNumberBase1() { return columnNumberBase1; }

   /**
    * Sets the current 1-based column number.
    * @param v the new column number
    */
   public void setColumnNumberBase1(int v) { columnNumberBase1 = v; }

   /** @return the complete source code string */
   public String getSourceCode() { return sourceCode; }

   /** @return the current lexical state */
   public LexerState getLexicalState() { return lexicalState; }

   /** @return the total number of characters in the source */
   public int getSourceCodeCharLength() { return sourceCode.length(); }

   /**
    * Replaces the current lexical state without pushing to the stack.
    * @param newState the new lexical state
    */
   public void replaceState(LexerState newState) { lexicalState = newState; }

   /**
    * Pushes the current state on the stack and switches to a new one.
    * @param newState the new lexical state
    */
   public void pushState(LexerState newState) { lexStateStack.push(lexicalState); lexicalState = newState; }

   /**
    * Pops the last pushed state and makes it current.
    * @return the new current lexical state
    */
   public LexerState popState() {
      if (!lexStateStack.isEmpty()) lexicalState = lexStateStack.pop();
      return lexicalState;
   }

   /**
    * Checks whether a given state exists anywhere in the stack.
    * @param state the state to look for
    * @return true if present, false otherwise
    */
   public boolean isAlreadyWithinAParentState(LexerState state) {
      for (LexerState s : lexStateStack) if (s == state) return true;
      return false;
   }

   /**
    * Checks whether the top element in the stack matches the given state.
    * @param state the state to check
    * @return true if the top matches, false otherwise
    */
   public boolean isParentState(LexerState state) {
      return !lexStateStack.isEmpty() && lexStateStack.peekLast() == state;
   }

   /**
    * Pops states until the given state is reached or the stack is empty.
    * @param to the target state to pop to
    * @return the current lexical state after popping
    */
   public LexerState popState(LexerState to) {
      while (!lexStateStack.isEmpty() && (this.lexicalState = popState()) != to) { /* loop */ }
      return lexicalState;
   }

   /** @return the next character without consuming, or -1 if EOF */
   public int peekChar() { return peekChar(0); }

   /**
    * Peeks ahead without consuming characters.
    * @param offset number of characters ahead to peek (0 = current)
    * @return the character code, or -1 if EOF
    */
   public int peekChar(int offset) {
      int next = positionBaseZero + offset;
      return next >= sourceCode.length() ? -1 : sourceCode.charAt(next);
   }

   /**
    * Consumes and returns the next character, updating line/column numbers.
    * @return the consumed character code, or -1 if EOF
    */
   public int consumeChar() {
      if (positionBaseZero >= sourceCode.length()) return -1;
      char ch = sourceCode.charAt(positionBaseZero++);
      if (ch == '\n') { lineNumberBase1++; columnNumberBase1 = 1; }
      else { columnNumberBase1++; }
      return ch;
   }

   /**
    * Consumes a fixed number of characters or until EOF.
    * @param numChars the number of characters to consume
    * @return the last character code read, or -1 if none
    */
   public int consumeChars(int numChars) {
      int ret = -1;
      for (int i = 0; i < numChars; i++) {
         int c = consumeChar();
         if (c == -1) return ret;
         ret = c;
      }
      return ret;
   }

   /**
    * Consumes one character, appends it to a buffer, and then peeks ahead.
    * @param sharedBuffer the buffer to append to
    * @return the next character code after append, or -1 if EOF
    */
   public int consumeAppendPeek(StringBuilder sharedBuffer) {
      int c = consumeChar();
      if (c == -1) return -1;
      sharedBuffer.append((char) c);
      return peekChar();
   }

   /**
    * Checks whether the upcoming characters match the given string.
    * @param s the string to compare against
    * @return true if the next characters match exactly
    */
   public boolean isNext(String s) {
      final int n = s.length();
      for (int i = 0; i < n; i++) {
         int c = peekChar(i);
         if (c == -1 || c != s.charAt(i)) return false;
      }
      return true;
   }

   /** @return the resource URL or identifier for this source */
   public String getResourceUrl() { return resourceUrl; }

   /**
    * @return a textual dump of the state stack and current state
    */
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      for (LexerState s : lexStateStack) sb.append(s).append('\n');
      sb.append(lexicalState).append('\n');
      return sb.toString();
   }
}
