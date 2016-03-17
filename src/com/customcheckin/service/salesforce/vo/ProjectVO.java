package com.customcheckin.service.salesforce.vo;

import com.force.service.vo.CustomVO;

public class ProjectVO extends CustomVO {
	private String Name;
	private Boolean EnableCustomCheckIn__c = false;
	private String Organization_ID__c;
	
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public Boolean getEnableCustomCheckIn__c() {
		return EnableCustomCheckIn__c;
	}
	public void setEnableCustomCheckIn__c(Boolean enableCustomCheckIn__c) {
		EnableCustomCheckIn__c = enableCustomCheckIn__c;
	}
	public String getOrganization_ID__c() {
		return Organization_ID__c;
	}
	public void setOrganization_ID__c(String organization_ID__c) {
		Organization_ID__c = organization_ID__c;
	}
	
	
}
