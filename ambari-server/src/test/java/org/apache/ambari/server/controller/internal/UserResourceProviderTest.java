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

package org.apache.ambari.server.controller.internal;

import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.RequestStatusResponse;
import org.apache.ambari.server.controller.UserRequest;
import org.apache.ambari.server.controller.UserResponse;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceProvider;
import org.apache.ambari.server.controller.utilities.PredicateBuilder;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * UserResourceProvider tests.
 */
public class UserResourceProviderTest {
  @Test
  public void testCreateResources() throws Exception {
    Resource.Type type = Resource.Type.User;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);
    RequestStatusResponse response = createNiceMock(RequestStatusResponse.class);

    managementController.createUsers(AbstractResourceProviderTest.Matcher.getUserRequestSet("User100"));

    // replay
    replay(managementController, response);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    // add the property map to a set for the request.  add more maps for multiple creates
    Set<Map<String, Object>> propertySet = new LinkedHashSet<Map<String, Object>>();

    Map<String, Object> properties = new LinkedHashMap<String, Object>();

    // add properties to the request map
    properties.put(UserResourceProvider.USER_USERNAME_PROPERTY_ID, "User100");

    propertySet.add(properties);

    // create the request
    Request request = PropertyHelper.getCreateRequest(propertySet, null);

    provider.createResources(request);

    // verify
    verify(managementController, response);
  }

  @Test
  public void testGetResources() throws Exception {
    Resource.Type type = Resource.Type.User;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);

    Set<UserResponse> allResponse = new HashSet<UserResponse>();
    allResponse.add(new UserResponse("User100", false));

    // set expectations
    expect(managementController.getUsers(AbstractResourceProviderTest.Matcher.getUserRequestSet("User100"))).
        andReturn(allResponse).once();

    // replay
    replay(managementController);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    Set<String> propertyIds = new HashSet<String>();

    propertyIds.add(UserResourceProvider.USER_USERNAME_PROPERTY_ID);
    propertyIds.add(UserResourceProvider.USER_PASSWORD_PROPERTY_ID);

    Predicate predicate = new PredicateBuilder().property(UserResourceProvider.USER_USERNAME_PROPERTY_ID).
        equals("User100").toPredicate();
    Request request = PropertyHelper.getReadRequest(propertyIds);
    Set<Resource> resources = provider.getResources(request, predicate);

    Assert.assertEquals(1, resources.size());
    for (Resource resource : resources) {
      String userName = (String) resource.getPropertyValue(UserResourceProvider.USER_USERNAME_PROPERTY_ID);
      Assert.assertEquals("User100", userName);
    }

    // verify
    verify(managementController);
  }

  @Test
  public void testUpdateResources() throws Exception {
    Resource.Type type = Resource.Type.User;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);
    RequestStatusResponse response = createNiceMock(RequestStatusResponse.class);

    // set expectations
    managementController.updateUsers(EasyMock.<Set<UserRequest>>anyObject());

    // replay
    replay(managementController, response);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    // add the property map to a set for the request.
    Map<String, Object> properties = new LinkedHashMap<String, Object>();

    properties.put(UserResourceProvider.USER_PASSWORD_PROPERTY_ID, "password");

    // create the request
    Request request = PropertyHelper.getUpdateRequest(properties, null);

    Predicate  predicate = new PredicateBuilder().property(UserResourceProvider.USER_USERNAME_PROPERTY_ID).
        equals("User100").toPredicate();
    provider.updateResources(request, predicate);

    // verify
    verify(managementController, response);
  }

  @Test
  public void testDeleteResources() throws Exception {
    Resource.Type type = Resource.Type.User;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);
    RequestStatusResponse response = createNiceMock(RequestStatusResponse.class);

    // set expectations
    managementController.deleteUsers(AbstractResourceProviderTest.Matcher.getUserRequestSet("User100"));

    // replay
    replay(managementController, response);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    Predicate predicate = new PredicateBuilder().property(UserResourceProvider.USER_USERNAME_PROPERTY_ID).
        equals("User100").toPredicate();
    provider.deleteResources(predicate);

    // verify
    verify(managementController, response);
  }
}
