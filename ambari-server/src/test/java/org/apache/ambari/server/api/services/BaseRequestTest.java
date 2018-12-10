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

package org.apache.ambari.server.api.services;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.ambari.server.api.handlers.RequestHandler;
import org.apache.ambari.server.api.predicate.InvalidQueryException;
import org.apache.ambari.server.api.predicate.PredicateCompiler;
import org.apache.ambari.server.api.resources.ResourceInstance;
import org.apache.ambari.server.controller.internal.TemporalInfoImpl;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.TemporalInfo;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Base tests for service requests.
 */
public abstract class BaseRequestTest {

  @Test
  public void testGetBody() {
    RequestBody body = createNiceMock(RequestBody.class);
    Request request = getTestRequest(null, body, null, null, null, null, null);

    assertSame(body, request.getBody());
  }

  @Test
  public void testGetResource() {
    ResourceInstance resource = createNiceMock(ResourceInstance.class);
    Request request = getTestRequest(null, null, null, null, null, null, resource);

    assertSame(resource, request.getResource());
  }

  @Test
  public void testGetApiVersion() {
    Request request = getTestRequest(null, null, null, null, null, null, null);
    assertEquals(1, request.getAPIVersion());
  }

  @Test
  public void testGetHttpHeaders() {
    HttpHeaders headers = createNiceMock(HttpHeaders.class);
    MultivaluedMap<String, String> mapHeaders = new MultivaluedMapImpl();
    Request request = getTestRequest(headers, null, null, null, null, null, null);

    expect(headers.getRequestHeaders()).andReturn(mapHeaders);
    replay(headers);

    assertSame(mapHeaders, request.getHttpHeaders());
    verify(headers);
  }

  @Test
  public void testProcess_noBody() throws Exception {
    String uriString = "http://localhost.com:8080/api/v1/clusters/c1";
    URI uri = new URI(URLEncoder.encode(uriString, "UTF-8"));
    PredicateCompiler compiler = createStrictMock(PredicateCompiler.class);
    UriInfo uriInfo = createMock(UriInfo.class);
    RequestHandler handler = createStrictMock(RequestHandler.class);
    Result result = createMock(Result.class);
    ResultStatus resultStatus = createMock(ResultStatus.class);
    ResultPostProcessor processor = createStrictMock(ResultPostProcessor.class);
    RequestBody body = createNiceMock(RequestBody.class);

    Request request = getTestRequest(null, body, uriInfo, compiler, handler, processor, null);

    //expectations
    expect(uriInfo.getRequestUri()).andReturn(uri).anyTimes();
    expect(handler.handleRequest(request)).andReturn(result);
    expect(result.getStatus()).andReturn(resultStatus).anyTimes();
    expect(resultStatus.isErrorState()).andReturn(false).anyTimes();
    processor.process(result);

    replay(compiler, uriInfo, handler, result, resultStatus, processor, body);

    Result processResult = request.process();

    verify(compiler, uriInfo, handler, result, resultStatus, processor, body);
    assertSame(result, processResult);
    assertNull(request.getQueryPredicate());
  }

  @Test
  public void testProcess_WithBody() throws Exception {
    String uriString = "http://localhost.com:8080/api/v1/clusters/c1";
    URI uri = new URI(URLEncoder.encode(uriString, "UTF-8"));
    PredicateCompiler compiler = createStrictMock(PredicateCompiler.class);
    UriInfo uriInfo = createMock(UriInfo.class);
    RequestHandler handler = createStrictMock(RequestHandler.class);
    Result result = createMock(Result.class);
    ResultStatus resultStatus = createMock(ResultStatus.class);
    ResultPostProcessor processor = createStrictMock(ResultPostProcessor.class);
    RequestBody body = createNiceMock(RequestBody.class);

    Request request = getTestRequest(null, body, uriInfo, compiler, handler, processor, null);

    //expectations
    expect(uriInfo.getRequestUri()).andReturn(uri).anyTimes();
    expect(handler.handleRequest(request)).andReturn(result);
    expect(result.getStatus()).andReturn(resultStatus).anyTimes();
    expect(resultStatus.isErrorState()).andReturn(false).anyTimes();
    processor.process(result);
    expect(body.getQueryString()).andReturn(null);

    replay(compiler, uriInfo, handler, result, resultStatus, processor, body);

    Result processResult = request.process();

    verify(compiler, uriInfo, handler, result, resultStatus, processor, body);
    assertSame(result, processResult);
    assertNull(request.getQueryPredicate());
  }


