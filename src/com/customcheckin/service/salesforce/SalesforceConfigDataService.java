package com.customcheckin.service.salesforce;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.axis.message.MessageElement;
import org.apache.log4j.Logger;

import com.customcheckin.model.ConfigRecord;
import com.customcheckin.service.compare.CompareRecords;
import com.force.service.raw.ForceDelegateRaw;
import com.sforce.soap.partner.sobject.SObject;

import javafx.beans.property.SimpleStringProperty;

public class SalesforceConfigDataService {
	private List<String> objNameLst;
	private final String lastModifiedDateString;
	private static Logger log = Logger.getRootLogger();
	private final ForceDelegateRaw gateRaw = SalesforceDevConnection.getInstance().getForceDelegateRaw();
	//i think we can query again.
	private static Map<String, String[]> sobjToHeadeMap;
	private static Map<String, Map<String, String[]>> sobjNameToRecordsMap;
	private static Map<String, List<ConfigRecord>> sobjToRecordConfigList;
	
	public SalesforceConfigDataService(Calendar lastModifiedDate) {
		this.objNameLst = getConfigObjListFromPMO();
		sobjNameToRecordsMap = new HashMap<>();
		sobjToRecordConfigList = new HashMap<>();
		sobjToHeadeMap = new HashMap<>();
		lastModifiedDate.add(Calendar.DATE, 1);
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		lastModifiedDateString = format1.format(lastModifiedDate.getTime())+"T00:00:00.000Z";
	}
	
