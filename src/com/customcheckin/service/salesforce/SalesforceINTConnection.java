package com.customcheckin.service.salesforce;

import org.apache.log4j.Logger;

import com.customcheckin.service.salesforce.vo.EnvironmentUserVO;
import com.customcheckin.util.SalesforceConnection;
import com.lib.util.StringUtils;

public class SalesforceINTConnection extends SalesforceConnection {
	private static SalesforceINTConnection instance;
	private static Logger log = Logger.getRootLogger();
	private EnvironmentUserVO intUserInfo;
	//todo - replace userName and pass instance with devUserInfo
	private String userName;
	private String pass;
	
	private SalesforceINTConnection() {
		intUserInfo = SalesforcePMOConnection.getInstance().getSalesforceIntUser();
		if (intUserInfo != null && StringUtils.isNonEmpty(intUserInfo.getName()) && StringUtils.isNonEmpty(intUserInfo.getPassword__c())) {
			userName = intUserInfo.getName();
			pass = intUserInfo.getPassword__c();
			init(intUserInfo.getName(), intUserInfo.getPassword__c());
		}
	}
	
	public static SalesforceINTConnection getInstance() {
		if (instance == null) {
			instance = new SalesforceINTConnection();
		}
		return instance;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		SalesforceINTConnection sd = SalesforceINTConnection.getInstance();

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
	
	public EnvironmentUserVO getIntUserInfo() {
		return intUserInfo;
	}
}
