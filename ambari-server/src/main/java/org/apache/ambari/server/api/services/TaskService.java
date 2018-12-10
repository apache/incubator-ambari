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
import org.apache.ambari.server.controller.spi.Resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for task resource requests.
 */
public class TaskService extends BaseService {
  /**
   * Parent cluster id.
   */
  private String m_clusterName;

  /**
   * Parent request id.
   */
  private String m_requestId;

  /**
   * Constructor.
   *
   * @param clusterName  cluster id
   * @param requestId    request id
   */
  public TaskService(String clusterName, String requestId) {
    m_clusterName = clusterName;
    m_requestId = requestId;
  }

  /**
   * Handles GET: /clusters/{clusterID}/requests/{requestID}/tasks/{taskID}
   * Get a specific task.
   *
   * @param headers  http headers
   * @param ui       uri info
   * @param taskId   component id
   *
   * @return a task resource representation
   */
  @GET
  @Path("{taskId}")
  @Produces("text/plain")
  public Response getTask(@Context HttpHeaders headers, @Context UriInfo ui,
                          @PathParam("taskId") String taskId) {

    return handleRequest(headers, null, ui, Request.Type.GET,
        createTaskResource(m_clusterName, m_requestId, taskId));
  }

  /**
   * Handles GET: /clusters/{clusterID}/requests/{requestID}/tasks
   * Get all tasks for a request.
   *
   * @param headers http headers
   * @param ui      uri info
   *
   * @return task collection resource representation
   */
  @GET
  @Produces("text/plain")
  public Response getComponents(@Context HttpHeaders headers, @Context UriInfo ui) {
    return handleRequest(headers, null, ui, Request.Type.GET,
        createTaskResource(m_clusterName, m_requestId, null));
  }

  /**
   * Create a task resource instance.
   *
   * @param clusterName  cluster name
   * @param requestId    request id
   * @param taskId       task id
   *
   * @return a task resource instance
   */
  ResourceInstance createTaskResource(String clusterName, String requestId, String taskId) {
    Map<Resource.Type,String> mapIds = new HashMap<Resource.Type, String>();
    mapIds.put(Resource.Type.Cluster, clusterName);
    mapIds.put(Resource.Type.Request, requestId);
    mapIds.put(Resource.Type.Task, taskId);

    return createResource(Resource.Type.Task, mapIds);
  }
}
