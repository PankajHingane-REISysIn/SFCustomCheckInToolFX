package com.customcheckin.service.salesforce;

import com.customcheckin.service.salesforce.vo.EnvironmentUserVO;
import com.customcheckin.util.SalesforceConnection;
import com.lib.util.StringUtils;

public class SalesforceDevConnection extends SalesforceConnection {
	private static SalesforceDevConnection instance;
	
	private SalesforceDevConnection() {
		EnvironmentUserVO devUserInfo = SalesforcePMOConnection.getInstance().getSalesforceDevUser();
		if (devUserInfo != null && StringUtils.isNonEmpty(devUserInfo.getName()) && StringUtils.isNonEmpty(devUserInfo.getPassword__c())) {
			init(devUserInfo.getName(), devUserInfo.getPassword__c());
		}
	}
	
	public static SalesforceDevConnection getInstance() {
		if (instance == null) {
			instance = new SalesforceDevConnection();
		}
		return instance;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
