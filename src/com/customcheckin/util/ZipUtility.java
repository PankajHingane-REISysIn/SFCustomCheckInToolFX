package com.customcheckin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *  * This utility extracts files and directories of a standard zip file to  * a
 * destination directory.  * @author www.codejava.net  *  
 */
public class ZipUtility {
	public static final String DEST_DIR = "DevOrg";
	/**
	 *  * Size of the buffer to read/write data  
	 */
	private static final int BUFFER_SIZE = 4096;

	List<String> fileList;

	public ZipUtility() {
		fileList = new ArrayList<String>();
	}

	/**
	 * Unzip it
	 * 
	 * @param zipFile
	 *            input zip file
	 * @param output
	 *            zip file output folder
	 */
	public void unZip(String zipFile, String outputFolder) {

		byte[] buffer = new byte[BUFFER_SIZE];

		try {

			// create output directory is not exists
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);

				System.out.println("file unzip : " + newFile.getAbsoluteFile());

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			System.out.println("Done");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void zip(String srcFolder, String zipFile) {
		generateFileList(srcFolder, new File(srcFolder));

		byte[] buffer = new byte[1024];

		try {

			FileOutputStream fos = new FileOutputStream(srcFolder + File.separator +zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			System.out.println("Output to Zip : " + zipFile);

			for (String file : this.fileList) {

				System.out.println("File Added : " + file);
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);

				FileInputStream in = new FileInputStream(srcFolder + File.separator + file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
			}

			zos.closeEntry();
			// remember close it
			zos.close();

			System.out.println("Done");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Traverse a directory and get all files, and add the file into fileList
	 * 
	 * @param node
	 *            file or directory
	 */
	private void generateFileList(String srcFolder, File node) {

		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(srcFolder, node.getAbsoluteFile().toString()));
		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(srcFolder, new File(node, filename));
			}
		}

	}

	/**
	 * Format the file path for zip
	 * 
	 * @param file
	 *            file path
	 * @return Formatted file path
	 */
	private String generateZipEntry(String srcFolder, String file) {
		return file.substring(srcFolder.length() + 1, file.length());
	}

	public static void main(String str[]) throws IOException {
		new ZipUtility().zip("D:\\tempFolder\\src", "deploy.zip");
		//new ZipUtility().unZip("D:\\CM Proceess\\SFCustomCheckInToolFX\\src.zip", DEST_DIR);
	}
}