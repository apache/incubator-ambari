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

var App = require('app');

App.MainHostDetailsController = Em.Controller.extend({
  name: 'mainHostDetailsController',
  content: null,
  isFromHosts: false,

  /**
   * open dashboard page
   */
  routeHome: function () {
    App.router.transitionTo('main.dashboard');
  },

  /**
   * open summary page of the selected service
   * @param event
   */
  routeToService: function(event){
    var service = event.context;
    App.router.transitionTo('main.services.service.summary',service);
  },

  /**
   * set new value to isFromHosts property
   * @param isFromHosts new value
   */
  setBack: function(isFromHosts){
    this.set('isFromHosts', isFromHosts);
  },

  /**
   * Send specific command to server
   * @param url
   * @param _method
   * @param postData
   * @param callback
   */
  sendCommandToServer : function(url, postData, _method, callback){
    var url =  (App.testMode) ?
      '/data/wizard/deploy/poll_1.json' : //content is the same as ours
      App.apiPrefix + '/clusters/' + App.router.getClusterName() + url;

    var method = App.testMode ? 'GET' : _method;

    $.ajax({
      type: method,
      url: url,
      data: JSON.stringify(postData),
      dataType: 'json',
      timeout: App.timeout,
      success: function(data){
        if(data && data.Requests){
          callback(data.Requests.id);
        } else{
          callback(null);
          console.log('cannot get request id from ', data);
        }
      },

      error: function (request, ajaxOptions, error) {
        //do something
        console.log('error on change component host status');
        App.ajax.defaultErrorHandler(request, url, method);
      },

      statusCode: require('data/statusCodes')
    });
  },

  /**
   * send command to server to start selected host component
   * @param event
   */
  startComponent: function (event) {
    var self = this;
    App.showConfirmationPopup(function() {
      var component = event.context;
      var context = Em.I18n.t('requestInfo.startHostComponent') + " " + component.get('displayName');
      self.sendStartComponentCommand(component, context);
    });
  },
  
  /**
   * PUTs a command to server to start a component. If no 
   * specific component is provided, all components are started.
   * @param component  When <code>null</code> all startable components are started. 
   * @param context  Context under which this command is beign sent. 
   */
  sendStartComponentCommand: function(component, context) {
    var url = component !== null ? 
        '/hosts/' + this.get('content.hostName') + '/host_components/' + component.get('componentName').toUpperCase() : 
        '/hosts/' + this.get('content.hostName') + '/host_components';
    var dataToSend = {
      RequestInfo : {
        "context" : context
      },
      Body:{
        HostRoles:{
          state: 'STARTED'
        }
      }
    };
    if (component === null) {
      var allComponents = this.get('content.hostComponents');
      var startable = [];
      allComponents.forEach(function (c) {
        if (c.get('isMaster') || c.get('isSlave')) {
          startable.push(c.get('componentName'));
        }
      });
      dataToSend.RequestInfo.query = "HostRoles/component_name.in(" + startable.join(',') + ")";
    }
    this.sendCommandToServer(url, dataToSend, 'PUT',
      function(requestId){

      if(!requestId){
        return;
      }

      console.log('Send request for STARTING successfully');

      if (App.testMode) {
        if(component === null){
          var allComponents = this.get('content.hostComponents');
          allComponents.forEach(function(component){
            component.set('workStatus', App.HostComponentStatus.stopping);
            setTimeout(function(){
              component.set('workStatus', App.HostComponentStatus.stopped);
            },App.testModeDelayForActions);
          });
        } else {
          component.set('workStatus', App.HostComponentStatus.starting);
          setTimeout(function(){
            component.set('workStatus', App.HostComponentStatus.started);
          },App.testModeDelayForActions);
        }
      } else {
        App.router.get('clusterController').loadUpdatedStatusDelayed(500);
      }
      App.router.get('backgroundOperationsController').showPopup();
    });
  },

  /**
   * Deletes the given host component, or all host components.
   * 
   * @param component  When <code>null</code> all host components are deleted.
   * @return  <code>null</code> when components get deleted.
   *          <code>{xhr: XhrObj, url: "http://", method: "DELETE"}</code> 
   *          when components failed to get deleted. 
   */
  _doDeleteHostComponent: function(component) {
    var url = component !== null ? 
        '/hosts/' + this.get('content.hostName') + '/host_components/' + component.get('componentName').toUpperCase() : 
        '/hosts/' + this.get('content.hostName') + '/host_components';
    url = App.apiPrefix + '/clusters/' + App.router.getClusterName() + url;
    var deleted = null;
    $.ajax({
      type: 'DELETE',
      url: url,
      timeout: App.timeout,
      async: false,
      success: function (data) {
        deleted = null;
        // If ZooKeeper Server component was removed, 
        // restart ZooKeeper service.
        /*
         * Commenting it out as user can restart service
         * whenever they want. We mention in message.
        if (component.get('componentName') === 'ZOOKEEPER_SERVER') {
          App.ajax.send({
            'name': 'service.item.start_stop',
            'sender': this,
            'data': {
              'requestInfo': 'Stop ZooKeeper',
              'serviceName': 'ZOOKEEPER',
              'state': 'INSTALLED'
            },
            'callback': function() {
              App.ajax.send({
                'name': 'service.item.start_stop',
                'sender': this,
                'data': {
                  'requestInfo': 'Start ZooKeeper',
                  'serviceName': 'ZOOKEEPER',
                  'state': 'STARTED'
                }
              });
            }
          });
        }*/
      },
      error: function (xhr, textStatus, errorThrown) {
        console.log('Error deleting host component');
        console.log(textStatus);
        console.log(errorThrown);
        deleted = {xhr: xhr, url: url, method: 'DELETE'};
      },
      statusCode: require('data/statusCodes')
    });
    return deleted;
  },

  /**
   * send command to server to upgrade selected host component
   * @param event
   */
  upgradeComponent: function (event) {
    var self = this;
    var component = event.context;
    App.showConfirmationPopup(function() {
      self.sendCommandToServer('/hosts/' + self.get('content.hostName') + '/host_components/' + component.get('componentName').toUpperCase(),{
            RequestInfo : {
              "context" : Em.I18n.t('requestInfo.upgradeHostComponent') + " " + component.get('displayName')
            },
            Body:{
              HostRoles:{
                stack_id: 'HDP-1.2.2',
                state: 'INSTALLED'
              }
            }
          }, 'PUT',
          function(requestId){
            if(!requestId){
              return;
            }

            console.log('Send request for UPGRADE successfully');

            if (App.testMode) {
              component.set('workStatus', App.HostComponentStatus.starting);
              setTimeout(function(){
                component.set('workStatus', App.HostComponentStatus.started);
              },App.testModeDelayForActions);
            } else {
              App.router.get('clusterController').loadUpdatedStatusDelayed(500);
            }

            App.router.get('backgroundOperationsController').showPopup();

          });
    });
  },
  /**
   * send command to server to stop selected host component
   * @param event
   */
  stopComponent: function (event) {
    var self = this;
    App.showConfirmationPopup(function() {
      var component = event.context;
      var context = Em.I18n.t('requestInfo.stopHostComponent')+ " " + component.get('displayName');
      self.sendStopComponentCommand(component, context);
    });
  },
  
  /**
   * PUTs a command to server to stop a component. If no 
   * specific component is provided, all components are stopped.
   * @param component  When <code>null</code> all components are stopped. 
   * @param context  Context under which this command is beign sent. 
   */
  sendStopComponentCommand: function(component, context){
    var url = component !== null ? 
        '/hosts/' + this.get('content.hostName') + '/host_components/' + component.get('componentName').toUpperCase() : 
        '/hosts/' + this.get('content.hostName') + '/host_components';
    var dataToSend = {
      RequestInfo : {
        "context" : context
      },
      Body:{
        HostRoles:{
          state: 'INSTALLED'
        }
      }
    };
    if (component === null) {
      var allComponents = this.get('content.hostComponents');
      var startable = [];
      allComponents.forEach(function (c) {
        if (c.get('isMaster') || c.get('isSlave')) {
          startable.push(c.get('componentName'));
        }
      });
      dataToSend.RequestInfo.query = "HostRoles/component_name.in(" + startable.join(',') + ")";
    }
    this.sendCommandToServer( url, dataToSend, 'PUT',
      function(requestId){
      if(!requestId){
        return;
      }

      console.log('Send request for STOPPING successfully');

      if (App.testMode) {
        if(component === null){
          var allComponents = this.get('content.hostComponents');
          allComponents.forEach(function(component){
            component.set('workStatus', App.HostComponentStatus.stopping);
            setTimeout(function(){
              component.set('workStatus', App.HostComponentStatus.stopped);
            },App.testModeDelayForActions);
          });
        } else {
          component.set('workStatus', App.HostComponentStatus.stopping);
          setTimeout(function(){
            component.set('workStatus', App.HostComponentStatus.stopped);
          },App.testModeDelayForActions);
        }
      } else {
        App.router.get('clusterController').loadUpdatedStatusDelayed(500);
      }
      App.router.get('backgroundOperationsController').showPopup();
    });
  },

  /**
   * send command to server to install selected host component
   * @param event
   */
  addComponent: function (event, context) {
    var self = this;
    var component = event.context;
    var componentName = component.get('componentName').toUpperCase().toString();
    var subComponentNames = component.get('subComponentNames');
    var displayName = component.get('displayName');

    var securityEnabled = App.router.get('mainAdminSecurityController').getUpdatedSecurityStatus();

    if (securityEnabled) {
      App.showConfirmationPopup(function() {
        self.primary(component);
      }, Em.I18n.t('hosts.host.addComponent.securityNote').format(componentName,self.get('content.hostName')));
    }
    else {
      var dn = displayName;
      if (subComponentNames !== null && subComponentNames.length > 0) {
        var dns = [];
        subComponentNames.forEach(function(scn){
          dns.push(App.format.role(scn));
        });
        dn += " ("+dns.join(", ")+")";
      }
      var dialogContent = 
        [Em.I18n.t('hosts.host.addComponent.msg').format(dn) + "<br><br>",
        '{{t hosts.host.addComponent.note}}'];
      App.ModalPopup.show({
        primary: Em.I18n.t('yes'),
        secondary: Em.I18n.t('no'),
        header: Em.I18n.t('popup.confirmation.commonHeader'),
        bodyClass: Ember.View.extend({
          template: Ember.Handlebars.compile(dialogContent.join(''))
        }),
        onPrimary: function () {
          this.hide();
          if (component.get('componentName') === 'CLIENTS') {
            // Clients component has many sub-components which 
            // need to be installed.
            var scs = component.get('subComponentNames');
            scs.forEach(function (sc) {
              var c = Em.Object.create({
                displayName: App.format.role(sc),
                componentName: sc
              });
              self.primary(c);
            });
          } else {
            self.primary(component);
          }
        }
      });
    }
  },
  primary: function(component) {
    var self = this;
    var componentName = component.get('componentName').toUpperCase().toString();
    var displayName = component.get('displayName');

    self.sendCommandToServer('/hosts?Hosts/host_name=' + self.get('content.hostName'), {
        RequestInfo: {
          "context": Em.I18n.t('requestInfo.installHostComponent') + " " + displayName
        },
        Body: {
          host_components: [
            {
              HostRoles: {
                component_name: componentName
              }
            }
          ]
        }
      },
      'POST',
      function (requestId) {

        console.log('Send request for ADDING NEW COMPONENT successfully');

        self.sendCommandToServer('/host_components?HostRoles/host_name=' + self.get('content.hostName') + '\&HostRoles/component_name=' + componentName + '\&HostRoles/state=INIT', {
            RequestInfo: {
              "context": Em.I18n.t('requestInfo.installNewHostComponent') + " " + displayName
            },
            Body: {
              HostRoles: {
                state: 'INSTALLED'
              }
            }
          },
          'PUT',
          function (requestId) {
            if (!requestId) {
              return;
            }

            console.log('Send request for INSTALLING NEW COMPONENT successfully');

            if (App.testMode) {
              component.set('workStatus', App.HostComponentStatus.installing);
              setTimeout(function () {
                component.set('workStatus', App.HostComponentStatus.stopped);
              }, App.testModeDelayForActions);
            } else {
              App.router.get('clusterController').loadUpdatedStatusDelayed(500);
            }

            App.router.get('backgroundOperationsController').showPopup();

          });
      });
  },
  /**
   * send command to server to install selected host component
   * @param event
   * @param context
   */
  installComponent: function (event, context) {
    var self = this;
    var component = event.context;
    var componentName = component.get('componentName').toUpperCase().toString();
    var displayName = component.get('displayName');

    App.ModalPopup.show({
      primary: Em.I18n.t('yes'),
      secondary: Em.I18n.t('no'),
      header: Em.I18n.t('popup.confirmation.commonHeader'),
      bodyClass: Ember.View.extend({
        template: Ember.Handlebars.compile([
          '{{t hosts.delete.popup.body}}<br /><br />',
          '{{t hosts.host.addComponent.note}}'
        ].join(''))
      }),
      onPrimary: function () {
        this.hide();
        self.sendCommandToServer('/hosts/' + self.get('content.hostName') + '/host_components/' + component.get('componentName').toUpperCase(), {
            RequestInfo: {
              "context": Em.I18n.t('requestInfo.installHostComponent') + " " + displayName
            },
            Body: {
              HostRoles: {
                state: 'INSTALLED'
              }
            }
          },
          'PUT',
          function (requestId) {
            if (!requestId) {
              return;
            }

            console.log('Send request for REINSTALL COMPONENT successfully');

            if (App.testMode) {
              component.set('workStatus', App.HostComponentStatus.installing);
              setTimeout(function () {
                component.set('workStatus', App.HostComponentStatus.stopped);
              }, App.testModeDelayForActions);
            } else {
              App.router.get('clusterController').loadUpdatedStatusDelayed(500);
            }

            App.router.get('backgroundOperationsController').showPopup();

          });
      }
    });
  },
  /**
   * send command to server to run decommission on DATANODE
   * @param event
   */
  decommission: function(event){
    var self = this;
    var decommissionHostNames = this.get('view.decommissionDataNodeHostNames');
    if (decommissionHostNames == null) {
      decommissionHostNames = [];
    }
    App.showConfirmationPopup(function(){
      var component = event.context;
      // Only HDFS service as of now
      var svcName = component.get('service.serviceName');
      if (svcName === "HDFS") {
        var hostName = self.get('content.hostName');
        var index = decommissionHostNames.indexOf(hostName);
        if (index < 0) {
          decommissionHostNames.push(hostName);
        }
        self.doDatanodeDecommission(decommissionHostNames, true);
      }
      App.router.get('backgroundOperationsController').showPopup();
    });
  },

  /**
   * Performs either Decommission or Recommission by updating the hosts list on
   * server.
   * @param decommission defines context for request (true for decommission and false for recommission)
   */
  doDatanodeDecommission: function(decommissionHostNames, decommission){
    var self = this;
    if (decommissionHostNames == null) {
      decommissionHostNames = [];
    }
    var invocationTag = String(new Date().getTime());
    var context = decommission ? Em.I18n.t('hosts.host.datanode.decommission') : Em.I18n.t('hosts.host.datanode.recommission');
    var clusterName = App.router.get('clusterController.clusterName');
    var clusterUrl = App.apiPrefix + '/clusters/' + clusterName;
    var configsUrl = clusterUrl + '/configurations';
    var configsData = {
      type: "hdfs-exclude-file",
      tag: invocationTag,
      properties: {
        datanodes: decommissionHostNames.join(',')
      }
    };
    var configsAjax = {
      type: 'POST',
      url: configsUrl,
      dataType: 'json',
      data: JSON.stringify(configsData),
      timeout: App.timeout,
      success: function(){
        var actionsUrl = clusterUrl + '/services/HDFS/actions/DECOMMISSION_DATANODE';
        var actionsData = {
          RequestInfo: {
            context: context},
          Body: {
            parameters: {
              excludeFileTag: invocationTag
            }
          }
        };
        var actionsAjax = {
          type: 'POST',
          url: actionsUrl,
          dataType: 'json',
          data: JSON.stringify(actionsData),
          timeout: App.timeout,
          success: function(){
            var persistUrl = App.apiPrefix + '/persist';
            var persistData = {
              "decommissionDataNodesTag": invocationTag
            };
            var persistPutAjax = {
              type: 'POST',
              url: persistUrl,
              dataType: 'json',
              data: JSON.stringify(persistData),
              timeout: App.timeout,
              success: function(){
                var view = self.get('view');
                view.loadDecommissionNodesList();
              }
            };
            jQuery.ajax(persistPutAjax);
          },
          error: function(xhr, textStatus, errorThrown){
            console.log(textStatus);
            console.log(errorThrown);
          }
        };
        jQuery.ajax(actionsAjax);
      },
      error: function(xhr, textStatus, errorThrown){
        console.log(textStatus);
        console.log(errorThrown);
      }
    }
    jQuery.ajax(configsAjax);
  },

  /**
   * send command to server to run recommission on DATANODE
   * @param event
   */
  recommission: function(event){
    var self = this;
    var decommissionHostNames = this.get('view.decommissionDataNodeHostNames');
    if (decommissionHostNames == null) {
      decommissionHostNames = [];
    }
    App.showConfirmationPopup(function(){
      var component = event.context;
      // Only HDFS service as of now
      var svcName = component.get('service.serviceName');
      if (svcName === "HDFS") {
        var hostName = self.get('content.hostName');
        var index = decommissionHostNames.indexOf(hostName);
        decommissionHostNames.splice(index, 1);
        self.doDatanodeDecommission(decommissionHostNames, false);
      }
      App.router.get('backgroundOperationsController').showPopup();
    });
  },
  
  doAction: function(option) {
    switch (option.context.action) {
      case "deleteHost":
        this.validateAndDeleteHost();
        break;
      case "startAllComponents":
        this.doStartAllComponents();
        break;
      case "stopAllComponents":
        this.doStopAllComponents();
        break;
      default:
        break;
    }
  },
  
  doStartAllComponents: function() {
    var self = this;
    var components = this.get('content.hostComponents');
    var componentsLength = components == null ? 0 : components.get('length');
    if (componentsLength > 0) {
      App.showConfirmationPopup(function() {
        self.sendStartComponentCommand(null, 
            Em.I18n.t('hosts.host.maintainance.startAllComponents.context'));
      });
    }
  },
  
  doStopAllComponents: function() {
    var self = this;
    var components = this.get('content.hostComponents');
    var componentsLength = components == null ? 0 : components.get('length');
    if (componentsLength > 0) {
      App.showConfirmationPopup(function() {
        self.sendStopComponentCommand(null, 
            Em.I18n.t('hosts.host.maintainance.stopAllComponents.context'));
      });
    }
  },

  /**
   * Deletion of hosts not supported for this version
   */
   validateAndDeleteHost: function () {
     if (!App.supports.deleteHost) {
       return;
     }
     var stoppedStates = [App.HostComponentStatus.stopped, 
                          App.HostComponentStatus.install_failed, 
                          App.HostComponentStatus.upgrade_failed];
     var masterComponents = [];
     var runningComponents = [];
     var unknownComponents = [];
     var nonDeletableComponents = [];
     var components = this.get('content.hostComponents');
     if (components!=null && components.get('length')>0){
       components.forEach(function (cInstance) { 
         var workStatus = cInstance.get('workStatus');
         if (cInstance.get('isMaster') && !cInstance.get('isDeletable')) {
           masterComponents.push(cInstance.get('displayName'));
         }
         if (stoppedStates.indexOf(workStatus) < 0) {
           runningComponents.push(cInstance.get('displayName'));
         }
         if (!cInstance.get('isDeletable')) {
           nonDeletableComponents.push(cInstance.get('displayName'));
         }
         if (workStatus === App.HostComponentStatus.unknown) {
           unknownComponents.push(cInstance.get('displayName'));
         }
       });
     }
     if (masterComponents.length > 0) {
       var bodyHtml = "<p><i class=\"icon-warning-sign\"></i> ";
       bodyHtml += Em.I18n.t('hosts.cant.do.popup.masterList.body').format(masterComponents.length);
       bodyHtml += "</p><i>";
       bodyHtml += masterComponents.join(", ");
       bodyHtml += "</i>";
       this.raiseDeleteComponentsError(bodyHtml);
       return;
     } else if (nonDeletableComponents.length > 0) {
       var bodyHtml = "<p><i class=\"icon-warning-sign\"></i> ";
       bodyHtml += Em.I18n.t('hosts.cant.do.popup.nonDeletableList.body').format(nonDeletableComponents.length);
       bodyHtml += "</p><i>";
       bodyHtml += nonDeletableComponents.join(", ");
       bodyHtml += "</i>";
       this.raiseDeleteComponentsError(bodyHtml);
       return;
     } else if(runningComponents.length > 0) {
       var bodyHtml = "<p><i class=\"icon-warning-sign\"></i> ";
       bodyHtml += Em.I18n.t('hosts.cant.do.popup.runningList.body').format(runningComponents.length);
       bodyHtml += "</p><i>";
       bodyHtml += runningComponents.join(", ");
       bodyHtml += "</i><br><br><p>";
       bodyHtml += Em.I18n.t('hosts.cant.do.popup.runningList.body.end');
       bodyHtml += "</p>";
       this.raiseDeleteComponentsError(bodyHtml);
       return;
     }
     this._doDeleteHost(unknownComponents);
  },
  
  raiseDeleteComponentsError: function (bodyHtml) {
    var self = this;
    App.ModalPopup.show({
      header: Em.I18n.t('hosts.cant.do.popup.title'),
      html: true,
      encodeBody: false,
      body: bodyHtml,
      primary: Em.I18n.t('ok'),
      secondary: null,
      onPrimary: function() {
        this.hide();
      }
    })
  },

  /**
   * show confirmation popup to delete host
   */
  _doDeleteHost: function(unknownComponents) {
    var self = this;
    var bodyHtml = "<p><i class=\"icon-warning-sign\"></i> ";
    bodyHtml += Em.I18n.t('hosts.delete.popup.body').format("<i>"+this.get('content.publicHostName')+"</i>");
    bodyHtml += "</p>";
    if (unknownComponents!=null && unknownComponents.length > 0) {
      bodyHtml += "<div class=\"alert\">";
      bodyHtml += Em.I18n.t('hosts.delete.popup.unknownComponents') + "<br>";
      bodyHtml += "<i>"
      bodyHtml += unknownComponents.join(", ");
      bodyHtml += "</i></div>";
    }
    bodyHtml += "<p>";
    bodyHtml += Em.I18n.t('hosts.delete.popup.body.msg1');
    bodyHtml += "</p><p>";
    bodyHtml += Em.I18n.t('hosts.delete.popup.body.msg2');
    bodyHtml += "</p><p>";
    bodyHtml += "<span class=\"label label-important\">"+Em.I18n.t('common.important')+"</span>  ";
    bodyHtml += Em.I18n.t('hosts.delete.popup.body.msg3');
    bodyHtml += "</p>";
    App.ModalPopup.show({
      header: Em.I18n.t('hosts.delete.popup.title'),
      html: true,
      encodeBody: false,
      body: bodyHtml,
      primary: Em.I18n.t('ok'),
      secondary: Em.I18n.t('common.cancel'),
      onPrimary: function() {
        var dialogSelf = this;
        var allComponents = self.get('content.hostComponents');
        var deleteError = null;
        allComponents.forEach(function(component){
          if (!deleteError) {
            deleteError = self._doDeleteHostComponent(component);
          }
        });
        if (!deleteError) {
          var url = App.apiPrefix + '/clusters/' + App.router.getClusterName() + '/hosts/' + self.get('content.hostName');
          $.ajax({
            type: 'DELETE',
            url: url,
            timeout: App.timeout,
            async: false,
            success: function (data) {
              dialogSelf.hide();
              App.router.get('updateController').updateAll();
              App.router.transitionTo('hosts.index');
            },
            error: function (xhr, textStatus, errorThrown) {
              console.log('Error deleting host component');
              console.log(textStatus);
              console.log(errorThrown);
              dialogSelf.hide();
              xhr.responseText = "{\"message\": \"" + xhr.statusText + "\"}";
              App.ajax.defaultErrorHandler(xhr, url, 'DELETE', xhr.status);
            },
            statusCode: require('data/statusCodes')
          });
        } else {
          dialogSelf.hide();
          deleteError.xhr.responseText = "{\"message\": \"" + deleteError.xhr.statusText + "\"}";
          App.ajax.defaultErrorHandler(deleteError.xhr, deleteError.url, deleteError.method, deleteError.xhr.status);
        }
      },
      onSecondary: function() {
        this.hide();
      }
    })
  }
})