package com.customcheckin.service.salesforce;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.axis.message.MessageElement;
import org.apache.log4j.Logger;

import com.customcheckin.model.ConfigObject;
import com.customcheckin.model.ConfigRecord;
import com.customcheckin.service.compare.CompareRecords;
import com.customcheckin.service.salesforce.vo.ConfigObjectVO;
import com.force.service.raw.ForceDelegateRaw;
import com.lib.util.CSVUtils;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;

import javafx.beans.property.SimpleStringProperty;

public class SalesforceConfigDataService {
	private List<ConfigObjectVO> configObjList;
	private final String lastModifiedDateString;
	private static Logger log = Logger.getRootLogger();
	private final ForceDelegateRaw gateRaw = SalesforceDevConnection.getInstance().getForceDelegateRaw();
	//i think we can query again.
//	private static Map<String, String[]> sobjToHeadeMapFromFile;
	private static Map<String, String[]> sobjToHeadeMapFromOrg;
	private static Map<String, Map<String, String[]>> sobjNameToRecordsMapFromOrg;
	private static Map<String, Map<String, String[]>> sobjNameToRecordsMapFromOrgWithDiff;
	private static Map<String, List<ConfigRecord>> sobjToRecordConfigList;
	private static Map<String, ConfigObjectVO> configobjAPIToInstance;
	
	private static final Integer THREAD_SIZE = 10;
	private static final Set<String> standardFieldToInclude = new HashSet<String>();;
	
	public SalesforceConfigDataService(Calendar lastModifiedDate) {
		this.configObjList = ConfigObjects.getInstance().getCustomObjects();
		//this.configObjList.addAll(ConfigObjects.getInstance().getCustomSettings());
		sobjNameToRecordsMapFromOrgWithDiff = new HashMap<>();
		sobjNameToRecordsMapFromOrg = new HashMap<>();
		configobjAPIToInstance = new HashMap<String, ConfigObjectVO>();
		sobjToRecordConfigList = new HashMap<>();
		sobjNameToRecordsMapFromOrgWithDiff = new HashMap<>();
		sobjToHeadeMapFromOrg = new HashMap<>();
		standardFieldToInclude.add("Name");
		lastModifiedDate.add(Calendar.DATE, 1);
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		lastModifiedDateString = format1.format(lastModifiedDate.getTime())+"T00:00:00.000Z";
		getConfigObjMap();
	}
	
	public static Map<String, List<ConfigRecord>> getSobjToRecordConfigList() {
		return sobjToRecordConfigList;
	}
	
	public static String[] getRecordsMap(String objName, String uniqueId) {
		return sobjNameToRecordsMapFromOrg.get(objName).get(uniqueId);
	}
	
	public static String[] getRecordsMapWithDiff(String objName, String uniqueId) {
		return sobjNameToRecordsMapFromOrgWithDiff.get(objName).get(uniqueId);
	}
	
	public static String[] getSObjHeader(String sobjName) {
		return sobjToHeadeMapFromOrg.get(sobjName);
	}
	
	public static ConfigObjectVO getConfigObjectVO(String objAPIName) {
		return configobjAPIToInstance.get(objAPIName);
	}
	
	private void getConfigObjMap() {
		for(ConfigObjectVO configObj : configObjList) {
			configobjAPIToInstance.put(configObj.getObjectAPIName(), configObj);
		}
	}
	
