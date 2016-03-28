package com.customcheckin.util;

import com.force.service.ForceDelegate;
import com.force.service.raw.ForceDelegateRaw;

public class SalesforceConnection {
	protected ForceDelegate gate;
	protected ForceDelegateRaw rawGate;
	
	protected void init(String username, String password) {
		Double apiVersion = PropertyManager.getInstance().getDouble("salesforce.api.version");
		if (apiVersion == null) {
			apiVersion = 32.0;
		}
		this.gate = ForceDelegate.login(username, password, false, apiVersion);
		this.rawGate = ForceDelegateRaw.login(username, password, false, apiVersion);
	}
	
	public String getSessionId() {
		return gate.getSessionId();
	}
	
	public ForceDelegate getForceDelegate() {
		return gate;
	}
	
	public ForceDelegateRaw getForceDelegateRaw() {
		return rawGate;
	}
}
