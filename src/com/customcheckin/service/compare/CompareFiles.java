package com.customcheckin.service.compare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.customcheckin.model.MetadataFile;
import com.customcheckin.service.salesforce.SalesforceDevConnection;
import com.customcheckin.service.salesforce.SalesforceMetadaProperties;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.util.UnzipUtility;
import com.sforce.soap.metadata.FileProperties;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;

public class CompareFiles {
	private static Logger log = Logger.getRootLogger();
	private String gitWorkspaceURL;
	private String sfWorkspaceURL;
	private Map<String, FileProperties> fileURLToPropertyForCompare = SalesforceMetadaProperties.fileURLToPropertyForCompare;

	public static void main(String args[]) {
		List<MetadataFile> metadaList = new CompareFiles().getMetadataFilesWithDifference();
		for (MetadataFile metadata : metadaList) {
			System.out.println(metadata.getName().get());
		}
	}

	public List<MetadataFile> getMetadataFilesWithDifference() {
		List<MetadataFile> returnMetadata = new ArrayList<>();
		gitWorkspaceURL = SalesforcePMOConnection.getInstance().getGITUser().getLocalWorkspacePath__c()
				+ "\\src\\";
		sfWorkspaceURL = UnzipUtility.DEST_DIR+"\\unpackaged";
		traverseToFolders(new File(sfWorkspaceURL), "src", returnMetadata);
		// todo - show newly created files.
		return returnMetadata;
	}

	public void traverseToFolders(File node, String relativePath, List<MetadataFile> returnMetadata) {

		log.info(relativePath);
		
		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				traverseToFolders(new File(node, filename), relativePath+"/"+filename, returnMetadata);
			}
		} else {
			File file = new File(gitWorkspaceURL+relativePath.replaceFirst("src", ""));
			log.info("File==" + file.getAbsolutePath());
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			if(fileURLToPropertyForCompare.containsKey(relativePath)) {
				FileProperties fileProperty = fileURLToPropertyForCompare.get(relativePath);
				String createDate = sdf.format(fileProperty.getCreatedDate().getTime());
				String modifiedDate = sdf.format(fileProperty.getLastModifiedDate().getTime());
				if(file.exists()) {
					if(isFileDifferent(node.getAbsolutePath(), gitWorkspaceURL +relativePath.replaceFirst("src", ""))) {
						log.info("file Different:" + file.getName());
						returnMetadata.add(
								new MetadataFile(new SimpleStringProperty(file.getName()), relativePath, gitWorkspaceURL +relativePath.replaceFirst("src", ""),node.getAbsolutePath(),  new SimpleBooleanProperty(false),
										new SimpleStringProperty(createDate), new SimpleStringProperty(modifiedDate)));
					}
				} else {
					log.info("New file:" + file.getName());
					returnMetadata.add(
							new MetadataFile(new SimpleStringProperty(file.getName()), relativePath, gitWorkspaceURL +relativePath.replaceFirst("src", ""), node.getAbsolutePath(),  new SimpleBooleanProperty(false),
									new SimpleStringProperty(createDate), new SimpleStringProperty(modifiedDate)));
				}
			}
		}

	}

	public Boolean isFileDifferent(String file1, String file2) {
		String text1 = readFile(file1);
		String text2 = readFile(file2);
		diff_match_patch dmp = new diff_match_patch();
		dmp.Diff_Timeout = 0;

		// Execute one reverse diff as a warmup.
		dmp.diff_main(text2, text1, false);
		System.gc();

		LinkedList<Diff> diffLst = dmp.diff_main(text1, text2, false);
		if (diffLst.size() == 1) {
			return false;
		}
		return true;
	}

	public List<String> getfileList(String folderSrc) {
		List<String> fileSet = new ArrayList<String>();
		File folder = new File(folderSrc);
		for (File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				fileSet.add(fileEntry.getName());
			}
		}
		return fileSet;
	}

	private static String readFile(String filename) {
		// Read a file from disk and return the text contents.
		StringBuffer strbuf = new StringBuffer();
		try {
			FileReader input = new FileReader(filename);
			BufferedReader bufRead = new BufferedReader(input);
			String line = bufRead.readLine();
			while (line != null) {
				strbuf.append(line);
				strbuf.append('\n');
				line = bufRead.readLine();
			}
			bufRead.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strbuf.toString();
	}
}