	private Map<String, SObject[]> getRecordsWithModifiedDate()
	        throws InterruptedException, ExecutionException {
	    //int threads = Runtime.getRuntime().availableProcessors();
	    ExecutorService service = Executors.newFixedThreadPool(THREAD_SIZE);

	    List<Future<Map<String, SObject[]>>> futures = new ArrayList<Future<Map<String, SObject[]>>>();
	    for (final ConfigObjectVO configObject : configObjList) {
	        Callable<Map<String, SObject[]>> callable = new Callable<Map<String, SObject[] >>() {
	            public Map<String, SObject[]> call() throws Exception {
	                // process your input here and compute the output
	                SalesforceConfigDataThread t = new SalesforceConfigDataThread(configObject, lastModifiedDateString, gateRaw);
	        		log.info("objName==" + configObject);
	                SObject[] records = t.getRecords();
	        		Map<String, SObject[]> sobjNameToRecordsMap = new HashMap<>();
	        		sobjNameToRecordsMap.put(configObject.getObjectAPIName(), records);
	                return sobjNameToRecordsMap;
	            }
	        };
	        futures.add(service.submit(callable));
	    }
	    service.shutdown();

	    Map<String, SObject[]> sobjNameToRecordsMap = new HashMap<>();
	    for (Future<Map<String, SObject[]>> future : futures) {
	    	sobjNameToRecordsMap.putAll(future.get());
	    }
	    return sobjNameToRecordsMap;
	}
	
	private String[] getColumns(String objName, SObject[] sobjList) {
		List<String> apiArray = new ArrayList<>();
		if(sobjList != null)
		for(MessageElement msgEle : sobjList[0].get_any()) {
			if(msgEle.getName().endsWith("__c") || standardFieldToInclude.contains(msgEle.getName()))
				apiArray.add(msgEle.getName());
		}
		String[] colArr = apiArray.toArray(new String[apiArray.size()]);
		sobjToHeadeMapFromOrg.put(objName, colArr);
		return colArr;
	}
	
	private Map<String, String[]> getRecordsByInternalId(String objAPIName, SObject[] sobjList) throws Exception {
		Map<String, String[]> internalIdByRecords = new HashMap<>();
		if(sobjList != null)
		for(SObject sobj : sobjList) {
			String uniqueIdVal = "";
			List<String> dataArray = new ArrayList<>();
			for(MessageElement field : sobj.get_any()) {
				if(field.getName().endsWith("__c") || field.getName().endsWith("__r") || standardFieldToInclude.contains(field.getName())) {
					//relationship field unique id is always fetch after relationship field Id.
					if(field.getName().endsWith("__r")) {
						//SObject relationObj = msgEle.getType().getNamespaceURI();
						log.info("field.getType()==========" + field.getType());
						if(field.getType() != null) {
							Object subResult = field.getValueAsType(field.getType());
							if (subResult instanceof QueryResult) { //found subresult with multiple records
								QueryResult qr = (QueryResult) subResult;
								SObject[] records = qr.getRecords();
							} else if (subResult instanceof SObject) { //found subresult with just only one record
								log.info("got single object");
								for(MessageElement parentField : ((SObject)subResult).get_any()) {
									dataArray.set(dataArray.size()-1, parentField.getValue());
									log.info("parentField.getValue()========" + parentField.getValue());
								}
							}
							
						}
					} else {
						if(field.getName().equalsIgnoreCase(configobjAPIToInstance.get(objAPIName).getInternalUniqueIdFieldAPIName())) {
							uniqueIdVal = field.getValue();
						}
						dataArray.add(field.getValue() == null ? "" : field.getValue());
					}
				}
				log.info("msgEle.getName()======" + field.getType());
				log.info("msgEle.getName()======" + field.getName());
				log.info("msgEle.getValue()======" + field.getValue());
			}
			internalIdByRecords.put(uniqueIdVal, dataArray.toArray(new String[dataArray.size()]));
		}
		sobjNameToRecordsMapFromOrg.put(objAPIName, internalIdByRecords);
		return internalIdByRecords;
	}
	
	public static List<ConfigRecord> getConfigRecordList(String objName) {
		return sobjToRecordConfigList.get(objName);
	}
	
	private Map<String, Map<String, String[]>> getRecordsWithDifference() throws Exception {
		Map<String, SObject[]> recordByObjName = getRecordsWithModifiedDate();
		Map<String, Map<String, String[]>> sobjNameToUniqueIdToData = new HashMap<>();
		for(String objName : recordByObjName.keySet()) {
			CompareRecords compareRecord = new CompareRecords(configobjAPIToInstance.get(objName));
//			sobjToHeadeMapFromFile.put(objName, compareRecord.getFileHeaders());
			sobjNameToUniqueIdToData.put(objName,
					compareRecord.readRecordsFromLocalGITRepoAndCompare(getRecordsByInternalId(objName, recordByObjName.get(objName)), 
												getColumns(objName, recordByObjName.get(objName))));
		}
		return sobjNameToUniqueIdToData;
	}
	
