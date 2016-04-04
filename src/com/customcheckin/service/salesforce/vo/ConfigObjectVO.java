package com.customcheckin.service.salesforce.vo;

import com.force.service.vo.CustomVO;

public class ConfigObjectVO extends CustomVO {
	private String Name;
	private String ObjectType__c;
	private String CustomSettingType__c;
	private String InternalUniqueIdFieldName__c;
	
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getObjectType__c() {
		return ObjectType__c;
	}
	public void setObjectType__c(String objectType__c) {
		ObjectType__c = objectType__c;
	}
	public String getCustomSettingType__c() {
		return CustomSettingType__c;
	}
	public void setCustomSettingType__c(String customSettingType__c) {
		CustomSettingType__c = customSettingType__c;
	}
	public String getInternalUniqueIdFieldName__c() {
		return InternalUniqueIdFieldName__c;
	}
	public void setInternalUniqueIdFieldName__c(String internalUniqueIdFieldName__c) {
		InternalUniqueIdFieldName__c = internalUniqueIdFieldName__c;
	}
	
	
}
