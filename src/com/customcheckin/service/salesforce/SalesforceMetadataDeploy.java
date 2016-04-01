package com.customcheckin.service.salesforce;

import java.io.File;
import java.util.Date;
import java.util.Enumeration;
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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.customcheckin.util.PropertyManager;
import com.customcheckin.util.Utility;
import com.customcheckin.util.ZipUtility;
import com.force.metadata.FileBasedDeployAndRetrieve;
import com.force.service.ForceDelegate;

public class SalesforceMetadataDeploy {
	private ForceDelegate gate;
	public SalesforceMetadataDeploy(ForceDelegate gate) {
		this.gate = gate;
	}
	
	private void generatePackageXML() throws ParserConfigurationException, TransformerException {
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
		verEle1.setTextContent("29.0");
		rootElement.appendChild(verEle1);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(Utility.getDeployBaseURL()+"\\Package.xml");
		transformer.transform(source, result);
	}
	
	private void getMetadataXMLFiles() {
		
	}
	
	public void deployFilesToTargetOrg() throws Exception {
		generatePackageXML();
		getMetadataXMLFiles();
		File file = new File(Utility.getDeployBaseURL(), "deploy.zip");
		if(file.exists()) {
			file.delete();
		}
		new ZipUtility().zip(Utility.getDeployBaseURL(), "deploy.zip");
		Double apiVersion = PropertyManager.getInstance().getDouble("salesforce.api.version");
		if (apiVersion == null) {
			apiVersion = 25.0;
		}
		
		//gate.deployZip(new File(Utility.getDeployBaseURL()+"\\deploy.zip"));
		gate.deployZip(new File("D:\\CM Proceess\\Framework org -Metadata Deployment\\deploy2.zip"));
	}
	
	public static void deployToINTOrg() throws Exception {
		SalesforceMetadataDeploy salesforceMetadataDeploy = new SalesforceMetadataDeploy(SalesforceINTConnection.getInstance().getForceDelegate());
		salesforceMetadataDeploy.deployFilesToTargetOrg();
	}
	
	public static void main(String str[]) throws Exception {
		SalesforceMetadataDeploy.deployToINTOrg();
	}

}
