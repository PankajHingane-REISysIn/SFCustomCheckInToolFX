package com.customcheckin.service.jira;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.customcheckin.model.JiraSearchCriteriaBean;
import com.customcheckin.model.JiraTicket;
import com.customcheckin.service.salesforce.SalesforceDevConnection;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.service.salesforce.vo.EnvironmentUserVO;
import com.customcheckin.util.PropertyManager;
import com.force.service.ForceDelegate;
import com.lib.util.StringUtils;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class JIRAConnection {
	private static final String REI_JIRA_URL = "https://tracker.reisys.com/";
	private static JIRAConnection instance;
	private JiraRestClient jiraRestClient;
	private List<JiraTicket> jiraTicketList;
	private String userName;
	private static Logger log = Logger.getRootLogger();

	private JIRAConnection() throws URISyntaxException {
		EnvironmentUserVO jiraUserInfo = SalesforcePMOConnection.getInstance().getJiraUser();
		if (jiraUserInfo != null && StringUtils.isNonEmpty(jiraUserInfo.getName()) && StringUtils.isNonEmpty(jiraUserInfo.getPassword__c())) {
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
		searchCriteriaList.add(new JiraSearchCriteriaBean("project", "=", "\""+projectName+"\""));
		searchCriteriaList.add(new JiraSearchCriteriaBean("assignee", "=", "\""+userName+"\""));
		searchCriteriaList.add(new JiraSearchCriteriaBean("status", "in", "(Open,Development)"));
		return getTickets(searchCriteriaList);
	}
	
	public List<JiraTicket> getTickets(List<JiraSearchCriteriaBean> searchCriteriaList) throws Exception {
		jiraTicketList = new ArrayList<JiraTicket>();
		if(searchCriteriaList.isEmpty()) {
			throw new Exception("Please define search criteria.");
		}
		String jqlStr = "";
		for(JiraSearchCriteriaBean searchCreiteria : searchCriteriaList) {
			jqlStr += searchCreiteria.toString() + " and ";
		}
		jqlStr = jqlStr.substring(0, jqlStr.length() - 4);
		//return first 50 records
		SearchResult searchResult = jiraRestClient.getSearchClient().searchJql(jqlStr).claim();
		for (BasicIssue issue : searchResult.getIssues()) {
			JiraTicket j = new JiraTicket();
			System.out.println(issue.getKey());
			j.setId(new SimpleStringProperty(issue.getKey()));
			j.setName(new SimpleStringProperty(issue.getKey()));
			j.setIsSelected(new SimpleBooleanProperty(false));
			
			jiraTicketList.add(j);
		}
		return jiraTicketList;
	}
	
	public static void main(String str[]) throws URISyntaxException, Exception {
		log.info(JIRAConnection.getInstance().getOpenTickets("GGP").size());
	}

}
