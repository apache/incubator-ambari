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

import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.AmbariServer;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceProvider;
import org.apache.ambari.server.controller.utilities.PropertyHelper;

/**
 * The default provider module implementation.
 */
public class DefaultProviderModule extends AbstractProviderModule {
  @Inject
  private AmbariManagementController managementController;

  // ----- Constructors ------------------------------------------------------

  /**
   * Create a default provider module.
   */
  public DefaultProviderModule() {
    if (managementController == null) {
      managementController = AmbariServer.getController();
    }
  }


  // ----- utility methods ---------------------------------------------------

  @Override
  protected ResourceProvider createResourceProvider(Resource.Type type) {
    Set<String>               propertyIds    = PropertyHelper.getPropertyIds(type);
    Map<Resource.Type,String> keyPropertyIds = PropertyHelper.getKeyPropertyIds(type);

    switch (type) {
      case Workflow:
        return new WorkflowResourceProvider(propertyIds, keyPropertyIds);
      case Job:
        return new JobResourceProvider(propertyIds, keyPropertyIds);
      case TaskAttempt:
        return new TaskAttemptResourceProvider(propertyIds, keyPropertyIds);
      default:
        return AbstractControllerResourceProvider.getResourceProvider(type, propertyIds,
            keyPropertyIds, managementController);
    }
  }
}
