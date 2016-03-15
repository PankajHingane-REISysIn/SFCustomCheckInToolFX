package com.customcheckin.home.ui;

import java.net.URISyntaxException;

import com.customcheckin.home.HomePage;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.service.jira.TicketHelper;

import javafx.fxml.FXML;

public class HomeScreenController {
	private HomePage homePage;
	
	public HomeScreenController() {
		
	}
	
	@FXML
    private void initialize() {
    }
	
	@FXML
    private void handleGetJiraTicketOnClick() throws URISyntaxException, Exception {
    	System.out.println("=========Fetching");
    	JIRAConnection jira = new JIRAConnection("pankaj.hingane", "Ved@123.com");
		new TicketHelper(jira.getConnection()).getOpenTicketList("State of MA - Internal", "pankaj.hingane");
		System.out.println("=========Completed");
    }
	
	public void setHomePage(HomePage homePage) {
		this.homePage = homePage;
	}

}
