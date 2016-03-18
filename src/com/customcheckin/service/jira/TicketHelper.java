package com.customcheckin.service.jira;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.customcheckin.model.JiraTicket;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class TicketHelper {
	private List<SearchCriteriaBean> searchCriteriaList;
	private JiraRestClient jiraRestClient;
	private List<JiraTicket> jiraTicketList;
	
	public TicketHelper(JiraRestClient jiraRestClient) {
		this.jiraRestClient = jiraRestClient;
	}
	public TicketHelper(JiraRestClient jiraRestClient, List<SearchCriteriaBean> searchCriteriaList) {
		this.jiraRestClient = jiraRestClient;
		this.searchCriteriaList = searchCriteriaList;
		// TODO Auto-generated constructor stub
	}
	public List<JiraTicket> getOpenTicketList(String projectName, String userName) throws Exception {
		/*System.out.println("===============1");
		List<String> issueList = new ArrayList<String>();
		
		SearchResult searchResult = jiraRestClient.getSearchClient().searchJql("project=\""+projectName+"\" and status in (Open) and assignee=\""+userName+"\"").claim();
		for (BasicIssue issue : searchResult.getIssues()) {
			System.out.println(issue.getKey() + issue.getSelf());
			issueList.add(issue.getKey());
		}
		System.out.println("===============2");
		return issueList;*/
		searchCriteriaList = new ArrayList<SearchCriteriaBean>();
		searchCriteriaList.add(new SearchCriteriaBean("project", "=", "\""+projectName+"\""));
		searchCriteriaList.add(new SearchCriteriaBean("assignee", "=", "\""+userName+"\""));
		searchCriteriaList.add(new SearchCriteriaBean("status", "in", "(Open)"));
		return getTicketList();
	}
	
	public List<JiraTicket> getTicketList() throws Exception {
		jiraTicketList = new ArrayList<JiraTicket>();
		if(searchCriteriaList.isEmpty()) {
			throw new Exception("Please define search criteria.");
		}
		String jqlStr = "";
		for(SearchCriteriaBean searchCreiteria : searchCriteriaList) {
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
	
	public static void main(String[] args) throws Exception {
		JIRAConnection jira = new JIRAConnection("pankaj.hingane", "Ved@123.com");
		new TicketHelper(jira.getConnection()).getOpenTicketList("State of MA - Internal", "pankaj.hingane");
		
		List<SearchCriteriaBean> searchCriteriaList = new ArrayList<SearchCriteriaBean>();
		searchCriteriaList.add(new SearchCriteriaBean("project", "=", "\"State of MA - Internal\""));
		searchCriteriaList.add(new SearchCriteriaBean("status", "in", "(Open)"));
		new TicketHelper(jira.getConnection(), searchCriteriaList).getTicketList();
		System.exit(0);
	}

}
