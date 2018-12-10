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

import org.apache.ambari.server.api.resources.ResourceInstance;
import org.apache.ambari.server.api.services.parsers.RequestBodyParser;
import org.apache.ambari.server.api.services.serializers.ResultSerializer;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


import static org.junit.Assert.assertEquals;

/**
* Unit tests for StacksService.
*/
public class StacksServiceTest extends BaseServiceTest {

  @Override
  public List<ServiceTestInvocation> getTestInvocations() throws Exception {
    List<ServiceTestInvocation> listInvocations = new ArrayList<ServiceTestInvocation>();

    //getStack
    StacksService service = new TestStacksService("stackName", null);
    Method m = service.getClass().getMethod("getStack", HttpHeaders.class, UriInfo.class, String.class);
    Object[] args = new Object[] {getHttpHeaders(), getUriInfo(), "stackName"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    //getStacks
    service = new TestStacksService(null, null);
    m = service.getClass().getMethod("getStacks", HttpHeaders.class, UriInfo.class);
    args = new Object[] {getHttpHeaders(), getUriInfo()};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    //getStackVersion
    service = new TestStacksService("stackName", "stackVersion");
    m = service.getClass().getMethod("getStackVersion", HttpHeaders.class, UriInfo.class, String.class, String.class);
    args = new Object[] {getHttpHeaders(), getUriInfo(), "stackName", "stackVersion"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    //getStackVersions
    service = new TestStacksService("stackName", null);
    m = service.getClass().getMethod("getStackVersions", HttpHeaders.class, UriInfo.class, String.class);
    args = new Object[] {getHttpHeaders(), getUriInfo(), "stackName"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    //todo: other methods

    return listInvocations;
  }

  private class TestStacksService extends StacksService {

    private String m_stackId;
    private String m_stackVersion;

    private TestStacksService(String stackName, String stackVersion) {
      m_stackId = stackName;
      m_stackVersion = stackVersion;
    }

    @Override
    ResourceInstance createStackResource(String stackName) {
      assertEquals(m_stackId, stackName);
      return getTestResource();
    }

    @Override
    ResourceInstance createStackVersionResource(String stackName, String stackVersion) {
      assertEquals(m_stackId, stackName);
      assertEquals(m_stackVersion, stackVersion);
      return getTestResource();
    }

    @Override
    ResourceInstance createRepositoryResource(String stackName,
        String stackVersion, String osType, String repoId) {
      return getTestResource();
    }

    @Override
    ResourceInstance createStackServiceResource(String stackName,
        String stackVersion, String serviceName) {
      return getTestResource();
    }

    ResourceInstance createStackConfigurationResource(String stackName,
        String stackVersion, String serviceName, String propertyName) {
      return getTestResource();
    }

    ResourceInstance createStackServiceComponentResource(String stackName,
        String stackVersion, String serviceName, String componentName) {
      return getTestResource();
    }

    ResourceInstance createOperatingSystemResource(String stackName,
        String stackVersion, String osType) {
      return getTestResource();
    }


    @Override
    RequestFactory getRequestFactory() {
      return getTestRequestFactory();
    }

    @Override
    protected RequestBodyParser getBodyParser() {
      return getTestBodyParser();
    }

    @Override
    protected ResultSerializer getResultSerializer() {
      return getTestResultSerializer();
    }
  }

}
