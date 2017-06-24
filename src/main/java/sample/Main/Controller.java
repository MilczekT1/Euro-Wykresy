package sample.Main;

import com.google.common.base.Throwables;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import sample.Exceptions.GuiAccessException;
import sample.General.MyLogger;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Controller implements Initializable{
    //-------------------------------------------------------------------TABS
    @FXML Tab groupsTab;
    //-------------------------------------------------------------------GROUP MANAGER (RIGHT)
    @FXML
    private TableView<GroupGate> tableWithCurrentGates;
    private TableColumn<GroupGate,String>  currentGateDescription;
    private TableColumn<GroupGate,String>  currentShortDescription;
    private TableColumn<GroupGate,String>  currentGateType;
    private TableColumn<GroupGate,String>  currentGateMeasureType;
    private TableColumn<GroupGate,String>  currentGateId;
    private ObservableList<GroupGate> currentGroupGates;
    
    @FXML
    private TableView<GroupGate> tableWithRemainingGates;
    private TableColumn<GroupGate,String> remainingGateDescription;
    private TableColumn<GroupGate,String> remainingShortDescription;
    private TableColumn<GroupGate,String> remainingGateType;
    private TableColumn<GroupGate,String> remainingGateMeasureType;
    private TableColumn<GroupGate,String> remainingGateId;
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
    @FXML TitledPane loginPane;
    @FXML TitledPane registerPane;
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
            registerPane.setDisable(true);
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
                    Pattern.matches("(\\w)+_(\\w)",login) && // Name_LastName
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
    //-------------------------------------------------------------------GROUPS
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
            //TODO: sprawdzic, jesli uda sie wywolac, to metode wrzuci sie do initListeners
            //editGroupChecker.getOnMouseClicked();
        } catch (GuiAccessException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        } catch (SQLException e) {
            // Communication error or tried to insert group with existing name.
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
    }
    public String getNewGroupName() throws GuiAccessException {
        if (newGroupTextField.isDisabled())
            if(comboMenuEdit.getSelectionModel().getSelectedItem() != null){
                return comboMenuEdit.getSelectionModel().getSelectedItem().toString();
            }
            else {
                throw new GuiAccessException("ERROR: nie wybrano grupy do edycji");
            }
        else {
            String result = newGroupTextField.getText();
            if(result != null && !result.equals(""))
                return newGroupTextField.getText();
            else
                throw new GuiAccessException("ERROR: pole jest puste");
        }
    }
    //TODO: zainicjowac te metody w initialize
    public void handleMouseClicked_ConfirmChanges(){
        try {
            dataContainer.setGroupId(DBGroupManager.dbGetGroupIdUsingGroupName(getNewGroupName()));
            for(GroupGate groupGate : tableWithCurrentGates.getItems()){
                // groupGate does not always contain groupId -> use dataContainer
                DBGroupManager.dbInsertCurrentGatesIntoGroup(groupGate.getGateId(), dataContainer.getGroupId());
            }
        } catch (GuiAccessException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
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
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataContainer = GuiDataContainer.getInstance();
        listToEdit = FXCollections.observableArrayList();
        editGroupChecker.setSelected(true);
    
        groupsTab.setDisable(true);
        
        initTables();
        initListeners();
        
        addComboBoxOptionsFromList(comboMenuEdit, listToEdit, DBGroupManager.dbGetAllExistingGroupNames());
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
        
        currentGateDescription.setCellValueFactory(new PropertyValueFactory<GroupGate, String>("Description"));
        currentGateId.setCellValueFactory(new PropertyValueFactory<GroupGate, String>("GateId"));
        currentGateMeasureType.setCellValueFactory(new PropertyValueFactory<GroupGate, String>("MeasureType"));
        currentGateType.setCellValueFactory(new PropertyValueFactory<GroupGate, String>("GateType"));
        currentShortDescription.setCellValueFactory(new PropertyValueFactory<GroupGate, String>("ShortDescription"));
        
        remainingGateDescription.setCellValueFactory(new PropertyValueFactory<GroupGate, String>("Description"));
        remainingGateId.setCellValueFactory(new PropertyValueFactory<GroupGate, String>("GateId"));
        remainingGateMeasureType.setCellValueFactory(new PropertyValueFactory<GroupGate, String>("MeasureType"));
        remainingGateType.setCellValueFactory(new PropertyValueFactory<GroupGate, String>("GateType"));
        remainingShortDescription.setCellValueFactory(new PropertyValueFactory<GroupGate, String>("ShortDescription"));
        
        tableWithCurrentGates.getColumns().addAll(currentShortDescription, currentGateDescription, currentGateType, currentGateMeasureType, currentGateId);
        tableWithRemainingGates.getColumns().addAll(remainingShortDescription, remainingGateDescription, remainingGateType, remainingGateMeasureType, remainingGateId);
        
        tableWithCurrentGates.setDisable(true);
    }
    private void initListeners(){
        tableWithRemainingGates.setRowFactory(new Callback<TableView<GroupGate>, TableRow<GroupGate>>() {
            @Override
            public TableRow<GroupGate> call(TableView<GroupGate> tableView) {
                final TableRow<GroupGate> row = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();
                final MenuItem removeMenuItem = new MenuItem("Dodaj do grupy");
                contextMenu.getItems().add(removeMenuItem);
                removeMenuItem.setOnAction((e) -> {
                    GroupGate lastAddedGroupGate = tableWithRemainingGates.getItems().get(row.getIndex());
                    tableWithCurrentGates.getItems().add(lastAddedGroupGate);
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
        tableWithCurrentGates.setRowFactory(new Callback<TableView<GroupGate>, TableRow<GroupGate>>() {
            @Override
            public TableRow<GroupGate> call(TableView<GroupGate> tableView) {
                final TableRow<GroupGate> row = new TableRow<>();
                final ContextMenu contextMenu = new ContextMenu();
                final MenuItem removeMenuItem = new MenuItem("Usuń z grupy");
                contextMenu.getItems().add(removeMenuItem);
                removeMenuItem.setOnAction((e) -> {
                    GroupGate lastRemovedGroupGate = tableWithCurrentGates.getItems().get(row.getIndex());
                    tableWithRemainingGates.getItems().add(lastRemovedGroupGate);
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
    
        groupsTab.setOnSelectionChanged( (e) -> {
            tableWithRemainingGates.getItems().clear();
            tableWithRemainingGates.getItems().addAll(DBGroupManager.dbGetAllGates());
        });
    
        comboMenuEdit.setOnAction((e) -> {
            try {
                dataContainer.setCurrentGroupName(getNewGroupName());
                tableWithCurrentGates.setDisable(false);
                List<GroupGate> groupGates = DBGroupManager.dbGetAllGatesFromGroup(dataContainer.getCurrentGroupName());
                if (groupGates != null){
                    currentGroupGates = FXCollections.observableList(groupGates);
                
                    ObservableList<GroupGate> distinctGroupGates = FXCollections.observableArrayList(DBGroupManager.dbGetAllGates());
                    distinctGroupGates.removeAll(currentGroupGates);
                
                    tableWithCurrentGates.getItems().clear();
                    tableWithRemainingGates.getItems().clear();
                
                    tableWithCurrentGates.getItems().addAll(currentGroupGates);
                    tableWithRemainingGates.getItems().addAll(distinctGroupGates);
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
    public void addComboBoxOptionsFromList(ComboBox box, ObservableList<String> list, List<String>  newOptions) {
        list.addAll(newOptions);
        box.setItems(list);
    }
}