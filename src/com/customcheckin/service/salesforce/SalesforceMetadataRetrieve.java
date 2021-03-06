package com.customcheckin.service.salesforce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.customcheckin.util.Utility;
import com.customcheckin.util.ZipUtility;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.PackageTypeMembers;
import com.sforce.soap.metadata.RetrieveMessage;
import com.sforce.soap.metadata.RetrieveRequest;
import com.sforce.soap.metadata.RetrieveResult;
import com.sforce.soap.metadata.RetrieveStatus;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * Sample that logs in and shows a menu of retrieve and deploy metadata options.
 */
public class SalesforceMetadataRetrieve {
	private static SalesforceMetadataRetrieve instance;
	private MetadataConnection metadataConnection;

	private static String ZIP_FILE = "src.zip";

	// manifest file that controls which components get retrieved
	private static String MANIFEST_FILE = "package.xml";

	private static final double API_VERSION = 36.0;

	// one second in milliseconds
	private static final long ONE_SECOND = 1000;

	// maximum number of attempts to deploy the zip file
	private static final int MAX_NUM_POLL_REQUESTS = 50;

	private static Logger log = Logger.getRootLogger();

	private SalesforceMetadataRetrieve() throws ConnectionException {
		getSFConnection();
	}

	public static SalesforceMetadataRetrieve getInstance() throws ConnectionException {
		if (instance == null) {
			instance = new SalesforceMetadataRetrieve();
		}
		return instance;
	}

	// todo -make use of existing session id
	private void getSFConnection() throws ConnectionException {
		SalesforceDevConnection sfDev = SalesforceDevConnection.getInstance();
		final ConnectorConfig config = new ConnectorConfig();
		config.setAuthEndpoint("https://login.salesforce.com/services/Soap/c/36.0");
		config.setServiceEndpoint("https://login.salesforce.com/services/Soap/c/36.0");
		config.setManualLogin(true);

		EnterpriseConnection enter = new EnterpriseConnection(config);
		LoginResult loginRes = enter.login(sfDev.getUserName(), sfDev.getPass());
		final ConnectorConfig configWithSession = new ConnectorConfig();
		configWithSession.setServiceEndpoint(loginRes.getMetadataServerUrl());
		configWithSession.setSessionId(loginRes.getSessionId());

		metadataConnection = new MetadataConnection(configWithSession);
	}
	
	private void generatePackageXML() throws ParserConfigurationException, TransformerException {
		Map<String, List<FileProperties>> filTypeToPropertyList = SalesforceMetadaProperties.getFileProperties();
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("Package");
		Attr attr = doc.createAttribute("xmlns");
		attr.setValue("http://soap.sforce.com/2006/04/metadata");
		rootElement.setAttributeNode(attr);
		doc.appendChild(rootElement);
		
		//classses
		for(String type : filTypeToPropertyList.keySet()) {
			Element classEle = doc.createElement("types");
			rootElement.appendChild(classEle);
			
			for(FileProperties fileProperties : filTypeToPropertyList.get(type)) {
				Element classEle1 = doc.createElement("members");
				classEle1.setTextContent(fileProperties.getFullName());
				classEle.appendChild(classEle1);
				
			}
			
			Element nameclassEle = doc.createElement("name");
			nameclassEle.setTextContent(type);
			classEle.appendChild(nameclassEle);
			
		}
		
		Element verEle1 = doc.createElement("version");
		verEle1.setTextContent("33.0");
		rootElement.appendChild(verEle1);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(MANIFEST_FILE);
		transformer.transform(source, result);
	}

	public void run(Calendar cal) throws Exception {
		//todo - replace Date
		SalesforceMetadaProperties.getSFMetadataProperty(metadataConnection, cal);
		List<Thread> threadLst = SalesforceMetadaProperties.threadListToWait;
		for(Thread thread : threadLst) {
			thread.join();
		}
		retrieveZip();
		new ZipUtility().unZip(ZIP_FILE, ZipUtility.DEST_DIR);
	}

