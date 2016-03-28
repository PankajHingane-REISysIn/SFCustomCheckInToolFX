package com.customcheckin.service.salesforce;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.force.service.ForceDelegate;
import com.force.service.raw.ForceDelegateRaw;
import com.sforce.soap.partner.sobject.SObject;

public class SalesforceConfigDataService {
	private List<String> objNameLst;
	private final Calendar lastModifiedDate;
	private final String lastModifiedDateString;
	private static Logger log = Logger.getRootLogger();
	private final ForceDelegateRaw gateRaw = SalesforceDevConnection.getInstance().getForceDelegateRaw();
	private static Map<String, SObject[]> sobjNameToRecordsMap;
	
	public SalesforceConfigDataService(Calendar lastModifiedDate) {
		this.objNameLst = getConfigObjListFromPMO();
		this.lastModifiedDate = lastModifiedDate;
		sobjNameToRecordsMap = new HashMap<>();
		lastModifiedDate.add(Calendar.DATE, 1);
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		lastModifiedDateString = format1.format(lastModifiedDate.getTime())+"T00:00:00.000Z";
	}
	
	public Map<String, SObject[]> getRecordsWithModifiedDate()
	        throws InterruptedException, ExecutionException {

	    int threads = Runtime.getRuntime().availableProcessors();
	    //todo - hardcode to 10
	    ExecutorService service = Executors.newFixedThreadPool(threads);

	    List<Future<Map<String, SObject[]>>> futures = new ArrayList<Future<Map<String, SObject[]>>>();
	    for (final String objName : objNameLst) {
	        Callable<Map<String, SObject[]>> callable = new Callable<Map<String, SObject[] >>() {
	            public Map<String, SObject[]> call() throws Exception {
	                // process your input here and compute the output
	                SalesforceConfigDataThread t = new SalesforceConfigDataThread(objName, lastModifiedDateString, gateRaw);
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
	
	public List<Integer> processInputs()
	        throws InterruptedException, ExecutionException {

	    int threads = Runtime.getRuntime().availableProcessors();
	    ExecutorService service = Executors.newFixedThreadPool(threads);

	    List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
	    for (final String objName : objNameLst) {
	        Callable<Integer> callable = new Callable<Integer>() {
	            public Integer call() throws Exception {
	            	SalesforceConfigDataThread t = new SalesforceConfigDataThread(objName, lastModifiedDateString, gateRaw);
	        		SObject[] records = t.getRecords();
	            	Integer output = 2;
	            	log.info("input========"+ objName);
	                // process your input here and compute the output
	                return output;
	            }
	        };
	        futures.add(service.submit(callable));
	    }

	    service.shutdown();

	    List<Integer> outputs = new ArrayList<Integer>();
	    for (Future<Integer> future : futures) {
	        outputs.add(future.get());
	    }
	    log.info("===" + outputs.size());
	    return outputs;
	}
	
	public static List<String> getConfigDataList(Calendar cal) throws InterruptedException, ExecutionException {
		SalesforceConfigDataService sfConfigService = new SalesforceConfigDataService(cal);
		//sfConfigService.processInputs();
		sobjNameToRecordsMap = sfConfigService.getRecordsWithModifiedDate();
		//log.info("sobjNameToRecordsMap======" + sobjNameToRecordsMap.size());
		List<String> objLstToReturn = new ArrayList<>();
		for(String str : sobjNameToRecordsMap.keySet()) {
			log.info("obj Name:" + sobjNameToRecordsMap.get(str));
			if(sobjNameToRecordsMap.get(str) != null && sobjNameToRecordsMap.get(str).length > 0) {
				log.info("sobjNameToRecordsMap.get(str).size()===" + sobjNameToRecordsMap.get(str).length);
				
				objLstToReturn.add(str);
			}
		}
		return objLstToReturn;
	}
	
	public static List<SObject> getRecords(String objName) {
		return Arrays.asList(sobjNameToRecordsMap.get(objName));
	}
	
	public static void readConfigFile(String fileURL) {
		
	}
	
	private List<String> getConfigObjListFromPMO() {
		//todo - read from SF
		List<String> objList = new ArrayList<>();
		objList.add("GGDemo2__DataTableConfig__c");
		objList.add("GGDemo2__AccordionContent__c");
		//objList.add("GGDemo2__CustomApp__c");
		objList.add("GGDemo2__ChartConfig__c");
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
	
	public static void main(String str[]) throws InterruptedException, ExecutionException {
		SalesforceConfigDataService.getConfigDataList(Calendar.getInstance());
	}
	

}
