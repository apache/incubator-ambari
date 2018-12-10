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
import org.apache.ambari.server.controller.ConfigurationRequest;
import org.apache.ambari.server.controller.ConfigurationResponse;
import org.apache.ambari.server.controller.RequestStatusResponse;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceProvider;
import org.apache.ambari.server.controller.utilities.PredicateBuilder;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.easymock.Capture;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.*;

/**
 * Tests for the configuration resource provider.
 */
public class ConfigurationResourceProviderTest {
  @Test
  public void testCreateResources() throws Exception {

    AmbariManagementController managementController = createMock(AmbariManagementController.class);
    RequestStatusResponse response = createNiceMock(RequestStatusResponse.class);

    managementController.createConfiguration(AbstractResourceProviderTest.Matcher.getConfigurationRequest(
        "Cluster100", "type", "tag", new HashMap<String, String>()));

    // replay
    replay(managementController, response);

    ConfigurationResourceProvider provider = new ConfigurationResourceProvider(
        PropertyHelper.getPropertyIds(Resource.Type.Configuration ),
        PropertyHelper.getKeyPropertyIds(Resource.Type.Configuration),
        managementController);

    Set<Map<String, Object>> propertySet = new LinkedHashSet<Map<String, Object>>();

    Map<String, Object> properties = new LinkedHashMap<String, Object>();

    properties.put(ConfigurationResourceProvider.CONFIGURATION_CLUSTER_NAME_PROPERTY_ID, "Cluster100");
    properties.put(ConfigurationResourceProvider.CONFIGURATION_CONFIG_TAG_PROPERTY_ID, "tag");
    properties.put(ConfigurationResourceProvider.CONFIGURATION_CONFIG_TYPE_PROPERTY_ID, "type");

    propertySet.add(properties);

    // create the request
    Request request = PropertyHelper.getCreateRequest(propertySet, null);

    provider.createResources(request);

    // verify
    verify(managementController, response);
  }

  @Test
  public void testGetResources() throws Exception {
    Resource.Type type = Resource.Type.Configuration;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);

    Set<ConfigurationResponse> allResponse = new HashSet<ConfigurationResponse>();
    allResponse.add(new ConfigurationResponse("Cluster100", "type", "tag1", null));
    allResponse.add(new ConfigurationResponse("Cluster100", "type", "tag2", null));
    allResponse.add(new ConfigurationResponse("Cluster100", "type", "tag3", null));

    Set<ConfigurationResponse> orResponse = new HashSet<ConfigurationResponse>();
    orResponse.add(new ConfigurationResponse("Cluster100", "type", "tag1", null));
    orResponse.add(new ConfigurationResponse("Cluster100", "type", "tag2", null));

    Capture<Set<ConfigurationRequest>> configRequestCapture1 = new Capture<Set<ConfigurationRequest>>();
    Capture<Set<ConfigurationRequest>> configRequestCapture2 = new Capture<Set<ConfigurationRequest>>();

    // set expectations
    //equals predicate
    expect(managementController.getConfigurations(
        capture(configRequestCapture1))).andReturn(allResponse).once();

    // OR predicate
    expect(managementController.getConfigurations(
        capture(configRequestCapture2))).andReturn(orResponse).once();

    // replay
    replay(managementController);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    Set<String> propertyIds = new HashSet<String>();

    propertyIds.add(ConfigurationResourceProvider.CONFIGURATION_CLUSTER_NAME_PROPERTY_ID);
    propertyIds.add(ConfigurationResourceProvider.CONFIGURATION_CONFIG_TAG_PROPERTY_ID);

    // equals predicate
    Predicate predicate = new PredicateBuilder().property(
        ConfigurationResourceProvider.CONFIGURATION_CLUSTER_NAME_PROPERTY_ID).equals("Cluster100").toPredicate();
    Request request = PropertyHelper.getReadRequest(propertyIds);
    Set<Resource> resources = provider.getResources(request, predicate);

    Set<ConfigurationRequest> setRequest = configRequestCapture1.getValue();
    assertEquals(1, setRequest.size());
    ConfigurationRequest configRequest = setRequest.iterator().next();
    assertEquals("Cluster100", configRequest.getClusterName());
    assertNull(configRequest.getType());
    assertNull(configRequest.getVersionTag());

