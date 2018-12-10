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

/**
 * Abstract view for config fields.
 * Add popover support to control
 */
App.ServiceConfigPopoverSupport = Ember.Mixin.create({

  /**
   * Config object. It will instance of App.ServiceConfigProperty
   */
  serviceConfig: null,
  placeholderBinding: 'serviceConfig.defaultValue',
  isPopoverEnabled: true,

  didInsertElement: function () {
    if (this.get('isPopoverEnabled') !== 'false') {
      App.popover(this.$(), {
        title: Em.I18n.t('installer.controls.serviceConfigPopover.title').format(
          this.get('serviceConfig.displayName'),
          (this.get('serviceConfig.displayName') == this.get('serviceConfig.name'))?'':this.get('serviceConfig.name')
        ),
        content: this.get('serviceConfig.description'),
        placement: 'right',
        trigger: 'hover'
      });
    }
  }
});

/**
 * Default input control
 * @type {*}
 */
App.ServiceConfigTextField = Ember.TextField.extend(App.ServiceConfigPopoverSupport, {

  valueBinding: 'serviceConfig.value',
  classNameBindings: 'textFieldClassName',
  placeholderBinding: 'serviceConfig.defaultValue',

  keyPress: function (event) {
    if (event.keyCode == 13) {
      return false;
    }
  },
  //Set editDone true for last edited config text field parameter
  focusOut: function(event){
    this.get('serviceConfig').set("editDone", true);
  },
  //Set editDone false for all current category config text field parameter
  focusIn: function(event){
    this.get("parentView.categoryConfigsAll").setEach("editDone", false);
  },

  textFieldClassName: function () {
    if (this.get('serviceConfig.unit')) {
      return ['input-small'];
    } else if (this.get('serviceConfig.displayType') === 'principal'){
      return ['span12'];
    } else {
      return ['span9'];
    }
  }.property('serviceConfig.displayType', 'serviceConfig.unit'),

  disabled: function () {
    return !this.get('serviceConfig.isEditable');
  }.property('serviceConfig.isEditable')

});

/**
 * Customized input control with Utits type specified
 * @type {*}
 */
App.ServiceConfigTextFieldWithUnit = Ember.View.extend(App.ServiceConfigPopoverSupport, {
  valueBinding: 'serviceConfig.value',
  classNames: ['input-append','with-unit'],
  placeholderBinding: 'serviceConfig.defaultValue',

  template: Ember.Handlebars.compile('{{view App.ServiceConfigTextField serviceConfigBinding="view.serviceConfig" isPopoverEnabled="false"}}<span class="add-on">{{view.serviceConfig.unit}}</span>'),

  disabled: function () {
    return !this.get('serviceConfig.isEditable');
  }.property('serviceConfig.isEditable')

});

/**
 * Password control
 * @type {*}
 */
App.ServiceConfigPasswordField = Ember.TextField.extend({

  serviceConfig: null,
  type: 'password',
  valueBinding: 'serviceConfig.value',
  classNames: [ 'span4' ],
  placeholder: Em.I18n.t('form.item.placeholders.typePassword'),

  template: Ember.Handlebars.compile('{{view view.retypePasswordView}}'),

  keyPress: function (event) {
    if (event.keyCode == 13) {
      return false;
    }
  },

  retypePasswordView: Ember.TextField.extend({
    placeholder: Em.I18n.t('form.passwordRetype'),
    type: 'password',
    classNames: [ 'span4', 'retyped-password' ],
    keyPress: function (event) {
      if (event.keyCode == 13) {
        return false;
      }
    },
    valueBinding: 'parentView.serviceConfig.retypedPassword',
    disabled: function () {
      return !this.get('parentView.serviceConfig.isEditable');
    }.property('parentView.serviceConfig.isEditable')
  }),

  disabled: function () {
    return !this.get('serviceConfig.isEditable');
  }.property('serviceConfig.isEditable')

});

/**
 * Textarea control
 * @type {*}
 */
App.ServiceConfigTextArea = Ember.TextArea.extend(App.ServiceConfigPopoverSupport, {

  valueBinding: 'serviceConfig.value',
  rows: 4,
  classNames: ['span9', 'directories'],
  placeholderBinding: 'serviceConfig.defaultValue',

  disabled: function () {
    return !this.get('serviceConfig.isEditable');
  }.property('serviceConfig.isEditable')

});

/**
 * Textarea control with bigger height
 * @type {*}
 */
App.ServiceConfigBigTextArea = App.ServiceConfigTextArea.extend({
  rows: 10
});

/**
 * Checkbox control
 * @type {*}
 */
App.ServiceConfigCheckbox = Ember.Checkbox.extend(App.ServiceConfigPopoverSupport, {

  checkedBinding: 'serviceConfig.value',

  disabled: function () {
    return !this.get('serviceConfig.isEditable');
  }.property('serviceConfig.isEditable')

});

