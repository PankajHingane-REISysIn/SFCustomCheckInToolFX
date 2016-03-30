package com.customcheckin.service.salesforce;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.force.service.ForceDelegate;
import com.force.service.raw.ForceDelegateRaw;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.sobject.SObject;

public class SalesforceConfigDataThread{
	private static Logger log = Logger.getRootLogger();
	private String objName;
	private String lastModifiedDate;
	private ForceDelegateRaw gate;
	
	public SalesforceConfigDataThread(String objName, String lastModifiedDate, ForceDelegateRaw gate) {
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
		DescribeSObjectResult desc = gate.describeSObject(objName);
		Field[] fields = desc.getFields();
		String query = "select Id,Name";
		for(Field field : fields) {
			if(field.getName().endsWith("__c")) {
				query += ", " + field.getName();
			}
		}
		query +=" from " + objName + " where LastModifiedDate > "+lastModifiedDate;
		SObject[] records = gate.queryMultiple(query, null);
		return records;
	}
	
	public static void main(String[] str) {
		Calendar lastModifiedDate = Calendar.getInstance();
		lastModifiedDate.add(Calendar.DATE, 1);
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(lastModifiedDate.getTime());
		// Output "Wed Sep 26 14:23:28 EST 2012"

		String formatted = format1.format(lastModifiedDate.getTime());
		System.out.println(formatted);
		// Output "2012-09-26"

		//System.out.println(format1.parse(formatted));
		
		SalesforceConfigDataThread t = new SalesforceConfigDataThread("GGDemo2__DataTableConfig__c", formatted, SalesforceDevConnection.getInstance().getForceDelegateRaw());
		t.getRecords();
	}

}
