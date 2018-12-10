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
import org.apache.ambari.server.controller.ServiceComponentHostRequest;
import org.apache.ambari.server.controller.ServiceComponentHostResponse;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceProvider;
import org.apache.ambari.server.controller.utilities.PredicateBuilder;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.apache.ambari.server.state.StackId;
import org.apache.ambari.server.state.State;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.ambari.server.Role;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * HostComponentResourceProvider tests.
 */
public class HostComponentResourceProviderTest {
  @Test
  public void testCreateResources() throws Exception {
    Resource.Type type = Resource.Type.HostComponent;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);
    RequestStatusResponse response = createNiceMock(RequestStatusResponse.class);

    managementController.createHostComponents(
        AbstractResourceProviderTest.Matcher.getHostComponentRequestSet(
            "Cluster100", "Service100", "Component100", "Host100", null, null));

    // replay
    replay(managementController, response);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    // add the property map to a set for the request.  add more maps for multiple creates
    Set<Map<String, Object>> propertySet = new LinkedHashSet<Map<String, Object>>();

    // Service 1: create a map of properties for the request
    Map<String, Object> properties = new LinkedHashMap<String, Object>();

    // add properties to the request map
    properties.put(HostComponentResourceProvider.HOST_COMPONENT_CLUSTER_NAME_PROPERTY_ID, "Cluster100");
    properties.put(HostComponentResourceProvider.HOST_COMPONENT_SERVICE_NAME_PROPERTY_ID, "Service100");
    properties.put(HostComponentResourceProvider.HOST_COMPONENT_COMPONENT_NAME_PROPERTY_ID, "Component100");
    properties.put(HostComponentResourceProvider.HOST_COMPONENT_HOST_NAME_PROPERTY_ID, "Host100");

    propertySet.add(properties);

    // create the request
    Request request = PropertyHelper.getCreateRequest(propertySet, null);

    provider.createResources(request);

