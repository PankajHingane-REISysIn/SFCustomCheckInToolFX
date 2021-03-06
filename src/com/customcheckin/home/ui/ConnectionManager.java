package com.customcheckin.home.ui;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;

import com.customcheckin.service.git.GITConnection;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.service.salesforce.SalesforceDevConnection;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;

public class ConnectionManager  extends Thread{
	private String type;
	private static Logger log = Logger.getRootLogger();
	public ConnectionManager(String type) {
		this.type = type;
		SalesforcePMOConnection.getInstance();
	}
	public void run() {
		log.info("type========" + type);
		if(type.equalsIgnoreCase("SFDEV")) {
			SalesforceDevConnection.getInstance();
		} else if(type.equalsIgnoreCase("JIRA")) {
			try {
				JIRAConnection.getInstance();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				log.error(e);
			}
		} else if(type.equalsIgnoreCase("GIT")) {
			try {
				GITConnection.getInstance();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidRemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.info("type========Completed:" + type);
		
	}
	public static void getAllConnections() {
		Thread sfDEVConn = new ConnectionManager("SFDEV");
		sfDEVConn.start();
		
		Thread jiraConn = new ConnectionManager("JIRA");
		jiraConn.start();
		
		Thread gitConn = new ConnectionManager("GIT");
		gitConn.start();
		
		/*try {
			sfDEVConn.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			jiraConn.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			gitConn.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		log.info("All connection available");
	}
	
	public static void main(String []str) {
		ConnectionManager.getAllConnections();
	}

}
