package com.customcheckin.service.salesforce;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class TestSF {
    private PartnerConnection partnerConnection = null;
    public static void main(String[] args) throws FileNotFoundException, ConnectionException {
    	TestSF samples = new TestSF();
		Login l1 =new Login();
		l1.setUserName("shah@force.pmo");
		l1.setPassword("2Amange1");
		samples.connectToSF(l1);
		samples.getActiveProjects();
		//l.LoginToPMO(l1);
    }
    
    public TestSF() {
    	
    }
    
    public TestSF(PartnerConnection partnerConnection) {
    	this.partnerConnection = partnerConnection;
    }
    
    public PartnerConnection getPartnerConnection() {
    	return partnerConnection;
    }
    
    public PartnerConnection connectToSF(Login login) throws FileNotFoundException, ConnectionException{
    	ConnectorConfig config = new ConnectorConfig();
        config.setUsername(login.getUserName());
        config.setPassword(login.getPassword());
        
        config.setAuthEndpoint("https://www.salesforce.com/services/Soap/u/36.0");
        config.setTraceMessage(true);
        config.setPrettyPrintXml(true);

        partnerConnection = new PartnerConnection(config);  
        System.out.println( "partnerConnection===" + partnerConnection);
		return partnerConnection;
    }
    
    public Boolean validePMOOrg() throws ConnectionException {
    	QueryResult queryResult = partnerConnection.query("select id from User where username='shah@force.pmo'");
    	if(queryResult.getRecords().length == 1) {
    		return true;
    	}
		return false;
    }
    
    public String getLastUsedProjectId() throws ConnectionException {
    	QueryResult queryResult = partnerConnection.query("select id,LastUsedProjectId__c from User where Id='"+ partnerConnection.getUserInfo().getUserId() +"'");
    	SObject[] sobjs = queryResult.getRecords();
    	SObject sobj = sobjs[0];
    	String lastUsedPrjctId = (String)sobj.getField("LastUsedProjectId__c");
    	return lastUsedPrjctId;
    }
    
    public List<Project> getActiveProjects() throws ConnectionException {
    	QueryResult queryResult = partnerConnection.query("select id,Name from Project__c where Active__c=true");
    	SObject[] records = queryResult.getRecords();
    	List<Project> projectListToReturn = new ArrayList<Project>();
    	for(SObject sobj : records) {
    		System.out.println("sobj========" + sobj);
    		Project project = new Project();
    		project.setId(sobj.getId());
    		project.setName((String) sobj.getField("Name"));
    		projectListToReturn.add(project);
    		System.out.println("project========" + project.getName());
    	}
    	return projectListToReturn;
    }
}

