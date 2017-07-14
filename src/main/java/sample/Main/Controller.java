package sample.Main;

import com.google.common.base.Throwables;
import com.jfoenix.controls.*;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.xy.XYDataset;

import sample.Exceptions.GuiAccessException;
import sample.General.MyLogger;
import sample.General.ThreadPool;
import sample.General.Utils;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.regex.Pattern;

public final class Controller implements Initializable {
    private static Controller instance;
    static Controller getInstance() {
        return instance;
    }
    
    //-------------------------------------------------------------------TABS
    @FXML private Tab groupsTab;
    @FXML private Tab chartsTab;
    //-------------------------------------------------------------------CHARTS
    @FXML private JFXComboBox comboChooseGroup;
    @FXML private JFXComboBox comboOnChartGates;
    @FXML private JFXComboBox comboNotOnChartGates;
    private ObservableList<String> listToChoose;
    private ObservableList<String> gatesOnChartList;
    private ObservableList<String> gatesNotOnChartList;
    @FXML private JFXTimePicker startTimePicker;
    @FXML private JFXTimePicker endTimePicker;
    @FXML private JFXDatePicker startDatePicker;
    @FXML private JFXDatePicker endDatePicker;
    @FXML private JFXButton loadDataButton;
    @FXML private JFXProgressBar progressBar;
    @FXML private ProgressIndicator progressIndicator;
    
    @FXML private AnchorPane topChartPane;
    @FXML private AnchorPane bottomChartPane;
    private JFreeChart analogChart;
    private JFreeChart digitalChart;
    private ChartViewer topChartViewer;
    private ChartViewer bottomChartViewer;
    
    private XYDataset xyAnalogDataset;
    private XYDataset xyDigitalDataset;
    
    public void importChartData() {
        Runtime.getRuntime().gc();
        LocalDate startDate = LocalDate.of(2017,2,1); //startDatePicker.getValue();
        LocalTime startTime = LocalTime.of(12,0); //startTimePicker.getValue();
        LocalDate endDate = LocalDate.of(2017,3,1); //endDatePicker.getValue();
        LocalTime endTime = LocalTime.of(12,0); //endTimePicker.getValue();
        
        Timestamp startPoint = Timestamp.valueOf(LocalDateTime.of(startDate, startTime));
        Timestamp endPoint = Timestamp.valueOf(LocalDateTime.of(endDate, endTime));
        
        if(startPoint.after(endPoint)) {
            Utils.showMessageDialog("Punkt początkowy jest później niż końcowy");
            //todo set default value
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            startTimePicker.setValue(null);
            endTimePicker.setValue(null);
            return;
        }
        
        ArrayList<DBDataImporter> importers = new ArrayList<>(20);//TODO: size from configuration
        for (int i = 0; i < dataContainer.chartGroupGates.size(); i++) {
            String gateId = dataContainer.chartGroupGates.get(i).getGateId();
            importers.add(new DBDataImporter(gateId, startPoint.getTime(), endPoint.getTime()));
        }
        ExecutorService executorService = ThreadPool.getInstance();
        for (DBDataImporter importer : importers) {
            executorService.execute(importer);
        }
        //unlock comboBoxes after 100% import
        executorService.execute(()-> {
            while(progressBar.getProgress() != 1){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    MyLogger.getLogger().log(Level.WARNING,Throwables.getStackTraceAsString(e).trim());
                }
            }
            comboChooseGroup.setDisable(false);
            comboNotOnChartGates.setDisable(false);
            comboOnChartGates.setDisable(false);
        });
        
