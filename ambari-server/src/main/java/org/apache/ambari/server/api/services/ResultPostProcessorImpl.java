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

import org.apache.ambari.server.api.resources.RequestResourceDefinition;
import org.apache.ambari.server.api.resources.ResourceDefinition;
import org.apache.ambari.server.api.resources.ResourceInstance;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.api.util.TreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes returned results to add href's and other content.
 */
public class ResultPostProcessorImpl implements ResultPostProcessor {
  /**
   * the associated request
   */
  private Request m_request;

  /**
   * Map of resource post processors keyed by resource type.
   * These are used to act on specific resource types contained in the result.
   */
  Map<Resource.Type, List<ResourceDefinition.PostProcessor>>
      m_mapPostProcessors = new HashMap<Resource.Type, List<ResourceDefinition.PostProcessor>>();


  /**
   * Constructor.
   *
   * @param request the associated request
   */
  public ResultPostProcessorImpl(Request request) {
    m_request = request;

    registerResourceProcessors(m_request.getResource());
  }

  @Override
  public void process(Result result) {
    processNode(result.getResultTree(), m_request.getURI());
  }

  /**
   * Process a node of the result tree.  Recursively calls child nodes.
   *
   * @param node the node to process
   * @param href the current href
   */
  private void processNode(TreeNode<Resource> node, String href) {
    Resource r = node.getObject();
    if (r != null) {
      List<ResourceDefinition.PostProcessor> listProcessors =
          m_mapPostProcessors.get(r.getType());
      for (ResourceDefinition.PostProcessor processor : listProcessors) {
        processor.process(m_request, node, href);
      }
      href = node.getProperty("href");
      int i = href.indexOf('?');
      if (i != -1) {
        href = href.substring(0, i);
      }
    } else {
      String isItemsCollection = node.getProperty("isCollection");
      if (node.getName() == null && "true".equals(isItemsCollection)) {
        node.setName("items");
        node.setProperty("href", href);
      }
    }
    for (TreeNode<Resource> child : node.getChildren()) {
      processNode(child, href);
    }
  }

  /**
   * Registers the resource processors.
   * Recursively registers child resource processors.
   *
   * @param resource the root resource
   */
  private void registerResourceProcessors(ResourceInstance resource) {
    //todo: reconsider registration mechanism
    Resource.Type type = resource.getResourceDefinition().getType();
    List<ResourceDefinition.PostProcessor> listProcessors = m_mapPostProcessors.get(type);
    if (listProcessors == null) {
      listProcessors = new ArrayList<ResourceDefinition.PostProcessor>();
      m_mapPostProcessors.put(type, listProcessors);
    }
    listProcessors.addAll(resource.getResourceDefinition().getPostProcessors());

    for (ResourceInstance child : resource.getSubResources().values()) {
      // avoid cycle
      if (!m_mapPostProcessors.containsKey(child.getResourceDefinition().getType())) {
        registerResourceProcessors(child);
      }
    }

    // always add Request post processors since they may be returned but will not be a child
    m_mapPostProcessors.put(Resource.Type.Request, new RequestResourceDefinition().getPostProcessors());
  }

}
