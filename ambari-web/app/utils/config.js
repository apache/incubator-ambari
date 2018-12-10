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
var stringUtils = require('utils/string_utils');

var serviceComponents = {};
var configGroupsByTag = [];
var globalPropertyToServicesMap = null;

App.config = Em.Object.create({
  /**
   * XML characters which should be escaped in values
   * http://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references#Predefined_entities_in_XML
   */
  xmlEscapeMap: {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': '&quot;',
    "'": '&apos;'
  },
  xmlUnEscapeMap: {
    "&amp;": "&",
    "&lt;": "<",
    "&gt;": ">",
    "&quot;": '"',
    "&apos;": "'"
  },
  
  /**
   * Since values end up in XML files (core-sit.xml, etc.), certain
   * XML sensitive characters should be escaped. If not we will have
   * an invalid XML document, and services will fail to start. 
   * 
   * Special characters in XML are defined at
   * http://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references#Predefined_entities_in_XML
   */
  escapeXMLCharacters: function(value) {
    var self = this;
    // To prevent double/triple replacing '&gt;' to '&gt;gt;' to '&gt;gt;gt', we need
    // to first unescape all XML chars, and then escape them again.
    var newValue = String(value).replace(/(&amp;|&lt;|&gt;|&quot;|&apos;)/g, function (s) {
      return self.xmlUnEscapeMap[s];
    });
    return String(newValue).replace(/[&<>"']/g, function (s) {
      return self.xmlEscapeMap[s];
    });
  },
  preDefinedServiceConfigs: function(){
    var configs = this.get('preDefinedGlobalProperties');
    var services = [];
    $.extend(true, [], require('data/service_configs')).forEach(function(service){
      service.configs = configs.filterProperty('serviceName', service.serviceName);
      services.push(service);
    });
    return services;
  }.property('preDefinedGlobalProperties'),
  configMapping: function() {
      if (App.get('isHadoop2Stack')) {
        return $.extend(true, [],require('data/HDP2/config_mapping'));
      }
    return $.extend(true, [],require('data/config_mapping'));
  }.property('App.isHadoop2Stack'),
  preDefinedGlobalProperties: function() {
    if (App.get('isHadoop2Stack')) {
      return $.extend(true, [], require('data/HDP2/global_properties').configProperties);
    }
    return $.extend(true, [], require('data/global_properties').configProperties);
  }.property('App.isHadoop2Stack'),
  preDefinedSiteProperties: function() {
    if (App.get('isHadoop2Stack')) {
      return $.extend(true, [], require('data/HDP2/site_properties').configProperties);
    }
    return $.extend(true, [], require('data/site_properties').configProperties);
  }.property('App.isHadoop2Stack'),
  preDefinedCustomConfigs: function () {
    if (App.get('isHadoop2Stack')) {
      return $.extend(true, [], require('data/HDP2/custom_configs'));
    }
    return $.extend(true, [], require('data/custom_configs'));
  }.property('App.isHadoop2Stack'),
  //categories which contain custom configs
  categoriesWithCustom: ['CapacityScheduler'],
  //configs with these filenames go to appropriate category not in Advanced
  customFileNames: function() {
    if (App.supports.capacitySchedulerUi) {
      if(App.get('isHadoop2Stack')){
        return ['capacity-scheduler.xml'];
      }
      return ['capacity-scheduler.xml', 'mapred-queue-acls.xml'];
    } else {
      return [];
    }
  }.property('App.isHadoop2Stack'),

  /**
   * Function should be used post-install as precondition check should not be done only after installer wizard
   * @param siteNames
   * @returns {Array}
   */
  getBySitename: function (siteNames) {
    var computedConfigs = this.get('configMapping').computed();
    var siteProperties = [];
    if (typeof siteNames === "string") {
      siteProperties = computedConfigs.filterProperty('filename', siteNames);
    } else if (siteNames instanceof Array) {
      siteNames.forEach(function (_siteName) {
        siteProperties = siteProperties.concat(computedConfigs.filterProperty('filename', _siteName));
      }, this);
    }
    return siteProperties;
  },


  /**
   * Cache of loaded configurations. This is useful in not loading
   * same configuration multiple times. It is populated in multiple
   * places.
   *
   * Example:
   * {
   *  'global_version1': {...},
   *  'global_version2': {...},
   *  'hdfs-site_version3': {...},
   * }
   */
  loadedConfigurationsCache: {},

  /**
   * Array of global "service/desired_tag/actual_tag" strings which
   * indicate different configurations. We cache these so that 
   * we dont have to recalculate if two tags are difference.
   */
  differentGlobalTagsCache:[],
  
  identifyCategory: function(config){
    var category = null;
    var serviceConfigMetaData = this.get('preDefinedServiceConfigs').findProperty('serviceName', config.serviceName);
    if (serviceConfigMetaData) {
      serviceConfigMetaData.configCategories.forEach(function (_category) {
        if (_category.siteFileNames && Array.isArray(_category.siteFileNames) && _category.siteFileNames.contains(config.filename)) {
          category = _category;
        }
      });
      category = (category == null) ? serviceConfigMetaData.configCategories.findProperty('siteFileName', config.filename) : category;
    }
    return category;
  },
  /**
   * additional handling for special properties such as
   * checkbox and digital which values with 'm' at the end
   * @param config
   */
  handleSpecialProperties: function(config){
    if (config.displayType === 'int' && /\d+m$/.test(config.value)) {
      config.value = config.value.slice(0, config.value.length - 1);
      config.defaultValue = config.value;
    }
    if (config.displayType === 'checkbox') {
      config.value = (config.value === 'true') ? config.defaultValue = true : config.defaultValue = false;
    }
  },
  /**
   * calculate config properties:
   * category, filename, isRequired, isUserProperty
   * @param config
   * @param isAdvanced
   * @param advancedConfigs
   */
  calculateConfigProperties: function(config, isAdvanced, advancedConfigs){
    if (!isAdvanced || this.get('customFileNames').contains(config.filename)) {
      var categoryMetaData = this.identifyCategory(config);
      if (categoryMetaData != null) {
        config.category = categoryMetaData.get('name');
        if(!isAdvanced) config.isUserProperty = true;
      }
    } else {
      config.category = config.category ? config.category : 'Advanced';
      config.description = isAdvanced && advancedConfigs.findProperty('name', config.name).description;
      config.filename = isAdvanced && advancedConfigs.findProperty('name', config.name).filename;
      config.isRequired = true;
    }
  },
  capacitySchedulerFilter: function () {
    var yarnRegex = /^yarn\.scheduler\.capacity\.root\.(?!unfunded)([a-z]([\_\-a-z0-9]{0,50}))\.(acl_administer_jobs|acl_submit_jobs|state|user-limit-factor|maximum-capacity|capacity)$/i;
    var self = this;
    if(App.get('isHadoop2Stack')){
      return function (_config) {
        return (yarnRegex.test(_config.name));
      }
    } else {
      return function (_config) {
        return (_config.name.indexOf('mapred.capacity-scheduler.queue.') !== -1) ||
          (/^mapred\.queue\.[a-z]([\_\-a-z0-9]{0,50})\.(acl-administer-jobs|acl-submit-job)$/i.test(_config.name));
      }
    }
  }.property('App.isHadoop2Stack'),
  /**
   * return:
   *   configs,
   *   globalConfigs,
   *   mappingConfigs
   *
   * @param configGroups
   * @param advancedConfigs
   * @param tags
   * @param serviceName
   * @return {object}
   */
  mergePreDefinedWithLoaded: function (configGroups, advancedConfigs, tags, serviceName) {
    var configs = [];
    var globalConfigs = [];
    var preDefinedConfigs = this.get('preDefinedGlobalProperties').concat(this.get('preDefinedSiteProperties'));
    var mappingConfigs = [];

    tags.forEach(function (_tag) {
      var isAdvanced = null;
      var properties = configGroups.filter(function (serviceConfigProperties) {
        return _tag.tagName === serviceConfigProperties.tag && _tag.siteName === serviceConfigProperties.type;
      });

      properties = (properties.length) ? properties.objectAt(0).properties : {};
      for (var index in properties) {
        var configsPropertyDef = preDefinedConfigs.findProperty('name', index) || null;
        var serviceConfigObj = App.ServiceConfig.create({
          name: index,
          value: properties[index],
          defaultValue: properties[index],
          filename: _tag.siteName + ".xml",
          isUserProperty: false,
          isOverridable: true,
          serviceName: serviceName,
          belongsToService: []
        });

        if (configsPropertyDef) {
          serviceConfigObj.displayType = configsPropertyDef.displayType;
          serviceConfigObj.isRequired = (configsPropertyDef.isRequired !== undefined) ? configsPropertyDef.isRequired : true;
          serviceConfigObj.isReconfigurable = (configsPropertyDef.isReconfigurable !== undefined) ? configsPropertyDef.isReconfigurable : true;
          serviceConfigObj.isVisible = (configsPropertyDef.isVisible !== undefined) ? configsPropertyDef.isVisible : true;
          serviceConfigObj.unit = (configsPropertyDef.unit !== undefined) ? configsPropertyDef.unit : undefined;
          serviceConfigObj.description = (configsPropertyDef.description !== undefined) ? configsPropertyDef.description : undefined;
          serviceConfigObj.isOverridable = configsPropertyDef.isOverridable === undefined ? true : configsPropertyDef.isOverridable;
          serviceConfigObj.serviceName = configsPropertyDef ? configsPropertyDef.serviceName : null;
          serviceConfigObj.index = configsPropertyDef.index;
          serviceConfigObj.isSecureConfig = configsPropertyDef.isSecureConfig === undefined ? false : configsPropertyDef.isSecureConfig;
          serviceConfigObj.belongsToService = configsPropertyDef.belongsToService;
          serviceConfigObj.category = configsPropertyDef.category;
        }
        if (_tag.siteName === 'global') {
          if (configsPropertyDef) {
            this.handleSpecialProperties(serviceConfigObj);
          } else {
            serviceConfigObj.isVisible = false;  // if the global property is not defined on ui metadata global_properties.js then it shouldn't be a part of errorCount
          }
          serviceConfigObj.id = 'puppet var';
          serviceConfigObj.displayName = configsPropertyDef ? configsPropertyDef.displayName : null;
          serviceConfigObj.options = configsPropertyDef ? configsPropertyDef.options : null;
          globalConfigs.push(serviceConfigObj);
        } else if (!this.getBySitename(serviceConfigObj.get('filename')).someProperty('name', index)) {
          isAdvanced = advancedConfigs.someProperty('name', index);
          serviceConfigObj.id = 'site property';
          if (!configsPropertyDef) {
            serviceConfigObj.displayType = stringUtils.isSingleLine(serviceConfigObj.value) ? 'advanced' : 'multiLine';
          }
          serviceConfigObj.displayName = configsPropertyDef ? configsPropertyDef.displayName : index;
          this.calculateConfigProperties(serviceConfigObj, isAdvanced, advancedConfigs);
          configs.push(serviceConfigObj);
        } else {
          mappingConfigs.push(serviceConfigObj);
        }
      }
    }, this);
    return {
      configs: configs,
      globalConfigs: globalConfigs,
      mappingConfigs: mappingConfigs
    }
  },
  /**
   * synchronize order of config properties with order, that on UI side
   * @param configSet
   * @return {Object}
   */
  syncOrderWithPredefined: function(configSet){
    var globalConfigs = configSet.globalConfigs,
        siteConfigs = configSet.configs,
        globalStart = [],
        siteStart = [];

    this.get('preDefinedGlobalProperties').mapProperty('name').forEach(function(name){
      var _global = globalConfigs.findProperty('name', name);
      if(_global){
        globalStart.push(_global);
        globalConfigs = globalConfigs.without(_global);
      }
    }, this);

    this.get('preDefinedSiteProperties').mapProperty('name').forEach(function(name){
      var _site = siteConfigs.findProperty('name', name);
      if(_site){
        siteStart.push(_site);
        siteConfigs = siteConfigs.without(_site);
      }
    }, this);

    var alphabeticalSort = function(a, b){
      if (a.name < b.name) return -1;
      if (a.name > b.name) return 1;
      return 0;
    }


    return {
      globalConfigs: globalStart.concat(globalConfigs.sort(alphabeticalSort)),
      configs: siteStart.concat(siteConfigs.sort(alphabeticalSort)),
      mappingConfigs: configSet.mappingConfigs
    }
  },

  /**
   * merge stored configs with pre-defined
   * @param storedConfigs
   * @param advancedConfigs
   * @return {*}
   */
  mergePreDefinedWithStored: function (storedConfigs, advancedConfigs) {
    var mergedConfigs = [];
    var preDefinedConfigs = $.extend(true, [], this.get('preDefinedGlobalProperties').concat(this.get('preDefinedSiteProperties')));
    var preDefinedNames = [];
    var storedNames = [];
    var names = [];
    var categoryMetaData = null;
    storedConfigs = (storedConfigs) ? storedConfigs : [];

    preDefinedNames = preDefinedConfigs.mapProperty('name');
    storedNames = storedConfigs.mapProperty('name');
    names = preDefinedNames.concat(storedNames).uniq();
    names.forEach(function (name) {
      var stored = storedConfigs.findProperty('name', name);
      var preDefined = preDefinedConfigs.findProperty('name', name);
      var configData = {};
      var isAdvanced = advancedConfigs.someProperty('name', name);
      if (preDefined && stored) {
        configData = preDefined;
        configData.value = stored.value;
        configData.defaultValue = stored.defaultValue;
        configData.overrides = stored.overrides;
        configData.filename = stored.filename;
        configData.description = stored.description;
      } else if (!preDefined && stored) {

        configData = {
          id: stored.id,
          name: stored.name,
          displayName: stored.name,
          serviceName: stored.serviceName,
          value: stored.value,
          defaultValue: stored.defaultValue,
          displayType: stringUtils.isSingleLine(stored.value) ? 'advanced' : 'multiLine',
          filename: stored.filename,
          category: 'Advanced',
          isUserProperty: stored.isUserProperty === true,
          isOverridable: true,
          overrides: stored.overrides,
          isRequired: true
        };
        this.calculateConfigProperties(configData, isAdvanced, advancedConfigs);
      } else if (preDefined && !stored) {
        configData = preDefined;
        if (isAdvanced) {
          var advanced = advancedConfigs.findProperty('name', configData.name);
          configData.value = advanced.value;
          configData.defaultValue = advanced.value;
          configData.filename = advanced.filename;
          configData.description = advanced.description;
        }
      }
      mergedConfigs.push(configData);
    }, this);
    return mergedConfigs;
  },
  /**
   * look over advanced configs and add missing configs to serviceConfigs
   * filter fetched configs by service if passed
   * @param serviceConfigs
   * @param advancedConfigs
   * @param serviceName
   */
  addAdvancedConfigs: function (serviceConfigs, advancedConfigs, serviceName) {
    var configsToVerifying = (serviceName) ? serviceConfigs.filterProperty('serviceName', serviceName) : serviceConfigs;
    advancedConfigs.forEach(function (_config) {
      var configCategory = 'Advanced';
      var categoryMetaData = null;
      if (_config) {
        if (this.get('configMapping').computed().someProperty('name', _config.name)) {
        } else if (!(configsToVerifying.someProperty('name', _config.name))) {
          if(this.get('customFileNames').contains(_config.filename)){
            categoryMetaData = this.identifyCategory(_config);
            if (categoryMetaData != null) {
              configCategory = categoryMetaData.get('name');
            }
          }
          _config.id = "site property";
          _config.category = configCategory;
          _config.displayName = _config.name;
          _config.defaultValue = _config.value;
          // make all advanced configs optional and populated by default
          /*
           * if (/\${.*}/.test(_config.value) || (service.serviceName !==
           * 'OOZIE' && service.serviceName !== 'HBASE')) { _config.isRequired =
           * false; _config.value = ''; } else if
           * (/^\s+$/.test(_config.value)) { _config.isRequired = false; }
           */
          _config.isRequired = true;
          _config.displayType = stringUtils.isSingleLine(_config.value) ? 'advanced' : 'multiLine';
          serviceConfigs.push(_config);
        }
      }
    }, this);
  },
  /**
   * Render a custom conf-site box for entering properties that will be written in *-site.xml files of the services
   */
  addCustomConfigs: function (configs) {
    var preDefinedCustomConfigs = $.extend(true, [], this.get('preDefinedCustomConfigs'));
    var stored = configs.filter(function (_config) {
      return this.get('categoriesWithCustom').contains(_config.category);
    }, this);
    if(App.supports.capacitySchedulerUi){
      var queueProperties = stored.filter(this.get('capacitySchedulerFilter'));
      if (queueProperties.length) {
        queueProperties.setEach('isQueue', true);
      }
    }
  },

  miscConfigVisibleProperty: function (configs, serviceToShow) {
    configs.forEach(function(item) {
      item.set("isVisible", item.belongsToService.some(function(cur){return serviceToShow.contains(cur)}));
    });
    return configs;
  },

  /**
   * render configs, distribute them by service
   * and wrap each in ServiceConfigProperty object
   * @param configs
   * @param storedConfigs
   * @param allInstalledServiceNames
   * @param selectedServiceNames
   * @param localDB
   * @return {Array}
   */
  renderConfigs: function (configs, storedConfigs, allInstalledServiceNames, selectedServiceNames) {
    var renderedServiceConfigs = [];
    var localDB = {
      hosts: App.db.getHosts(),
      masterComponentHosts: App.db.getMasterComponentHosts(),
      slaveComponentHosts: App.db.getSlaveComponentHosts()
    };
    var services = [];

    this.get('preDefinedServiceConfigs').forEach(function (serviceConfig) {
      if (allInstalledServiceNames.contains(serviceConfig.serviceName) || serviceConfig.serviceName === 'MISC') {
        console.log('pushing ' + serviceConfig.serviceName, serviceConfig);
        if (selectedServiceNames.contains(serviceConfig.serviceName) || serviceConfig.serviceName === 'MISC') {
          serviceConfig.showConfig = true;
        }
        services.push(serviceConfig);
      }
    });
    services.forEach(function (service) {
      var serviceConfig = {};
      var configsByService = [];
      var serviceConfigs = configs.filterProperty('serviceName', service.serviceName);
      serviceConfigs.forEach(function (_config) {
        var serviceConfigProperty = {};
        _config.isOverridable = (_config.isOverridable === undefined) ? true : _config.isOverridable;
        serviceConfigProperty = App.ServiceConfigProperty.create(_config);
        this.updateHostOverrides(serviceConfigProperty, _config);
        if (!storedConfigs) {
          serviceConfigProperty.initialValue(localDB);
        }
        this.tweakDynamicDefaults(localDB, serviceConfigProperty, _config);
        serviceConfigProperty.validate();
        configsByService.pushObject(serviceConfigProperty);
      }, this);
      serviceConfig = this.createServiceConfig(service.serviceName);
      serviceConfig.set('showConfig', service.showConfig);

      // Use calculated default values for some configs
      var recommendedDefaults = {};
      if (!storedConfigs && service.defaultsProviders) {
        service.defaultsProviders.forEach(function(defaultsProvider) {
          var defaults = defaultsProvider.getDefaults(localDB);
          for(var name in defaults) {
        	recommendedDefaults[name] = defaults[name];
            var config = configsByService.findProperty('name', name);
            if (config) {
              config.set('value', defaults[name]);
              config.set('defaultValue', defaults[name]);
            }
          }
        });
      }
      if (service.configsValidator) {
    	service.configsValidator.set('recommendedDefaults', recommendedDefaults);
    	var validators = service.configsValidator.get('configValidators');
    	for (var validatorName in validators) {
        var c = configsByService.findProperty('name', validatorName);
          if (c) {
            c.set('serviceValidator', service.configsValidator);
          }
        }
      }

      serviceConfig.set('configs', configsByService);
      renderedServiceConfigs.push(serviceConfig);
    }, this);
    return renderedServiceConfigs;
  },
  /**
  Takes care of the "dynamic defaults" for the HCFS configs.  Sets
  some of the config defaults to previously user-entered data.
  **/
  tweakDynamicDefaults: function (localDB, serviceConfigProperty, config) {
    console.log("Step7: Tweaking Dynamic defaults");
    var firstHost = null;
    for(var host in localDB.hosts) {
      firstHost = host;
      break;
    }
    try {
      if (typeof(config == "string") && config.defaultValue.indexOf("{firstHost}") >= 0) {
        serviceConfigProperty.set('value', serviceConfigProperty.value.replace(new RegExp("{firstHost}"), firstHost));
        serviceConfigProperty.set('defaultValue', serviceConfigProperty.defaultValue.replace(new RegExp("{firstHost}"), firstHost));
      }
    } catch (err) {
      // Nothing to worry about here, most likely trying indexOf on a non-string
    }
  },
  /**
   * create new child configs from overrides, attach them to parent config
   * override - value of config, related to particular host(s)
   * @param configProperty
   * @param storedConfigProperty
   */
  updateHostOverrides: function (configProperty, storedConfigProperty) {
    if (storedConfigProperty.overrides != null && storedConfigProperty.overrides.length > 0) {
      var overrides = [];
      storedConfigProperty.overrides.forEach(function (overrideEntry) {
        // create new override with new value
        var newSCP = App.ServiceConfigProperty.create(configProperty);
        newSCP.set('value', overrideEntry.value);
        newSCP.set('isOriginalSCP', false); // indicated this is overridden value,
        newSCP.set('parentSCP', configProperty);
        var hostsArray = Ember.A([]);
        overrideEntry.hosts.forEach(function (host) {
          hostsArray.push(host);
        });
        newSCP.set('selectedHostOptions', hostsArray);
        overrides.pushObject(newSCP);
      });
      configProperty.set('overrides', overrides);
    }
  },
  /**
   * create new ServiceConfig object by service name
   * @param serviceName
   */
  createServiceConfig: function (serviceName) {
    var preDefinedServiceConfig = App.config.get('preDefinedServiceConfigs').findProperty('serviceName', serviceName);
    var serviceConfig = App.ServiceConfig.create({
      filename: preDefinedServiceConfig.filename,
      serviceName: preDefinedServiceConfig.serviceName,
      displayName: preDefinedServiceConfig.displayName,
      configCategories: preDefinedServiceConfig.configCategories,
      configs: []
    });
    serviceConfig.configCategories.filterProperty('isCustomView', true).forEach(function (category) {
      switch (category.name) {
        case 'CapacityScheduler':
          if(App.supports.capacitySchedulerUi){
            category.set('customView', App.ServiceConfigCapacityScheduler);
          } else {
            category.set('isCustomView', false);
          }
          break;
      }
    }, this);
    return serviceConfig;
  },
  /**
   * GETs all cluster level sites in one call.
   *
   * @return Array of all site configs
   */
  loadConfigsByTags: function (tags) {
    var urlParams = [];
    tags.forEach(function (_tag) {
      urlParams.push('(type=' + _tag.siteName + '&tag=' + _tag.tagName + ')');
    });
    var params = urlParams.join('|');
    App.ajax.send({
      name: 'config.on_site',
      sender: this,
      data: {
        params: params
      },
      success: 'loadConfigsByTagsSuccess'
    });
    return configGroupsByTag;
  },

  loadConfigsByTagsSuccess: function (data) {
    if (data.items) {
      configGroupsByTag = [];
      data.items.forEach(function (item) {
        this.loadedConfigurationsCache[item.type + "_" + item.tag] = item.properties;
        configGroupsByTag.push(item);
      }, this);
    }
  },
  /**
   * Generate serviceProperties save it to localDB
   * called form stepController step6WizardController
   *
   * @param serviceName
   * @return {*}
   */
  loadAdvancedConfig: function (serviceName) {
    App.ajax.send({
      name: 'config.advanced',
      sender: this,
      data: {
        serviceName: serviceName,
        stack2VersionUrl: App.get('stack2VersionURL'),
        stackVersion: App.get('currentStackVersionNumber')
      },
      success: 'loadAdvancedConfigSuccess'
    });
    return serviceComponents[serviceName];
    //TODO clean serviceComponents
  },

  loadAdvancedConfigSuccess: function (data, opt, params) {
    console.log("TRACE: In success function for the loadAdvancedConfig; url is ", opt.url);
    var properties = [];
    if (data.items.length) {
      data.items.forEach(function (item) {
        item = item.StackConfigurations;
        item.isVisible = item.type !== 'global.xml';
        properties.push({
          serviceName: item.service_name,
          name: item.property_name,
          value: item.property_value,
          description: item.property_description,
          isVisible: item.isVisible,
          filename: item.filename || item.type
        });
      }, this);
      serviceComponents[data.items[0].StackConfigurations.service_name] = properties;
    }
  },

  /**
   * Determine the map which shows which services
   * each global property effects.
   *
   * @return {*}
   * Example:
   * {
   *  'hive_pid_dir': ['HIVE'],
   *  ...
   * }
   */
  loadGlobalPropertyToServicesMap: function () {
    if (globalPropertyToServicesMap == null) {
      App.ajax.send({
        name: 'config.advanced.global',
        sender: this,
        data: {
          stack2VersionUrl: App.get('stack2VersionURL')
        },
        success: 'loadGlobalPropertyToServicesMapSuccess'
      });
    }
    return globalPropertyToServicesMap;
  },
  
  loadGlobalPropertyToServicesMapSuccess: function (data) {
    globalPropertyToServicesMap = {};
    if(data.items!=null){
      data.items.forEach(function(service){
        service.configurations.forEach(function(config){
          if("global.xml" === config.StackConfigurations.type){
            if(!(config.StackConfigurations.property_name in globalPropertyToServicesMap)){
              globalPropertyToServicesMap[config.StackConfigurations.property_name] = [];
            }
            globalPropertyToServicesMap[config.StackConfigurations.property_name].push(service.StackServices.service_name);
          }
        });
      });
    }
  },
  
  /**
   * When global configuration changes, not all services are effected
   * by all properties. This method determines if a given service
   * is effected by the difference in desired and actual configs.
   * 
   * This method might make a call to server to determine the actual
   * key/value pairs involved.
   */
  isServiceEffectedByGlobalChange: function (service, desiredTag, actualTag) {
    var effected = false;
    if (service != null && desiredTag != null && actualTag != null) {
      if(this.differentGlobalTagsCache.indexOf(service+"/"+desiredTag+"/"+actualTag) < 0){
        this.loadGlobalPropertyToServicesMap();
        var desiredConfigs = this.loadedConfigurationsCache['global_' + desiredTag];
        var actualConfigs = this.loadedConfigurationsCache['global_' + actualTag];
        var requestTags = [];
        if (!desiredConfigs) {
          requestTags.push({
            siteName: 'global',
            tagName: desiredTag
          });
        }
        if (!actualConfigs) {
          requestTags.push({
            siteName: 'global',
            tagName: actualTag
          });
        }
        if (requestTags.length > 0) {
          this.loadConfigsByTags(requestTags);
          desiredConfigs = this.loadedConfigurationsCache['global_' + desiredTag];
          actualConfigs = this.loadedConfigurationsCache['global_' + actualTag];
        }
        if (desiredConfigs != null && actualConfigs != null) {
          for ( var property in desiredConfigs) {
            if (!effected) {
              var dpv = desiredConfigs[property];
              var apv = actualConfigs[property];
              if (dpv !== apv && globalPropertyToServicesMap[property] != null) {
                effected = globalPropertyToServicesMap[property].indexOf(service) > -1;
                if(effected){
                  this.differentGlobalTagsCache.push(service+"/"+desiredTag+"/"+actualTag);
                }
              }
            }
          }
        }
      }else{
        effected = true; // We already know they are different
      }
    }
    return effected;
  },

  /**
   * Hosts can override service configurations per property. This method GETs
   * the overriden configurations and sets only the changed properties into
   * the 'overrides' of serviceConfig.
   *
   *
   */
  loadServiceConfigHostsOverrides: function (serviceConfigs, loadedHostToOverrideSiteToTagMap) {
    var configKeyToConfigMap = {};
    serviceConfigs.forEach(function (item) {
      configKeyToConfigMap[item.name] = item;
    });
    var typeTagToHostMap = {};
    var urlParams = [];
    for (var hostname in loadedHostToOverrideSiteToTagMap) {
      var overrideTypeTags = loadedHostToOverrideSiteToTagMap[hostname];
      for (var type in overrideTypeTags) {
        var tag = overrideTypeTags[type];
        typeTagToHostMap[type + "///" + tag] = hostname;
        urlParams.push('(type=' + type + '&tag=' + tag + ')');
      }
    }
    var params = urlParams.join('|');
    if (urlParams.length) {
      App.ajax.send({
        name: 'config.host_overrides',
        sender: this,
        data: {
          params: params,
          configKeyToConfigMap: configKeyToConfigMap,
          typeTagToHostMap: typeTagToHostMap
        },
        success: 'loadServiceConfigHostsOverridesSuccess'
      });
    }
  },
  loadServiceConfigHostsOverridesSuccess: function (data, opt, params) {
    console.debug("loadServiceConfigHostsOverrides: Data=", data);
    data.items.forEach(function (config) {
      App.config.loadedConfigurationsCache[config.type + "_" + config.tag] = config.properties;
      var hostname = params.typeTagToHostMap[config.type + "///" + config.tag];
      var properties = config.properties;
      for (var prop in properties) {
        var serviceConfig = params.configKeyToConfigMap[prop];
        var hostOverrideValue = properties[prop];
        if (serviceConfig && serviceConfig.displayType === 'int') {
          if (/\d+m$/.test(hostOverrideValue)) {
            hostOverrideValue = hostOverrideValue.slice(0, hostOverrideValue.length - 1);
          }
        } else if (serviceConfig && serviceConfig.displayType === 'checkbox') {
          switch (hostOverrideValue) {
            case 'true':
              hostOverrideValue = true;
              break;
            case 'false':
              hostOverrideValue = false;
              break;
          }
        }
        if (serviceConfig) {
          // Value of this property is different for this host.
          var overrides = 'overrides';
          if (!(overrides in serviceConfig)) {
            serviceConfig.overrides = {};
          }
          if (!(hostOverrideValue in serviceConfig.overrides)) {
            serviceConfig.overrides[hostOverrideValue] = [];
          }
          console.log("loadServiceConfigHostsOverrides(): [" + hostname + "] OVERRODE(" + serviceConfig.name + "): " + serviceConfig.value + " -> " + hostOverrideValue);
          serviceConfig.overrides[hostOverrideValue].push(hostname);
        }
      }
    });
    console.log("loadServiceConfigHostsOverrides(): Finished loading.");
  },

  /**
   * Set all site property that are derived from other site-properties
   */
  setConfigValue: function (mappedConfigs, allConfigs, config, globalConfigs) {
    var globalValue;
    if (config.value == null) {
      return;
    }
    var fkValue = config.value.match(/<(foreignKey.*?)>/g);
    var fkName = config.name.match(/<(foreignKey.*?)>/g);
    var templateValue = config.value.match(/<(templateName.*?)>/g);
    if (fkValue) {
      fkValue.forEach(function (_fkValue) {
        var index = parseInt(_fkValue.match(/\[([\d]*)(?=\])/)[1]);
        if (mappedConfigs.someProperty('name', config.foreignKey[index])) {
          globalValue = mappedConfigs.findProperty('name', config.foreignKey[index]).value;
          config.value = config.value.replace(_fkValue, globalValue);
        } else if (allConfigs.someProperty('name', config.foreignKey[index])) {
          if (allConfigs.findProperty('name', config.foreignKey[index]).value === '') {
            globalValue = allConfigs.findProperty('name', config.foreignKey[index]).defaultValue;
          } else {
            globalValue = allConfigs.findProperty('name', config.foreignKey[index]).value;
          }
          config.value = config.value.replace(_fkValue, globalValue);
        }
      }, this);
    }

    // config._name - formatted name from original config name
    if (fkName) {
      fkName.forEach(function (_fkName) {
        var index = parseInt(_fkName.match(/\[([\d]*)(?=\])/)[1]);
        if (mappedConfigs.someProperty('name', config.foreignKey[index])) {
          globalValue = mappedConfigs.findProperty('name', config.foreignKey[index]).value;
          config._name = config.name.replace(_fkName, globalValue);
        } else if (allConfigs.someProperty('name', config.foreignKey[index])) {
          if (allConfigs.findProperty('name', config.foreignKey[index]).value === '') {
            globalValue = allConfigs.findProperty('name', config.foreignKey[index]).defaultValue;
          } else {
            globalValue = allConfigs.findProperty('name', config.foreignKey[index]).value;
          }
          config._name = config.name.replace(_fkName, globalValue);
        }
      }, this);
    }

    //For properties in the configMapping file having foreignKey and templateName properties.
    if (templateValue) {
      templateValue.forEach(function (_value) {
        var index = parseInt(_value.match(/\[([\d]*)(?=\])/)[1]);
        if (globalConfigs.someProperty('name', config.templateName[index])) {
          var globalValue = globalConfigs.findProperty('name', config.templateName[index]).value;
          config.value = config.value.replace(_value, globalValue);
        } else {
          config.value = null;
        }
      }, this);
    }
  },
  complexConfigs: [
    {
      "id": "site property",
      "name": "capacity-scheduler",
      "displayName": "Capacity Scheduler",
      "value": "",
      "defaultValue": "",
      "description": "Capacity Scheduler properties",
      "displayType": "custom",
      "isOverridable": true,
      "isRequired": true,
      "isVisible": true,
      "serviceName": "YARN",
      "filename": "capacity-scheduler.xml",
      "category": "CapacityScheduler"
    }
  ],

  /**
   * transform set of configs from file
   * into one config with textarea content:
   * name=value
   * @param configs
   * @param filename
   * @return {*}
   */
  fileConfigsIntoTextarea: function(configs, filename){
    var fileConfigs = configs.filterProperty('filename', filename);
    var value = '';
    var defaultValue = '';
    var complexConfig = this.get('complexConfigs').findProperty('filename', filename);
    if(complexConfig){
      fileConfigs.forEach(function(_config){
        value += _config.name + '=' + _config.value + '\n';
        defaultValue += _config.name + '=' + _config.defaultValue + '\n';
      }, this);
      complexConfig.value = value;
      complexConfig.defaultValue = defaultValue;
      configs = configs.filter(function(_config){
        return _config.filename !== filename;
      });
      configs.push(complexConfig);
    }
    return configs;
  },

  /**
   * transform one config with textarea content
   * into set of configs of file
   * @param configs
   * @param filename
   * @return {*}
   */
  textareaIntoFileConfigs: function(configs, filename){
    var complexConfigName = this.get('complexConfigs').findProperty('filename', filename).name;
    var configsTextarea = configs.findProperty('name', complexConfigName);
    if (configsTextarea) {
      var properties = configsTextarea.get('value').replace(/( |\n)+/g, '\n').split('\n');

      properties.forEach(function (_property) {
        var name, value;
        if (_property) {
          _property = _property.split('=');
          name = _property[0];
          value = (_property[1]) ? _property[1] : "";
          configs.push(Em.Object.create({
            id: configsTextarea.get('id'),
            name: name,
            value: value,
            defaultValue: value,
            serviceName: configsTextarea.get('serviceName'),
            filename: filename
          }));
        }
      });
      return configs.without(configsTextarea);
    }
    console.log('ERROR: textarea config - ' + complexConfigName + ' is missing');
    return configs;
  },

  /**
   * trim trailing spaces for all properties.
   * trim both trailing and leading spaces for host displayType and hive/oozie datebases url.
   * for directory or directories displayType format string for further using.
   * for password and values with spaces only do nothing.
   * @param property
   * @returns {*}
   */
  trimProperty: function(property, isEmberObject){
    var displayType = (isEmberObject) ? property.get('displayType') : property.displayType;
    var value = (isEmberObject) ? property.get('value') : property.value;
    var name = (isEmberObject) ? property.get('name') : property.name;
    var rez;
    switch (displayType){
      case 'directories':
      case 'directory':
        rez = value.trim().split(/\s+/g).join(',');
        break;
      case 'host':
        rez = value.trim();
        break;
      case 'password':
        break;
      case 'advanced':
        if(name == 'hive_jdbc_connection_url' || name == 'oozie_jdbc_connection_url') {
          rez = value.trim();
        }
      default:
        rez = (typeof value == 'string') ? value.replace(/(\s+$)/g, '') : value;
    }
    return ((rez == '') || (rez == undefined)) ? value : rez;
  },

  OnNnHAHideSnn: function(ServiceConfig) {
    var configCategories = ServiceConfig.get('configCategories');
    var snCategory = configCategories.findProperty('name', 'SNameNode');
    var activeNn = App.HDFSService.find('HDFS').get('activeNameNode.hostName');
    if (snCategory && activeNn) {
      configCategories.removeObject(snCategory);
    }
  }

});
