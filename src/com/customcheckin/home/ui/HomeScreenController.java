package com.customcheckin.home.ui;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.customcheckin.home.HomePage;
import com.customcheckin.model.ConfigRecord;
import com.customcheckin.model.JiraTicket;
import com.customcheckin.model.MetadataFile;
import com.customcheckin.service.compare.CompareFiles;
import com.customcheckin.service.git.GITConnection;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.service.salesforce.SalesforceConfigDataService;
import com.customcheckin.service.salesforce.SalesforceMetadataRetrieve;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.util.ZipUtility;
import com.lib.util.CSVUtils;
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
import javafx.scene.control.DatePicker;
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
	private TableColumn<MetadataFile, String> metadataCreateDateColumn;
	@FXML
	private TableColumn<MetadataFile, String> metadataModifiedDateColumn;
	@FXML
	private TableColumn<MetadataFile, Boolean> metaDataChekBoxColumn;
	
	//Config Data
	@FXML
	private ComboBox<String> configObjList;
	@FXML
	private TableView<ConfigRecord> configDataList;
	@FXML
	private TableColumn<ConfigRecord, String> configNameColumn;
	@FXML
	private TableColumn<ConfigRecord, String> configInternalIdColumn;
	@FXML
	private TableColumn<ConfigRecord, String> configCol1Column;
	@FXML
	private TableColumn<ConfigRecord, String> configCol2Column;
	@FXML
	private TableColumn<ConfigRecord, Boolean> configChekBoxColumn;
	
	@FXML
    DatePicker metadataDatePicker;
	
	@FXML
    DatePicker configDatePicker;

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
		initilizeConfigTable();
		ConnectionManager.getAllConnections();
	}

	private void initilizeJiraTable() {
		jiraNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());

		jiraChekBoxColumn.setCellValueFactory(cellData -> cellData.getValue().getIsSelected()); 
		jiraChekBoxColumn.setCellFactory(param -> new CheckBoxTableCell<JiraTicket, Boolean>());
	}

	private void initilizeMetadataTable() {
		metadataNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
		metadataModifiedDateColumn.setCellValueFactory(cellData -> cellData.getValue().getLastModifiedDate());
		metadataCreateDateColumn.setCellValueFactory(cellData -> cellData.getValue().getCreateDate());

		metaDataChekBoxColumn.setCellValueFactory(cellData -> cellData.getValue().getIsSelected()); 
		metaDataChekBoxColumn.setCellFactory(param -> new CheckBoxTableCell<MetadataFile, Boolean>());
	}
	
	private void initilizeConfigTable() {
		configNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
		configInternalIdColumn.setCellValueFactory(cellData -> cellData.getValue().getInternalUniqueId());
		//configCol1Column.setCellValueFactory(cellData -> cellData.getValue().getCol1());
		//configCol2Column.setCellValueFactory(cellData -> cellData.getValue().getCol2());
		configChekBoxColumn.setCellValueFactory(cellData -> cellData.getValue().getIsSelected());
		configChekBoxColumn.setCellFactory(param -> new CheckBoxTableCell<ConfigRecord, Boolean>());
	}

	@FXML
	private void handleGetJiraTicketOnClick() throws URISyntaxException, Exception {
		List<JiraTicket> rickets = JIRAConnection.getInstance().getOpenTickets("GGP");
		homePage.getJiraTicketComboList().addAll(rickets);
	}
	
	@FXML
	private void handleGetConfigDataOnClick() throws URISyntaxException, Exception {
		LocalDate localeDate = configDatePicker.getValue();
		Date convertToDate = Date.from(localeDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		DateFormat format=new SimpleDateFormat("yyyy/mm/dd");
		format.format(convertToDate);
		Calendar cal=format.getCalendar();
		List<String> sobjList = SalesforceConfigDataService.getConfigDataList(cal);
		homePage.getConfigObjComboList().clear();
		homePage.getConfigObjComboList().addAll(sobjList);
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
	    /*if(selectedJiraTicket.isEmpty()) {
	    	Alert alert = new Alert(AlertType.INFORMATION);
	        alert.setTitle("Select Jira Ticket");
	        alert.setHeaderText("");
	        alert.setContentText("Please select Jira Ticket.");
	        alert.showAndWait();
	    } else {*/
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
	    	Map<String, List<ConfigRecord>> configRecords = SalesforceConfigDataService.getSobjToRecordConfigList();
	    	for(String objAPIName : configRecords.keySet()) {
	    		List<String[]> selectedConfigRecords = new ArrayList<>();
	    		for(ConfigRecord configRecord : configRecords.get(objAPIName)) {
	    			if(configRecord.getIsSelected().get()) {
	    				selectedConfigRecords.add(SalesforceConfigDataService.getRecordsMap(objAPIName, configRecord.getInternalUniqueId().get()));
	    			}
	    		}
	    		if(selectedConfigRecords.size() > 0) {
	    			//todo - read internalId from Org
	    			CSVUtils.updateCSVFile(GITConnection.getInstance().getGitUserInfo().getLocalWorkspacePath__c()+"\\Config\\"+objAPIName+".csv", 
	    					"GGDemo2__InternalUniqueID__c", 
	    					SalesforceConfigDataService.getSObjHeader(objAPIName), selectedConfigRecords);
	    			fileNames.add("Config/"+objAPIName+".csv");
	    			
	    		}
	    	}
	    	GITConnection.getInstance().pushRepo(selectedJiraTicket, fileNames);
	    //}
	}

	@FXML
	private void handleGetMetadaOnClick() throws URISyntaxException, Exception {
		LocalDate localeDate = metadataDatePicker.getValue();
		Date convertToDate = Date.from(localeDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		DateFormat format=new SimpleDateFormat("yyyy/mm/dd");
		format.format(convertToDate);
		Calendar cal=format.getCalendar();
		List<MetadataFile> metadataFileList = new ArrayList<>();
		GetMetadataThreads.getAllData(cal);
		metadataFileList = new CompareFiles().getMetadataFilesWithDifference();
		/*ObservableList<MetadataFile> metadataFileList1 = homePage.getMetadataFileList();
		metadataFileList1 = FXCollections.observableArrayList();*/
		homePage.getMetadataFileList().clear();
		homePage.getMetadataFileList().addAll(metadataFileList);
		System.out.println("=========Completed");
	}
	
	@FXML
	private void handleConfiListOnChange() throws URISyntaxException, Exception {
		String selectedItem = configObjList.getSelectionModel().getSelectedItem();
		List<ConfigRecord> configRecordList = SalesforceConfigDataService.getConfigRecordList(selectedItem);
		//System.out.println("=========configRecordList" + configRecordList.size() + configRecordList.get(0).getInternalUniqueId());
		homePage.getConfigRecordList().clear();
		homePage.getConfigRecordList().addAll(configRecordList);
	}

	public void setHomePage(HomePage homePage) {
		this.homePage = homePage;
		metadataFileList.setItems(homePage.getMetadataFileList());
		jiraList.setItems(homePage.getJiraTicketComboList());
		configObjList.setItems(homePage.getConfigObjComboList());
		configDataList.setItems(homePage.getConfigRecordList());
	}

}