App.ServiceConfigRadioButtons = Ember.View.extend({
  template: Ember.Handlebars.compile([
    '{{#each option in view.options}}',
    '{{#unless option.hidden}}',
    '<label class="radio">',
    '{{#view App.ServiceConfigRadioButton nameBinding = "view.name" valueBinding = "option.displayName"}}',
    '{{/view}}',
    '{{option.displayName}} &nbsp;',
    '</label>',
    '{{/unless}}',
    '{{/each}}'
  ].join('\n')),

  didInsertElement: function () {
    // on page render, automatically populate JDBC URLs only for default database settings
    // so as to not lose the user's customizations on these fields
    if (App.clusterStatus.clusterState == 'CLUSTER_NOT_CREATED_1' && ['New MySQL Database', 'New Derby Database'].contains(this.get('serviceConfig.value'))) {
      this.onOptionsChange();
    }
  },

  configs: function () {
    return this.get('categoryConfigsAll').filterProperty('isObserved', true);
  }.property('categoryConfigsAll'),

  serviceConfig: null,
  categoryConfigsAll: null,

  onOptionsChange: function () {
    var connectionUrl = this.get('connectionUrl');
    if (connectionUrl) {
      if (this.get('serviceConfig.serviceName') === 'HIVE') {
        switch (this.get('serviceConfig.value')) {
          case 'New MySQL Database':
          case 'Existing MySQL Database':
            connectionUrl.set('value', "jdbc:mysql://" + this.get('hostName') + "/" + this.get('databaseName') + "?createDatabaseIfNotExist=true");
            break;
          case 'Existing Oracle Database':
            connectionUrl.set('value', "jdbc:oracle:thin:@//" + this.get('hostName') + ":1521/" + this.get('databaseName'));
            break;
        }
      } else if (this.get('serviceConfig.serviceName') === 'OOZIE') {
        switch (this.get('serviceConfig.value')) {
          case 'New Derby Database':
            connectionUrl.set('value', "jdbc:derby:${oozie.data.dir}/${oozie.db.schema.name}-db;create=true");
            break;
          case 'Existing MySQL Database':
            connectionUrl.set('value', "jdbc:mysql://" + this.get('hostName') + "/" + this.get('databaseName'));
            break;
          case 'Existing Oracle Database':
            connectionUrl.set('value', "jdbc:oracle:thin:@//" + this.get('hostName') + ":1521/" + this.get('databaseName'));
            break;
        }
      }
      connectionUrl.set('defaultValue', connectionUrl.get('value'));
    }
  }.observes('databaseName', 'hostName', 'connectionUrl'),

  nameBinding: 'serviceConfig.radioName',

  databaseName: function () {
    switch (this.get('serviceConfig.serviceName')) {
      case 'HIVE':
        return this.get('categoryConfigsAll').findProperty('name', 'hive_database_name').get('value');
      case 'OOZIE':
        return this.get('categoryConfigsAll').findProperty('name', 'oozie_database_name').get('value');
      default:
        return null;
    }
  }.property('configs.@each.value', 'serviceConfig.serviceName'),


  hostName: function () {
    var value = this.get('serviceConfig.value');

    if (this.get('serviceConfig.serviceName') === 'HIVE') {
      switch (value) {
        case 'New MySQL Database':
          return this.get('categoryConfigsAll').findProperty('name', 'hive_ambari_host').get('value');
        case 'Existing MySQL Database':
          return this.get('categoryConfigsAll').findProperty('name', 'hive_existing_mysql_host').get('value');
        case 'Existing Oracle Database':
          return this.get('categoryConfigsAll').findProperty('name', 'hive_existing_oracle_host').get('value');
      }
    } else if (this.get('serviceConfig.serviceName') === 'OOZIE') {
      switch (value) {
        case 'New Derby Database':
          return this.get('categoryConfigsAll').findProperty('name', 'oozie_ambari_host').get('value');
        case 'Existing MySQL Database':
          return this.get('categoryConfigsAll').findProperty('name', 'oozie_existing_mysql_host').get('value');
        case 'Existing Oracle Database':
          return this.get('categoryConfigsAll').findProperty('name', 'oozie_existing_oracle_host').get('value');
      }
    }
  }.property('serviceConfig.serviceName', 'serviceConfig.value', 'configs.@each.value'),

  connectionUrl: function () {
    if (this.get('serviceConfig.serviceName') === 'HIVE') {
      return this.get('categoryConfigsAll').findProperty('name', 'hive_jdbc_connection_url');
    } else {
      return this.get('categoryConfigsAll').findProperty('name', 'oozie_jdbc_connection_url');
    }
  }.property('serviceConfig.serviceName'),

  optionsBinding: 'serviceConfig.options',
  disabled: function () {
    return !this.get('serviceConfig.isEditable');
  }.property('serviceConfig.isEditable')
});

