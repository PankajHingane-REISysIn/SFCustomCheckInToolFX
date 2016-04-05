package com.customcheckin.service.salesforce;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.customcheckin.service.salesforce.vo.ConfigObjectVO;
import com.force.service.ForceDelegate;
import com.force.service.raw.ForceDelegateRaw;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;

public class SalesforceConfigDataThread{
	private static Logger log = Logger.getRootLogger();
	private ConfigObjectVO objName;
	private String lastModifiedDate;
	private ForceDelegateRaw gate;
	
	public SalesforceConfigDataThread(ConfigObjectVO objName, String lastModifiedDate, ForceDelegateRaw gate) {
		this.objName = objName;
		this.lastModifiedDate = lastModifiedDate;
		this.gate = gate;
	}
	
	public void run() {
		log.info("objName========" + objName);
		getRecords();
	}
	
	public SObject[] getRecords() {
		// todo - cache of objects
		try {
			DescribeSObjectResult desc = gate.describeSObject(objName.getObjectAPIName());
			objName.setObjectLabel(desc.getLabel());
			Field[] fields = desc.getFields();
			String query = "select Id,Name";
			for(Field field : fields) {
				if(field.getName().endsWith("__c")) {
					query += ", " + field.getName();
				}
			}
			query +=" from " + objName.getObjectAPIName() + " where LastModifiedDate > "+lastModifiedDate;
			SObject[] records = gate.queryMultiple(query, null);
			return records;
			
		} catch(Exception ex) {
			ex.printStackTrace();
			log.error(ex);
		}
		return null;
	}
	
	public static void main(String[] str) {
		Calendar lastModifiedDate = Calendar.getInstance();
		lastModifiedDate.add(Calendar.DATE, 1);
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		String formatted = format1.format(lastModifiedDate.getTime());
		SalesforceConfigDataThread t = new SalesforceConfigDataThread(ConfigObjects.getInstance().getCustomObjects().get(0) , formatted, 
					SalesforceDevConnection.getInstance().getForceDelegateRaw());
		t.getRecords();
	}

}
