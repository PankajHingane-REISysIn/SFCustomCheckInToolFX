package com.customcheckin.service.salesforce;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class SalesforceMetadaProperties extends Thread{
	private MetadataConnection metadataConnection;
	private ListMetadataQuery[] lstMetadataQuery;
	private Calendar lastCheckIndate;
	private Map<String, FileProperties> fileURLToProperty = new HashMap<>();
	public static Map<String, FileProperties> fileURLToPropertyForCompare;
	public static List<Thread> threadListToWait;
	
	public SalesforceMetadaProperties(MetadataConnection metadataConnection, ListMetadataQuery[] lstMetadataQuery, Calendar lastCheckIndate) {
		this.metadataConnection = metadataConnection;
		this.lstMetadataQuery = lstMetadataQuery;
		this.lastCheckIndate = lastCheckIndate;
	}
	
	public void run() {
		listMetadataWithFilter();
	}
	
	public void listMetadataWithFilter() {
		if(lastCheckIndate==null) {
			lastCheckIndate =  Calendar.getInstance();
		}
		try {
			// query.setFolder(null);
			double asOfVersion = 36.0;
			// Assuming that the SOAP binding has already been established.
			FileProperties[] lmr = metadataConnection.listMetadata(lstMetadataQuery, asOfVersion);
			if (lmr != null) {
				for (FileProperties fileProperty : lmr) {
					System.out.println("Component fullName: " + fileProperty.getFileName());
					System.out.println("Component type: " + fileProperty.getType());
					if( lastCheckIndate.compareTo(fileProperty.getLastModifiedDate()) < 0 ) {
						System.out.println("Component type: " + fileProperty.getLastModifiedDate());
						// relative file path to FileProperty.
						fileURLToPropertyForCompare.put("src/"+fileProperty.getFileName(), fileProperty);
					}
				}
			}
		} catch (ConnectionException ce) {
			ce.printStackTrace();
		}
	}
	
	public static void getSFMetadataProperty(MetadataConnection metadataConnection, Calendar cal) {
		fileURLToPropertyForCompare = new HashMap<>();
		threadListToWait = new ArrayList<>();
		ListMetadataQuery query = new ListMetadataQuery();
		query.setType("CustomObject");
		ListMetadataQuery query1 = new ListMetadataQuery();
		query1.setType("ApexClass");
		ListMetadataQuery[] lstmetadata = new ListMetadataQuery[] { query, query1 };
		Thread thread1 = new SalesforceMetadaProperties(metadataConnection, lstmetadata, cal);
		thread1.start();
		threadListToWait.add(thread1);
		
	}
	
	public static void main(String str[]) throws ConnectionException, InterruptedException {
		
		
		SalesforceDevConnection sfDev = SalesforceDevConnection.getInstance();
		final ConnectorConfig config = new ConnectorConfig();
		config.setAuthEndpoint("https://login.salesforce.com/services/Soap/c/36.0");
		config.setServiceEndpoint("https://login.salesforce.com/services/Soap/c/36.0");
		config.setManualLogin(true);

		EnterpriseConnection enter = new EnterpriseConnection(config);
		LoginResult loginRes = enter.login(sfDev.getUserName(), sfDev.getPass());
		final ConnectorConfig configWithSession = new ConnectorConfig();
		configWithSession.setServiceEndpoint(loginRes.getMetadataServerUrl());
		configWithSession.setSessionId(loginRes.getSessionId());

		MetadataConnection metadataConnection = new MetadataConnection(configWithSession);
		
		
		////////////////////////////////////////////
		ListMetadataQuery query = new ListMetadataQuery();
		query.setType("CustomObject");
		ListMetadataQuery query1 = new ListMetadataQuery();
		query1.setType("ApexClass");
		ListMetadataQuery[] lstmetadata = new ListMetadataQuery[] { query, query1 };
		//SalesforceMetadaProperties sfProperty = new SalesforceMetadaProperties(metadataConnection, lstmetadata, null);
		//sfProperty.listMetadataWithFilter();
		Thread fetchMetada = new SalesforceMetadaProperties(metadataConnection, lstmetadata, null);
		fetchMetada.start();
		fetchMetada.join();
		SalesforceMetadaProperties sfM = (SalesforceMetadaProperties)fetchMetada;
		System.out.println(sfM.getFileURLToProperty().keySet().size());
	}

	/**
	 * @return the fileURLToProperty
	 */
	public Map<String, FileProperties> getFileURLToProperty() {
		return fileURLToProperty;
	}

	/**
	 * @param fileURLToProperty the fileURLToProperty to set
	 */
	public void setFileURLToProperty(Map<String, FileProperties> fileURLToProperty) {
		this.fileURLToProperty = fileURLToProperty;
	}

}
