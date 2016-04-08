package com.customcheckin.service.salesforce;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.customcheckin.model.ConfigObject;
import com.customcheckin.service.salesforce.vo.ConfigObjectVO;
import com.force.service.ForceDelegate;
import com.force.service.raw.ForceDelegateRaw;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;

public class SalesforceConfigDataThread{
	private static Logger log = Logger.getRootLogger();
	private ConfigObjectVO custObj;
	private String lastModifiedDate;
	private ForceDelegateRaw gate;
	
	public SalesforceConfigDataThread(ConfigObjectVO objName, String lastModifiedDate, ForceDelegateRaw gate) {
		this.custObj = objName;
		this.lastModifiedDate = lastModifiedDate;
		this.gate = gate;
	}
	
	public void run() {
		log.info("objName========" + custObj);
		getRecords();
	}
	
	public SObject[] getRecords() {
		try {
			DescribeSObjectResult desc = gate.describeSObject(custObj.getObjectAPIName());
			custObj.setObjectLabel(desc.getLabel());
			Field[] fields = desc.getFields();
			String query = "select Id,Name";
			for(Field field : fields) {
				if(field.getName().endsWith("__c")) {
					query += ", " + field.getName();
					if(custObj.getFieldAPIToLabelList().containsKey(field.getName())) {
						custObj.setFieldlabel(field.getName(), field.getLabel());
					}
					log.info(field.getType().getValue());
					if(field.getType().getValue().equalsIgnoreCase("reference")) {
						if(SalesforceConfigDataService.getConfigObjectVO(field.getReferenceTo()[0]) != null) {
							ConfigObjectVO custObj = SalesforceConfigDataService.getConfigObjectVO(field.getReferenceTo()[0]);
							query +=", "+field.getName().replace("__c", "__r.") + custObj.getInternalUniqueIdFieldAPIName();
						}
					}
					field.getLabel();
				}
			}
			if(custObj.getObjectType__c().equalsIgnoreCase("Custom Setting") && custObj.getCustomSettingType__c().equalsIgnoreCase("Hierarchy")) {
				query +=" ,SetupOwnerId";
			}
			query +=" from " + custObj.getObjectAPIName() + " where LastModifiedDate > "+lastModifiedDate;
			// filter records for Hierarchy custom setting. Retrieve only OWD and Profile level records
			if(custObj.getObjectType__c().equalsIgnoreCase("Custom Setting") && custObj.getCustomSettingType__c().equalsIgnoreCase("Hierarchy")) {
				query +=" and SetupOwner.type not in('User')";
			}
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
		SalesforceConfigDataService configDataService = new SalesforceConfigDataService(lastModifiedDate);
		SalesforceConfigDataThread t = new SalesforceConfigDataThread(ConfigObjects.getInstance().getCustomObjects().get(10) , formatted, 
					SalesforceDevConnection.getInstance().getForceDelegateRaw());
		t.getRecords();
	}

}