App.ServiceConfigRadioButton = Ember.Checkbox.extend({
  tagName: 'input',
  attributeBindings: ['type', 'name', 'value', 'checked'],
  checked: false,
  type: 'radio',
  name: null,
  value: null,

  didInsertElement: function () {
    if (this.get('parentView.serviceConfig.value') === this.get('value')) {
      this.set('checked', true);
    }
  },

  click: function () {
    this.set('checked', true);
    this.onChecked();
  },

  onChecked: function () {
    this.set('parentView.serviceConfig.value', this.get('value'));
    var components = this.get('parentView.serviceConfig.options');
    components
      .forEach(function (_component) {
        if (_component.foreignKeys) {
          _component.foreignKeys.forEach(function (_componentName) {
            if (this.get('parentView.categoryConfigsAll').someProperty('name', _componentName)) {
              var component = this.get('parentView.categoryConfigsAll').findProperty('name', _componentName);
              if (_component.displayName === this.get('value')) {
                component.set('isVisible', true);
              } else {
                component.set('isVisible', false);
              }
            }
          }, this);
        }
      }, this);
  }.observes('checked'),

  disabled: function () {
    return !this.get('parentView.serviceConfig.isEditable');
  }.property('parentView.serviceConfig.isEditable')
});

App.ServiceConfigComboBox = Ember.Select.extend(App.ServiceConfigPopoverSupport, {
  contentBinding: 'serviceConfig.options',
  selectionBinding: 'serviceConfig.value',
  classNames: [ 'span3' ],
  disabled: function () {
    return !this.get('serviceConfig.isEditable');
  }.property('serviceConfig.isEditable')
});


/**
 * Base component for host config with popover support
 */
App.ServiceConfigHostPopoverSupport = Ember.Mixin.create({

  /**
   * Config object. It will instance of App.ServiceConfigProperty
   */
  serviceConfig: null,

  didInsertElement: function () {
    App.popover(this.$(), {
      title: this.get('serviceConfig.displayName'),
      content: this.get('serviceConfig.description'),
      placement: 'right',
      trigger: 'hover'
    });
  }
});

/**
 * Master host component.
 * Show hostname without ability to edit it
 * @type {*}
 */
App.ServiceConfigMasterHostView = Ember.View.extend(App.ServiceConfigHostPopoverSupport, {

  classNames: ['master-host', 'span6'],
  valueBinding: 'serviceConfig.value',

  template: Ember.Handlebars.compile('{{value}}')

});

/**
 * Base component to display Multiple hosts
 * @type {*}
 */
App.ServiceConfigMultipleHostsDisplay = Ember.Mixin.create(App.ServiceConfigHostPopoverSupport, {

  hasNoHosts: function () {
    console.log('view', this.get('viewName')); //to know which View cause errors
    console.log('controller', this.get('controller').name); //should be slaveComponentGroupsController
    if (!this.get('value')) {
      return true;
    }
    return this.get('value').length === 0;
  }.property('value'),

  hasOneHost: function () {
    return this.get('value').length === 1;
  }.property('value'),

  hasMultipleHosts: function () {
    return this.get('value').length > 1;
  }.property('value'),

  otherLength: function () {
    var len = this.get('value').length;
    if (len > 2) {
      return Em.I18n.t('installer.controls.serviceConfigMultipleHosts.others').format(len - 1);
    } else {
      return Em.I18n.t('installer.controls.serviceConfigMultipleHosts.other');
    }
  }.property('value')

})


/**
 * Multiple master host component.
 * Show hostnames without ability to edit it
 * @type {*}
 */
App.ServiceConfigMasterHostsView = Ember.View.extend(App.ServiceConfigMultipleHostsDisplay, {

  viewName: "serviceConfigMasterHostsView",
  valueBinding: 'serviceConfig.value',

  classNames: ['master-hosts', 'span6'],
  templateName: require('templates/wizard/master_hosts'),

  /**
   * Onclick handler for link
   */
  showHosts: function () {
    var serviceConfig = this.get('serviceConfig');
    App.ModalPopup.show({
      header: Em.I18n.t('installer.controls.serviceConfigMasterHosts.header').format(serviceConfig.category),
      bodyClass: Ember.View.extend({
        serviceConfig: serviceConfig,
        templateName: require('templates/wizard/master_hosts_popup')
      }),
      onPrimary: function () {
        this.hide();
      },
      secondary: null
    });
  }

});

/**
 * Show tabs list for slave hosts
 * @type {*}
 */