	public Map<String, SObject[]> getRecordsWithModifiedDate()
	        throws InterruptedException, ExecutionException {

	    //int threads = Runtime.getRuntime().availableProcessors();
	    //todo - hardcode to 10
	    ExecutorService service = Executors.newFixedThreadPool(10);

	    List<Future<Map<String, SObject[]>>> futures = new ArrayList<Future<Map<String, SObject[]>>>();
	    for (final String objName : objNameLst) {
	        Callable<Map<String, SObject[]>> callable = new Callable<Map<String, SObject[] >>() {
	            public Map<String, SObject[]> call() throws Exception {
	                // process your input here and compute the output
	                SalesforceConfigDataThread t = new SalesforceConfigDataThread(objName, lastModifiedDateString, gateRaw);
	        		log.info("objName==" + objName);
	                SObject[] records = t.getRecords();
	        		Map<String, SObject[]> sobjNameToRecordsMap = new HashMap<>();
	        		sobjNameToRecordsMap.put(objName, records);
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
	    log.info("sobjNameToRecordsMap.keySet().size()====" + sobjNameToRecordsMap.keySet().size());
	    return sobjNameToRecordsMap;
	}
	
	public String[] getColumns(SObject[] sobjList) {
		List<String> apiArray = new ArrayList<>();
		if(sobjList != null)
		for(MessageElement msgEle : sobjList[0].get_any()) {
			if(msgEle.getName().endsWith("__c") || msgEle.getName().equalsIgnoreCase("Name"))
				apiArray.add(msgEle.getName());
		}
		String[] colArr = apiArray.toArray(new String[apiArray.size()]);
		return colArr;
	}
	
	public Map<String, String[]> getRecordsByInternalId(String objAPIName, SObject[] sobjList) {
		Map<String, String[]> internalIdByRecords = new HashMap<>();
		if(sobjList != null)
		for(SObject sobj : sobjList) {
			String uniqueIdVal = "";
			List<String> dataArray = new ArrayList<>();
			for(MessageElement msgEle : sobj.get_any()) {
				if(msgEle.getName().endsWith("__c") || msgEle.getName().equalsIgnoreCase("Name")) {
					if(msgEle.getName().equalsIgnoreCase("GGDemo2__InternalUniqueID__c")) {
						uniqueIdVal = msgEle.getValue();
					}
					dataArray.add(msgEle.getValue() == null ? "" : msgEle.getValue());
				}
			}
			internalIdByRecords.put(uniqueIdVal, dataArray.toArray(new String[dataArray.size()]));
		}
		return internalIdByRecords;
	}
	
	public static List<ConfigRecord> getConfigRecordList(String objName) {
		return sobjToRecordConfigList.get(objName);
	}
	
	public Map<String, Map<String, String[]>> getRecordsWithDifference() throws InterruptedException, ExecutionException, IOException {
		Map<String, SObject[]> recordByObjName = getRecordsWithModifiedDate();
		Map<String, Map<String, String[]>> sobjNameToUniqueIdToData = new HashMap<>();
		for(String objName : recordByObjName.keySet()) {
			CompareRecords compareRecord = new CompareRecords(objName);
			sobjToHeadeMap.put(objName, compareRecord.getFileHeaders());
			sobjNameToUniqueIdToData.put(objName,
					compareRecord.readRecordsFromLocalGITRepoAndCompare(getRecordsByInternalId(objName, recordByObjName.get(objName)), 
												getColumns(recordByObjName.get(objName))));
		}
		return sobjNameToUniqueIdToData;
	}
	
	public static List<String> getConfigDataList(Calendar cal) throws InterruptedException, ExecutionException, IOException {
		SalesforceConfigDataService sfConfigService = new SalesforceConfigDataService(cal);
		//sfConfigService.processInputs();
		try {
			sobjNameToRecordsMap = sfConfigService.getRecordsWithDifference();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//log.info("sobjNameToRecordsMap======" + sobjNameToRecordsMap.size());
		List<String> objLstToReturn = new ArrayList<>();
		for(String objName : sobjNameToRecordsMap.keySet()) {
			log.info("obj Name:" + sobjNameToRecordsMap.get(objName));
			if(sobjNameToRecordsMap.get(objName) != null && sobjNameToRecordsMap.get(objName).keySet().size() > 0) {
				log.info("sobjNameToRecordsMap.get(str).size()===" + sobjNameToRecordsMap.get(objName).keySet().size());
				objLstToReturn.add(objName);
				if(!sobjToRecordConfigList.containsKey(objName)) {
					sobjToRecordConfigList.put(objName, new ArrayList<>());
				}
				for(String[] obj : sobjNameToRecordsMap.get(objName).values()) {
					// todo read config index
					ConfigRecord configRec = new ConfigRecord( new SimpleStringProperty("test") , new SimpleStringProperty("test1"),
							new SimpleStringProperty(obj[2]), new SimpleStringProperty(obj[3]));
					sobjToRecordConfigList.get(objName).add(configRec);
				}
			}
		}
		return objLstToReturn;
	}
	
	/*public static List<SObject> getRecords(String objName) {
		return Arrays.asList(sobjNameToRecordsMap.get(objName));
	}*/
	
	public static void readConfigFile(String fileURL) {
		
	}
	
	private List<String> getConfigObjListFromPMO() {
		//todo - read from SF
		List<String> objList = new ArrayList<>();
		objList.add("GGDemo2__DataTableConfig__c");
		//objList.add("GGDemo2__AccordionContent__c");
		//objList.add("GGDemo2__CustomApp__c");
		//objList.add("GGDemo2__ChartConfig__c");
		//objList.add("GGDemo2__DataTableConfig__c");
		/*objList.add("GGDemo2__FlexGridConfig__c");
		objList.add("GGDemo2__ErrorMessageConfig__c");
		objList.add("GGDemo2__PageAttachmentConfig__c");
		objList.add("GGDemo2__PageBlockConfig__c");
		objList.add("GGDemo2__PhaseConfig__c");
		objList.add("GGDemo2__SObjectLayoutConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__AccordionContent__c");
		objList.add("GGDemo2__CustomApp__c");
		objList.add("GGDemo2__ChartConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__FlexGridConfig__c");
		objList.add("GGDemo2__ErrorMessageConfig__c");
		objList.add("GGDemo2__PageAttachmentConfig__c");
		objList.add("GGDemo2__PageBlockConfig__c");
		objList.add("GGDemo2__PhaseConfig__c");
		objList.add("GGDemo2__SObjectLayoutConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__AccordionContent__c");
		objList.add("GGDemo2__CustomApp__c");
		objList.add("GGDemo2__ChartConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__FlexGridConfig__c");
		objList.add("GGDemo2__ErrorMessageConfig__c");
		objList.add("GGDemo2__PageAttachmentConfig__c");
		objList.add("GGDemo2__PageBlockConfig__c");
		objList.add("GGDemo2__PhaseConfig__c");
		objList.add("GGDemo2__SObjectLayoutConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__AccordionContent__c");
		objList.add("GGDemo2__CustomApp__c");
		objList.add("GGDemo2__ChartConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__FlexGridConfig__c");
		objList.add("GGDemo2__ErrorMessageConfig__c");
		objList.add("GGDemo2__PageAttachmentConfig__c");
		objList.add("GGDemo2__PageBlockConfig__c");
		objList.add("GGDemo2__PhaseConfig__c");
		objList.add("GGDemo2__SObjectLayoutConfig__c");
		objList.add("GGDemo2__PageAttachmentConfig__c");
		objList.add("GGDemo2__PageBlockConfig__c");
		objList.add("GGDemo2__PhaseConfig__c");
		objList.add("GGDemo2__SObjectLayoutConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__AccordionContent__c");
		objList.add("GGDemo2__CustomApp__c");
		objList.add("GGDemo2__ChartConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__FlexGridConfig__c");
		objList.add("GGDemo2__ErrorMessageConfig__c");
		objList.add("GGDemo2__PageAttachmentConfig__c");
		objList.add("GGDemo2__PageBlockConfig__c");
		objList.add("GGDemo2__PhaseConfig__c");
		objList.add("GGDemo2__SObjectLayoutConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__AccordionContent__c");
		objList.add("GGDemo2__CustomApp__c");
		objList.add("GGDemo2__ChartConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__FlexGridConfig__c");
		objList.add("GGDemo2__ErrorMessageConfig__c");
		objList.add("GGDemo2__PageAttachmentConfig__c");
		objList.add("GGDemo2__PageBlockConfig__c");
		objList.add("GGDemo2__PhaseConfig__c");
		objList.add("GGDemo2__SObjectLayoutConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__AccordionContent__c");
		objList.add("GGDemo2__CustomApp__c");
		objList.add("GGDemo2__ChartConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__FlexGridConfig__c");
		objList.add("GGDemo2__ErrorMessageConfig__c");
		objList.add("GGDemo2__PageAttachmentConfig__c");
		objList.add("GGDemo2__PageBlockConfig__c");
		objList.add("GGDemo2__PhaseConfig__c");
		objList.add("GGDemo2__SObjectLayoutConfig__c");
		objList.add("GGDemo2__PageAttachmentConfig__c");
		objList.add("GGDemo2__PageBlockConfig__c");
		objList.add("GGDemo2__PhaseConfig__c");
		objList.add("GGDemo2__SObjectLayoutConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__AccordionContent__c");
		objList.add("GGDemo2__CustomApp__c");
		objList.add("GGDemo2__ChartConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__FlexGridConfig__c");
		objList.add("GGDemo2__ErrorMessageConfig__c");
		objList.add("GGDemo2__PageAttachmentConfig__c");
		objList.add("GGDemo2__PageBlockConfig__c");
		objList.add("GGDemo2__PhaseConfig__c");
		objList.add("GGDemo2__SObjectLayoutConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__AccordionContent__c");
		objList.add("GGDemo2__CustomApp__c");
		objList.add("GGDemo2__ChartConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__FlexGridConfig__c");
		objList.add("GGDemo2__ErrorMessageConfig__c");
		objList.add("GGDemo2__PageAttachmentConfig__c");
		objList.add("GGDemo2__PageBlockConfig__c");
		objList.add("GGDemo2__PhaseConfig__c");
		objList.add("GGDemo2__SObjectLayoutConfig__c");
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__AccordionContent__c");
		objList.add("GGDemo2__CustomApp__c");*/
		return objList;
	}
	
	public static void main(String str[]) throws InterruptedException, ExecutionException, IOException {
		Calendar calendar = Calendar.getInstance(); // this would default to now
		calendar.add(Calendar.DAY_OF_MONTH, -10);
		SalesforceConfigDataService.getConfigDataList(calendar);
	}
	

}
