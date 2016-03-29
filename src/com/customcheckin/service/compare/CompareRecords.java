package com.customcheckin.service.compare;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.customcheckin.service.git.GITConnection;
import com.lib.util.CSVUtils;

public class CompareRecords {
	private String filePath;
	private static Logger log = Logger.getRootLogger();
	private String[] fileHeaders;
	public CompareRecords(String objName) throws IOException {
		filePath = GITConnection.getInstance().getGitUserInfo().getLocalWorkspacePath__c()+"\\Config\\"+objName+".csv";
	}
	
	public String[] getFileHeaders() throws IOException {
		if(fileHeaders == null) {
			fileHeaders = CSVUtils.getHeader(filePath);
		}
		return fileHeaders;
	}
	public Map<String, String[]> readRecordsFromLocalGITRepoAndCompare(Map<String, String[]> recodByInternalId, String[] header) throws IOException {
		getFileHeaders();
		Map<String, List<String[]>> data = CSVUtils.readFileByGrouping(filePath, true, getInternalIdIndex(fileHeaders), recodByInternalId.keySet());
		Map<String, String[]> differentRecords = new HashMap<>();
		for(String internalId : data.keySet()) {
			for(String str : data.get(internalId).get(0)) {
				log.info("data from file:" + str);
			}
			for(String str : recodByInternalId.get(internalId)) {
				log.info("data from database:" + str);
			}
			log.info("data from database with internal:" + recodByInternalId.get(internalId));
			if(CSVUtils.CompareRecord(fileHeaders, data.get(internalId).get(0), header, recodByInternalId.get(internalId))) {
				log.info("Found difference==");
				differentRecords.put(internalId,  data.get(internalId).get(0));
			}
		}
		return differentRecords;
	}
	
	public int getInternalIdIndex(String[] headers) {
		for(int i=0;i<headers.length;i++) {
			//todo replace
			// read from postinstallscript
			if(headers[i].equalsIgnoreCase("GGDemo2__InternalUniqueID__c")) {
				return i;
			}
		}
		return -1;
	}
	

}
