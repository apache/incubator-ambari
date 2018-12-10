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

package org.apache.ambari.server.serveraction;

public class ServerAction {

  public static final String ACTION_NAME = "ACTION_NAME";

  /**
   * The commands supported by the server. A command is a named alias to the
   * action implementation at the server
   */
  public static class Command {
    /**
     * Finalize the upgrade request
     */
    public static final String FINALIZE_UPGRADE = "FINALIZE_UPGRADE";
  }

  public static class PayloadName {
    public final static String CURRENT_STACK_VERSION = "current_stack_version";
    public final static String CLUSTER_NAME = "cluster_name";
  }
}
