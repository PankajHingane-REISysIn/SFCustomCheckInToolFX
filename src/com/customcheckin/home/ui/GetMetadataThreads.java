package com.customcheckin.home.ui;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.customcheckin.service.git.GITConnection;
import com.customcheckin.service.salesforce.SalesforceFileBasedRetrieve;

public class GetMetadataThreads  extends Thread{
	private String type;
	private static Logger log = Logger.getRootLogger();
	public GetMetadataThreads(String type) {
		this.type = type;
	}
	public void run() {
		log.info("type========" + type);
		if(type.equalsIgnoreCase("pullRepo")) {
			try {
				GITConnection.getInstance().pullRepo();
			} catch (IOException | GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(type.equalsIgnoreCase("fetchMetadata")) {
			try {
				SalesforceFileBasedRetrieve.getInstance().run();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(type.equalsIgnoreCase("getSFRecords")) {
		}
		log.info("type========Completed:" + type);
		
	}
	public static void getAllData() {
		/*Thread pullRepo = new GetMetadataThreads("pullRepo");
		pullRepo.start();*/
		
		Thread fetchMetada = new GetMetadataThreads("fetchMetadata");
		fetchMetada.start();
		
		Thread sfRecords = new GetMetadataThreads("getSFRecords");
		sfRecords.start();
		
		/*try {
			pullRepo.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		try {
			fetchMetada.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sfRecords.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info("All connection available");
	}
	
	public static void main(String []str) {
		GetMetadataThreads.getAllData();
	}

}
