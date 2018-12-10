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

package org.apache.ambari.server.state;

import java.util.List;
import java.util.Map;

/**
 * Represents a single instance of a 'Config Type'
 */
public interface Config {

  /**
   * @return Config Type
   */
  public String getType();

  /**
   * @return Version Tag this config instance is mapped to
   */
  public String getVersionTag();

  /**
   * @return Properties that define this config instance
   */
  public Map<String, String> getProperties();

  /**
   * Change the version tag
   * @param versionTag
   */
  public void setVersionTag(String versionTag);

  /**
   * Replace properties with new provided set
   * @param properties Property Map to replace existing one
   */
  public void setProperties(Map<String, String> properties);

  /**
   * Update provided properties' values.
   * @param properties Property Map with updated values
   */
  public void updateProperties(Map<String, String> properties);

  /**
   * Delete certain properties
   * @param properties Property keys to be deleted
   */
  public void deleteProperties(List<String> properties);
  
  /**
   * Persist the configuration.
   */
  public void persist();
}
