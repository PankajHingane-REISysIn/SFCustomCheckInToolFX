package com.customcheckin.service.salesforce.vo;

import com.force.service.vo.CustomVO;

public class UserVO extends CustomVO {
	private String Firstname;
	private String Lastname;
	private String email;
	private String username;
	private String CurrentProjectId__c;
	private String UserType__c;
	
	public String getUserType__c() {
		return UserType__c;
	}
	public void setUserType__c(String userType__c) {
		UserType__c = userType__c;
	}
	public String getCurrentProjectId__c() {
		return CurrentProjectId__c;
	}
	public void setCurrentProjectId__c(String currentProjectId__c) {
		CurrentProjectId__c = currentProjectId__c;
	}
	public String getFirstname() {
		return Firstname;
	}
	public void setFirstname(String firstname) {
		Firstname = firstname;
	}
	public String getLastname() {
		return Lastname;
	}
	public void setLastname(String lastname) {
		Lastname = lastname;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	
}