  @Test
  public void testProcess_QueryInURI() throws Exception {
    HttpHeaders headers = createNiceMock(HttpHeaders.class);
    String uriString = "http://localhost.com:8080/api/v1/clusters/c1?foo=foo-value&bar=bar-value";
    URI uri = new URI(URLEncoder.encode(uriString, "UTF-8"));
    PredicateCompiler compiler = createStrictMock(PredicateCompiler.class);
    Predicate predicate = createNiceMock(Predicate.class);
    UriInfo uriInfo = createMock(UriInfo.class);
    RequestHandler handler = createStrictMock(RequestHandler.class);
    Result result = createMock(Result.class);
    ResultStatus resultStatus = createMock(ResultStatus.class);
    ResultPostProcessor processor = createStrictMock(ResultPostProcessor.class);
    RequestBody body = createNiceMock(RequestBody.class);

    Request request = getTestRequest(headers, body, uriInfo, compiler, handler, processor, null);

    //expectations
    expect(uriInfo.getRequestUri()).andReturn(uri).anyTimes();
    expect(body.getQueryString()).andReturn(null);
    expect(compiler.compile("foo=foo-value&bar=bar-value")).andReturn(predicate);
    expect(handler.handleRequest(request)).andReturn(result);
    expect(result.getStatus()).andReturn(resultStatus).anyTimes();
    expect(resultStatus.isErrorState()).andReturn(false).anyTimes();
    processor.process(result);

    replay(headers, compiler, uriInfo, handler, result, resultStatus, processor, predicate, body);

    Result processResult = request.process();

    verify(headers, compiler, uriInfo, handler, result, resultStatus, processor, predicate, body);

    assertSame(processResult, result);
    assertSame(predicate, request.getQueryPredicate());
  }

  @Test
  public void testProcess_QueryInBody() throws Exception {
    HttpHeaders headers = createNiceMock(HttpHeaders.class);
    String uriString = "http://localhost.com:8080/api/v1/clusters/c1";
    URI uri = new URI(URLEncoder.encode(uriString, "UTF-8"));
    PredicateCompiler compiler = createStrictMock(PredicateCompiler.class);
    Predicate predicate = createNiceMock(Predicate.class);
    UriInfo uriInfo = createMock(UriInfo.class);
    RequestHandler handler = createStrictMock(RequestHandler.class);
    Result result = createMock(Result.class);
    ResultStatus resultStatus = createMock(ResultStatus.class);
    ResultPostProcessor processor = createStrictMock(ResultPostProcessor.class);
    RequestBody body = createNiceMock(RequestBody.class);

    Request request = getTestRequest(headers, body, uriInfo, compiler, handler, processor, null);

    //expectations
    expect(uriInfo.getRequestUri()).andReturn(uri).anyTimes();
    expect(body.getQueryString()).andReturn("foo=bar");
    expect(compiler.compile("foo=bar")).andReturn(predicate);
    expect(handler.handleRequest(request)).andReturn(result);
    expect(result.getStatus()).andReturn(resultStatus).anyTimes();
    expect(resultStatus.isErrorState()).andReturn(false).anyTimes();
    processor.process(result);

    replay(headers, compiler, uriInfo, handler, result, resultStatus, processor, predicate, body);

    Result processResult = request.process();

    verify(headers, compiler, uriInfo, handler, result, resultStatus, processor, predicate, body);

    assertSame(processResult, result);
    assertSame(predicate, request.getQueryPredicate());
  }

  @Test
  public void testProcess_QueryInBodyAndURI() throws Exception {
    HttpHeaders headers = createNiceMock(HttpHeaders.class);
    String uriString = "http://localhost.com:8080/api/v1/clusters/c1?bar=value";
    URI uri = new URI(URLEncoder.encode(uriString, "UTF-8"));
    PredicateCompiler compiler = createStrictMock(PredicateCompiler.class);
    Predicate predicate = createNiceMock(Predicate.class);
    UriInfo uriInfo = createMock(UriInfo.class);
    RequestHandler handler = createStrictMock(RequestHandler.class);
    Result result = createMock(Result.class);
    ResultStatus resultStatus = createMock(ResultStatus.class);
    ResultPostProcessor processor = createStrictMock(ResultPostProcessor.class);
    RequestBody body = createNiceMock(RequestBody.class);

    Request request = getTestRequest(headers, body, uriInfo, compiler, handler, processor, null);

    //expectations
    expect(uriInfo.getRequestUri()).andReturn(uri).anyTimes();
    expect(body.getQueryString()).andReturn("foo=bar");
    expect(compiler.compile("foo=bar")).andReturn(predicate);
    expect(handler.handleRequest(request)).andReturn(result);
    expect(result.getStatus()).andReturn(resultStatus).anyTimes();
    expect(resultStatus.isErrorState()).andReturn(false).anyTimes();
    processor.process(result);

    replay(headers, compiler, uriInfo, handler, result, resultStatus, processor, predicate, body);

    Result processResult = request.process();

    verify(headers, compiler, uriInfo, handler, result, resultStatus, processor, predicate, body);

    assertSame(processResult, result);
    assertSame(predicate, request.getQueryPredicate());
  }

