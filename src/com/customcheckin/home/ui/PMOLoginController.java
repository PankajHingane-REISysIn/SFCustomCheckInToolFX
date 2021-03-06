package com.customcheckin.home.ui;

import javax.xml.bind.JAXBException;

import com.customcheckin.home.HomePage;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.util.PropertyManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PMOLoginController {
	private HomePage homePage;
	@FXML
    private TextField userField;
	@FXML
    private TextField passField;
	
	private Stage dialogStage;
	
	@FXML
    private void initialize() {
    }
	
	
	@FXML
    private void handleLogin() throws JAXBException {
		try {
			//todo - first login to sf then write to file
			PropertyManager.getInstance().setString("pmo.username", userField.getText());
			PropertyManager.getInstance().setString("pmo.password", passField.getText());
			PropertyManager.getInstance().storePropertyFile();
			SalesforcePMOConnection.getInstance();
			
				Alert alert = new Alert(AlertType.INFORMATION);
		        alert.setTitle("PMO Login");
		        alert.setHeaderText("");
		        alert.setContentText("PMO Login Successfull.");
		        alert.showAndWait();
		        dialogStage.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			PropertyManager.getInstance().setString("pmo.username", "");
			PropertyManager.getInstance().setString("pmo.password", "");
			PropertyManager.getInstance().storePropertyFile();
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
