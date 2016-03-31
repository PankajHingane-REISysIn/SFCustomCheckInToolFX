package com.customcheckin.service.salesforce;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

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
	private static Map<String, List<FileProperties>> filTypeToPropertyList;
	public static List<Thread> threadListToWait;
	private static Logger log = Logger.getRootLogger();
	
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
					if( lastCheckIndate.compareTo(fileProperty.getLastModifiedDate()) <= 0 ) {
						// relative file path to FileProperty.\
						log.info("fileProperty.getFileName():" + fileProperty.getFileName());
						log.info("fileProperty.getType():" + fileProperty.getType());
						fileURLToPropertyForCompare.put("src/"+fileProperty.getFileName(), fileProperty);
						if(!filTypeToPropertyList.containsKey(fileProperty.getType())) {
							filTypeToPropertyList.put(fileProperty.getType(), new ArrayList<>());
						}
						filTypeToPropertyList.get(fileProperty.getType()).add(fileProperty);
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
		filTypeToPropertyList = new HashMap<>();
		List<String> metadataTypes = new ArrayList<>();
		metadataTypes.add("CustomObject");
		metadataTypes.add("ApexClass");
		metadataTypes.add("ApexComponent");
		metadataTypes.add("ApexPage");
		metadataTypes.add("ApexTrigger");
		metadataTypes.add("CustomLabels");
		metadataTypes.add("CustomField");
		List<ListMetadataQuery[]> metadaQrList = getMetadataConnections(metadataTypes);
		for(ListMetadataQuery[] metadataQueryArr : metadaQrList) {
			Thread thread1 = new SalesforceMetadaProperties(metadataConnection, metadataQueryArr, cal);
			thread1.start();
			threadListToWait.add(thread1);
		}
		
	}
	
	public static Map<String, List<FileProperties>> getFileProperties() {
		return filTypeToPropertyList;
	}
	
	private static List<ListMetadataQuery[]> getMetadataConnections(List<String> types) {
		List<ListMetadataQuery[]> metadataListToReturn = new ArrayList<>();
		int count=0;
		List<ListMetadataQuery> metadataList = null;
		for(String metadataType : types) {
			if(count == 0) {
				metadataList = new ArrayList<>();
			}
			ListMetadataQuery metadataquery = new ListMetadataQuery();
			metadataquery.setType(metadataType);
			metadataList.add(metadataquery);
			count++;
			if(count == 2) {
				count = 0;
				ListMetadataQuery[] metaArr = new ListMetadataQuery[metadataList.size()];
				metaArr = metadataList.toArray(metaArr);
				metadataListToReturn.add(metaArr);
			}
		}
		return metadataListToReturn;
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
		Calendar cal = Calendar.getInstance();
		SalesforceMetadaProperties.getSFMetadataProperty(metadataConnection, cal);
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
