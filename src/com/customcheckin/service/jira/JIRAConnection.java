package com.customcheckin.service.jira;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

public class JIRAConnection {
	private static final String REI_JIRA_URL = "https://tracker.reisys.com/";
	private String userName;
	private String password;
	public JIRAConnection(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}
	
	public JiraRestClient getConnection() throws URISyntaxException {
		JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = new URI(REI_JIRA_URL);
        return factory.createWithBasicHttpAuthentication(uri, userName, password);
	}

}
