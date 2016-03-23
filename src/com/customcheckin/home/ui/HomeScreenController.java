package com.customcheckin.home.ui;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.customcheckin.home.HomePage;
import com.customcheckin.model.JiraTicket;
import com.customcheckin.model.MetadataFile;
import com.customcheckin.service.filecomparison.CompareFiles;
import com.customcheckin.service.git.GITConnection;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.service.salesforce.SalesforceFileBasedRetrieve;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.util.UnzipUtility;
import com.customcheckin.util.Utility;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class HomeScreenController implements Initializable {
	private HomePage homePage;
	private static Logger log = Logger.getRootLogger();
	// metadata table
	@FXML
	private TableView<MetadataFile> metadataFileList;
	@FXML
	private TableColumn<MetadataFile, String> metadataNameColumn;
	@FXML
	private TableColumn<MetadataFile, Boolean> metaDataChekBoxColumn;

	// jira table
	@FXML
	private TableView<JiraTicket> jiraList;
	@FXML
	private TableColumn<JiraTicket, String> jiraNameColumn;
	@FXML
	private TableColumn<JiraTicket, Boolean> jiraChekBoxColumn;

	public HomeScreenController() {

	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// todo - load components using multi-threading
		initilizeMetadataTable();
		initilizeJiraTable();
		ConnectionManager.getAllConnections();
	}

	private void initilizeJiraTable() {
		jiraNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());

		jiraChekBoxColumn.setCellValueFactory(cellData -> cellData.getValue().getIsSelected()); 
		jiraChekBoxColumn.setCellFactory(param -> new CheckBoxTableCell<JiraTicket, Boolean>());
	}

	private void initilizeMetadataTable() {
		metadataNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());

		metaDataChekBoxColumn.setCellValueFactory(cellData -> cellData.getValue().getIsSelected()); 
		metaDataChekBoxColumn.setCellFactory(param -> new CheckBoxTableCell<MetadataFile, Boolean>());
	}

	@FXML
	private void handleGetJiraTicketOnClick() throws URISyntaxException, Exception {
		List<JiraTicket> rickets = JIRAConnection.getInstance().getOpenTickets("GGP");
		homePage.getJiraTicketComboList().addAll(rickets);
	}
	
	@FXML
	private void handleCommitAndPush() throws URISyntaxException, Exception {
		ObservableList<JiraTicket> data = jiraList.getItems();
		String selectedJiraTicket = "";
	    for (JiraTicket jiraTicket : data){
	        //check the boolean value of each item to determine checkbox state
	    	log.info("====" + jiraTicket.getIsSelected().get());
	    	if(jiraTicket.getIsSelected().get()) {
	    		selectedJiraTicket = jiraTicket.getId().get();
	    	}
	    }
	    if(selectedJiraTicket.isEmpty()) {
	    	Alert alert = new Alert(AlertType.INFORMATION);
	        alert.setTitle("Select Jira Ticket");
	        alert.setHeaderText("");
	        alert.setContentText("Please select Jira Ticket.");
	        alert.showAndWait();
	    } else {
	    	List<MetadataFile> metadaFiles = homePage.getMetadataFileList();
	    	List<String> fileNames = new ArrayList<>();
	    	for(MetadataFile metadataFile : metadaFiles) {
	    		if(metadataFile.getIsSelected().get()) {
	    			log.info("Adding file==>>" + metadataFile.getName().get());
	    			fileNames.add(metadataFile.getRelativeFilePath());
	    			Utility.replaceFile(metadataFile.getSfPath(), metadataFile.getGitPath());
	    			log.info("srcpath+metadataFile.getName().get()==>>" + metadataFile.getSfPath());
	    		}
	    	}
	    	GITConnection.getInstance().pushRepo(selectedJiraTicket, fileNames);
	    }
	}

	@FXML
	private void handleGetMetadaOnClick() throws URISyntaxException, Exception {
		List<MetadataFile> metadataFileList = new ArrayList<>();
		GetMetadataThreads.getAllData();
		metadataFileList = new CompareFiles().getMetadataFilesWithDifference();
		homePage.getMetadataFileList().addAll(metadataFileList);
		System.out.println("=========Completed");
	}

	public void setHomePage(HomePage homePage) {
		this.homePage = homePage;
		metadataFileList.setItems(homePage.getMetadataFileList());
		jiraList.setItems(homePage.getJiraTicketComboList());
	}

}
