package com.customcheckin.home;

import java.io.IOException;

import com.customcheckin.home.ui.HomeScreenController;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class HomePage extends Application {
	private Stage primaryStage;
    private BorderPane rootLayout;

	@Override
	public void start(Stage primaryStage) throws IOException {
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Custom CheckIn Tool");
        initRootLayout();
        
        showHomePage();
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
        loader.setLocation(HomePage.class.getResource("ui/HomeScreen.fxml"));
        AnchorPane homeScreenPane = (AnchorPane) loader.load();

        // Set person overview into the center of root layout.
        rootLayout.setCenter(homeScreenPane);
        HomeScreenController controller = loader.getController();
        controller.setHomePage(this);
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
}
