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
package org.apache.ambari.server.orm.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @author ncole
 *
 */
@SuppressWarnings("serial")
public class ComponentConfigMappingEntityPK implements Serializable {
  private Long clusterId;
  private String serviceName;
  private String componentName;
  private String configType;

  @Id
  @Column(name = "cluster_id", insertable = true, updatable = true, nullable = false, length = 10)
  public Long getClusterId() {
    return clusterId;
  }

  public void setClusterId(Long clusterId) {
    this.clusterId = clusterId;
  }

  @Id
  @Column(name = "service_name", insertable = true, updatable = true, nullable = false)
  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  @Column(name = "component_name", insertable = true, updatable = true, nullable = false)
  @Id
  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String name) {
    componentName = name;
  }

  @Column(name = "config_type", insertable = true, updatable = false, nullable = false)
  @Id
  public String getConfigType() {
    return configType;
  }

  public void setConfigType(String type) {
    configType = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ComponentConfigMappingEntityPK that = (ComponentConfigMappingEntityPK) o;

    if (clusterId != null ? !clusterId.equals(that.clusterId) : that.clusterId != null) return false;
    if (componentName != null ? !componentName.equals(that.componentName) : that.componentName != null) return false;
    if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = clusterId != null ? clusterId.intValue() : 0;
    result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
    result = 31 * result + (componentName != null ? componentName.hashCode() : 0);
    return result;
  }





}
