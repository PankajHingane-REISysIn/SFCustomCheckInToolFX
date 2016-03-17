package com.customcheckin.home.ui;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.customcheckin.home.HomePage;
import com.customcheckin.model.JIRATicket;
import com.customcheckin.model.MetadataFile;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.service.jira.TicketHelper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
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

public class HomeScreenController {
	private HomePage homePage;
	@FXML
	private TableView<MetadataFile> metadataFileList;
	@FXML
    private TableColumn<MetadataFile, String> metadataNameColumn;
	@FXML
    private TableColumn<MetadataFile, Boolean> metaDataChekBoxColumn;
	
	@FXML
	  private ComboBox<String> JIRATicketCombo;
	
	public HomeScreenController() {
		
	}
	
	@FXML
    private void initialize() {
		metadataNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
		
		metaDataChekBoxColumn.setCellValueFactory( new PropertyValueFactory<MetadataFile,Boolean>( "isSelected" ) );
		metaDataChekBoxColumn.setCellFactory( new Callback<TableColumn<MetadataFile,Boolean>, TableCell<MetadataFile,Boolean>>()
		{
		    @Override
		    public TableCell<MetadataFile,Boolean> call( TableColumn<MetadataFile,Boolean> param )
		    {
		    	return new CheckBoxTableCell<MetadataFile,Boolean>()
		        {
		            {
		                setAlignment( Pos.CENTER );
		            }
		            public void updateItem( BooleanProperty item, boolean empty )
		            {
		                if ( ! empty )
		                {
		                    TableRow  row = getTableRow();

		                    if ( row != null )
		                    {
		                        Integer rowNo = row.getIndex();
		                        TableViewSelectionModel  sm = getTableView().getSelectionModel();
		                        System.out.println("sm===="+ sm.getFocusedIndex());
		                        System.out.println("rowNo===="+ rowNo);
		                        if ( item.get() )  sm.select( rowNo );
		                        else  sm.clearSelection( rowNo );
		                    }
		                }

		                super.updateItem( item.getValue(), empty );
		            }
		        };
		    }
		} );
		metaDataChekBoxColumn.setEditable( true );
		metaDataChekBoxColumn.setMaxWidth( 50 );
		metaDataChekBoxColumn.setMinWidth( 50 );
    }
	
	@FXML
    private void handleGetJIRATicketOnClick() throws URISyntaxException, Exception {
    	System.out.println("=========Fetching");
    	JIRAConnection jira = new JIRAConnection("pankaj.hingane", "Ved@123.com");
		List<JIRATicket> JIRATicketList = new TicketHelper(jira.getConnection()).getOpenTicketList("State of MA - Internal", "pankaj.hingane");
		List<String> JIRATicketListToCombo = new ArrayList<String>();
		for(JIRATicket JIRATicket : JIRATicketList) {
			System.out.println("=========JIRATicket:" + JIRATicket.getId());
			JIRATicketListToCombo.add(JIRATicket.getId().get());
		}
		List<MetadataFile> metadataFileList = new ArrayList<>();
		metadataFileList.add(new MetadataFile(new SimpleStringProperty("sample4"), new SimpleBooleanProperty(false)));
		metadataFileList.add(new MetadataFile(new SimpleStringProperty("sample5"), new SimpleBooleanProperty(false)));
		homePage.getMetadataFileList().addAll(metadataFileList);
		homePage.getJiraTicketComboList().addAll(JIRATicketListToCombo);
		System.out.println("=========Completed");
    }
	
	public void setHomePage(HomePage homePage) {
		this.homePage = homePage;
		metadataFileList.setItems(homePage.getMetadataFileList());
		JIRATicketCombo.setItems(homePage.getJiraTicketComboList());
	}

}
