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

package org.apache.ambari.server.orm.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.ambari.server.orm.entities.ComponentConfigMappingEntity;

@Singleton
public class ComponentConfigMappingDAO {

  @Inject
  Provider<EntityManager> entityManagerProvider;
  @Inject
  DaoUtils daoUtils;

  @Transactional
  public List<ComponentConfigMappingEntity> findByType(
      Collection<String> configTypes) {
    TypedQuery<ComponentConfigMappingEntity> query =
        entityManagerProvider.get().createQuery(
            "SELECT config FROM ComponentConfigMappingEntity config"
            + " WHERE config.configType IN ?1",
        ComponentConfigMappingEntity.class);
    return daoUtils.selectList(query, configTypes);
  }

  @Transactional
  public List<ComponentConfigMappingEntity> findByComponentAndType(long clusterId, String serviceName, String componentName,
                                                                   Collection<String> configTypes) {
    if (configTypes.isEmpty()) {
      return Collections.emptyList();
    }
    TypedQuery<ComponentConfigMappingEntity> query = entityManagerProvider.get().createQuery("SELECT config " +
        "FROM ComponentConfigMappingEntity config " +
        "WHERE " +
        "config.clusterId = ?1 " +
        "AND config.serviceName = ?2 " +
        "AND config.componentName = ?3 " +
        "AND config.configType IN ?4",
        ComponentConfigMappingEntity.class);
    return daoUtils.selectList(query, clusterId, serviceName, componentName, configTypes);
  }

  @Transactional
  public void refresh(
      ComponentConfigMappingEntity componentConfigMappingEntity) {
    entityManagerProvider.get().refresh(componentConfigMappingEntity);
  }

  @Transactional
  public ComponentConfigMappingEntity merge(
      ComponentConfigMappingEntity componentConfigMappingEntity) {
    return entityManagerProvider.get().merge(
        componentConfigMappingEntity);
  }

  @Transactional
  public void remove(
      ComponentConfigMappingEntity componentConfigMappingEntity) {
    entityManagerProvider.get().remove(merge(componentConfigMappingEntity));
  }

  @Transactional
  public void removeByType(Collection<String> configTypes) {
    for (ComponentConfigMappingEntity entity : findByType(configTypes)) {
      remove(entity);
    }
  }

}