App.SlaveComponentGroupsMenu = Em.CollectionView.extend({

  content: function () {
    return this.get('controller.componentGroups');
  }.property('controller.componentGroups'),

  tagName: 'ul',
  classNames: ["nav", "nav-tabs"],

  itemViewClass: Em.View.extend({
    classNameBindings: ["active"],

    active: function () {
      return this.get('content.active');
    }.property('content.active'),

    errorCount: function () {
      return this.get('content.properties').filterProperty('isValid', false).filterProperty('isVisible', true).get('length');
    }.property('content.properties.@each.isValid', 'content.properties.@each.isVisible'),

    template: Ember.Handlebars.compile('<a {{action showSlaveComponentGroup view.content target="controller"}} href="#"> {{view.content.name}}{{#if view.errorCount}}<span class="badge badge-important">{{view.errorCount}}</span>{{/if}}</a><i {{action removeSlaveComponentGroup view.content target="controller"}} class="icon-remove"></i>')
  })
});

/**
 * <code>Add group</code> button
 * @type {*}
 */
App.AddSlaveComponentGroupButton = Ember.View.extend({

  tagName: 'span',
  slaveComponentName: null,

  didInsertElement: function () {
    App.popover(this.$(), {
      title: Em.I18n.t('installer.controls.addSlaveComponentGroupButton.title').format(this.get('slaveComponentName')),
      content: Em.I18n.t('installer.controls.addSlaveComponentGroupButton.content').format(this.get('slaveComponentName'), this.get('slaveComponentName'), this.get('slaveComponentName')),
      placement: 'right',
      trigger: 'hover'
    });
  }

});

/**
 * Multiple Slave Hosts component
 * @type {*}
 */
App.ServiceConfigSlaveHostsView = Ember.View.extend(App.ServiceConfigMultipleHostsDisplay, {

  viewName: 'serviceConfigSlaveHostsView',

  classNames: ['slave-hosts', 'span6'],

  valueBinding: 'serviceConfig.value',

  templateName: require('templates/wizard/slave_hosts'),

  /**
   * Onclick handler for link
   */
  showHosts: function () {
    var serviceConfig = this.get('serviceConfig');
    App.ModalPopup.show({
      header: Em.I18n.t('installer.controls.serviceConfigMasterHosts.header').format(serviceConfig.category),
      bodyClass: Ember.View.extend({
        serviceConfig: serviceConfig,
        templateName: require('templates/wizard/master_hosts_popup')
      }),
      onPrimary: function () {
        this.hide();
      },
      secondary: null
    });
  }

});

/**
 * properties for present active slave group
 * @type {*}
 */
App.SlaveGroupPropertiesView = Ember.View.extend({

  viewName: 'serviceConfigSlaveHostsView',

  group: function () {
    return this.get('controller.activeGroup');
  }.property('controller.activeGroup'),

  groupConfigs: function () {
    console.log("************************************************************************");
    console.log("The value of group is: " + this.get('group'));
    console.log("************************************************************************");
    return this.get('group.properties');
  }.property('group.properties.@each').cacheable(),

  errorCount: function () {
    return this.get('group.properties').filterProperty('isValid', false).filterProperty('isVisible', true).get('length');
  }.property('configs.@each.isValid', 'configs.@each.isVisible')
});

/**
 * DropDown component for <code>select hosts for groups</code> popup
 * @type {*}
 */
App.SlaveComponentDropDownGroupView = Ember.View.extend({

  viewName: "slaveComponentDropDownGroupView",

  /**
   * On change handler for <code>select hosts for groups</code> popup
   * @param event
   */
  changeGroup: function (event) {
    var host = this.get('content');
    var groupName = $('#' + this.get('elementId') + ' select').val();
    this.get('controller').changeHostGroup(host, groupName);
  },

  optionTag: Ember.View.extend({

    /**
     * Whether current value(OptionTag value) equals to host value(assigned to SlaveComponentDropDownGroupView.content)
     */
    selected: function () {
      return this.get('parentView.content.group') === this.get('content');
    }.property('content')
  })
});

/**
 * Show info about current group
 * @type {*}
 */
App.SlaveComponentChangeGroupNameView = Ember.View.extend({

  contentBinding: 'controller.activeGroup',
  classNames: ['control-group'],
  classNameBindings: 'error',
  error: false,
  setError: function () {
    this.set('error', false);
  }.observes('controller.activeGroup'),
  errorMessage: function () {
    return this.get('error') ? Em.I18n.t('installer.controls.slaveComponentChangeGroupName.error') : '';
  }.property('error'),

  /**
   * Onclick handler for saving updated group name
   * @param event
   */
  changeGroupName: function (event) {
    var inputVal = $('#' + this.get('elementId') + ' input[type="text"]').val();
    if (inputVal !== this.get('content.name')) {
      var result = this.get('controller').changeSlaveGroupName(this.get('content'), inputVal);
      this.set('error', result);
    }
  }
});

