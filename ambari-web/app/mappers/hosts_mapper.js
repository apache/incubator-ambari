/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

var App = require('app');

App.hostsMapper = App.QuickDataMapper.create({

  model: App.Host,
  config: {
    id: 'Hosts.host_name',
    host_name: 'Hosts.host_name',
    public_host_name: 'Hosts.public_host_name',
    cluster_id: 'Hosts.cluster_name',// 1
    rack: 'Hosts.rack_info',
    host_components_key: 'host_components',
    host_components_type: 'array',
    host_components: {
      item: 'id'
    },
    cpu: 'Hosts.cpu_count',
    memory: 'Hosts.total_mem',
    disk_info: 'Hosts.disk_info',
    disk_total: 'metrics.disk.disk_total',
    disk_free: 'metrics.disk.disk_free',
    health_status: 'Hosts.host_status',
    load_one: 'metrics.load.load_one',
    load_five: 'metrics.load.load_five',
    load_fifteen: 'metrics.load.load_fifteen',
    cpu_usage: 'cpu_usage',
    memory_usage: 'memory_usage',
    last_heart_beat_time: "Hosts.last_heartbeat_time",
    os_arch: 'Hosts.os_arch',
    os_type: 'Hosts.os_type',
    ip: 'Hosts.ip',
    disk_usage: 'disk_usage'
  },
  map: function (json) {
    if (json.items) {
      var result = this.parse(json.items);
      var clientHosts = App.Host.find();
      if (clientHosts != null && clientHosts.get('length') !== result.length) {
        var serverHostIds = {};
        result.forEach(function (host) {
          serverHostIds[host.id] = host.id;
        });
        var hostsToDelete = [];
        clientHosts.forEach(function (host) {
          if (host !== null && !serverHostIds[host.get('hostName')]) {
            // Delete old ones as new ones will be
            // loaded by loadMany().
            hostsToDelete.push(host);
          }
        });
        hostsToDelete.forEach(function (host) {
          host.deleteRecord();
        });
      }
      App.store.loadMany(this.get('model'), result);
    }
  },

  parse: function(items) {
    var result = [];
    items.forEach(function (item) {

      // Disk Usage
      if (item.metrics && item.metrics.disk && item.metrics.disk.disk_total && item.metrics.disk.disk_free) {
        var diskUsed = item.metrics.disk.disk_total - item.metrics.disk.disk_free;
        var diskUsedPercent = (100 * diskUsed) / item.metrics.disk.disk_total;
        item.disk_usage = diskUsedPercent.toFixed(1);
      }
      // CPU Usage
      if (item.metrics && item.metrics.cpu && item.metrics.cpu.cpu_system && item.metrics.cpu.cpu_user) {
        var cpuUsedPercent = item.metrics.cpu.cpu_system + item.metrics.cpu.cpu_user;
        item.cpu_usage = cpuUsedPercent.toFixed(1);
      }
      // Memory Usage
      if (item.metrics && item.metrics.memory && item.metrics.memory.mem_free && item.metrics.memory.mem_total) {
        var memUsed = item.metrics.memory.mem_total - item.metrics.memory.mem_free;
        var memUsedPercent = (100 * memUsed) / item.metrics.memory.mem_total;
        item.memory_usage = memUsedPercent.toFixed(1);
      }

      item.host_components.forEach(function (host_component) {
        host_component.id = host_component.HostRoles.component_name + "_" + host_component.HostRoles.host_name;
      }, this);
      result.push(this.parseIt(item, this.config));

    }, this);
    result = this.sortByPublicHostName(result);
    return result;
  },
  /**
   * Default data sorting by public_host_name field
   * @param data
   * @return {Array}
   */
  sortByPublicHostName: function(data) {
    data.sort(function(a, b) {
      var ap = a.public_host_name;
      var bp = b.public_host_name;
      if (ap > bp) return 1;
      if (ap < bp) return -1;
      return 0;
    });
    return data;
  }

});
