package com.customcheckin.home.ui;

import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.atlassian.jira.util.json.JSONException;
import com.customcheckin.home.HomePage;
import com.customcheckin.model.JiraTicket;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.service.salesforce.SalesforceINTConnection;
import com.customcheckin.service.salesforce.SalesforceMetadataDeploy;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.demo.HomePageDemo;
import com.force.service.ForceDelegate;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
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

public class DeploymentController  implements Initializable {
	private HomePage homePage;
	private HomePageDemo homePageDemo;
	private static Logger log = Logger.getRootLogger();
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
	
	@FXML
	private ComboBox<String> envList;
	
	private List<String> jiraTicketList;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		log.info("In the initialize");
		// TODO Auto-generated method stub
		initilizeJiraTable();
		
	}
	
	private void initilizeJiraTable() {
		jiraNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
		jiraDescColumn.setCellValueFactory(cellData -> cellData.getValue().getDescription());
		jiraCreatedDateColumn.setCellValueFactory(cellData -> cellData.getValue().getCreatedDate());
		jiraReporterColumn.setCellValueFactory(cellData -> cellData.getValue().getReporter());

		jiraChekBoxColumn.setCellValueFactory(cellData -> cellData.getValue().getIsSelected()); 
		jiraChekBoxColumn.setCellFactory(param -> new CheckBoxTableCell<JiraTicket, Boolean>());
	}
	
	@FXML
	private void handleGetEnv() {
		homePage.getEnvList().clear();
		homePage.getEnvList().add("INT");
		homePage.getEnvList().add("TEST");
		homePage.getEnvList().add("Production");
		
	}
	
	@FXML
	private void handleGetJiraTicketOnClick() throws URISyntaxException, Exception {
		// todo - check filter criteria with Shah
		ProgressForm pForm = new ProgressForm();
		Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws URISyntaxException, Exception {
	            List<JiraTicket> tickets = JIRAConnection.getInstance().getOpenTickets(SalesforcePMOConnection.getInstance().getJiraEnvirnment().getExternalId1__c());
				homePage.getJiraTicketComboList().clear();
				homePage.getJiraTicketComboList().addAll(tickets);
                updateProgress(10, 10);
                return null ;
            }
        };
        
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
        });

        pForm.getDialogStage().show();

        Thread thread = new Thread(task);
        thread.start();

	}
	
	@FXML
	private void handleDeployOnClick() throws URISyntaxException, Exception {
		ObservableList<JiraTicket> data = jiraList.getItems();
		jiraTicketList = new ArrayList<>();
		for (JiraTicket jiraTicket : data) {
			//check the boolean value of each item to determine checkbox state
			if(jiraTicket.getIsSelected().get()) {
				jiraTicketList.add(jiraTicket.getId().get());
			}
		}
			SalesforceMetadataDeploy sfDeploy = new SalesforceMetadataDeploy(getForceDeledgator());
			sfDeploy.deploy(jiraTicketList);
			updateJiraTicketStatus();
			
	}
	
	private ForceDelegate getForceDeledgator() {
		String selectedEnv = envList.getValue();
		if(selectedEnv.equalsIgnoreCase("INT")) {
			return SalesforceINTConnection.getInstance().getForceDelegate();
		} else if(selectedEnv.equalsIgnoreCase("Test")) {
			return SalesforceINTConnection.getInstance().getForceDelegate();
		} else if(selectedEnv.equalsIgnoreCase("Production")) {
			return SalesforceINTConnection.getInstance().getForceDelegate();
		}
		return null;
	}
	
	private void updateJiraTicketStatus() throws JSONException, URISyntaxException {
		String selectedEnv = envList.getValue();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String dateStr = dateFormat.format(cal.getTime());
		System.out.println(dateStr); //2014/08/06 16:00:22
		if(selectedEnv.equalsIgnoreCase("INT")) {
			for(String jiraTicket : jiraTicketList) {
				JIRAConnection.getInstance().updateField(jiraTicket, "INTEGRATION Deployed On", dateStr);
				Map<String, String> paramMap = new HashMap<>();
				paramMap.put("value", "Yes");
				JIRAConnection.getInstance().updateField(jiraTicket, "INTEGRATION Deployed?", paramMap);
			}
		} else if(selectedEnv.equalsIgnoreCase("Test")) {
			
		} else if(selectedEnv.equalsIgnoreCase("Production")) {
			
		}
	}
	
	public void setHomePage(HomePage homePage) {
		this.homePage = homePage;
		envList.setItems(homePage.getEnvList());
		jiraList.setItems(homePage.getJiraTicketComboList());
	}
	
	public void setHomePageDemo(HomePageDemo homePage) {
		this.homePageDemo = homePage;
		envList.setItems(homePageDemo.getEnvList());
		jiraList.setItems(homePageDemo.getJiraTicketComboList());
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
}
