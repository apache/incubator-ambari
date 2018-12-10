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
package org.apache.ambari.server.controller;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a user maintenance request.
 */
public class UserResponse {

  private Set<String> roles = Collections.emptySet();
  private final String userName;
  private final boolean isLdapUser;

  public UserResponse(String name, boolean isLdapUser) {
    this.userName = name;
    this.isLdapUser = isLdapUser;
  }

  public String getUsername() {
    return userName;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> userRoles) {
    roles = userRoles;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UserResponse that = (UserResponse) o;

    if (userName != null ?
        !userName.equals(that.userName) : that.userName != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = userName != null ? userName.hashCode() : 0;
    return result;
  }

  /**
   * @return the isLdapUser
   */
  public boolean isLdapUser() {
    return isLdapUser;
  }

}