  @Test
  public void testProcess_WithBody_InvalidQuery() throws Exception {
    UriInfo uriInfo = createMock(UriInfo.class);
    String uriString = "http://localhost.com:8080/api/v1/clusters/c1";
    URI uri = new URI(URLEncoder.encode(uriString, "UTF-8"));
    PredicateCompiler compiler = createStrictMock(PredicateCompiler.class);
    RequestBody body = createNiceMock(RequestBody.class);
    Exception exception = new InvalidQueryException("test");

    Request request = getTestRequest(null, body, uriInfo, compiler, null, null, null);

    //expectations
    expect(uriInfo.getRequestUri()).andReturn(uri).anyTimes();
    expect(body.getQueryString()).andReturn("blahblahblah");
    expect(compiler.compile("blahblahblah")).andThrow(exception);

    replay(compiler, uriInfo, body);

    Result processResult = request.process();

    verify(compiler, uriInfo, body);

    assertEquals(400, processResult.getStatus().getStatusCode());
    assertTrue(processResult.getStatus().isErrorState());
    assertEquals("Unable to compile query predicate: test", processResult.getStatus().getMessage());
  }

  @Test
  public void testProcess_noBody_ErrorStateResult() throws Exception {
    String uriString = "http://localhost.com:8080/api/v1/clusters/c1";
    URI uri = new URI(URLEncoder.encode(uriString, "UTF-8"));
    PredicateCompiler compiler = createStrictMock(PredicateCompiler.class);
    UriInfo uriInfo = createMock(UriInfo.class);
    RequestHandler handler = createStrictMock(RequestHandler.class);
    Result result = createMock(Result.class);
    ResultStatus resultStatus = createMock(ResultStatus.class);
    ResultPostProcessor processor = createStrictMock(ResultPostProcessor.class);
    RequestBody body = createNiceMock(RequestBody.class);

    Request request = getTestRequest(null, body, uriInfo, compiler, handler, processor, null);

    //expectations
    expect(uriInfo.getRequestUri()).andReturn(uri).anyTimes();
    expect(handler.handleRequest(request)).andReturn(result);
    expect(result.getStatus()).andReturn(resultStatus).anyTimes();
    expect(resultStatus.isErrorState()).andReturn(true).anyTimes();

    replay(compiler, uriInfo, handler, result, resultStatus, processor, body);

    Result processResult = request.process();

    verify(compiler, uriInfo, handler, result, resultStatus, processor, body);
    assertSame(result, processResult);
    assertNull(request.getQueryPredicate());
  }

  @Test
  public void testGetFields() throws Exception {
    String fields = "prop,category/prop1,category2/category3/prop2[1,2,3],prop3[4,5,6],category4[7,8,9],sub-resource/*[10,11,12],finalProp";
    UriInfo uriInfo = createMock(UriInfo.class);

    @SuppressWarnings("unchecked")
    MultivaluedMap<String, String> mapQueryParams = createMock(MultivaluedMap.class);

    expect(uriInfo.getQueryParameters()).andReturn(mapQueryParams);
    expect(mapQueryParams.getFirst("fields")).andReturn(fields);

    replay(uriInfo, mapQueryParams);

    Request request = getTestRequest(null, null, uriInfo, null, null, null, null);
    Map<String, TemporalInfo> mapFields = request.getFields();

    assertEquals(7, mapFields.size());

    String prop = "prop";
    assertTrue(mapFields.containsKey(prop));
    assertNull(mapFields.get(prop));

    String prop1 = PropertyHelper.getPropertyId("category", "prop1");
    assertTrue(mapFields.containsKey(prop1));
    assertNull(mapFields.get(prop1));

    String prop2 = PropertyHelper.getPropertyId("category2/category3", "prop2");
    assertTrue(mapFields.containsKey(prop2));
    assertEquals(new TemporalInfoImpl(1, 2, 3), mapFields.get(prop2));

    String prop3 = "prop3";
    assertTrue(mapFields.containsKey(prop3));
    assertEquals(new TemporalInfoImpl(4, 5, 6), mapFields.get(prop3));

    String category4 = "category4";
    assertTrue(mapFields.containsKey(category4));
    assertEquals(new TemporalInfoImpl(7, 8, 9), mapFields.get(category4));

    String subResource = PropertyHelper.getPropertyId("sub-resource", "*");
    assertTrue(mapFields.containsKey(subResource));
    assertEquals(new TemporalInfoImpl(10, 11, 12), mapFields.get(subResource));

    String finalProp = "finalProp";
    assertTrue(mapFields.containsKey(finalProp));
    assertNull(mapFields.get(finalProp));

    verify(uriInfo, mapQueryParams);
  }

   protected abstract Request getTestRequest(HttpHeaders headers, RequestBody body, UriInfo uriInfo, PredicateCompiler compiler,
                                             RequestHandler handler, ResultPostProcessor processor, ResourceInstance resource);

}
