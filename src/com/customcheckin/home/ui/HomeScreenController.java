package com.customcheckin.home.ui;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.customcheckin.home.HomePage;
import com.customcheckin.model.JiraTicket;
import com.customcheckin.model.MetadataFile;
import com.customcheckin.service.jira.JIRAConnection;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class HomeScreenController implements Initializable {
	private HomePage homePage;

	// metadata table
	@FXML
	private TableView<MetadataFile> metadataFileList;
	@FXML
	private TableColumn<MetadataFile, String> metadataNameColumn;
	@FXML
	private TableColumn<MetadataFile, Boolean> metaDataChekBoxColumn;

	// jira table
	@FXML
	private TableView<JiraTicket> jiraList;
	@FXML
	private TableColumn<JiraTicket, String> jiraNameColumn;
	@FXML
	private TableColumn<JiraTicket, Boolean> jiraChekBoxColumn;

	public HomeScreenController() {

	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// todo - load components using multi-threading
		initilizeMetadataTable();
		initilizeJiraTable();
		ConnectionManager.getAllConnections();
	}

	private void initilizeJiraTable() {
		jiraNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());

		jiraChekBoxColumn.setCellValueFactory(new PropertyValueFactory<JiraTicket, Boolean>("isSelected"));
		jiraChekBoxColumn
				.setCellFactory(new Callback<TableColumn<JiraTicket, Boolean>, TableCell<JiraTicket, Boolean>>() {
					@Override
					public TableCell<JiraTicket, Boolean> call(TableColumn<JiraTicket, Boolean> param) {
						return new CheckBoxTableCell<JiraTicket, Boolean>() {
							{
								setAlignment(Pos.CENTER);
							}

							public void updateItem(BooleanProperty item, boolean empty) {
								if (!empty) {
									TableRow row = getTableRow();

									if (row != null) {
										Integer rowNo = row.getIndex();
										TableViewSelectionModel sm = getTableView().getSelectionModel();
										System.out.println("sm====" + sm.getFocusedIndex());
										System.out.println("rowNo====" + rowNo);
										if (item.get())
											sm.select(rowNo);
										else
											sm.clearSelection(rowNo);
									}
								}

								super.updateItem(item.getValue(), empty);
							}
						};
					}
				});
		jiraChekBoxColumn.setEditable(true);
	}

	private void initilizeMetadataTable() {
		metadataNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());

		metaDataChekBoxColumn.setCellValueFactory(new PropertyValueFactory<MetadataFile, Boolean>("isSelected"));
		metaDataChekBoxColumn
				.setCellFactory(new Callback<TableColumn<MetadataFile, Boolean>, TableCell<MetadataFile, Boolean>>() {
					@Override
					public TableCell<MetadataFile, Boolean> call(TableColumn<MetadataFile, Boolean> param) {
						return new CheckBoxTableCell<MetadataFile, Boolean>() {
							{
								setAlignment(Pos.CENTER);
							}

							public void updateItem(BooleanProperty item, boolean empty) {
								if (!empty) {
									TableRow row = getTableRow();

									if (row != null) {
										Integer rowNo = row.getIndex();
										TableViewSelectionModel sm = getTableView().getSelectionModel();
										System.out.println("sm====" + sm.getFocusedIndex());
										System.out.println("rowNo====" + rowNo);
										if (item.get())
											sm.select(rowNo);
										else
											sm.clearSelection(rowNo);
									}
								}

								super.updateItem(item.getValue(), empty);
							}
						};
					}
				});
		metaDataChekBoxColumn.setEditable(true);
	}

	@FXML
	private void handleGetJiraTicketOnClick() throws URISyntaxException, Exception {
		List<JiraTicket> rickets = JIRAConnection.getInstance().getOpenTickets("GGP");
		homePage.getJiraTicketComboList().addAll(rickets);
	}

	@FXML
	private void handleGetMetadaOnClick() throws URISyntaxException, Exception {
		List<MetadataFile> metadataFileList = new ArrayList<>();
		metadataFileList.add(new MetadataFile(new SimpleStringProperty("sample4"), new SimpleBooleanProperty(false)));
		metadataFileList.add(new MetadataFile(new SimpleStringProperty("sample5"), new SimpleBooleanProperty(false)));
		homePage.getMetadataFileList().addAll(metadataFileList);
		System.out.println("=========Completed");

	}

	public void setHomePage(HomePage homePage) {
		this.homePage = homePage;
		metadataFileList.setItems(homePage.getMetadataFileList());
		jiraList.setItems(homePage.getJiraTicketComboList());
	}

}
