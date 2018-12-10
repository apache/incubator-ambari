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

import org.apache.ambari.server.controller.predicate.*;
import org.apache.ambari.server.controller.spi.Predicate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * QueryParser unit tests.
 */
public class QueryParserTest {

  @Test
  public void testParse_simple() throws Exception {
    List<Token> listTokens = new ArrayList<Token>();
    //a=b
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "a"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "b"));

    QueryParser parser = new QueryParser();
    Predicate p = parser.parse(listTokens.toArray(new Token[listTokens.size()]));

    assertEquals(new EqualsPredicate<String>("a", "b"), p);
  }

  @Test
  public void testParse() throws InvalidQueryException {
    List<Token> listTokens = new ArrayList<Token>();
    // foo=bar&(a<1&(b<=2|c>3)&d>=100)|e!=5&!(f=6|g=7)
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "bar"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "&"));
    listTokens.add(new Token(Token.TYPE.BRACKET_OPEN, "("));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "<"));
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
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "&"));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, ">="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "d"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "100"));
    listTokens.add(new Token(Token.TYPE.BRACKET_CLOSE, ")"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "|"));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "!="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "e"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "5"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "&"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_UNARY_OPERATOR, "!"));
    listTokens.add(new Token(Token.TYPE.BRACKET_OPEN, "("));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "f"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "6"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "|"));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "g"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "7"));
    listTokens.add(new Token(Token.TYPE.BRACKET_CLOSE, ")"));

    QueryParser parser = new QueryParser();
    Predicate p = parser.parse(listTokens.toArray(new Token[listTokens.size()]));

    EqualsPredicate<String> fooPred = new EqualsPredicate<String>("foo", "bar");
    LessPredicate<String> aPred = new LessPredicate<String>("a", "1");
    LessEqualsPredicate<String> bPred = new LessEqualsPredicate<String>("b", "2");
    GreaterEqualsPredicate<String> cPred = new GreaterEqualsPredicate<String>("c", "3");
    GreaterEqualsPredicate<String> dPred = new GreaterEqualsPredicate<String>("d", "100");
    NotPredicate ePred = new NotPredicate(new EqualsPredicate<String>("e", "5"));
    EqualsPredicate fPred = new EqualsPredicate<String>("f", "6");
    EqualsPredicate gPRed = new EqualsPredicate<String>("g", "7");
    OrPredicate bORcPred = new OrPredicate(bPred, cPred);
    AndPredicate aANDbORcPred = new AndPredicate(aPred, bORcPred);
    AndPredicate aANDbORcANDdPred = new AndPredicate(aANDbORcPred, dPred);
    AndPredicate fooANDaANDbORcANDdPred = new AndPredicate(fooPred, aANDbORcANDdPred);
    OrPredicate fORgPred = new OrPredicate(fPred, gPRed);
    NotPredicate NOTfORgPred = new NotPredicate(fORgPred);
    AndPredicate eANDNOTfORgPred = new AndPredicate(ePred, NOTfORgPred);
    OrPredicate rootPredicate = new OrPredicate(fooANDaANDbORcANDdPred, eANDNOTfORgPred);

    assertEquals(rootPredicate, p);
  }

  @Test
  public void testParse_NotOp__simple() throws Exception {
    List<Token> listTokens = new ArrayList<Token>();
    //!a=b
    listTokens.add(new Token(Token.TYPE.LOGICAL_UNARY_OPERATOR, "!"));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "a"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "b"));

    QueryParser parser = new QueryParser();
    Predicate p = parser.parse(listTokens.toArray(new Token[listTokens.size()]));

    assertEquals(new NotPredicate(new EqualsPredicate<String>("a", "b")), p);
  }

  @Test
  public void testParse_NotOp() throws Exception {
    List<Token> listTokens = new ArrayList<Token>();
     //a=1&!b=2
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "a"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_OPERATOR, "&"));
    listTokens.add(new Token(Token.TYPE.LOGICAL_UNARY_OPERATOR, "!"));
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "b"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "2"));

    QueryParser parser = new QueryParser();
    Predicate p = parser.parse(listTokens.toArray(new Token[listTokens.size()]));

    EqualsPredicate aPred = new EqualsPredicate<String>("a", "1");
    EqualsPredicate bPred = new EqualsPredicate<String>("b", "2");
    NotPredicate notPred = new NotPredicate(bPred);
    AndPredicate andPred = new AndPredicate(aPred, notPred);

    assertEquals(andPred, p);
  }

  @Test
  public void testParse_InOp__simple() throws Exception {
    List<Token> listTokens = new ArrayList<Token>();
    // foo.in(one,two,3)
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR_FUNC, ".in("));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "one,two,3"));
    listTokens.add(new Token(Token.TYPE.BRACKET_CLOSE, ")"));

    QueryParser parser = new QueryParser();
    Predicate p = parser.parse(listTokens.toArray(new Token[listTokens.size()]));

    EqualsPredicate ep1 = new EqualsPredicate("foo", "one");
    EqualsPredicate ep2 = new EqualsPredicate("foo", "two");
    EqualsPredicate ep3 = new EqualsPredicate("foo", "3");

    OrPredicate orPredicate = new OrPredicate(ep1, ep2, ep3);

    assertEquals(orPredicate, p);
  }

  @Test
  public void testParse_InOp__exception() throws Exception {
    List<Token> listTokens = new ArrayList<Token>();
    // foo.in()
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR_FUNC, ".in("));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "foo"));
    listTokens.add(new Token(Token.TYPE.BRACKET_CLOSE, ")"));

    QueryParser parser = new QueryParser();
    try {
      parser.parse(listTokens.toArray(new Token[listTokens.size()]));
      fail("Expected InvalidQueryException due to missing right operand");
    } catch (InvalidQueryException e) {
      // expected
    }
  }

  @Test
  public void testParse_isEmptyOp__simple() throws Exception {
    List<Token> listTokens = new ArrayList<Token>();
    // category1.isEmpty()
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR_FUNC, ".isEmpty("));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "category1"));
    listTokens.add(new Token(Token.TYPE.BRACKET_CLOSE, ")"));

    QueryParser parser = new QueryParser();
    Predicate p = parser.parse(listTokens.toArray(new Token[listTokens.size()]));

    assertEquals(new CategoryIsEmptyPredicate("category1"), p);
  }

  @Test
  public void testParse_isEmptyOp__exception() throws Exception {
    List<Token> listTokens = new ArrayList<Token>();
    // category1.isEmpty()
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR_FUNC, ".isEmpty("));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "category1"));
    // missing closing bracket

    QueryParser parser = new QueryParser();
    try {
      parser.parse(listTokens.toArray(new Token[listTokens.size()]));
      fail("Expected InvalidQueryException due to missing closing bracket");
    } catch (InvalidQueryException e) {
      // expected
    }
  }

  @Test
  public void testParse_isEmptyOp__exception2() throws Exception {
    List<Token> listTokens = new ArrayList<Token>();
    // category1.isEmpty()
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR_FUNC, ".isEmpty("));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "category1"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "one,two,3"));
    listTokens.add(new Token(Token.TYPE.BRACKET_CLOSE, ")"));

    QueryParser parser = new QueryParser();
    try {
      parser.parse(listTokens.toArray(new Token[listTokens.size()]));
      fail("Expected InvalidQueryException due to existence of right operand");
    } catch (InvalidQueryException e) {
      // expected
    }
  }

  @Test
  public void testParse_noTokens() throws InvalidQueryException {
    assertNull(new QueryParser().parse(new Token[0]));
  }

  @Test
  public void testParse_mismatchedBrackets() {
    List<Token> listTokens = new ArrayList<Token>();
    // a=1&(b<=2|c>3
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

    try {
      new QueryParser().parse(listTokens.toArray(new Token[listTokens.size()]));
      fail("Expected InvalidQueryException due to missing closing bracket");
    } catch (InvalidQueryException e) {
      // expected
    }
  }

  @Test
  public void testParse_outOfOrderTokens() {
    List<Token> listTokens = new ArrayList<Token>();
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));
    listTokens.add(new Token(Token.TYPE.PROPERTY_OPERAND, "a"));
    listTokens.add(new Token(Token.TYPE.VALUE_OPERAND, "1"));
    // should be a logical operator
    listTokens.add(new Token(Token.TYPE.RELATIONAL_OPERATOR, "="));

    try {
      new QueryParser().parse(listTokens.toArray(new Token[listTokens.size()]));
      fail("Expected InvalidQueryException due to invalid last token");
    } catch (InvalidQueryException e) {
      // expected
    }
  }
}