    // verify
    verify(managementController, response);
  }

  @Test
  public void testGetResources() throws Exception {
    Resource.Type type = Resource.Type.HostComponent;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);

    Set<ServiceComponentHostResponse> allResponse = new HashSet<ServiceComponentHostResponse>();
    StackId stackId = new StackId("HDP-0.1");
    StackId stackId2 = new StackId("HDP-0.2");
    allResponse.add(new ServiceComponentHostResponse(
        "Cluster100", "Service100", "Component100", "Host100", null, null, State.INSTALLED.toString(),
        stackId.getStackId(), State.STARTED.toString(),
        stackId2.getStackId()));
    allResponse.add(new ServiceComponentHostResponse(
        "Cluster100", "Service100", "Component101", "Host100", null, null, State.INSTALLED.toString(),
        stackId.getStackId(), State.STARTED.toString(),
        stackId2.getStackId()));
    allResponse.add(new ServiceComponentHostResponse(
        "Cluster100", "Service100", "Component102", "Host100", null, null, State.INSTALLED.toString(),
        stackId.getStackId(), State.STARTED.toString(),
        stackId2.getStackId()));
    Map<String, String> expectedNameValues = new HashMap<String, String>();
    expectedNameValues.put(
        HostComponentResourceProvider.HOST_COMPONENT_CLUSTER_NAME_PROPERTY_ID, "Cluster100");
    expectedNameValues.put(
        HostComponentResourceProvider.HOST_COMPONENT_STATE_PROPERTY_ID, State.INSTALLED.toString());
    expectedNameValues.put(
        HostComponentResourceProvider.HOST_COMPONENT_STACK_ID_PROPERTY_ID, stackId.getStackId());
    expectedNameValues.put(
        HostComponentResourceProvider.HOST_COMPONENT_DESIRED_STATE_PROPERTY_ID, State.STARTED.toString());
    expectedNameValues.put(
        HostComponentResourceProvider.HOST_COMPONENT_DESIRED_STACK_ID_PROPERTY_ID, stackId2.getStackId());

    // set expectations
    expect(managementController.getHostComponents(
        AbstractResourceProviderTest.Matcher.getHostComponentRequestSet(
            "Cluster100", null, null, null, null, null))).andReturn(allResponse).once();

    // replay
    replay(managementController);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    Set<String> propertyIds = new HashSet<String>();

    propertyIds.add(HostComponentResourceProvider.HOST_COMPONENT_CLUSTER_NAME_PROPERTY_ID);
    propertyIds.add(HostComponentResourceProvider.HOST_COMPONENT_COMPONENT_NAME_PROPERTY_ID);
    propertyIds.add(HostComponentResourceProvider.HOST_COMPONENT_STATE_PROPERTY_ID);
    propertyIds.add(HostComponentResourceProvider.HOST_COMPONENT_STACK_ID_PROPERTY_ID);
    propertyIds.add(HostComponentResourceProvider.HOST_COMPONENT_DESIRED_STATE_PROPERTY_ID);
    propertyIds.add(HostComponentResourceProvider.HOST_COMPONENT_DESIRED_STACK_ID_PROPERTY_ID);

    Predicate predicate = new PredicateBuilder().property(
        HostComponentResourceProvider.HOST_COMPONENT_CLUSTER_NAME_PROPERTY_ID).equals("Cluster100").toPredicate();
    Request request = PropertyHelper.getReadRequest(propertyIds);
    Set<Resource> resources = provider.getResources(request, predicate);

    Assert.assertEquals(3, resources.size());
    Set<String> names = new HashSet<String>();
    for (Resource resource : resources) {
      for (String key : expectedNameValues.keySet()) {
        Assert.assertEquals(expectedNameValues.get(key), resource.getPropertyValue(key));
      }
      names.add((String) resource.getPropertyValue(
          HostComponentResourceProvider.HOST_COMPONENT_COMPONENT_NAME_PROPERTY_ID));
    }
    // Make sure that all of the response objects got moved into resources
    for (ServiceComponentHostResponse response : allResponse) {
      Assert.assertTrue(names.contains(response.getComponentName()));
    }

    // verify
    verify(managementController);
  }


  @Test
  public void testGetResources_return_ha_status_property() throws Exception {
    Resource.Type type = Resource.Type.HostComponent;
    AmbariManagementController managementController = createMock(AmbariManagementController.class);
    Set<ServiceComponentHostResponse> allResponse = new HashSet<ServiceComponentHostResponse>();
     for (Role role : Role.values()) {
        allResponse.add(new ServiceComponentHostResponse(
        "Cluster100", "Service100", role.toString(), "Host100", null, null, "", "", "", ""));
     }
    


    // set expectations
    expect(managementController.getHostComponents(
        AbstractResourceProviderTest.Matcher.getHostComponentRequestSet(
            "Cluster100", null, null, null, null, null))).andReturn(allResponse).once();

    // replay
    replay(managementController);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    Set<String> propertyIds = new HashSet<String>();

    propertyIds.add(HostComponentResourceProvider.HOST_COMPONENT_CLUSTER_NAME_PROPERTY_ID);
    propertyIds.add(HostComponentResourceProvider.HOST_COMPONENT_COMPONENT_NAME_PROPERTY_ID);

    Predicate predicate = new PredicateBuilder().property(
        HostComponentResourceProvider.HOST_COMPONENT_CLUSTER_NAME_PROPERTY_ID).equals("Cluster100").toPredicate();
    Request request = PropertyHelper.getReadRequest(propertyIds);
    Set<Resource> resources = provider.getResources(request, predicate);
    
    for (Resource resource : resources) {
       Object ha_status = resource.getPropertyValue(
          HostComponentResourceProvider.HOST_COMPONENT_HIGH_AVAILABILITY_PROPERTY_ID);
       //ha_status must have only HBASE_MASTER component
       if(ha_status != null) 
       {
          Assert.assertEquals(Role.HBASE_MASTER.toString(), resource.getPropertyValue(
          HostComponentResourceProvider.HOST_COMPONENT_COMPONENT_NAME_PROPERTY_ID).toString());
       }else{
          Assert.assertNotSame(Role.HBASE_MASTER.toString(), resource.getPropertyValue(
          HostComponentResourceProvider.HOST_COMPONENT_COMPONENT_NAME_PROPERTY_ID).toString());
       }
    }
    
    verify(managementController);
  }

  @Test
  public void testUpdateResources() throws Exception {
    Resource.Type type = Resource.Type.HostComponent;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);
    RequestStatusResponse response = createNiceMock(RequestStatusResponse.class);

    Map<String, String> mapRequestProps = new HashMap<String, String>();
    mapRequestProps.put("context", "Called from a test");

    Set<ServiceComponentHostResponse> nameResponse = new HashSet<ServiceComponentHostResponse>();
    nameResponse.add(new ServiceComponentHostResponse(
        "Cluster102", "Service100", "Component100", "Host100", null, null, "STARTED", "", "", ""));

    // set expectations
    expect(managementController.getHostComponents(
        EasyMock.<Set<ServiceComponentHostRequest>>anyObject())).andReturn(nameResponse).once();
    expect(managementController.updateHostComponents(
        AbstractResourceProviderTest.Matcher.getHostComponentRequestSet(
            "Cluster102", null, "Component100", "Host100", null, "STARTED"),
            eq(mapRequestProps), eq(false))).andReturn(response).once();

    // replay
    replay(managementController, response);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    Map<String, Object> properties = new LinkedHashMap<String, Object>();

    properties.put(HostComponentResourceProvider.HOST_COMPONENT_STATE_PROPERTY_ID, "STARTED");

    // create the request
    Request request = PropertyHelper.getUpdateRequest(properties, mapRequestProps);

    // update the cluster named Cluster102
    Predicate predicate = new PredicateBuilder().property(
        HostComponentResourceProvider.HOST_COMPONENT_CLUSTER_NAME_PROPERTY_ID).equals("Cluster102").toPredicate();
    provider.updateResources(request, predicate);

    // verify
    verify(managementController, response);
  }

  @Test
  public void testDeleteResources() throws Exception {
    Resource.Type type = Resource.Type.HostComponent;

    AmbariManagementController managementController = createMock(AmbariManagementController.class);
    RequestStatusResponse response = createNiceMock(RequestStatusResponse.class);

    // set expectations
    expect(managementController.deleteHostComponents(
        AbstractResourceProviderTest.Matcher.getHostComponentRequestSet(
            null, null, "Component100", "Host100", null, null))).andReturn(response);

    // replay
    replay(managementController, response);

    ResourceProvider provider = AbstractControllerResourceProvider.getResourceProvider(
        type,
        PropertyHelper.getPropertyIds(type),
        PropertyHelper.getKeyPropertyIds(type),
        managementController);

    AbstractResourceProviderTest.TestObserver observer = new AbstractResourceProviderTest.TestObserver();

    ((ObservableResourceProvider)provider).addObserver(observer);

    Predicate predicate = new PredicateBuilder().
        property(HostComponentResourceProvider.HOST_COMPONENT_COMPONENT_NAME_PROPERTY_ID).equals("Component100").and().
        property(HostComponentResourceProvider.HOST_COMPONENT_HOST_NAME_PROPERTY_ID).equals("Host100").toPredicate();
    provider.deleteResources(predicate);


    ResourceProviderEvent lastEvent = observer.getLastEvent();
    Assert.assertNotNull(lastEvent);
    Assert.assertEquals(Resource.Type.HostComponent, lastEvent.getResourceType());
    Assert.assertEquals(ResourceProviderEvent.Type.Delete, lastEvent.getType());
    Assert.assertEquals(predicate, lastEvent.getPredicate());
    Assert.assertNull(lastEvent.getRequest());

    // verify
    verify(managementController, response);
  }

  @Test
  public void testCheckPropertyIds() throws Exception {
    Set<String> propertyIds = new HashSet<String>();
    propertyIds.add("foo");
    propertyIds.add("cat1/foo");
    propertyIds.add("cat2/bar");
    propertyIds.add("cat2/baz");
    propertyIds.add("cat3/sub1/bam");
    propertyIds.add("cat4/sub2/sub3/bat");
    propertyIds.add("cat5/subcat5/map");

    Map<Resource.Type, String> keyPropertyIds = new HashMap<Resource.Type, String>();

    AmbariManagementController managementController = createMock(AmbariManagementController.class);

    AbstractResourceProvider provider =
        (AbstractResourceProvider) AbstractControllerResourceProvider.getResourceProvider(
            Resource.Type.HostComponent,
            propertyIds,
            keyPropertyIds,
            managementController);

    Set<String> unsupported = provider.checkPropertyIds(Collections.singleton("foo"));
    Assert.assertTrue(unsupported.isEmpty());

    // note that key is not in the set of known property ids.  We allow it if its parent is a known property.
    // this allows for Map type properties where we want to treat the entries as individual properties
    Assert.assertTrue(provider.checkPropertyIds(Collections.singleton("cat5/subcat5/map/key")).isEmpty());

    unsupported = provider.checkPropertyIds(Collections.singleton("bar"));
    Assert.assertEquals(1, unsupported.size());
    Assert.assertTrue(unsupported.contains("bar"));

    unsupported = provider.checkPropertyIds(Collections.singleton("cat1/foo"));
    Assert.assertTrue(unsupported.isEmpty());

    unsupported = provider.checkPropertyIds(Collections.singleton("cat1"));
    Assert.assertTrue(unsupported.isEmpty());

    unsupported = provider.checkPropertyIds(Collections.singleton("config"));
    Assert.assertTrue(unsupported.isEmpty());

    unsupported = provider.checkPropertyIds(Collections.singleton("config/unknown_property"));
    Assert.assertTrue(unsupported.isEmpty());
  }
}
