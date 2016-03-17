package com.customcheckin.service.salesforce.vo;

import com.force.service.vo.CustomVO;

public class EnvironmentVO extends CustomVO {
	private String Name;
	private Boolean Active__c = false;
	private Boolean DEVSalesforceOrg__c = false;
	private String Project__c;
	private Boolean Sandbox__c = false;
	private Integer Sequence__c;
	private String Type__c;
	private String URL__c;
	
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public Boolean getActive__c() {
		return Active__c;
	}
	public void setActive__c(Boolean active__c) {
		Active__c = active__c;
	}
	public Boolean getDEVSalesforceOrg__c() {
		return DEVSalesforceOrg__c;
	}
	public void setDEVSalesforceOrg__c(Boolean dEVSalesforceOrg__c) {
		DEVSalesforceOrg__c = dEVSalesforceOrg__c;
	}
	public String getProject__c() {
		return Project__c;
	}
	public void setProject__c(String project__c) {
		Project__c = project__c;
	}
	public Boolean getSandbox__c() {
		return Sandbox__c;
	}
	public void setSandbox__c(Boolean sandbox__c) {
		Sandbox__c = sandbox__c;
	}
	public Integer getSequence__c() {
		return Sequence__c;
	}
	public void setSequence__c(Integer sequence__c) {
		Sequence__c = sequence__c;
	}
	public String getType__c() {
		return Type__c;
	}
	public void setType__c(String type__c) {
		Type__c = type__c;
	}
	public String getURL__c() {
		return URL__c;
	}
	public void setURL__c(String uRL__c) {
		URL__c = uRL__c;
	}
	
	
}
