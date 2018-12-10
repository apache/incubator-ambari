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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@IdClass(ServiceConfigMappingEntityPK.class)
@Entity
@Table(name="serviceconfigmapping")
public class ServiceConfigMappingEntity {

  @Id
  @Column(name = "cluster_id", nullable = false, insertable = false, updatable = false)
  private Long clusterId;

  @Id
  @Column(name = "service_name", nullable = false, insertable = false, updatable = false)
  private String serviceName;

  @Id
  @Column(name = "config_type", nullable = false, insertable = true, updatable = false)
  private String configType;

  @Column(name = "config_tag", nullable = false, insertable = true, updatable = true)
  private String configVersion;

  @Column(name = "timestamp", nullable = false, insertable = true, updatable = true)
  private Long timestamp;

  @ManyToOne
  @JoinColumns({
      @JoinColumn(name = "cluster_id", referencedColumnName = "cluster_id", nullable = false),
      @JoinColumn(name = "service_name", referencedColumnName = "service_name", nullable = false) })
  private ClusterServiceEntity serviceEntity;

  @ManyToOne
  @JoinColumns({
      @JoinColumn(name = "cluster_id", referencedColumnName = "cluster_id", nullable = false, insertable = false, updatable = false),
      @JoinColumn(name = "config_type", referencedColumnName = "type_name", nullable = false, insertable = false, updatable = false),
      @JoinColumn(name = "config_tag", referencedColumnName = "version_tag", nullable = false, insertable = false, updatable = false)
  })
  private ClusterConfigEntity clusterConfigEntity;

  public Long getClusterId() {
    return clusterId;
  }

  public void setClusterId(Long id) {
    clusterId = id;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String name) {
    serviceName = name;
  }

  public String getConfigType() {
    return configType;
  }

  public void setConfigType(String type) {
    configType = type;
  }

  public String getVersionTag() {
    return configVersion;
  }

  public void setVersionTag(String tag) {
    configVersion = tag;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long stamp) {
    timestamp = stamp;
  }

  public ClusterServiceEntity getServiceEntity() {
    return serviceEntity;
  }

  public void setServiceEntity(ClusterServiceEntity entity) {
    serviceEntity = entity;
  }

  public ClusterConfigEntity getClusterConfigEntity() {
    return clusterConfigEntity;
  }

  public void setClusterConfigEntity(ClusterConfigEntity clusterConfigEntity) {
    this.clusterConfigEntity = clusterConfigEntity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ServiceConfigMappingEntity that = (ServiceConfigMappingEntity) o;

    if (clusterId != null ? !clusterId.equals(that.clusterId) : that.clusterId != null) return false;
    if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
    if (configType != null ? !configType.equals(that.configType) : that.configType != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = clusterId !=null ? clusterId.intValue() : 0;
    result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
    result = 31 * result + (configType != null ? configType.hashCode() : 0);
    return result;
  }




}