	public static List<ConfigObject> getConfigDataList(Calendar cal) throws InterruptedException, ExecutionException, IOException {
		SalesforceConfigDataService sfConfigService = new SalesforceConfigDataService(cal);
		//sfConfigService.processInputs();
		try {
			sobjNameToRecordsMapFromOrgWithDiff = sfConfigService.getRecordsWithDifference();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//log.info("sobjNameToRecordsMap======" + sobjNameToRecordsMap.size());
		List<ConfigObject> objLstToReturn = new ArrayList<>();
		for(String objName : sobjNameToRecordsMapFromOrgWithDiff.keySet()) {
			log.info("obj Name:" +objName);
			ConfigObjectVO configObj = configobjAPIToInstance.get(objName);
			Integer nameIndex = CSVUtils.getIndex(sobjToHeadeMapFromOrg.get(objName), "Name");
			Integer uniqueValIndex = CSVUtils.getIndex(sobjToHeadeMapFromOrg.get(objName), configObj.getInternalUniqueIdFieldAPIName());
			Integer col1ValIndex = configObj.getConfigFieldList().size() < 1 ? -1 : 
							CSVUtils.getIndex(sobjToHeadeMapFromOrg.get(objName), configObj.getConfigFieldList().get(0));
			Integer col2ValIndex = configObj.getConfigFieldList().size() < 2 ? -1 : 
				CSVUtils.getIndex(sobjToHeadeMapFromOrg.get(objName), configObj.getConfigFieldList().get(1));
			Integer col3ValIndex = configObj.getConfigFieldList().size() < 3 ? -1 : 
				CSVUtils.getIndex(sobjToHeadeMapFromOrg.get(objName), configObj.getConfigFieldList().get(2));
			Integer col4ValIndex = configObj.getConfigFieldList().size() < 4 ? -1 : 
				CSVUtils.getIndex(sobjToHeadeMapFromOrg.get(objName), configObj.getConfigFieldList().get(3));
			if(sobjNameToRecordsMapFromOrgWithDiff.get(objName) != null && sobjNameToRecordsMapFromOrgWithDiff.get(objName).keySet().size() > 0) {
				log.info("sobjNameToRecordsMap.get(str).size()===" + sobjNameToRecordsMapFromOrgWithDiff.get(objName).keySet().size());
				objLstToReturn.add(new ConfigObject(configObj.getObjectLabel(), objName));
				if(!sobjToRecordConfigList.containsKey(objName)) {
					sobjToRecordConfigList.put(objName, new ArrayList<>());
				}
				for(String[] obj : sobjNameToRecordsMapFromOrgWithDiff.get(objName).values()) {
					log.info("obj[uniqueValIndex]======" + obj[uniqueValIndex]);
					ConfigRecord configRec = new ConfigRecord( new SimpleStringProperty(obj[nameIndex]) , new SimpleStringProperty(obj[uniqueValIndex]),
							new SimpleStringProperty(col1ValIndex==-1? "":obj[col1ValIndex]), new SimpleStringProperty(col2ValIndex==-1? "":obj[col2ValIndex]),
							new SimpleStringProperty(col3ValIndex==-1? "":obj[col3ValIndex]), new SimpleStringProperty(col4ValIndex==-1? "":obj[col4ValIndex]));
					sobjToRecordConfigList.get(objName).add(configRec);
				}
			}
		}
		return objLstToReturn;
	}
	
	public static void main(String str[]) throws InterruptedException, ExecutionException, IOException {
		Calendar calendar = Calendar.getInstance(); // this would default to now
		calendar.add(Calendar.DAY_OF_MONTH, -7);
		SalesforceConfigDataService.getConfigDataList(calendar);
	}
	

}