	private void retrieveZip() throws Exception {
		generatePackageXML();
		RetrieveRequest retrieveRequest = new RetrieveRequest();
		// The version in package.xml overrides the version in RetrieveRequest
		retrieveRequest.setApiVersion(API_VERSION);
		setUnpackaged(retrieveRequest);
		log.info("========" + metadataConnection.getConfig().getAuthEndpoint());
		AsyncResult asyncResult = metadataConnection.retrieve(retrieveRequest);
		RetrieveResult result = waitForRetrieveCompletion(asyncResult);

		if (result.getStatus() == RetrieveStatus.Failed) {
			throw new Exception(result.getErrorStatusCode() + " msg: " + result.getErrorMessage());
		} else if (result.getStatus() == RetrieveStatus.Succeeded) {
			// Print out any warning messages
			StringBuilder stringBuilder = new StringBuilder();
			if (result.getMessages() != null) {
				for (RetrieveMessage rm : result.getMessages()) {
					stringBuilder.append(rm.getFileName() + " - " + rm.getProblem() + "\n");
				}
			}
			if (stringBuilder.length() > 0) {
				log.info("Retrieve warnings:\n" + stringBuilder);
			}

			log.info("Writing results to zip file");
			File resultsFile = new File(ZIP_FILE);
			FileOutputStream os = new FileOutputStream(resultsFile);

			try {
				os.write(result.getZipFile());
			} finally {
				os.close();
			}
		}
	}

	private RetrieveResult waitForRetrieveCompletion(AsyncResult asyncResult) throws Exception {
		// Wait for the retrieve to complete
		int poll = 0;
		long waitTimeMilliSecs = ONE_SECOND;
		String asyncResultId = asyncResult.getId();
		RetrieveResult result = null;
		do {
			Thread.sleep(waitTimeMilliSecs);
			// Double the wait time for the next iteration
			waitTimeMilliSecs *= 2;
			if (poll++ > MAX_NUM_POLL_REQUESTS) {
				throw new Exception("Request timed out.  If this is a large set "
						+ "of metadata components, check that the time allowed "
						+ "by MAX_NUM_POLL_REQUESTS is sufficient.");
			}
			result = metadataConnection.checkRetrieveStatus(asyncResultId, true);
			log.info("Retrieve Status: " + result.getStatus());
		} while (!result.isDone());

		return result;
	}

	private void setUnpackaged(RetrieveRequest request) throws Exception {
		// Edit the path, if necessary, if your package.xml file is located
		// elsewhere
		File unpackedManifest = new File(MANIFEST_FILE);
		log.info("Manifest file: " + unpackedManifest.getAbsolutePath());

		if (!unpackedManifest.exists() || !unpackedManifest.isFile()) {
			throw new Exception("Should provide a valid retrieve manifest " + "for unpackaged content. Looking for "
					+ unpackedManifest.getAbsolutePath());
		}

		// Note that we use the fully quualified class name because
		// of a collision with the java.lang.Package class
		com.sforce.soap.metadata.Package p = parsePackageManifest(unpackedManifest);
		request.setUnpackaged(p);
	}

	private com.sforce.soap.metadata.Package parsePackageManifest(File file)
			throws ParserConfigurationException, IOException, SAXException {
		com.sforce.soap.metadata.Package packageManifest = null;
		List<PackageTypeMembers> listPackageTypes = new ArrayList<PackageTypeMembers>();
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputStream inputStream = new FileInputStream(file);
		Element d = db.parse(inputStream).getDocumentElement();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c instanceof Element) {
				Element ce = (Element) c;
				NodeList nodeList = ce.getElementsByTagName("name");
				if (nodeList.getLength() == 0) {
					continue;
				}
				String name = nodeList.item(0).getTextContent();
				NodeList m = ce.getElementsByTagName("members");
				List<String> members = new ArrayList<String>();
				for (int i = 0; i < m.getLength(); i++) {
					Node mm = m.item(i);
					members.add(mm.getTextContent());
				}
				PackageTypeMembers packageTypes = new PackageTypeMembers();
				packageTypes.setName(name);
				packageTypes.setMembers(members.toArray(new String[members.size()]));
				listPackageTypes.add(packageTypes);
			}
		}
		packageManifest = new com.sforce.soap.metadata.Package();
		PackageTypeMembers[] packageTypesArray = new PackageTypeMembers[listPackageTypes.size()];
		packageManifest.setTypes(listPackageTypes.toArray(packageTypesArray));
		packageManifest.setVersion(API_VERSION + "");
		return packageManifest;
	}

	public static void main(String[] args) throws Exception {
		SalesforceMetadataRetrieve sample = SalesforceMetadataRetrieve.getInstance();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -3);
		sample.run(cal);;
	}

}