    Assert.assertEquals(3, resources.size());
    boolean containsResource1 = false;
    boolean containsResource2 = false;
    boolean containsResource3 = false;

    for (Resource resource : resources) {
      String clusterName = (String) resource.getPropertyValue(
          ConfigurationResourceProvider.CONFIGURATION_CLUSTER_NAME_PROPERTY_ID);
      Assert.assertEquals("Cluster100", clusterName);
      String tag = (String) resource.getPropertyValue(
          ConfigurationResourceProvider.CONFIGURATION_CONFIG_TAG_PROPERTY_ID);

      if (tag.equals("tag1")) {
        containsResource1 = true;
      } else if (tag.equals("tag2")) {
        containsResource2 = true;
      } else if (tag.equals("tag3")) {
        containsResource3 = true;
      }
    }
    assertTrue(containsResource1);
    assertTrue(containsResource2);
    assertTrue(containsResource3);

    // OR predicate
    predicate = new PredicateBuilder().property(
        ConfigurationResourceProvider.CONFIGURATION_CONFIG_TAG_PROPERTY_ID).equals("tag1").or().
        property(ConfigurationResourceProvider.CONFIGURATION_CONFIG_TAG_PROPERTY_ID).equals("tag2").toPredicate();

    request = PropertyHelper.getReadRequest(propertyIds);
    resources = provider.getResources(request, predicate);

    setRequest = configRequestCapture2.getValue();
    assertEquals(2, setRequest.size());
    boolean containsTag1 = false;
    boolean containsTag2 = false;
    for (ConfigurationRequest cr : setRequest) {
      assertNull(cr.getClusterName());
      if (cr.getVersionTag().equals("tag1")) {
        containsTag1 = true;
      } else if (cr.getVersionTag().equals("tag2")) {
        containsTag2 = true;
      }
    }
    assertTrue(containsTag1);
    assertTrue(containsTag2);

    Assert.assertEquals(2, resources.size());
    containsResource1 = false;
    containsResource2 = false;

    for (Resource resource : resources) {
      String clusterName = (String) resource.getPropertyValue(
          ConfigurationResourceProvider.CONFIGURATION_CLUSTER_NAME_PROPERTY_ID);
      Assert.assertEquals("Cluster100", clusterName);
      String tag = (String) resource.getPropertyValue(
          ConfigurationResourceProvider.CONFIGURATION_CONFIG_TAG_PROPERTY_ID);

      if (tag.equals("tag1")) {
        containsResource1 = true;
      } else if (tag.equals("tag2")) {
        containsResource2 = true;
      }
    }
    assertTrue(containsResource1);
    assertTrue(containsResource2);

    // verify
    verify(managementController);
  }

  @Test
  public void testUpdateResources() throws Exception {
    Resource.Type type = Resource.Type.Configuration;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);
    RequestStatusResponse response = createNiceMock(RequestStatusResponse.class);

    // replay
    replay(managementController, response);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    // add the property map to a set for the request.
    Map<String, Object> properties = new LinkedHashMap<String, Object>();

    // create the request
    Request request = PropertyHelper.getUpdateRequest(properties, null);

    Predicate predicate = new PredicateBuilder().property(
        ConfigurationResourceProvider.CONFIGURATION_CONFIG_TAG_PROPERTY_ID).equals("Configuration100").toPredicate();

    try {
      provider.updateResources(request, predicate);
      Assert.fail("Expected an UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }

    // verify
    verify(managementController, response);
  }

  @Test
  public void testDeleteResources() throws Exception {
    Resource.Type type = Resource.Type.Configuration;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);

    // replay
    replay(managementController);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    Predicate predicate = new PredicateBuilder().property(
        ConfigurationResourceProvider.CONFIGURATION_CONFIG_TAG_PROPERTY_ID).equals("Configuration100").toPredicate();
    try {
      provider.deleteResources(predicate);
      Assert.fail("Expected an UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // expected
    }

    // verify
    verify(managementController);
  }
}
