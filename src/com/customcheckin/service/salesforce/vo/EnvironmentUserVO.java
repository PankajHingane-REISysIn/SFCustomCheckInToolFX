package com.customcheckin.service.salesforce.vo;

import java.util.Date;

import com.force.service.vo.CustomVO;

public class EnvironmentUserVO extends CustomVO {
	private String Name;
	private String Environment__c;
	private EnvironmentVO Environment__r = new EnvironmentVO();
	private String LocalWorkspacePath__c;
	private Date LastCheckInDate__c;
	private String Password__c;
	private String PMOUser__c;
	private UserVO PMOUser__r = new UserVO();
	
	public UserVO getPMOUser__r() {
		return PMOUser__r;
	}
	public void setPMOUser__r(UserVO pMOUser__r) {
		PMOUser__r = pMOUser__r;
	}
	public EnvironmentVO getEnvironment__r() {
		return Environment__r;
	}
	public void setEnvironment__r(EnvironmentVO environment__r) {
		Environment__r = environment__r;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getEnvironment__c() {
		return Environment__c;
	}
	public void setEnvironment__c(String environment__c) {
		Environment__c = environment__c;
	}
	public String getLocalWorkspacePath__c() {
		return LocalWorkspacePath__c;
	}
	public void setLocalWorkspacePath__c(String localWorkspacePath__c) {
		LocalWorkspacePath__c = localWorkspacePath__c;
	}
	public Date getLastCheckInDate__c() {
		return LastCheckInDate__c;
	}
	public void setLastCheckInDate__c(Date lastCheckInDate__c) {
		LastCheckInDate__c = lastCheckInDate__c;
	}
	public String getPassword__c() {
		return Password__c;
	}
	public void setPassword__c(String password__c) {
		Password__c = password__c;
	}
	public String getPMOUser__c() {
		return PMOUser__c;
	}
	public void setPMOUser__c(String pMOUser__c) {
		PMOUser__c = pMOUser__c;
	}
	
	
}
