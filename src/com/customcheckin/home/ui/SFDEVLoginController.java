package com.customcheckin.home.ui;

import java.util.List;

import javax.xml.bind.JAXBException;

import com.customcheckin.home.HomePage;
import com.customcheckin.service.salesforce.SalesforceDevConnection;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.service.salesforce.vo.ProjectVO;
import com.customcheckin.util.PropertyManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SFDEVLoginController {
	private HomePage homePage;
	@FXML
    private TextField userField;
	@FXML
    private TextField passField;
	
	private Stage dialogStage;
	
	@FXML
	  private ComboBox<ProjectVO> projectListCombo;
	
	@FXML
    private void initialize() {
		ObservableList<ProjectVO> obj = FXCollections.observableArrayList();
		List<ProjectVO> prjctList = SalesforcePMOConnection.getInstance().getActiveProjects();
		projectListCombo.setItems(obj);
    }
	
	
	@FXML
    private void handleLogin() throws JAXBException {
		try {
			//todo - first login to sf then write to file
			PropertyManager.getInstance().setString("pmo.username", userField.getText());
			PropertyManager.getInstance().setString("pmo.password", passField.getText());
			PropertyManager.getInstance().storePropertyFile();
			SalesforcePMOConnection pmo = SalesforcePMOConnection.getInstance();
			ProjectVO project = projectListCombo.getValue();
			//todo validation
			pmo.storeCurrentProject(project.getId());
			pmo.storeSalesforceDevUser(userField.getText(), passField.getText());
			
			SalesforceDevConnection.getInstance();
			
				Alert alert = new Alert(AlertType.INFORMATION);
		        alert.setTitle("DEV Login");
		        alert.setHeaderText("");
		        alert.setContentText("DEV Login Successfull.");
		        alert.showAndWait();
		        dialogStage.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Alert alert = new Alert(AlertType.WARNING);
	        alert.setTitle("PMO Login");
	        alert.setHeaderText("Login Failed.");
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
