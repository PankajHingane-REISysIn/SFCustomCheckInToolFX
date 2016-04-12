package com.customcheckin.service.salesforce.vo;

import com.force.service.vo.CustomVO;

public class EnvironmentVO extends CustomVO {
	private String Name;
	private Boolean Active__c = false;
	private String Project__c;
	private Boolean Sandbox__c = false;
	private Integer Sequence__c;
	private String Type__c;
	private String URL__c;
	private String ExternalId1__c;
	private String ExternalId2__c;
	private String GITBranchName__c;
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
	public String getExternalId1__c() {
		return ExternalId1__c;
	}
	public void setExternalId1__c(String externalId1__c) {
		ExternalId1__c = externalId1__c;
	}
	public String getExternalId2__c() {
		return ExternalId2__c;
	}
	public void setExternalId2__c(String externalId2__c) {
		ExternalId2__c = externalId2__c;
	}
	public String getGITBranchName__c() {
		return GITBranchName__c;
	}
	public void setGITBranchName__c(String gITBranchName__c) {
		GITBranchName__c = gITBranchName__c;
	}
	
	
}
