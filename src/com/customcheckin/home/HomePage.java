package com.customcheckin.home;

import java.io.IOException;

import com.customcheckin.home.ui.HomeScreenController;
import com.customcheckin.home.ui.PMOLoginController;
import com.customcheckin.model.MetadataFile;
import com.customcheckin.util.PropertyManager;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HomePage extends Application {
	private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<MetadataFile> metadataFileList = FXCollections.observableArrayList();
      
    private ObservableList<String> jiraTicketComboList = FXCollections.observableArrayList();

    public HomePage() {
    	//jiraTicketList.add(new JiraTicket(new SimpleStringProperty("Sample")));
    }
	@Override
	public void start(Stage primaryStage) throws IOException {
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Custom CheckIn Tool");
        initRootLayout();
        
        //
        if(PropertyManager.getInstance().getString("pmo.username").isEmpty()){
        	showPMOLoginPage();
        } else {
        	showHomePage();
        }
        //todo add condition for other logins
	}
	
	public void initRootLayout() throws IOException {
        //try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(HomePage.class.getResource("ui/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            
        /*} catch (IOException e) {
            e.printStackTrace();
        }*/
    }
	
	public void showHomePage() throws IOException {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(HomePage.class.getResource("ui/HomeScreen2.fxml"));
        AnchorPane homeScreenPane = (AnchorPane) loader.load();

        rootLayout.setCenter(homeScreenPane);
        HomeScreenController controller = loader.getController();
        controller.setHomePage(this);
	}
	
	public void showPMOLoginPage() throws IOException {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(HomePage.class.getResource("ui/PMOLogin.fxml"));
        AnchorPane pmoLoginPane = (AnchorPane) loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("PMO Login");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(pmoLoginPane);
        dialogStage.setScene(scene);

        // Set the person into the controller.
        PMOLoginController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        //controller.setPerson(person);

        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();
        
        /*rootLayout.setCenter(pmoLoginPane);
        PMOLoginController controller = loader.getController();
        controller.setHomePage(this);*/
	}
	
	public ObservableList<MetadataFile> getMetadataFileList() {
        return metadataFileList;
    }
	
	/**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
	public static void main(String[] args) {
		launch(args);
	}
	/**
	 * @return the jiraTicketComboList
	 */
	public ObservableList<String> getJiraTicketComboList() {
		return jiraTicketComboList;
	}
}
