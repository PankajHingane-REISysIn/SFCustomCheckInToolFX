package com.customcheckin.util;

import com.force.service.ForceDelegate;

public class SalesforceConnection {
	protected ForceDelegate gate;
	
	protected void init(String username, String password) {
		Double apiVersion = PropertyManager.getInstance().getDouble("salesforce.api.version");
		if (apiVersion == null) {
			apiVersion = 32.0;
		}
		this.gate = ForceDelegate.login(username, password, false, apiVersion);
	}
	
	public String getSessionId() {
		return gate.getSessionId();
	}
}
