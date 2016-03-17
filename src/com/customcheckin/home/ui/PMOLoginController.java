package com.customcheckin.home.ui;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.customcheckin.home.HomePage;
import com.customcheckin.model.PMOLogin;
import com.customcheckin.service.SessionData.SessionData;
import com.customcheckin.service.salesforce.Login;
import com.customcheckin.service.salesforce.SFService;
import com.sforce.ws.ConnectionException;

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
		//todo remove duplicate Bean
		SFService sfService = new SFService();
		Login login =new Login(userField.getText(), passField.getText());
		try {
			sfService.connectToSF(login);
			Boolean isSFOrg = sfService.validePMOOrg();
			SessionData.pmoPartnerConnection = sfService.getPartnerConnection();
			if(isSFOrg) {
				JAXBContext context = JAXBContext
						.newInstance(PMOLogin.class);
				Marshaller m = context.createMarshaller();
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				PMOLogin p =new PMOLogin(userField.getText(), passField.getText());
				File file = new File("PMOLogin.xml");
				m.marshal(p, file);
				System.out.println(file.getAbsolutePath());
				Alert alert = new Alert(AlertType.INFORMATION);
		        //alert.initOwner(homePage.getPrimaryStage());
		        alert.setTitle("PMO Login");
		        alert.setHeaderText("");
		        alert.setContentText("PMO Login Successfull.");
		        alert.showAndWait();
		        dialogStage.close();
		        System.out.println("====>> Completed");
			}
		} catch (FileNotFoundException | ConnectionException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Alert alert = new Alert(AlertType.WARNING);
	        //alert.initOwner(homePage.getPrimaryStage());
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
