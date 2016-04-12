package com.customcheckin.home.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.apache.log4j.Logger;

import com.customcheckin.home.HomePage;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MenuController {
	private static Logger log = Logger.getRootLogger();
	@FXML
	private MenuItem editGITDetails;
	@FXML
	private MenuItem editJIRADetails;
	@FXML
	private MenuItem editDEVORGDetails;
	@FXML
	private MenuItem editINTORGDetails;
	@FXML
	private MenuItem gitURL;
	@FXML
	private MenuItem jiraURL;
	
	@FXML
	private void handleEditDevOrgCredentials() throws IOException {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(HomePage.class.getResource("ui/SFDevLogin.fxml"));
        AnchorPane pmoLoginPane = (AnchorPane) loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("SF DEV Login");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        //dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(pmoLoginPane);
        dialogStage.setScene(scene);

        // Set the person into the controller.
        SFDEVLoginController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        dialogStage.showAndWait();
	}
	
	@FXML
	private void editGITCredentials() throws IOException {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(HomePage.class.getResource("ui/GITLogin.fxml"));
        AnchorPane pmoLoginPane = (AnchorPane) loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("GIT Login");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        //dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(pmoLoginPane);
        dialogStage.setScene(scene);

        GITLoginController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();
	}
	
	public void editJIRALoginPage() throws IOException {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(HomePage.class.getResource("ui/JiraLogin.fxml"));
        AnchorPane pmoLoginPane = (AnchorPane) loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("JIRA Login");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        Scene scene = new Scene(pmoLoginPane);
        dialogStage.setScene(scene);

        // Set the person into the controller.
        JIRALoginController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        //controller.setPerson(person);

        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();
	}
	
	@FXML
	public void gotoJIRA() {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(new URI("https://tracker.reisys.com/"));
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
		
	}
	
	@FXML
	public void gotoGIT() {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(new URI(SalesforcePMOConnection.getInstance().getGitEnvirnment().getURL__c()));
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}
	
}
