package com.customcheckin.service.salesforce;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import com.force.service.ForceDelegate;
import com.sforce.ws.ConnectionException;

public class TestSF {
    private static Logger log = Logger.getRootLogger();
    
    public static void main(String[] args) throws FileNotFoundException, ConnectionException {
    	ForceDelegate gate = ForceDelegate.login("gg.pmo");
    	Integer cnt = gate.queryCount("Select count() from Environment__c", null);
    	log.info("count: " + cnt);
    	
    	ForceDelegate gate1 = ForceDelegate.login("pankaj.hingane@business.org1", "test@123", false, 23.0);
    	Integer cnt1 = gate1.queryCount("Select count() from Application__c", null);
    }
    
}

