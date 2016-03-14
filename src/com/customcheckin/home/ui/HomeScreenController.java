package com.customcheckin.home.ui;

import java.net.URISyntaxException;
import java.util.List;

import com.customcheckin.home.HomePage;
import com.customcheckin.model.JiraTicket;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.service.jira.TicketHelper;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class HomeScreenController {
	private HomePage homePage;
	@FXML
	private TableView<JiraTicket> JiraTicketTable;
	@FXML
    private TableColumn<JiraTicket, String> nameColumn;
	
	public HomeScreenController() {
		
	}
	
	@FXML
    private void initialize() {
		nameColumn.setCellValueFactory(cellData -> cellData.getValue().getId());
    }
	
	@FXML
    private void handleGetJiraTicketOnClick() throws URISyntaxException, Exception {
    	System.out.println("=========Fetching");
    	JIRAConnection jira = new JIRAConnection("pankaj.hingane", "Ved@123.com");
		List<JiraTicket> jiraTicketList = new TicketHelper(jira.getConnection()).getOpenTicketList("State of MA - Internal", "pankaj.hingane");
		homePage.getJiraTicketList().addAll(jiraTicketList);
		System.out.println("=========Completed");
    }
	
	public void setHomePage(HomePage homePage) {
		this.homePage = homePage;
		JiraTicketTable.setItems(homePage.getJiraTicketList());
	}

}
