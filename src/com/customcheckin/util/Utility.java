package com.customcheckin.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.service.salesforce.vo.EnvironmentVO;

public class Utility {
	
	public static void replaceFile(String src, String target) throws IOException {
		Path from = Paths.get(src); //convert from String to Path
		Path to = Paths.get(target); //convert from String to Path
		File targetFile = new File(target);
		if(!targetFile.exists()) {
			if(!targetFile.getParentFile().exists()) {
				targetFile.getParentFile().mkdirs();
			}
			targetFile.createNewFile();
		}
		Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
	}
	
	public static String getMetadataDeployBaseURL() {
		EnvironmentVO gitEnv = SalesforcePMOConnection.getInstance().getGitEnvirnment();
		String remoteRepoURL = gitEnv.getURL__c();
		return remoteRepoURL.substring(remoteRepoURL.lastIndexOf("/") + 1)+"/MetadaDataDeployment";
	}
	
	public static String getConfigDeployBaseURL() {
		EnvironmentVO gitEnv = SalesforcePMOConnection.getInstance().getGitEnvirnment();
		String remoteRepoURL = gitEnv.getURL__c();
		return remoteRepoURL.substring(remoteRepoURL.lastIndexOf("/") + 1)+"/ConfigDataDeployment";
	}
	
	public static void main(String str[]) throws IOException {
		replaceFile("D:\\logs\\sample.txt", "D:\\logs\\log34\\log34\\log34\\log34\\sample8.txt");
	}

}
