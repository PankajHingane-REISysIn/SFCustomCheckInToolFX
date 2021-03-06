package com.customcheckin.home.ui;

import java.util.List;

import javax.xml.bind.JAXBException;

import com.customcheckin.home.HomePage;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.service.salesforce.SalesforceDevConnection;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.service.salesforce.vo.EnvironmentUserVO;
import com.customcheckin.service.salesforce.vo.ProjectVO;
import com.customcheckin.service.salesforce.vo.UserVO;
import com.customcheckin.util.PropertyManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class JIRALoginController {
	private HomePage homePage;
	@FXML
    private TextField userField;
	@FXML
    private TextField passField;
	
	private Stage dialogStage;
	
	@FXML
    private void initialize() {
		EnvironmentUserVO jiraLoginUser = SalesforcePMOConnection.getInstance().getJiraUser();
		if(jiraLoginUser != null) {
			userField.setText(jiraLoginUser.getName());
			passField.setText(jiraLoginUser.getPassword__c());
		}
    }
	
	
	@FXML
    private void handleLogin() throws JAXBException {
		try {
			//todo - first login to sf then write to file
			SalesforcePMOConnection pmo = SalesforcePMOConnection.getInstance();
			pmo.storeJiraDevUser(userField.getText(), passField.getText());
			JIRAConnection.getInstance();
			
			//SalesforceDevConnection.getInstance();
			
				Alert alert = new Alert(AlertType.INFORMATION);
		        alert.setTitle("JIRA Login");
		        alert.setHeaderText("");
		        alert.setContentText("JIRA Login Successfull.");
		        alert.showAndWait();
		        dialogStage.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Alert alert = new Alert(AlertType.WARNING);
	        alert.setTitle("JIRA Login");
	        alert.setHeaderText("JIRA Failed.");
	        alert.setContentText(e.getMessage());
	        alert.showAndWait();
		}
		
    }
	
	public void setHomePage(HomePage homePage) {
		this.homePage = homePage;
	}
	
	public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}
