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

import java.util.Date;
import java.util.Set;

@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_name", "ldap_user"})})
@Entity
@NamedQueries({
    @NamedQuery(name = "localUserByName", query = "SELECT user FROM UserEntity user where lower(user.userName)=:username AND user.ldapUser=false"),
    @NamedQuery(name = "ldapUserByName", query = "SELECT user FROM UserEntity user where lower(user.userName)=:username AND user.ldapUser=true")
})
@TableGenerator(name = "user_id_generator",
    table = "ambari_sequences", pkColumnName = "sequence_name", valueColumnName = "value"
    , pkColumnValue = "user_id_seq"
    , initialValue = 2
    , allocationSize = 1
    )
public class UserEntity {

  @Id
  @Column(name = "user_id")
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "user_id_generator")
  private Integer userId;

  @Column(name = "user_name")
  private String userName;

  @Column(name = "ldap_user")
  private Integer ldapUser = 0;

  @Column(name = "user_password")
  @Basic
  private String userPassword;

  @Column(name = "create_time")
  @Basic
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date createTime = new Date();

  @ManyToMany(mappedBy = "userEntities")
  private Set<RoleEntity> roleEntities;

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public Boolean getLdapUser() {
    return ldapUser == 0 ? Boolean.FALSE : Boolean.TRUE;
  }

  public void setLdapUser(Boolean ldapUser) {
    if (ldapUser == null) {
      this.ldapUser = null;
    } else {
      this.ldapUser = ldapUser ? 1 : 0;
    }
  }

  public String getUserPassword() {
    return userPassword;
  }

  public void setUserPassword(String userPassword) {
    this.userPassword = userPassword;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UserEntity that = (UserEntity) o;

    if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
    if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
    if (ldapUser != null ? !ldapUser.equals(that.ldapUser) : that.ldapUser != null) return false;
    if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
    if (userPassword != null ? !userPassword.equals(that.userPassword) : that.userPassword != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = userId != null ? userId.hashCode() : 0;
    result = 31 * result + (userName != null ? userName.hashCode() : 0);
    result = 31 * result + (userPassword != null ? userPassword.hashCode() : 0);
    result = 31 * result + (ldapUser != null ? ldapUser.hashCode() : 0);
    result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
    return result;
  }

  public Set<RoleEntity> getRoleEntities() {
    return roleEntities;
  }

  public void setRoleEntities(Set<RoleEntity> roleEntities) {
    this.roleEntities = roleEntities;
  }
}
