package com.customcheckin.service.salesforce;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.customcheckin.service.git.GITConnection;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.util.PropertyManager;
import com.customcheckin.util.Utility;
import com.customcheckin.util.ZipUtility;
import com.force.metadata.FileBasedDeployAndRetrieve;
import com.force.service.ForceDelegate;

public class SalesforceMetadataDeploy {
	private ForceDelegate gate;
	private static Logger log = Logger.getRootLogger();
	
	public SalesforceMetadataDeploy(ForceDelegate gate) {
		this.gate = gate;
	}
	
	private void generatePackageXML() throws ParserConfigurationException, TransformerException, IOException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("Package");
		Attr attr = doc.createAttribute("xmlns");
		attr.setValue("http://soap.sforce.com/2006/04/metadata");
		rootElement.setAttributeNode(attr);
		doc.appendChild(rootElement);
		
		//pages
		Element pageEle = doc.createElement("types");
		rootElement.appendChild(pageEle);
		
		Element pageEle1 = doc.createElement("members");
		pageEle1.setTextContent("*");
		pageEle.appendChild(pageEle1);
		
		Element namepageEle = doc.createElement("name");
		namepageEle.setTextContent("ApexPage");
		pageEle.appendChild(namepageEle);
		//classses
		Element classEle = doc.createElement("types");
		rootElement.appendChild(classEle);
		
		Element classEle1 = doc.createElement("members");
		classEle1.setTextContent("*");
		classEle.appendChild(classEle1);
		
		Element nameclassEle = doc.createElement("name");
		nameclassEle.setTextContent("ApexClass");
		classEle.appendChild(nameclassEle);
		
		// profile
		Element profileEle = doc.createElement("types");
		rootElement.appendChild(profileEle);
		
		Element profileEle1 = doc.createElement("members");
		profileEle1.setTextContent("*");
		profileEle.appendChild(profileEle1);
		
		Element nameProfileEle = doc.createElement("name");
		nameProfileEle.setTextContent("Profile");
		profileEle.appendChild(nameProfileEle);
		
		Element verEle1 = doc.createElement("version");
		verEle1.setTextContent("33.0");
		rootElement.appendChild(verEle1);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		File packageXML = new File(Utility.getMetadataDeployBaseURL()+"\\src\\package.xml");
		log.info("packageXML Path:" + packageXML.getAbsolutePath());
		if(!packageXML.exists()) {
			if(!packageXML.getParentFile().exists()) {
				packageXML.getParentFile().mkdirs();
				log.info("parent folder Path:"+packageXML.getParentFile().getAbsolutePath());
			}
			packageXML.createNewFile();
			log.info("Created Package.xml:");
		}
		StreamResult result = new StreamResult(packageXML);
		transformer.transform(source, result);
		log.info("Package XML operation completed.");
	}
	
	public void deployFilesToTargetOrg() throws Exception {
		generatePackageXML();
		File file = new File(Utility.getMetadataDeployBaseURL()+"\\src", "deploy.zip");
		if(file.exists()) {
			file.delete();
			file.createNewFile();
		}
		new ZipUtility().zip(Utility.getMetadataDeployBaseURL()+"\\src", "deploy.zip");
		Double apiVersion = PropertyManager.getInstance().getDouble("salesforce.api.version");
		if (apiVersion == null) {
			apiVersion = 25.0;
		}
		
		gate.deployZip(new File(Utility.getMetadataDeployBaseURL()+"\\src"+"\\deploy.zip"));
		//gate.deployZip(new File("D:\\CM Proceess\\Framework org -Metadata Deployment\\deploy2.zip"));
	}
	
	public void deploy(List<String> jiraDefects) throws Exception {
		//delete existing file
		File file = new File(Utility.getMetadataDeployBaseURL()+"\\src");
		if(file.exists()) {
			FileUtils.deleteDirectory(file);
			log.info("Deleting folder:"+file.getAbsolutePath());
			file.delete();
		}
		List<String> commits = JIRAConnection.getInstance().getCommits(jiraDefects);
		//commits.add("e676f82c0f1df2172e37ac50f851af1faa00e0cb");
		commits.add("41070fa1389a4805960e7c67263a58dc6952fa46");
		GITConnection.getInstance().getFiles("", commits);
		deployFilesToTargetOrg();
	}
	
	public static void main(String str[]) throws Exception {
		SalesforceMetadataDeploy sfMetadaDeploy = new SalesforceMetadataDeploy(SalesforceINTConnection.getInstance().getForceDelegate());
		sfMetadaDeploy.deploy(null);
	}

}
