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

import org.apache.ambari.server.api.handlers.RequestHandler;
import org.apache.ambari.server.api.predicate.PredicateCompiler;
import org.apache.ambari.server.api.resources.ResourceInstance;
import org.junit.Test;

import static org.junit.Assert.*;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

/**
 * GetRequest unit tests.
 */
public class GetRequestTest extends BaseRequestTest {

  @Test
  public void testRequestType() throws Exception {
    Request r = new GetRequest(null, new RequestBody(), null, null);
    assertSame(Request.Type.GET, r.getRequestType());
  }


  protected Request getTestRequest(HttpHeaders headers, RequestBody body, UriInfo uriInfo, final PredicateCompiler compiler,
                                   final RequestHandler handler, final ResultPostProcessor processor, ResourceInstance resource) {
    return new GetRequest(headers, body, uriInfo, resource) {
      @Override
      protected PredicateCompiler getPredicateCompiler() {
        return compiler;
      }

      @Override
      protected RequestHandler getRequestHandler() {
        return handler;
      }

      @Override
      public ResultPostProcessor getResultPostProcessor() {
        return processor;
      }
    };
  }
}
