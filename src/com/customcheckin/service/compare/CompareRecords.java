package com.customcheckin.service.compare;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.customcheckin.service.git.GITConnection;
import com.customcheckin.service.salesforce.vo.ConfigObjectVO;
import com.lib.util.CSVUtils;

public class CompareRecords {
	private String filePath;
	private static Logger log = Logger.getRootLogger();
	private String[] fileHeaders;
	private Boolean fileFound = true;
	private ConfigObjectVO custObj;
	public CompareRecords(ConfigObjectVO custObj) throws IOException {
		this.custObj = custObj;
		filePath = GITConnection.getInstance().getGitUserInfo().getLocalWorkspacePath__c()+"\\Config\\"+custObj.getName()+".csv";
		log.info("Obj Name: " + custObj);
	}
	
	public String[] getFileHeaders() {
		if(fileHeaders == null) {
			try {
				fileHeaders = CSVUtils.getHeader(filePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				log.error(e);
				fileFound = false;
				fileHeaders = new String[0];
			}
		}
		return fileHeaders;
	}
	public Map<String, String[]> readRecordsFromLocalGITRepoAndCompare(Map<String, String[]> recodByInternalId, String[] header) throws IOException {
		getFileHeaders();
		if(!fileFound) {
			return recodByInternalId;
		}
		Map<String, List<String[]>> data = CSVUtils.readFileByGrouping(filePath, true, getInternalIdIndex(fileHeaders), recodByInternalId.keySet());
		Map<String, String[]> differentRecords = new HashMap<>();
		log.info("In read records===" + data.keySet().size());
		for(String internalId : recodByInternalId.keySet()) {
			log.info("Comparing====internalId:"+ internalId);
			if( !data.containsKey(internalId) || CSVUtils.CompareRecord(fileHeaders, data.get(internalId).get(0), header, recodByInternalId.get(internalId))) {
				log.info("Found difference==");
				differentRecords.put(internalId,  recodByInternalId.get(internalId));
			}
		}
		return differentRecords;
	}
	
	public int getInternalIdIndex(String[] headers) {
		for(int i=0;i<headers.length;i++) {
			//todo replace
			// read from postinstallscript
			if(headers[i].equalsIgnoreCase(custObj.getInternalUniqueIdFieldAPIName())) {
				return i;
			}
		}
		return -1;
	}
	

}
