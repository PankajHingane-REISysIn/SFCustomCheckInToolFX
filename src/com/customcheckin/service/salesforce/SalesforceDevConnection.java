package com.customcheckin.service.salesforce;

import org.apache.log4j.Logger;

import com.customcheckin.service.salesforce.vo.EnvironmentUserVO;
import com.customcheckin.util.SalesforceConnection;
import com.lib.util.StringUtils;

public class SalesforceDevConnection extends SalesforceConnection {
	private static SalesforceDevConnection instance;
	private static Logger log = Logger.getRootLogger();
	private EnvironmentUserVO devUserInfo;
	//todo - replace userName and pass instance with devUserInfo
	private String userName;
	private String pass;
	
	private SalesforceDevConnection() {
		devUserInfo = SalesforcePMOConnection.getInstance().getSalesforceDevUser();
		if (devUserInfo != null && StringUtils.isNonEmpty(devUserInfo.getName()) && StringUtils.isNonEmpty(devUserInfo.getPassword__c())) {
			userName = devUserInfo.getName();
			pass = devUserInfo.getPassword__c();
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
		
		SalesforceDevConnection sd = SalesforceDevConnection.getInstance();

	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return the pass
	 */
	public String getPass() {
		return pass;
	}
	
	public EnvironmentUserVO getDevUserInfo() {
		return devUserInfo;
	}
}
