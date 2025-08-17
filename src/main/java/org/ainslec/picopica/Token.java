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
 * Represents a single token produced by the {@link Lexer}.
 * Stores the token's text, type, lexical state, position, and optional extra context.
 */
public class Token {
	
   private int startLineBase1;
   private int startColumnBase1;
   private int endLineBase1;
   private int endColumnBase1Inclusive;
   private int length;
   private int type;
   private LexerState lexicalState;
   private String token;
   private Object additionalContext;

   /**
    * Creates a new token.
    *
    * @param startLineBase1        starting line number (1-based)
    * @param startColumnBase1      starting column number (1-based)
    * @param endLineBase1Exclusive ending line number (exclusive, 1-based)
    * @param endColumnBase1Exclusive ending column number (exclusive, 1-based)
    * @param token                 token text
    * @param type                  token type (see {@link TokenType})
    * @param lexicalState          lexer state when this token was created
    */
   public Token(int startLineBase1, int startColumnBase1, int endLineBase1Exclusive,
                int endColumnBase1Exclusive, String token, int type, LexerState lexicalState) {
      this.startLineBase1 = startLineBase1;
      this.startColumnBase1 = startColumnBase1;
      this.endLineBase1 = endLineBase1Exclusive;
      this.endColumnBase1Inclusive = endColumnBase1Exclusive - 1;
      this.token = token;
      this.length = token == null ? 0 : token.length();
      this.type = type;
      this.lexicalState = lexicalState;
   }

   /** @param endLineBase1 sets the ending line number (1-based) */
   public void setEndLineBase1(int endLineBase1) {
      this.endLineBase1 = endLineBase1;
   }

   /** @param endColumnBase1Exclusive sets the ending column number (exclusive, 1-based) */
   public void setEndColumnBase1Exclusive(int endColumnBase1Exclusive) {
      this.endColumnBase1Inclusive = endColumnBase1Exclusive - 1;
   }

   /** @return the token text */
   public String getToken() {
      return token;
   }

   /** @return the token length in characters */
   public int getLength() {
      return length;
   }

   /** @return the token type constant (see {@link TokenType}) */
   public int getType() {
      return type;
   }

   /** @return the starting line number (1-based) */
   public int getStartLineBase1() {
      return startLineBase1;
   }

   /** @return the starting column number (1-based) */
   public int getStartColumnBase1() {
      return startColumnBase1;
   }

   /** @return the ending column number (inclusive, 1-based) */
   public int getEndColumnBase1Inclusive() {
      return endColumnBase1Inclusive;
   }

   /** @return the ending line number (1-based) */
   public int getEndLineBase1() {
      return endLineBase1;
   }

   /** @return the lexical state of the token */
   public LexerState getLexicalState() {
      return lexicalState;
   }

   /** @param token sets the token text */
   public void setToken(String token) {
      this.token = token;
   }

   /** @param type sets the token type constant */
   public void setType(int type) {
      this.type = type;
   }

   /** @param additionalContext sets additional context for the token */
   public void setAdditionalContext(Object additionalContext) {
      this.additionalContext = additionalContext;
   }

   /** @return any additional context object for this token */
   public Object getAdditionalContext() {
      return additionalContext;
   }

   /**
    * Returns a string representation of the token, including type name and text.
    *
    * @param lexTokens array mapping type constants to type names
    * @return formatted token string
    */
   public String toString(String[] lexTokens) {
      StringBuilder builder = new StringBuilder();
      builder.append("Token [");
      builder.append(lexicalState).append(", ");
      builder.append(startLineBase1).append(",").append(startColumnBase1);
      builder.append(" --> ").append(endLineBase1).append(",").append(endColumnBase1Inclusive);
      builder.append(", type=");
      builder.append(lexTokens != null && type >= 0 && type < lexTokens.length
              ? lexTokens[type]
              : String.valueOf(type));
      if (token != null && type != TokenType.TOKEN_EOF) {
         builder.append(", token=\"").append(token.replace("\"", "\\\"")).append("\"");
      }
      builder.append("]");
      return builder.toString();
   }

   /**
    * Returns a string representation using {@link TokenType#LEX_TOKENS} for the type name.
    * @return formatted token string
    */
   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Token [");
      builder.append(lexicalState).append(", ");
      builder.append(startLineBase1).append(",").append(startColumnBase1);
      builder.append(" --> ").append(endLineBase1).append(", ").append(endColumnBase1Inclusive);
      builder.append(", type=");
      builder.append(type >= 0 && type < TokenType.LEX_TOKENS.length
              ? TokenType.LEX_TOKENS[type]
              : type);
      if (token != null && type != TokenType.TOKEN_EOF) {
         builder.append(", token=\"").append(token.replace("\"", "\\\"")).append("\"");
      }
      builder.append("]");
      return builder.toString();
   }
}
