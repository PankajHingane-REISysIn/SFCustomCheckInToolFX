package com.customcheckin.home;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.customcheckin.home.ui.DeploymentController;
import com.customcheckin.home.ui.GITLoginController;
import com.customcheckin.home.ui.HomeScreenController;
import com.customcheckin.home.ui.JIRALoginController;
import com.customcheckin.home.ui.PMOLoginController;
import com.customcheckin.home.ui.SFDEVLoginController;
import com.customcheckin.model.ConfigObject;
import com.customcheckin.model.ConfigRecord;
import com.customcheckin.model.JiraTicket;
import com.customcheckin.model.MetadataFile;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.util.PropertyManager;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class HomePage extends Application {
	private static Logger log = Logger.getRootLogger();
	private Stage primaryStage;
	private BorderPane rootLayout;
	private ObservableList<MetadataFile> metadataFileList = FXCollections.observableArrayList();
	private ObservableList<ConfigRecord> configRecordList = FXCollections.observableArrayList();

	private ObservableList<JiraTicket> jiraTicketComboList = FXCollections.observableArrayList();

	private ObservableList<ConfigObject> configObjComboList = FXCollections.observableArrayList();

	private ObservableList<String> envList = FXCollections.observableArrayList();

	private Pane splashLayout;
	private ProgressBar loadProgress;
	private Label progressText;
	// private Stage primaryStage;
	private static final int SPLASH_WIDTH = 676;
	private static final int SPLASH_HEIGHT = 227;

	public HomePage() {
	}

	@Override
	public void start(Stage primaryStage) throws IOException {

		if (PropertyManager.getInstance().getString("pmo.username").isEmpty()) {
			showPMOLoginPage();
		} else {
			final Task<ObservableList<String>> tasks = new Task<ObservableList<String>>() {
				@Override
				protected ObservableList<String> call() throws InterruptedException {
					ObservableList<String> tasks = FXCollections.<String> observableArrayList();
					updateMessage("Connecting PMO Org...");
					SalesforcePMOConnection.getInstance().getCurrentPMOUser();
					updateMessage("Connected to PMO Org.");

					return tasks;
				}
			};

			showSplash(primaryStage, tasks, () -> showMainStage(tasks.valueProperty()));
			new Thread(tasks).start();
		}
	}

	private void showMainStage(ReadOnlyObjectProperty<ObservableList<String>> tasks) {
		primaryStage = new Stage(StageStyle.DECORATED);
		primaryStage.setTitle("CM Tool");
		primaryStage.getIcons().add(new Image(HomePage.class.getResource("logo.png").toExternalForm()));

		final ListView<String> peopleView = new ListView<>();
		peopleView.itemsProperty().bind(tasks);

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(HomePage.class.getResource("ui/RootLayout.fxml"));
		try {
			rootLayout = (BorderPane) loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scene scene = new Scene(rootLayout);
		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
		primaryStage.show();

		try {
			showHomePage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void showSplash(final Stage initStage, Task<?> task, InitCompletionHandler initCompletionHandler) {
		progressText.textProperty().bind(task.messageProperty());
		loadProgress.progressProperty().bind(task.progressProperty());
		task.stateProperty().addListener((observableValue, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				loadProgress.progressProperty().unbind();
				loadProgress.setProgress(1);
				initStage.toFront();
				FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
				fadeSplash.setFromValue(1.0);
				fadeSplash.setToValue(0.0);
				fadeSplash.setOnFinished(actionEvent -> initStage.hide());
				fadeSplash.play();

				initCompletionHandler.complete();
			} // todo add code to gracefully handle other task states.
		});

		Scene splashScene = new Scene(splashLayout);
		initStage.initStyle(StageStyle.UNDECORATED);
		final Rectangle2D bounds = Screen.getPrimary().getBounds();
		initStage.setScene(splashScene);
		initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
		initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
		initStage.show();
	}

	public interface InitCompletionHandler {
		public void complete();
	}

	@Override
	public void init() {
		ImageView splash = new ImageView(new Image(HomePage.class.getResource("logo.png").toExternalForm()));
		loadProgress = new ProgressBar();
		loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
		progressText = new Label("Connecting to PMO Org . . .");
		splashLayout = new VBox();
		splashLayout.getChildren().addAll(splash, loadProgress, progressText);
		progressText.setAlignment(Pos.CENTER);
		splashLayout.setStyle(
				"-fx-padding: 5; " + "-fx-background-color: cornsilk; " + "-fx-border-width:5; " + "-fx-border-color: "
						+ "linear-gradient(" + "to bottom, " + "chocolate, " + "derive(chocolate, 50%)" + ");");
		splashLayout.setEffect(new DropShadow());
	}

	public void showHomePage() throws IOException {
		if (SalesforcePMOConnection.getInstance().isCurrentUserCMAdmin()) {
			loadDeploymentTool();
		} else {
			loadCheckInTool();
		}
	}

	private void loadCheckInTool() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(HomePage.class.getResource("ui/HomeScreen2.fxml"));
		AnchorPane homeScreenPane = (AnchorPane) loader.load();

		rootLayout.setCenter(homeScreenPane);
		HomeScreenController controller = loader.getController();
		controller.setHomePage(this);

	}

	private void loadDeploymentTool() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(HomePage.class.getResource("ui/DeploymentHome.fxml"));
		AnchorPane homeScreenPane = (AnchorPane) loader.load();

		rootLayout.setCenter(homeScreenPane);
		DeploymentController controller = loader.getController();
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

		PMOLoginController controller = loader.getController();
		controller.setDialogStage(dialogStage);

		// Show the dialog and wait until the user closes it
		dialogStage.showAndWait();

		showDEVLoginPage();
	}

	public void showDEVLoginPage() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(HomePage.class.getResource("ui/SFDevLogin.fxml"));
		AnchorPane pmoLoginPane = (AnchorPane) loader.load();

		Stage dialogStage = new Stage();
		dialogStage.setTitle("SF DEV Login");
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(primaryStage);
		Scene scene = new Scene(pmoLoginPane);
		dialogStage.setScene(scene);

		// Set the person into the controller.
		SFDEVLoginController controller = loader.getController();
		controller.setDialogStage(dialogStage);

		// Show the dialog and wait until the user closes it
		dialogStage.showAndWait();
		showJIRALoginPage();
	}

	public void showJIRALoginPage() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(HomePage.class.getResource("ui/JiraLogin.fxml"));
		AnchorPane pmoLoginPane = (AnchorPane) loader.load();

		Stage dialogStage = new Stage();
		dialogStage.setTitle("JIRA Login");
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(primaryStage);
		Scene scene = new Scene(pmoLoginPane);
		dialogStage.setScene(scene);

		// Set the person into the controller.
		JIRALoginController controller = loader.getController();
		controller.setDialogStage(dialogStage);

		// Show the dialog and wait until the user closes it
		dialogStage.showAndWait();
		showGITLoginPage();
	}

	public void showGITLoginPage() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(HomePage.class.getResource("ui/GITLogin.fxml"));
		AnchorPane pmoLoginPane = (AnchorPane) loader.load();

		Stage dialogStage = new Stage();
		dialogStage.setTitle("GIT Login");
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(primaryStage);
		Scene scene = new Scene(pmoLoginPane);
		dialogStage.setScene(scene);

		GITLoginController controller = loader.getController();
		controller.setDialogStage(dialogStage);
		// Show the dialog and wait until the user closes it
		dialogStage.showAndWait();
	}

	public ObservableList<MetadataFile> getMetadataFileList() {
		return metadataFileList;
	}

	public ObservableList<ConfigRecord> getConfigRecordList() {
		return configRecordList;
	}

	/**
	 * Returns the main stage.
	 * 
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
	public ObservableList<JiraTicket> getJiraTicketComboList() {
		return jiraTicketComboList;
	}

	public ObservableList<ConfigObject> getConfigObjComboList() {
		return configObjComboList;
	}

	public ObservableList<String> getEnvList() {
		return envList;
	}

	public void setEnvList(ObservableList<String> envList) {
		this.envList = envList;
	}
}
