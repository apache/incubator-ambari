/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.ambari.server.api.predicate;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 * QueryLexer unit tests
 */
public class QueryLexerTest {

  @Test
  public void testTokens_simple() throws InvalidQueryException {
    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "a"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "&"));
    listTokens.add(new Token(Token.TYPE.BRACKET_OPEN, "("));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "<="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "b"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "2"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "|"));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, ">"));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "c"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "3"));
    listTokens.add(new Token(Token.TYPE.BRACKET_CLOSE, ")"));

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("a=1&(b<=2|c>3)");

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testTokens_multipleBrackets() throws InvalidQueryException {
    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "<"));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "a"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "&"));
    listTokens.add(new Token(Token.TYPE.BRACKET_OPEN, "("));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "<="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "b"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "2"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "&"));
    listTokens.add(new Token(Token.TYPE.BRACKET_OPEN, "("));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, ">="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "c"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "3"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "|"));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "!="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "d"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "4"));
    listTokens.add(new Token(Token.TYPE.BRACKET_CLOSE, ")"));
    listTokens.add(new Token(Token.TYPE.BRACKET_CLOSE, ")"));

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("a<1&(b<=2&(c>=3|d!=4))");

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testUnaryNot() throws Exception {
    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("!foo<5");

    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.LOGICAL_UNARY_OPERATOR, "!"));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "<"));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "5"));
    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testInOperator() throws Exception {
    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("foo.in(one, two, 3)");

    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR_FUNC, ".in("));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "one, two, 3"));
    listTokens.add(new Token(Token.TYPE.BRACKET_CLOSE, ")"));

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testIsEmptyOperator() throws Exception {
    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("category1.isEmpty()");

    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR_FUNC, ".isEmpty("));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "category1"));
    listTokens.add(new Token(Token.TYPE.BRACKET_CLOSE, ")"));

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testTokens_ignoreFieldsSyntax___noPredicate() throws InvalidQueryException {

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("fields=foo,bar");
    assertEquals(0, tokens.length);
  }

  @Test
  public void testTokens_ignoreFieldsSyntax___fieldsFirst() throws InvalidQueryException {

    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("fields=foo,bar&foo=1");

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testTokens_ignoreFieldsSyntax___fieldsLast() throws InvalidQueryException {

    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("foo=1&fields=foo,bar");

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testTokens_ignoreUnderscoreSyntax___noPredicate() throws InvalidQueryException {

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("_=1");
    assertEquals(0, tokens.length);
  }

  @Test
  public void testTokens_ignoreUnderscoreSyntax___fieldsFirst() throws InvalidQueryException {

    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("_=111111&foo=1");

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testTokens_ignoreUnderscoreSyntax___fieldsLast() throws InvalidQueryException {

    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("foo=1&_=11111");

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testTokens_ignore__multipleIgnoreFields() throws InvalidQueryException {
    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("fields=a/b&foo=1&_=5555555");

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testTokens_ignore__multipleConsecutiveIgnoreFields() throws InvalidQueryException {
    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("foo=1&fields=a/b&_=5555555");

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testTokens_ignore__multipleConsecutiveIgnoreFields2() throws InvalidQueryException {
    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("fields=a/b&_=5555555&foo=1");

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testTokens_ignore__fieldsMiddle() throws InvalidQueryException {
    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "&"));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "bar"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "2"));

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("foo=1&fields=a/b&bar=2");

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testTokens_ignore__fieldsMiddle2() throws InvalidQueryException {
    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "&"));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "bar"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "2"));

    QueryLexer lexer = new QueryLexer();
    Token[] tokens = lexer.tokens("foo=1&fields=a/b,c&_=123&bar=2");

    assertArrayEquals(listTokens.toArray(new Token[listTokens.size()]), tokens);
  }

  @Test
  public void testTokens_invalidRelationalOp() {
    try {
      new QueryLexer().tokens("foo=1&bar|5");
      fail("Expected InvalidQueryException due to invalid relational op");
    } catch (InvalidQueryException e) {
      //expected
    }
  }

  @Test
  public void testTokens_invalidLogicalOp() {
    try {
      new QueryLexer().tokens("foo=1<5=2");
      fail("Expected InvalidQueryException due to invalid logical op");
    } catch (InvalidQueryException e) {
      //expected
    }
  }

  @Test
  public void testTokens_invalidLogicalOp2() {
    try {
      new QueryLexer().tokens("foo=1&&5=2");
      fail("Expected InvalidQueryException due to invalid logical op");
    } catch (InvalidQueryException e) {
      //expected
    }
  }
}
