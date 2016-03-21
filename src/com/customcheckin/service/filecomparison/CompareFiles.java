package com.customcheckin.service.filecomparison;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.customcheckin.model.MetadataFile;
import com.customcheckin.service.salesforce.SalesforceDevConnection;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.util.UnzipUtility;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;

public class CompareFiles {
	private static Logger log = Logger.getRootLogger();
	public static void main(String args[]) {
		List<MetadataFile> metadaList = new CompareFiles().getMetadataFilesWithDifference();
		for (MetadataFile metadata : metadaList) {
			System.out.println(metadata.getName().get());
		}
	}

	public List<MetadataFile> getMetadataFilesWithDifference() {
		List<MetadataFile> returnMetadata = new ArrayList<>();
		//returnMetadata.add(new MetadataFile(new SimpleStringProperty("Simple1"), new SimpleBooleanProperty(false)));
		//returnMetadata.add(new MetadataFile(new SimpleStringProperty("Simple2"), new SimpleBooleanProperty(false)));
		//returnMetadata.add(new MetadataFile(new SimpleStringProperty("Simple3"), new SimpleBooleanProperty(false)));
		String text1 = SalesforcePMOConnection.getInstance().getGITUser().getLocalWorkspacePath__c();
		String text2 = UnzipUtility.DEST_DIR+"\\unpackaged\\";
		log.info("text1====" + text1);
		log.info("text2====" + text2);
		List<String> fileList1 = getfileList(text1);
		List<String> fileList2 = getfileList(text2);
		fileList1.retainAll(fileList2);
		for (String fileName : fileList1) {
			if (isFileDifferent(text1 + fileName, text2 + fileName)) {
				returnMetadata
						.add(new MetadataFile(new SimpleStringProperty(fileName), new SimpleBooleanProperty(false)));
			}
		}
		return returnMetadata;
	}

	public Boolean isFileDifferent(String file1, String file2) {
		String text1 = readFile(file1);
		String text2 = readFile(file2);
		diff_match_patch dmp = new diff_match_patch();
		dmp.Diff_Timeout = 0;

		// Execute one reverse diff as a warmup.
		dmp.diff_main(text2, text1, false);
		System.gc();

		long start_time = System.currentTimeMillis();
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
