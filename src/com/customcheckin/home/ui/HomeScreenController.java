package com.customcheckin.home.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;

import com.customcheckin.home.HomePage;
import com.customcheckin.model.ConfigObject;
import com.customcheckin.model.ConfigRecord;
import com.customcheckin.model.JiraTicket;
import com.customcheckin.model.MetadataFile;
import com.customcheckin.service.compare.CompareFiles;
import com.customcheckin.service.git.GITConnection;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.service.salesforce.SalesforceConfigDataService;
import com.customcheckin.service.salesforce.SalesforceINTConnection;
import com.customcheckin.service.salesforce.SalesforceMetadataDeploy;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.service.salesforce.vo.ConfigObjectVO;
import com.customcheckin.util.Utility;
import com.lib.util.CSVUtils;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
	private ComboBox<ConfigObject> configObjList;
	@FXML
	private TableView<ConfigRecord> configDataList;
	@FXML
	private TableColumn<ConfigRecord, String> configNameColumn;
	@FXML
	private TableColumn<ConfigRecord, String> configCol1Column;
	@FXML
	private TableColumn<ConfigRecord, String> configCol2Column;
	@FXML
	private TableColumn<ConfigRecord, String> configCol3Column;
	@FXML
	private TableColumn<ConfigRecord, String> configCol4Column;
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
	private TableColumn<JiraTicket, String> jiraDescColumn;
	@FXML
	private TableColumn<JiraTicket, String> jiraReporterColumn;
	@FXML
	private TableColumn<JiraTicket, String> jiraCreatedDateColumn;
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
		//todo read date from SF
		metadataDatePicker.setValue(LocalDate.now());
		configDatePicker.setValue(LocalDate.now());
	}

	private void initilizeJiraTable() {
		jiraNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
		jiraDescColumn.setCellValueFactory(cellData -> cellData.getValue().getDescription());
		jiraCreatedDateColumn.setCellValueFactory(cellData -> cellData.getValue().getCreatedDate());
		jiraReporterColumn.setCellValueFactory(cellData -> cellData.getValue().getReporter());

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
		configCol1Column.setCellValueFactory(cellData -> cellData.getValue().getCol1());
		configCol2Column.setCellValueFactory(cellData -> cellData.getValue().getCol2());
		configCol3Column.setCellValueFactory(cellData -> cellData.getValue().getCol3());
		configCol4Column.setCellValueFactory(cellData -> cellData.getValue().getCol4());
		configChekBoxColumn.setCellValueFactory(cellData -> cellData.getValue().getIsSelected());
		configChekBoxColumn.setCellFactory(param -> new CheckBoxTableCell<ConfigRecord, Boolean>());
	}
	
	@FXML
	private void handleGetJiraTicketOnClick() throws URISyntaxException, Exception {
		List<JiraTicket> tickets = JIRAConnection.getInstance().getOpenTickets(SalesforcePMOConnection.getInstance().getJiraEnvirnment().getExternalId1__c());
		homePage.getJiraTicketComboList().addAll(tickets);
	}
	
	@FXML
	private void handleGetConfigDataOnClick() throws URISyntaxException, Exception {
		LocalDate localeDate = configDatePicker.getValue();
		Date convertToDate = Date.from(localeDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		DateFormat format=new SimpleDateFormat("yyyy/mm/dd");
		format.format(convertToDate);
		Calendar cal=format.getCalendar();
		List<ConfigObject> sobjList = SalesforceConfigDataService.getConfigDataList(cal);
		Collections.sort(sobjList);
		homePage.getConfigObjComboList().clear();
		homePage.getConfigObjComboList().addAll(sobjList);
	}
	
	@FXML
	private void handleCommitAndPush() {
		List<String> fileNames = new ArrayList<>();
		try {
			ObservableList<JiraTicket> data = jiraList.getItems();
			String selectedJiraTicket = "";
			for (JiraTicket jiraTicket : data) {
				//check the boolean value of each item to determine checkbox state
				//todo - single selection
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
				for(MetadataFile metadataFile : metadaFiles) {
					if(metadataFile.getIsSelected().get()) {
						log.info("Adding file==>>" + metadataFile.getName().get());
						fileNames.add(metadataFile.getRelativeFilePath());
						Utility.replaceFile(metadataFile.getSfPath(), metadataFile.getGitPath());
						log.info("srcpath+metadataFile.getName().get()==>>" + metadataFile.getSfPath());
					}
				}
				Map<String, List<ConfigRecord>> configRecords = SalesforceConfigDataService.getSobjToRecordConfigList();
				if(configRecords != null)
					for(String objAPIName : configRecords.keySet()) {
						List<String[]> selectedConfigRecords = new ArrayList<>();
						for(ConfigRecord configRecord : configRecords.get(objAPIName)) {
							if(configRecord.getIsSelected().get()) {
								log.info("Internal unique Id:"+configRecord.getInternalUniqueId().get());
								selectedConfigRecords.add(SalesforceConfigDataService.getRecordsMapWithDiff(objAPIName, configRecord.getInternalUniqueId().get()));
							}
						}
						if(selectedConfigRecords.size() > 0) {
							CSVUtils.updateCSVFile(GITConnection.getInstance().getGitUserInfo().getLocalWorkspacePath__c()+"\\Config\\"+ SalesforceConfigDataService.getConfigObjectVO(objAPIName).getName() +".csv", 
									SalesforceConfigDataService.getConfigObjectVO(objAPIName).getInternalUniqueIdFieldAPIName(), 
									SalesforceConfigDataService.getSObjHeader(objAPIName), selectedConfigRecords);
							fileNames.add("Config/"+SalesforceConfigDataService.getConfigObjectVO(objAPIName).getName()+".csv");
							
						}
					}
				GITConnection.getInstance().pushRepo(selectedJiraTicket, fileNames);
				// todo -
				//SalesforcePMOConnection.getInstance().storeLastCheckInDate();
				Boolean deployToINT = true;
				if(deployToINT) {
					SalesforceMetadataDeploy sfDeploy = new SalesforceMetadataDeploy(SalesforceINTConnection.getInstance().getForceDelegate());
					List<String> jiraTicket = new ArrayList<>();
					jiraTicket.add(selectedJiraTicket);
					sfDeploy.deploy(jiraTicket);
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					String dateStr = dateFormat.format(cal.getTime());
					JIRAConnection.getInstance().updateField(selectedJiraTicket, "INTEGRATION Deployed On", dateStr);
					Map<String, String> paramMap = new HashMap<>();
					paramMap.put("value", "Yes");
					JIRAConnection.getInstance().updateField(selectedJiraTicket, "INTEGRATION Deployed?", paramMap);
				}
				clearTables();
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("CheckIn Successfully.");
				alert.setHeaderText("");
				alert.setContentText("Data is checked-In to GIT.");
				alert.showAndWait();
			}
			
		} catch(Exception ex) {
			try {
				GITConnection.getInstance().revertFile(fileNames);
				//todo handle excetpions
			} catch (RefAlreadyExistsException e) {
				//e.printStackTrace();
				log.error(e);
			} catch (RefNotFoundException e) {
				log.error(e);
				//e.printStackTrace();
			} catch (InvalidRefNameException e) {
				log.error(e);
				//e.printStackTrace();
			} catch (CheckoutConflictException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				log.error(e);
				//e.printStackTrace();
			} catch (IOException e) {
				log.error(e);
				//e.printStackTrace();
			}
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Error in Check-In.");
			alert.setHeaderText("");
			alert.setContentText("Error:"+ex.getMessage());
			alert.showAndWait();
		}
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
		homePage.getMetadataFileList().clear();
		homePage.getMetadataFileList().addAll(metadataFileList);
		System.out.println("=========Completed");
	}
	
	@FXML
	private void handleConfiListOnChange() throws URISyntaxException, Exception {
		String selectedItem = configObjList.getSelectionModel().getSelectedItem().getObjectAPIName();
		List<ConfigRecord> configRecordList = SalesforceConfigDataService.getConfigRecordList(selectedItem);
		updateConfigTableHeader(selectedItem);
		homePage.getConfigRecordList().clear();
		homePage.getConfigRecordList().addAll(configRecordList);
	}
	
	private void updateConfigTableHeader(String objectAPIName) {
		ConfigObjectVO configObject = SalesforceConfigDataService.getConfigObjectVO(objectAPIName);
		configCol1Column.setText(configObject.getConfigFieldList().size() > 0 ? 
				configObject.getFieldAPIToLabelList().get(configObject.getConfigFieldList().get(0)) : "");
		configCol2Column.setText(configObject.getConfigFieldList().size() > 1 ? 
				configObject.getFieldAPIToLabelList().get(configObject.getConfigFieldList().get(1)) : "");
		configCol3Column.setText(configObject.getConfigFieldList().size() > 2 ? 
				configObject.getFieldAPIToLabelList().get(configObject.getConfigFieldList().get(2)) : "");
		configCol4Column.setText(configObject.getConfigFieldList().size() > 3 ? 
				configObject.getFieldAPIToLabelList().get(configObject.getConfigFieldList().get(3)) : "");
	}
	
	private void clearTables() {
		homePage.getMetadataFileList().clear();
		homePage.getConfigRecordList().clear();
	}
	
	public void setHomePage(HomePage homePage) {
		this.homePage = homePage;
		metadataFileList.setItems(homePage.getMetadataFileList());
		jiraList.setItems(homePage.getJiraTicketComboList());
		configObjList.setItems(homePage.getConfigObjComboList());
		configDataList.setItems(homePage.getConfigRecordList());
	}
	
	public static class ProgressForm {
        private final Stage dialogStage;
        private final ProgressBar pb = new ProgressBar();
        private final ProgressIndicator pin = new ProgressIndicator();

        public ProgressForm() {
            dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            // PROGRESS BAR
            final Label label = new Label();
            label.setText("Loading... Please wait");

            pb.setProgress(0);
            pin.setProgress(0);

            final HBox hb = new HBox();
            hb.setSpacing(5);
            hb.setAlignment(Pos.CENTER);
            hb.getChildren().addAll(pb, pin);

            Scene scene = new Scene(hb);
            dialogStage.setScene(scene);
        }

        public void activateProgressBar(final Task<?> task)  {
            pb.progressProperty().bind(task.progressProperty());
            pin.progressProperty().bind(task.progressProperty());
            dialogStage.show();
        }

        public Stage getDialogStage() {
            return dialogStage;
        }
    }

}
