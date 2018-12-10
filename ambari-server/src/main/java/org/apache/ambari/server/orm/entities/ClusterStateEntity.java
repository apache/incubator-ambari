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

import javax.persistence.*;

import static org.apache.commons.lang.StringUtils.defaultString;

@javax.persistence.Table(name = "clusterstate")
@Entity
public class ClusterStateEntity {

  @Id
  @Column(name = "cluster_id", nullable = false, insertable = false, updatable = false, length = 10)
  private Long clusterId;

  @Basic
  @Column(name = "current_cluster_state", insertable = true, updatable = true)
  private String currentClusterState = "";

  @Basic
  @Column(name = "current_stack_version", insertable = true, updatable = true)
  private String currentStackVersion = "";

  @OneToOne
  @JoinColumn(name = "cluster_id", referencedColumnName = "cluster_id", nullable = false)
  private ClusterEntity clusterEntity;

  public Long getClusterId() {
    return clusterId;
  }

  public void setClusterId(Long clusterId) {
    this.clusterId = clusterId;
  }

  public String getCurrentClusterState() {
    return defaultString(currentClusterState);
  }

  public void setCurrentClusterState(String currentClusterState) {
    this.currentClusterState = currentClusterState;
  }

  public String getCurrentStackVersion() {
    return defaultString(currentStackVersion);
  }

  public void setCurrentStackVersion(String currentStackVersion) {
    this.currentStackVersion = currentStackVersion;
  }

    @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ClusterStateEntity that = (ClusterStateEntity) o;

    if (clusterId != null ? !clusterId.equals(that.clusterId) : that.clusterId != null) return false;
    if (currentClusterState != null
        ? !currentClusterState.equals(that.currentClusterState) : that.currentClusterState != null) return false;
    if (currentStackVersion != null
        ? !currentStackVersion.equals(that.currentStackVersion) : that.currentStackVersion != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = clusterId !=null ? clusterId.intValue() : 0;
    result = 31 * result + (currentClusterState != null ? currentClusterState.hashCode() : 0);
    return result;
  }

  public ClusterEntity getClusterEntity() {
    return clusterEntity;
  }

  public void setClusterEntity(ClusterEntity clusterEntity) {
    this.clusterEntity = clusterEntity;
  }
}
