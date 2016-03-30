package com.customcheckin.home.ui;

import java.io.IOException;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.customcheckin.service.git.GITConnection;
import com.customcheckin.service.salesforce.SalesforceMetadataRetrieve;

public class GetMetadataThreads  extends Thread{
	private String type;
	private static Logger log = Logger.getRootLogger();
	Calendar cal;
	public GetMetadataThreads(String type, Calendar cal) {
		this.type = type;
		this.cal = cal;
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
				SalesforceMetadataRetrieve.getInstance().run(cal);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(type.equalsIgnoreCase("getSFRecords")) {
		}
		log.info("type========Completed:" + type);
		
	}
	public static void getAllData(Calendar cal) {
		/*Thread pullRepo = new GetMetadataThreads("pullRepo");
		pullRepo.start();*/
		
		Thread fetchMetada = new GetMetadataThreads("fetchMetadata", cal);
		fetchMetada.start();
		
		Thread sfRecords = new GetMetadataThreads("getSFRecords", cal);
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
		GetMetadataThreads.getAllData(null);
	}

}
