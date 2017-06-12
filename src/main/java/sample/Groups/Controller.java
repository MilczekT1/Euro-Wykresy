package sample.Groups;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import sample.exceptions.GuiAccessException;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class Controller implements Initializable{
    //-------------------------------------------------------------------TABS
    @FXML Tab groupsTab;
    //-------------------------------------------------------------------GROUP MANAGER (RIGHT)
    @FXML
    private TableView<Gate> tableWithCurrentGates;
    private TableColumn<Gate,String>  currentGateDescription;
    private TableColumn<Gate,String>  currentShortDescription;
    private TableColumn<Gate,String>  currentGateType;
    private TableColumn<Gate,String>  currentGateMeasureType;
    private TableColumn<Gate,String>  currentGateId;
    private ObservableList<Gate> currentGates;
    
    @FXML
    private TableView<Gate> tableWithRemainingGates;
    private TableColumn<Gate,String> remainingGateDescription;
    private TableColumn<Gate,String> remainingShortDescription;
    private TableColumn<Gate,String> remainingGateType;
    private TableColumn<Gate,String> remainingGateMeasureType;
    private TableColumn<Gate,String> remainingGateId;
    //-------------------------------------------------------------------GROUP MANAGER (LEFT)
    @FXML CheckBox addGroupChecker;
    @FXML CheckBox editGroupChecker;
    @FXML TextField newGroupTextField;
    @FXML private Button confirmChangesButton;
    @FXML private Button addGroupButton;
    @FXML private ComboBox comboMenuEdit;
    private ObservableList<String> listToEdit;
    //-------------------------------------------------------------------LOGIN
    
    private static Integer accessLevel;
    @FXML TextField login_Login;
    @FXML TextField login_Password;
    @FXML Button login_LogInButton;
    @FXML Label login_Label;
    
    @FXML TextField register_Login;
    @FXML TextField register_Password;
    @FXML TextField register_RepeatedPassword;
    @FXML Button register_SignInButton;
    @FXML Label register_Label;
    
    public void loginUser(){
        String login = login_Login.getText();
        String password = login_Password.getText();
        login_Label.setText("");
        if(DBAuthenticator.tryToLoginAndReturnAccessType(login, DBAuthenticator.hashPassword(password), dataContainer)){
            accessLevel = dataContainer.getAccessLevel();
            login_Label.setText("Udane logowanie");
            if (accessLevel.equals(1))
                groupsTab.setDisable(false);
            else
                groupsTab.setDisable(true);
        }
        else {
            login_Label.setText("Nieudane logowanie");
        }
    }
    public void registerUser(){
        String login = register_Login.getText();
        String password = register_Password.getText();
        String repeatedPassword = register_RepeatedPassword.getText();
        register_Label.setText("");
        if (password.equals(repeatedPassword) &&
                    Pattern.matches("(\\w)+@(\\w)+\\.(\\w)+",login) &&
                    Pattern.matches("^\\S{6,100}",password)) {
            if (DBAuthenticator.tryToRegister(login, DBAuthenticator.hashPassword(password))) {
                register_Label.setDisable(false);
                register_Label.setText("udana rejestracja");
            } else {
                register_Label.setDisable(false);
                register_Label.setText("Nieudana próba rejestracji");
            }
        }
        else {
            register_Label.setDisable(false);
            register_Label.setText("Nieprawidlowe dane!!");
        }
    }
    //-------------------------------------------------------------------
    private GuiDataContainer dataContainer;
    
    public void addGroup(){
        try {
            String groupName = getNewGroupName();
            DBGroupManager.dbAddGroup(groupName);
    
            // fix GUI after not user-driven changes
            comboMenuEdit.getSelectionModel().clearSelection();
            listToEdit.clear();
            addComboBoxOptionsFromList(comboMenuEdit,listToEdit, DBGroupManager.dbGetAllExistingGroupNames());
            //simulate enabling "Edycja" with mouse
            editGroupChecker.setSelected(true);
            handleMouseClicked_OnEditChecker();
        } catch (GuiAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            // Communication error or tried to insert group with existing name.
            e.printStackTrace();
        }
    }
    public String getNewGroupName() throws GuiAccessException {
        if (newGroupTextField.isDisabled())
            if(comboMenuEdit.getSelectionModel().getSelectedItem() != null){
                return comboMenuEdit.getSelectionModel().getSelectedItem().toString();
            }
            else {
                throw new GuiAccessException("ERROD: nie wybrano grupy do edycji");
            }
        else {
            String result = newGroupTextField.getText();
            if(result != null && !result.equals(""))
                return newGroupTextField.getText();
            else
                throw new GuiAccessException("ERROR: pole jest puste");
        }
    }
    public void handleMouseClicked_ConfirmChanges(){
        try {
            dataContainer.setGroupId(DBGroupManager.dbGetGroupIdUsingGroupName(getNewGroupName()));
            for(Gate gate: tableWithCurrentGates.getItems()){
                // gate does not always contain groupId -> use dataContainer
                DBGroupManager.dbInsertCurrentGatesIntoGroup(gate.getGateId(), dataContainer.getGroupId());
            }
        } catch (GuiAccessException e) {
            e.printStackTrace();
        }
    }
    public void handleMouseClicked_OnAddChecker(){
        if (addGroupChecker.isSelected()) {
            if (editGroupChecker.isSelected()) {
                editGroupChecker.setSelected(false);
            }
            newGroupTextField.setDisable(false);
            comboMenuEdit.setDisable(true);
            comboMenuEdit.getSelectionModel().clearSelection();
            tableWithCurrentGates.getItems().clear();
        }else {
            if (!editGroupChecker.isSelected()){
                addGroupChecker.setSelected(true);
            }
            else {
                comboMenuEdit.setDisable(false);
                newGroupTextField.setDisable(true);
            }
        }
    }
    public void handleMouseClicked_OnEditChecker(){
        if (editGroupChecker.isSelected()) {
            if (addGroupChecker.isSelected()) {
                addGroupChecker.setSelected(false);
            }
            comboMenuEdit.setDisable(false);
            comboMenuEdit.getSelectionModel().clearSelection();
            newGroupTextField.setDisable(true);
            newGroupTextField.setText("");

        }
        else{
            if (!addGroupChecker.isSelected()){
                editGroupChecker.setSelected(true);
            }
        }
    }
    
    public void addComboBoxOptionsFromList(ComboBox box, ObservableList<String> list, List<String>  newOptions) {
        list.addAll(newOptions);
        box.setItems(list);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataContainer = GuiDataContainer.getInstance();
        listToEdit = FXCollections.observableArrayList();
        editGroupChecker.setSelected(true);
        
        initTables();
        groupsTab.setOnSelectionChanged( (e) -> {
            tableWithRemainingGates.getItems().clear();
            tableWithRemainingGates.getItems().addAll(DBGroupManager.dbGetAllGates());
        });
        groupsTab.setDisable(true);
    
        addComboBoxOptionsFromList(comboMenuEdit, listToEdit, DBGroupManager.dbGetAllExistingGroupNames());
        comboMenuEdit.setOnAction((e) -> {
            try {
                dataContainer.setCurrentGroupName(getNewGroupName());
                tableWithCurrentGates.setDisable(false);
                List<Gate> gates = DBGroupManager.dbGetAllGatesFromGroup(dataContainer.getCurrentGroupName());
                if (gates != null){
                    currentGates = FXCollections.observableList(gates);
    
                    ObservableList<Gate> distinctGates = FXCollections.observableArrayList(DBGroupManager.dbGetAllGates());
                    distinctGates.removeAll(currentGates);
    
                    tableWithCurrentGates.getItems().clear();
                    tableWithRemainingGates.getItems().clear();
    
                    tableWithCurrentGates.getItems().addAll(currentGates);
                    tableWithRemainingGates.getItems().addAll(distinctGates);
                } else {
                    tableWithCurrentGates.getItems().clear();
                }
            } catch (GuiAccessException e1) {
                e1.printStackTrace();
            }
        });
        newGroupTextField.setOnKeyReleased((e) -> {
            try {
                dataContainer.setCurrentGroupName(getNewGroupName());
            } catch (GuiAccessException e1) {
                e1.printStackTrace();
            }
        });
    }
    private void initTables(){
        currentGateDescription = new TableColumn<>("Description");
        currentShortDescription = new TableColumn("ShortDescription");
        currentGateType = new TableColumn("GateType");
        currentGateMeasureType = new TableColumn("MeasureType");
        currentGateId = new TableColumn<>("GateId");
    
        remainingGateDescription = new TableColumn<>("Description");
        remainingShortDescription = new TableColumn("ShortDescription");;
        remainingGateType = new TableColumn("GateType");
        remainingGateMeasureType = new TableColumn("MeasureType");
        remainingGateId = new TableColumn<>("GateId");
        
        currentGateDescription.setCellValueFactory(new PropertyValueFactory<Gate, String>("Description"));
        currentGateId.setCellValueFactory(new PropertyValueFactory<Gate, String>("GateId"));
        currentGateMeasureType.setCellValueFactory(new PropertyValueFactory<Gate, String>("MeasureType"));
        currentGateType.setCellValueFactory(new PropertyValueFactory<Gate, String>("GateType"));
        currentShortDescription.setCellValueFactory(new PropertyValueFactory<Gate, String>("ShortDescription"));
        
        remainingGateDescription.setCellValueFactory(new PropertyValueFactory<Gate, String>("Description"));
        remainingGateId.setCellValueFactory(new PropertyValueFactory<Gate, String>("GateId"));
        remainingGateMeasureType.setCellValueFactory(new PropertyValueFactory<Gate, String>("MeasureType"));
        remainingGateType.setCellValueFactory(new PropertyValueFactory<Gate, String>("GateType"));
        remainingShortDescription.setCellValueFactory(new PropertyValueFactory<Gate, String>("ShortDescription"));
        
        tableWithCurrentGates.getColumns().addAll(currentShortDescription, currentGateDescription, currentGateType, currentGateMeasureType, currentGateId);
        tableWithRemainingGates.getColumns().addAll(remainingShortDescription, remainingGateDescription, remainingGateType, remainingGateMeasureType, remainingGateId);
        
        tableWithCurrentGates.setDisable(true);
    
        tableWithRemainingGates.setRowFactory(new Callback<TableView<Gate>, TableRow<Gate>>() {
            @Override
            public TableRow<Gate> call(TableView<Gate> tableView) {
                final TableRow<Gate> row = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();
                final MenuItem removeMenuItem = new MenuItem("Dodaj do grupy");
                contextMenu.getItems().add(removeMenuItem);
                removeMenuItem.setOnAction((e) -> {
                    Gate lastAddedGate = tableWithRemainingGates.getItems().get(row.getIndex());
                    tableWithCurrentGates.getItems().add(lastAddedGate);
                    tableWithRemainingGates.getItems().remove(row.getItem());
                });
                // Set context menu on row, but use a binding to make it only show for non-empty rows:
                row.contextMenuProperty().bind(
                        Bindings.when(row.emptyProperty())
                                .then((ContextMenu)null)
                                .otherwise(contextMenu)
                );
                return row ;
            }
        });
        tableWithCurrentGates.setRowFactory(new Callback<TableView<Gate>, TableRow<Gate>>() {
            @Override
            public TableRow<Gate> call(TableView<Gate> tableView) {
                final TableRow<Gate> row = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();
                final MenuItem removeMenuItem = new MenuItem("Usuń z grupy");
                contextMenu.getItems().add(removeMenuItem);
                removeMenuItem.setOnAction((e) -> {
                    Gate lastRemovedGate = tableWithCurrentGates.getItems().get(row.getIndex());
                    tableWithRemainingGates.getItems().add(lastRemovedGate);
                    tableWithCurrentGates.getItems().remove(row.getItem());
                    tableWithCurrentGates.sort();
                });
                // Set context menu on row, but use a binding to make it only show for non-empty rows:
                row.contextMenuProperty().bind(
                        Bindings.when(row.emptyProperty())
                                .then((ContextMenu)null)
                                .otherwise(contextMenu)
                );
                return row ;
            }
        });
    }
    
}