        comboChooseGroup.setDisable(true);
        loadDataButton.setDisable(true);
        //todo
        //System.out.println(new Timestamp(1485979049614L).toLocalDateTime().toString());
    }
    //-------------------------------------------------------------------LOGIN & REGISTER
    private static Integer accessLevel;
    @FXML private JFXTextField login_Login;
    @FXML private JFXPasswordField login_Password;
    @FXML private Label login_Label;
    @FXML private Label loggedUser_Label;
    
    @FXML private JFXTextField register_Login;
    @FXML private JFXPasswordField register_Password;
    @FXML private JFXPasswordField register_RepeatedPassword;
    @FXML private Label register_Label;
    @FXML
    public void loginUserAndSetGuiData() {
        String login = login_Login.getText();
        String password = login_Password.getText();
        login_Label.setText("");
        DBAuthenticator.getInstance().connectIfNullOrClosed();
        if (DBAuthenticator.tryToLoginAndReturnAccessType(login, DBAuthenticator.hashPassword(password), dataContainer)) {
            accessLevel = dataContainer.getAccessLevel();
            login_Label.setTextFill(Paint.valueOf("green"));
            login_Label.setText("Udane logowanie");
            if (accessLevel.equals(1)) {
                groupsTab.setDisable(false);
            } else {
                groupsTab.setDisable(true);
            }
            //Init gui data
            DBGroupManager.getInstance().connectIfNullOrClosed();
            addComboBoxOptionsFromList(comboMenuEdit, listToEdit, DBGroupManager.dbGetAllExistingGroupNames());
            addComboBoxOptionsFromList(comboGroupToDelete,listToEdit);
            addComboBoxOptionsFromList(comboChooseGroup, listToChoose, DBGroupManager.dbGetAllExistingGroupNames());
            
            chartsTab.setDisable(false);
            loggedUser_Label.setText("Zalogowany: " + login_Login.getText());
            login_Login.clear();
            login_Password.clear();
            
            DBAuthenticator.getInstance().closeConnection();
        } else {
            login_Label.setTextFill(Paint.valueOf("red"));
            login_Label.setText("Nieudane logowanie");
            login_Password.clear();
        }
    }
    @FXML
    public void registerUser() {
        String login = register_Login.getText();
        String password = register_Password.getText();
        String repeatedPassword = register_RepeatedPassword.getText();
        register_Label.setText("");
        if (password.equals(repeatedPassword) && Pattern.matches("\\w+_\\w+", login) && // Name_LastName
                    Pattern.matches("^\\S{6,100}", password)) {
            DBAuthenticator.getInstance().connectIfNullOrClosed();
            if (DBAuthenticator.tryToRegister(login, DBAuthenticator.hashPassword(password))) {
                register_Label.setDisable(false);
                register_Label.setTextFill(Paint.valueOf("green"));
                register_Label.setText("Udana rejestracja");
            } else {
                register_Label.setDisable(false);
                register_Label.setTextFill(Paint.valueOf("red"));
                register_Label.setText("Nieudana próba rejestracji");
            }
        } else {
            register_Label.setDisable(false);
            register_Label.setTextFill(Paint.valueOf("red"));
            register_Label.setText("Nieprawidlowe dane!!");
        }
        register_Password.clear();
        register_RepeatedPassword.clear();
    }
    
    //-------------------------------------------------------------------GROUP MANAGER
    @FXML private CheckBox addGroupChecker;
    @FXML private CheckBox editGroupChecker;
    @FXML private JFXButton addGroupButton;
    @FXML private JFXButton deleteGroupButton;
    @FXML private JFXButton confirmChangesButton;
    @FXML private JFXComboBox comboGroupToDelete;
    @FXML private JFXComboBox comboMenuEdit;
    @FXML private JFXTextField newGroupTextField;
    @FXML private JFXTextField filterTextField;
    private ObservableList<String> listToEdit;
    private ObservableList<GroupGate> distinctGroupGates;
    private FilteredList<GroupGate> filteredGroupGates;
    private SortedList<GroupGate> sortedData;
    
    @FXML private TableView<GroupGate> tableWithCurrentGates;
    @FXML private TableView<GroupGate> tableWithRemainingGates;
    private ObservableList<GroupGate> currentGroupGates;
    //-------------------------------------------------------------------
    
    private GuiDataContainer dataContainer;
    
    private String getNewGroupName() throws GuiAccessException {
        if (newGroupTextField.isDisabled())
            if (comboMenuEdit.getSelectionModel().getSelectedItem() != null) {
                return getSelectedItemFrom(comboMenuEdit);
            } else {
                throw new GuiAccessException("ERROR: nie wybrano grupy do edycji");
            }
        else {
            String result = newGroupTextField.getText();
            if (result != null && !result.equals(""))
                return newGroupTextField.getText();
            else
                throw new GuiAccessException("ERROR: pole jest puste");
        }
    }
    
    public void handleMouseClicked_OnAddChecker() {
        if (addGroupChecker.isSelected()) {
            if (editGroupChecker.isSelected()) {
                editGroupChecker.setSelected(false);
            }
            newGroupTextField.setDisable(false);
            addGroupButton.setDisable(false);
            comboGroupToDelete.setDisable(false);
            comboGroupToDelete.getSelectionModel().clearSelection();
            deleteGroupButton.setDisable(false);
            
            comboMenuEdit.setDisable(true);
            comboMenuEdit.getSelectionModel().clearSelection();
            confirmChangesButton.setDisable(true);
            
            tableWithCurrentGates.getItems().clear();
            
        } else {
            if (!editGroupChecker.isSelected()) {
                addGroupChecker.setSelected(true);
            } else {
                comboMenuEdit.setDisable(false);
                newGroupTextField.setDisable(true);
            }
        }
    }
    public void handleMouseClicked_OnEditChecker() {
        if (editGroupChecker.isSelected()) {
            if (addGroupChecker.isSelected()) {
                addGroupChecker.setSelected(false);
            }
            comboMenuEdit.setDisable(false);
            comboMenuEdit.getSelectionModel().clearSelection();
            confirmChangesButton.setDisable(false);
            
            newGroupTextField.setDisable(true);
            newGroupTextField.setText("");
            addGroupButton.setDisable(true);
            comboGroupToDelete.setDisable(true);
            deleteGroupButton.setDisable(true);
        } else {
            if (!addGroupChecker.isSelected()) {
                editGroupChecker.setSelected(true);
            }
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        initVariables();
        initTables();
        initCharts();
        initListeners();
    }
    private void initVariables(){
        listToEdit                  = FXCollections.observableArrayList();
        listToChoose                = FXCollections.observableArrayList();
        distinctGroupGates          = FXCollections.observableArrayList();
        gatesNotOnChartList         = FXCollections.observableArrayList();
        gatesOnChartList            = FXCollections.observableArrayList();
        
        dataContainer = GuiDataContainer.getInstance();
        editGroupChecker.setSelected(true);
        newGroupTextField.setDisable(true);
    
        addGroupButton.setDisable(true);
        deleteGroupButton.setDisable(true);
        comboGroupToDelete.setDisable(true);
    
        groupsTab.setDisable(true);
        chartsTab.setDisable(true);
    
        startTimePicker.setIs24HourView(true);
        endTimePicker.setIs24HourView(true);
        comboOnChartGates.setDisable(true);
        comboNotOnChartGates.setDisable(true);
        loadDataButton.setDisable(true);
    }
    private void initTables() {
        TableColumn<GroupGate, String> currentGateDescription = new TableColumn<>("Description");
        TableColumn<GroupGate, String> currentShortDescription = new TableColumn("ShortDescription");
        TableColumn<GroupGate, String> currentGateType = new TableColumn("GateType");
        TableColumn<GroupGate, String> currentGateMeasureType = new TableColumn("MeasureType");
        TableColumn<GroupGate, String> currentGateId = new TableColumn<>("GateId");
        
        TableColumn<GroupGate, String> remainingGateDescription = new TableColumn<>("Description");
        TableColumn<GroupGate, String> remainingShortDescription = new TableColumn("ShortDescription");
        TableColumn<GroupGate, String> remainingGateType = new TableColumn("GateType");
        TableColumn<GroupGate, String> remainingGateMeasureType = new TableColumn("MeasureType");
        TableColumn<GroupGate, String> remainingGateId = new TableColumn<>("GateId");
        
        currentGateDescription.setCellValueFactory(new PropertyValueFactory<>("Description"));
        currentGateId.setCellValueFactory(new PropertyValueFactory<>("GateId"));
        currentGateMeasureType.setCellValueFactory(new PropertyValueFactory<>("MeasureType"));
        currentGateType.setCellValueFactory(new PropertyValueFactory<>("GateType"));
        currentShortDescription.setCellValueFactory(new PropertyValueFactory<>("ShortDescription"));
        
        remainingGateDescription.setCellValueFactory(new PropertyValueFactory<>("Description"));
        remainingGateId.setCellValueFactory(new PropertyValueFactory<>("GateId"));
        remainingGateMeasureType.setCellValueFactory(new PropertyValueFactory<>("MeasureType"));
        remainingGateType.setCellValueFactory(new PropertyValueFactory<>("GateType"));
        remainingShortDescription.setCellValueFactory(new PropertyValueFactory<>("ShortDescription"));
        
        tableWithCurrentGates.getColumns().addAll(currentShortDescription, currentGateDescription, currentGateType, currentGateMeasureType, currentGateId);
        tableWithRemainingGates.getColumns().addAll(remainingShortDescription, remainingGateDescription, remainingGateType, remainingGateMeasureType, remainingGateId);
        
        tableWithCurrentGates.setDisable(true);
    }
    private void initCharts(){
        createDefaultAnalogChartInAnalogChartViewer("Wykres Analogowy", "Wartosc");
        createDefaultDigitalChartInDigitalChartViewer("Wykres Cyfrowy", "Wartosc");
        
        topChartViewer.prefHeightProperty().bind(topChartPane.heightProperty());
        topChartPane.getChildren().add(topChartViewer);
        
        bottomChartViewer.prefHeightProperty().bind(bottomChartPane.heightProperty());
        bottomChartPane.getChildren().add(bottomChartViewer);
    }
    private void initListeners() {
        //groups
        tableWithRemainingGates.setRowFactory(tableView -> {
            final TableRow<GroupGate> row = new TableRow<>();
            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem removeMenuItem = new MenuItem("Dodaj do grupy");
            contextMenu.getItems().add(removeMenuItem);
            removeMenuItem.setOnAction((e) -> {
                GroupGate lastAddedGroupGate = tableWithRemainingGates.getItems().get(row.getIndex());
                tableWithCurrentGates.getItems().add(lastAddedGroupGate);
                tableWithCurrentGates.sort();
                //tableWithRemainingGates.getItems().remove(row.getItem());
                
                distinctGroupGates.remove(lastAddedGroupGate);
                filteredGroupGates = new FilteredList<>(distinctGroupGates, p -> true);
                sortedData = new SortedList<>(filteredGroupGates);
                sortedData.comparatorProperty().bind(tableWithRemainingGates.comparatorProperty());
                tableWithRemainingGates.setItems(sortedData);
                tableWithRemainingGates.sort();
            });
            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
            return row;
        });
        tableWithCurrentGates.setRowFactory((TableView<GroupGate> tableView) -> {
            final TableRow<GroupGate> row = new TableRow<>();
            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem removeMenuItem = new MenuItem("Usuń z grupy");
            contextMenu.getItems().add(removeMenuItem);
            removeMenuItem.setOnAction((e) -> {
                GroupGate lastRemovedGroupGate = tableWithCurrentGates.getItems().get(row.getIndex());
                tableWithCurrentGates.getItems().remove(row.getItem());
                tableWithCurrentGates.sort();
                
                List<GroupGate> groupGates = tableWithCurrentGates.getItems();
                groupGates.remove(lastRemovedGroupGate);
                if (groupGates != null) {
                    currentGroupGates = FXCollections.observableList(groupGates);
        
                    distinctGroupGates = FXCollections.observableArrayList(DBGroupManager.dbGetAllGates());
                    distinctGroupGates.removeAll(currentGroupGates);
                    filteredGroupGates = new FilteredList<>(distinctGroupGates, p -> true);
        
                    sortedData = new SortedList<>(filteredGroupGates);
                    sortedData.comparatorProperty().bind(tableWithRemainingGates.comparatorProperty());
                    tableWithCurrentGates.setItems(currentGroupGates);
                    tableWithRemainingGates.setItems(sortedData);
                }
            });
            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
            return row;
        });
    
        filterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredGroupGates.setPredicate(groupGate -> {
                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (groupGate.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches description.
                } else if (groupGate.getShortDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches short description.
                } else if (groupGate.getGateId().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches gateId.
                }
                else if (groupGate.getGateType().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches gateType.
                }
                else if (groupGate.getMeasureType().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches measureType.
                }
                return false; // Does not match.
            });
        });
        
        deleteGroupButton.setOnMouseClicked(e1 ->{
            String nameOfGroupToDelete = getSelectedItemFrom(comboGroupToDelete);
            String groupId = DBGroupManager.dbGetGroupIdUsingGroupName(nameOfGroupToDelete);
            if (groupId != null){
                if (DBGroupManager.dbDeleteGroupUsingGroupId(groupId)){
                    List<String> allExistingGroupNames = DBGroupManager.dbGetAllExistingGroupNames();
                    comboMenuEdit.getSelectionModel().clearSelection();
                    comboGroupToDelete.getSelectionModel().clearSelection();
                    comboChooseGroup.getSelectionModel().clearSelection();
                    listToEdit.clear();
                    listToChoose.clear();
                    
                    addComboBoxOptionsFromList(comboMenuEdit, listToEdit, allExistingGroupNames);
                    addComboBoxOptionsFromList(comboGroupToDelete, listToEdit);
                    addComboBoxOptionsFromList(comboChooseGroup, listToChoose, allExistingGroupNames);
                }
            }
        });
        comboMenuEdit.setOnAction((e) -> {
            try {
                dataContainer.setCurrentGroupName(getNewGroupName());
                tableWithCurrentGates.setDisable(false);
                List<GroupGate> groupGates = DBGroupManager.dbGetAllGatesFromGroup(dataContainer.getCurrentGroupName());
                if (groupGates != null) {
                    currentGroupGates = FXCollections.observableList(groupGates);
    
                    distinctGroupGates = FXCollections.observableArrayList(DBGroupManager.dbGetAllGates());
                    distinctGroupGates.removeAll(currentGroupGates);
                    filteredGroupGates = new FilteredList<>(distinctGroupGates, p -> true);
                    
                    sortedData = new SortedList<>(filteredGroupGates);
                    sortedData.comparatorProperty().bind(tableWithRemainingGates.comparatorProperty());
                    tableWithCurrentGates.setItems(currentGroupGates);
                    tableWithRemainingGates.setItems(sortedData);
                } else {
                    tableWithCurrentGates.getItems().clear();
                }
            } catch (GuiAccessException e1) {
                MyLogger.getLogger().log(Level.WARNING,Throwables.getStackTraceAsString(e1).trim());
            }
        });
        newGroupTextField.setOnKeyReleased((e) -> {
            try {
                dataContainer.setCurrentGroupName(getNewGroupName());
            } catch (GuiAccessException e1) {
                MyLogger.getLogger().log(Level.WARNING,Throwables.getStackTraceAsString(e1).trim());
            }
        });
    
        confirmChangesButton.setOnMouseClicked((event) ->{
            try {
                //TODO: batch
                dataContainer.setGroupId(DBGroupManager.dbGetGroupIdUsingGroupName(getNewGroupName()));
                DBGroupManager.dbRemoveAllGatesFromGroup(dataContainer.getGroupId());
                for (GroupGate groupGate : tableWithCurrentGates.getItems()) {
                    // groupGate does not always contain groupId -> use dataContainer
                    DBGroupManager.dbInsertCurrentGatesIntoGroup(groupGate.getGateId(), dataContainer.getGroupId());
                }
                ////dataContainer.getGateIdsToDeleteFromGroup().clear();
            } catch (GuiAccessException | SQLException e) {
                MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            }
        });
        addGroupButton.setOnMouseClicked((e2) ->{
            try {
                String groupName = getNewGroupName();
                DBGroupManager.dbAddGroup(groupName);
        
                // fix GUI after not user-driven changes
                comboMenuEdit.getSelectionModel().clearSelection();
                comboChooseGroup.getSelectionModel().clearSelection();
                listToEdit.clear();
                listToChoose.clear();
                
                List<String> allExistingGroupNames = DBGroupManager.dbGetAllExistingGroupNames();
                addComboBoxOptionsFromList(comboMenuEdit, listToEdit, allExistingGroupNames);
                addComboBoxOptionsFromList(comboGroupToDelete, listToEdit);
                addComboBoxOptionsFromList(comboChooseGroup, listToChoose, allExistingGroupNames);
        
                //simulate enabling "Edycja" with mouse
                editGroupChecker.setSelected(true);
                handleMouseClicked_OnEditChecker();
        
            } catch (GuiAccessException | SQLException e) {
                MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            }
        });
        //charts
        comboChooseGroup.setOnAction((e) -> {
            gatesNotOnChartList.clear();
            gatesOnChartList.clear();
            
            dataContainer.setChartGroupGates(DBGroupManager.dbGetAllGatesFromGroup(
                    getSelectedItemFrom(comboChooseGroup)));
            GuiDataContainer.getAllChartData().clear();
            
            ArrayList<String> groupGatesNames = new ArrayList<>();
            for (GroupGate gate : dataContainer.getChartGroupGates()) {
                groupGatesNames.add(gate.getDescription());
            }
            
            addComboBoxOptionsFromList(comboNotOnChartGates, gatesNotOnChartList, groupGatesNames);
            
            loadDataButton.setDisable(false);
            comboOnChartGates.setDisable(true);
            comboNotOnChartGates.setDisable(true);
            setProgress(0);
            
            //Clear charts
            createDefaultAnalogChartInAnalogChartViewer("Wykres Analogowy", "Wartosc");
            createDefaultDigitalChartInDigitalChartViewer("Wykres Cyfrowy", "Wartosc");
        });
        
        // Add gateData to chart
        comboNotOnChartGates.setOnHiding((e) -> {
            if (comboNotOnChartGates.getSelectionModel().getSelectedItem() != null) {
                String selectedGateToAdd = getSelectedItemFrom(comboNotOnChartGates);
                String gateId = dataContainer.getGateIdUsingDescription(selectedGateToAdd);
                String gateType = dataContainer.getGateTypeUsingDescription(selectedGateToAdd);
                
                for (GateData gateData : GuiDataContainer.getAllChartData()) {
                    if (gateData.getGateId().equals(gateId)) {
                        try {
                            addDatasetAndStandardXYItemRendererToChart(gateType, selectedGateToAdd, gateData);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        break;
                    }
                }
                
                addComboBoxOptionsFromList(comboOnChartGates, gatesOnChartList, Collections.singletonList(selectedGateToAdd));
                comboNotOnChartGates.getItems().remove(selectedGateToAdd);
                comboNotOnChartGates.getSelectionModel().clearSelection();
            }
        });
        // do usuniecia wykresu
        comboOnChartGates.setOnHiding((e) -> {
            if (comboOnChartGates.getSelectionModel().getSelectedItem() != null) {
                String selectedGateToRemove = getSelectedItemFrom(comboOnChartGates);
                String gateTypeOfGateToRemove = dataContainer.getGateTypeUsingDescription(selectedGateToRemove);
                
                if (gateTypeOfGateToRemove.equals("A")){
                    createDefaultAnalogChartInAnalogChartViewer("Wykres Analogowy", "Wartosc");
                } else if (gateTypeOfGateToRemove.equals("D")){
                    createDefaultDigitalChartInDigitalChartViewer("Wykres Cyfrowy", "Wartosc");
                }
                
                String selectedGateToAdd;
                String gateId;
                String gateTypeOfGateToAdd;
                for (Object x : comboOnChartGates.getItems()) {
                    selectedGateToAdd = x.toString();
                    if (selectedGateToRemove.equals(selectedGateToAdd)) { continue; }
                    
                    gateId = dataContainer.getGateIdUsingDescription(selectedGateToAdd);
                    gateTypeOfGateToAdd = dataContainer.getGateTypeUsingDescription(selectedGateToAdd);
                    
                    for (GateData gateData : GuiDataContainer.getAllChartData()) {
                        if (gateData.getGateId().equals(gateId)) {
                            if (!gateTypeOfGateToAdd.equals(gateTypeOfGateToRemove)){
                                break;
                            } else{
                                try {
                                    addDatasetAndStandardXYItemRendererToChart(gateTypeOfGateToAdd, selectedGateToAdd, gateData);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                }
                addComboBoxOptionsFromList(comboNotOnChartGates, gatesNotOnChartList, Collections.singletonList(selectedGateToRemove));
                comboOnChartGates.getItems().remove(selectedGateToRemove);
                comboOnChartGates.getSelectionModel().clearSelection();
                Runtime.getRuntime().gc();
            }
        });
    }
    
    private void createDefaultAnalogChartInAnalogChartViewer(String title, String measureType){
        xyAnalogDataset = null;
        analogChart = Chart.createChart(xyAnalogDataset, title, measureType);
        analogChart.getXYPlot().getDomainAxis().setAutoRange(true);
        if (topChartViewer != null)
            topChartViewer.setChart(analogChart);
        else
            topChartViewer = new ChartViewer(analogChart);
    }
    private void createDefaultDigitalChartInDigitalChartViewer(String title, String measureType){
        xyDigitalDataset = null;
        digitalChart = Chart.createChart(xyDigitalDataset, title, measureType);
        digitalChart.getXYPlot().getDomainAxis().setAutoRange(true);
        if (bottomChartViewer != null)
            bottomChartViewer.setChart(digitalChart);
        else
            bottomChartViewer = new ChartViewer(digitalChart);
    }
    
    private String getSelectedItemFrom(JFXComboBox combo){
        return combo.getSelectionModel().getSelectedItem().toString();
    }
    private void addComboBoxOptionsFromList(ComboBox box, ObservableList<String> list, List<String> newOptions) {
        list.addAll(newOptions);
        list.sort(Comparator.naturalOrder());
        box.setItems(list);
    }
    private void addComboBoxOptionsFromList(ComboBox box, ObservableList<String> list) {
        list.sort(Comparator.naturalOrder());
        box.setItems(list);
    }
    private void addDatasetAndStandardXYItemRendererToChart(String gateType, String description, GateData gateData) throws Exception {
        if (gateType.equals("A") || gateType.equals("S")) {
            int index = analogChart.getXYPlot().getDatasetCount();
            analogChart.getXYPlot().setDataset(index, Chart.putGateValues(gateData, description, gateType));
            analogChart.getXYPlot().setRenderer(index, new StandardXYItemRenderer());
        } else if (gateType.equals("D")){
            int index = digitalChart.getXYPlot().getDatasetCount();
            digitalChart.getXYPlot().setDataset(index,Chart.putGateValues(gateData,description, gateType));
            digitalChart.getXYPlot().setRenderer(index, new XYStepRenderer());
        }
    }
    public synchronized void changeProgress() {
        double oldProgress = progressBar.getProgress();
        double denominator = (double) GuiDataContainer.getInstance().getChartGroupGates().size();
        double newProgress = oldProgress + (1 / denominator);
        dataContainer.getAmountOfProcessedThreads().value +=1;
        if (GuiDataContainer.getInstance().getAmountOfProcessedThreads().value == denominator){
            newProgress = 1;
            GuiDataContainer.getInstance().getAmountOfProcessedThreads().value = 0;
        }
        setProgress(newProgress);
    }
    private void setProgress(double newProgress){
        progressBar.setProgress(newProgress);
        progressIndicator.setProgress(newProgress);
    }
    
}
