package com.customcheckin.service.jira;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.customcheckin.model.JiraSearchCriteriaBean;
import com.customcheckin.model.JiraTicket;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.service.salesforce.vo.EnvironmentUserVO;
import com.lib.util.StringUtils;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class JIRAConnection {
	private static final String REI_JIRA_URL = SalesforcePMOConnection.getInstance().getJiraEnvirnment().getURL__c();
	private static JIRAConnection instance;
	private JiraRestClient jiraRestClient;
	private List<JiraTicket> jiraTicketList;
	private String userName;
	private static Logger log = Logger.getRootLogger();

	private JIRAConnection() throws URISyntaxException {
		EnvironmentUserVO jiraUserInfo = SalesforcePMOConnection.getInstance().getJiraUser();
		if (jiraUserInfo != null && StringUtils.isNonEmpty(jiraUserInfo.getName())
				&& StringUtils.isNonEmpty(jiraUserInfo.getPassword__c())) {
			init(jiraUserInfo.getName(), jiraUserInfo.getPassword__c());
			log.info("Jira==" + jiraUserInfo.getPassword__c());
			userName = jiraUserInfo.getName();
		}
	}

	public static JIRAConnection getInstance() throws URISyntaxException {
		if (instance == null) {
			instance = new JIRAConnection();
		}
		return instance;
	}

	private void init(String userName, String password) throws URISyntaxException {
		JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
		URI uri = new URI(REI_JIRA_URL);
		jiraRestClient = factory.createWithBasicHttpAuthentication(uri, userName, password);
	}

	public List<JiraTicket> getOpenTickets(String projectName) throws Exception {
		List<JiraSearchCriteriaBean> searchCriteriaList = new ArrayList<JiraSearchCriteriaBean>();
		searchCriteriaList.add(new JiraSearchCriteriaBean("project", "=", "\"" + projectName + "\""));
		searchCriteriaList.add(new JiraSearchCriteriaBean("assignee", "=", "\"" + userName + "\""));
		searchCriteriaList.add(new JiraSearchCriteriaBean("status", "in", "(Open,Development)"));
		return getTickets(searchCriteriaList);
	}

	public List<JiraTicket> getTickets(List<JiraSearchCriteriaBean> searchCriteriaList) throws Exception {
		jiraTicketList = new ArrayList<JiraTicket>();
		if (searchCriteriaList.isEmpty()) {
			throw new Exception("Please define search criteria.");
		}
		String jqlStr = "";
		for (JiraSearchCriteriaBean searchCreiteria : searchCriteriaList) {
			jqlStr += searchCreiteria.toString() + " and ";
		}
		jqlStr = jqlStr.substring(0, jqlStr.length() - 4);
		// return first 50 records
		SearchResult searchResult = jiraRestClient.getSearchClient().searchJql(jqlStr).claim();

		for (BasicIssue issue : searchResult.getIssues()) {
			JiraTicket j = new JiraTicket();
			System.out.println(issue.getKey());
			j.setId(new SimpleStringProperty(issue.getKey()));
			j.setName(new SimpleStringProperty(issue.getKey()));
			j.setIsSelected(new SimpleBooleanProperty(false));
			final com.atlassian.jira.rest.client.domain.Issue issueDetail = jiraRestClient.getIssueClient()
					.getIssue(issue.getKey()).claim();
			j.setDescription(new SimpleStringProperty(issueDetail.getSummary()));
			j.setCreatedDate(new SimpleStringProperty(issueDetail.getCreationDate().toString()));
			j.setReporter(new SimpleStringProperty(issueDetail.getReporter().getDisplayName()));
			// j.set
			jiraTicketList.add(j);
		}
		return jiraTicketList;
	}

	public void updateField(String jiraTicketNo, String ticketName, Object value) throws JSONException {
		com.atlassian.jira.rest.client.domain.Issue issue = jiraRestClient.getIssueClient().getIssue(jiraTicketNo)
				.claim();
		for(com.atlassian.jira.rest.client.domain.Field field : issue.getFields()){
			log.info("field===" + field.getName());
			log.info("field===" + field.getType());
			log.info("field===" + field.getValue());
		}
		com.atlassian.jira.rest.client.domain.Field customField = issue.getFieldByName(ticketName);
		log.info("===" + customField.getValue());
		update(issue, customField, value);
	}

	private static void update(com.atlassian.jira.rest.client.domain.Issue issue, Field field, Object value)
			throws JSONException {

		try {
			HttpURLConnection connection = urlConnection(forIssue(issue), withEncoding());

			connection.connect();

			writeData(connection, jsonEditIssue(field, value));
			checkResponse(connection);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private static void writeData(HttpURLConnection connection, JSONObject fields) throws IOException {
		System.out.println(fields);
		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
		out.write(fields.toString());
		out.flush();
		out.close();
	}

	private static JSONObject jsonEditIssue(Field field, Object value) throws JSONException {
		JSONObject summary = new JSONObject().accumulate(field.getId(), value);
		JSONObject fields = new JSONObject().accumulate("fields", summary);
		return fields;
	}

	private static String withEncoding() {
		String userPassword = SalesforcePMOConnection.getInstance().getJiraUser().getName() + ":"
				+ SalesforcePMOConnection.getInstance().getJiraUser().getPassword__c();
		byte[] byteArray = Base64.encodeBase64(userPassword.getBytes());
		String encodedString = new String(byteArray);
		return encodedString;
	}

	private static HttpURLConnection urlConnection(URL url, String encoding) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestMethod("PUT");
		connection.setRequestProperty("Authorization", "Basic " + encoding);
		return connection;
	}

	private static void checkResponse(HttpURLConnection connection) throws IOException {

		if (HttpURLConnection.HTTP_NO_CONTENT != connection.getResponseCode()) {

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}

			System.err.println(stringBuilder.toString());
		}
	}

	private static URL forIssue(com.atlassian.jira.rest.client.domain.Issue issue) throws MalformedURLException {
		return issue.getSelf().toURL();
	} 
	
	public List<String> getCommits(List<String> jiraTicketNos) {
		List<String> commits = new ArrayList<String>();
		return commits;
	}

	public static void main(String str[]) throws URISyntaxException, Exception {
		log.info(JIRAConnection.getInstance().getOpenTickets("GGP").size());
		//JIRAConnection.getInstance().updateField("GGP-3", "TEST Deployed On", "2016-04-10");
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("value", "No");
		JIRAConnection.getInstance().updateField("GGP-3", "TEST Deployed?", paramMap);
	}

}
