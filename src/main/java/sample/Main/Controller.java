package sample.Main;

import com.google.common.base.Throwables;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTimePicker;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.data.xy.XYDataset;

import sample.Exceptions.GuiAccessException;
import sample.General.MyLogger;
import sample.General.ThreadPool;

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

public class Controller implements Initializable {
    private static Controller instance;
    protected static Controller getInstance() {
        return instance;
    }
    private static ExecutorService executorService = ThreadPool.getInstance();
    
    //-------------------------------------------------------------------TABS
    @FXML private Tab groupsTab;
    //-------------------------------------------------------------------CHARTS
    @FXML private ComboBox comboChooseGroup;
    @FXML private ComboBox comboOnChartGates;
    @FXML private ComboBox comboNotOnChartGates;
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
    protected ChartViewer topChartViewer;
    protected ChartViewer bottomChartViewer;
    
    protected XYDataset xyAnalogDataset;
    protected XYDataset xyDigitalDataset;
    
    public void importChartData() {
        //LocalDate startDate = startDatePicker.getValue();
        //LocalTime startTime = startTimePicker.getValue();
        LocalDate startDate = LocalDate.of(2017,1,1);
        LocalTime startTime = LocalTime.of(1,0);
        Timestamp startPoint = Timestamp.valueOf(LocalDateTime.of(startDate, startTime));
        System.out.println(startPoint.getTime());
        
        //LocalDate endDate = endDatePicker.getValue();
        //LocalTime endTime = endTimePicker.getValue();
        LocalDate endDate = LocalDate.of(2017,4,1);
        LocalTime endTime = LocalTime.of(1,0);
        Timestamp endPoint = Timestamp.valueOf(LocalDateTime.of(endDate, endTime));
        System.out.println(endPoint.getTime());
        
        LinkedList<DBDataImporter> importers = new LinkedList<>();
        for (int i = 0; i < dataContainer.chartGroupGates.size(); i++) {
            String gateId = dataContainer.chartGroupGates.get(i).getGateId();
            importers.add(new DBDataImporter(gateId, startPoint.getTime(), endPoint.getTime()));
        }
        
        executorService = ThreadPool.getInstance();
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
            comboNotOnChartGates.setDisable(false);
            comboOnChartGates.setDisable(false);
        });
        
        loadDataButton.setDisable(true);
        //System.out.println(new Timestamp(1485979049614L).toLocalDateTime().toString());
    }
    
    //-------------------------------------------------------------------LOGIN
    private static Integer accessLevel;
    @FXML private TitledPane registerPane;
    @FXML private TextField login_Login;
    @FXML private TextField login_Password;
    @FXML private Label login_Label;
    
    @FXML private TextField register_Login;
    @FXML private TextField register_Password;
    @FXML private TextField register_RepeatedPassword;
    @FXML private Label register_Label;
    
    @FXML private TitledPane configPane;
    
    public void loginUser() {
        String login = login_Login.getText();
        String password = login_Password.getText();
        login_Label.setText("");
        if (DBAuthenticator.tryToLoginAndReturnAccessType(login, DBAuthenticator.hashPassword(password), dataContainer)) {
            accessLevel = dataContainer.getAccessLevel();
            login_Label.setText("Udane logowanie");
            if (accessLevel.equals(1)) {
                groupsTab.setDisable(false);
                configPane.setDisable(false);
            } else {
                groupsTab.setDisable(true);
                configPane.setDisable(true);
            }
            registerPane.setDisable(true);
        } else {
            login_Label.setText("Nieudane logowanie");
        }
    }
    
    public void registerUser() {
        String login = register_Login.getText();
        String password = register_Password.getText();
        String repeatedPassword = register_RepeatedPassword.getText();
        register_Label.setText("");
        if (password.equals(repeatedPassword) && Pattern.matches("(\\w)+_(\\w)", login) && // Name_LastName
                    Pattern.matches("^\\S{6,100}", password)) {
            if (DBAuthenticator.tryToRegister(login, DBAuthenticator.hashPassword(password))) {
                register_Label.setDisable(false);
                register_Label.setText("udana rejestracja");
            } else {
                register_Label.setDisable(false);
                register_Label.setText("Nieudana próba rejestracji");
            }
        } else {
            register_Label.setDisable(false);
            register_Label.setText("Nieprawidlowe dane!!");
        }
    }
    
    //-------------------------------------------------------------------GROUP MANAGER (RIGHT)
    @FXML private TableView<GroupGate> tableWithCurrentGates;
    private TableColumn<GroupGate, String> currentGateDescription;
    private TableColumn<GroupGate, String> currentShortDescription;
    private TableColumn<GroupGate, String> currentGateType;
    private TableColumn<GroupGate, String> currentGateMeasureType;
    private TableColumn<GroupGate, String> currentGateId;
    private ObservableList<GroupGate> currentGroupGates;
    @FXML private TableView<GroupGate> tableWithRemainingGates;
    private TableColumn<GroupGate, String> remainingGateDescription;
    private TableColumn<GroupGate, String> remainingShortDescription;
    private TableColumn<GroupGate, String> remainingGateType;
    private TableColumn<GroupGate, String> remainingGateMeasureType;
    private TableColumn<GroupGate, String> remainingGateId;
    //-------------------------------------------------------------------GROUP MANAGER (LEFT)
    @FXML private CheckBox addGroupChecker;
    @FXML private CheckBox editGroupChecker;
    @FXML private TextField newGroupTextField;
    @FXML private ComboBox comboMenuEdit;
    private ObservableList<String> listToEdit;
    //-------------------------------------------------------------------GROUPS
    private GuiDataContainer dataContainer;
    
    public void addGroup() {
        try {
            String groupName = getNewGroupName();
            DBGroupManager.dbAddGroup(groupName);
    
            // fix GUI after not user-driven changes
            comboMenuEdit.getSelectionModel().clearSelection();
            listToEdit.clear();
            addComboBoxOptionsFromList(comboMenuEdit, listToEdit, DBGroupManager.dbGetAllExistingGroupNames());
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
    
    private String getNewGroupName() throws GuiAccessException {
        if (newGroupTextField.isDisabled())
            if (comboMenuEdit.getSelectionModel().getSelectedItem() != null) {
                return comboMenuEdit.getSelectionModel().getSelectedItem().toString();
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
    
    //TODO: zainicjowac te metody w initialize
    public void handleMouseClicked_ConfirmChanges() {
        try {
            dataContainer.setGroupId(DBGroupManager.dbGetGroupIdUsingGroupName(getNewGroupName()));
            for (GroupGate groupGate : tableWithCurrentGates.getItems()) {
                // groupGate does not always contain groupId -> use dataContainer
                DBGroupManager.dbInsertCurrentGatesIntoGroup(groupGate.getGateId(), dataContainer.getGroupId());
            }
        } catch (GuiAccessException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
    }
    
    public void handleMouseClicked_OnAddChecker() {
        if (addGroupChecker.isSelected()) {
            if (editGroupChecker.isSelected()) {
                editGroupChecker.setSelected(false);
            }
            newGroupTextField.setDisable(false);
            comboMenuEdit.setDisable(true);
            comboMenuEdit.getSelectionModel().clearSelection();
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
            newGroupTextField.setDisable(true);
            newGroupTextField.setText("");
        } else {
            if (!addGroupChecker.isSelected()) {
                editGroupChecker.setSelected(true);
            }
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataContainer = GuiDataContainer.getInstance();
        listToEdit = FXCollections.observableArrayList();
        listToChoose = FXCollections.observableArrayList();
        editGroupChecker.setSelected(true);
        groupsTab.setDisable(true);
        addComboBoxOptionsFromList(comboMenuEdit, listToEdit, DBGroupManager.dbGetAllExistingGroupNames());
        addComboBoxOptionsFromList(comboChooseGroup, listToChoose, DBGroupManager.dbGetAllExistingGroupNames());
        initTables();
        initListeners();
    
        gatesNotOnChartList = FXCollections.observableArrayList();
        gatesOnChartList = FXCollections.observableArrayList();
        startTimePicker.setIs24HourView(true);
        endTimePicker.setIs24HourView(true);
        comboOnChartGates.setDisable(true);
        comboNotOnChartGates.setDisable(true);
        loadDataButton.setDisable(true);
    
        configPane.setDisable(true);
    
    
        ////////////////////////TODO: CHARTS
        
        xyAnalogDataset = Chart.createDefaultDataset();
        xyDigitalDataset = Chart.createDefaultDataset();
        analogChart = Chart.createChart(xyAnalogDataset, "analog", "cisnienie");
        digitalChart = Chart.createChart(xyDigitalDataset, "cyfrowy", "parowki");
        
        topChartViewer = new ChartViewer(analogChart);
        bottomChartViewer = new ChartViewer(digitalChart);
        
        topChartPane.getChildren().add(topChartViewer);
        bottomChartPane.getChildren().add(bottomChartViewer);
        
        instance = this;
    }
    
    private void initTables() {
        currentGateDescription = new TableColumn<>("Description");
        currentShortDescription = new TableColumn("ShortDescription");
        currentGateType = new TableColumn("GateType");
        currentGateMeasureType = new TableColumn("MeasureType");
        currentGateId = new TableColumn<>("GateId");
        
        remainingGateDescription = new TableColumn<>("Description");
        remainingShortDescription = new TableColumn("ShortDescription");
        ;
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
    
    private void initListeners() {
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
                row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
                return row;
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
                row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
                return row;
            }
        });
        
        groupsTab.setOnSelectionChanged((e) -> {
            tableWithRemainingGates.getItems().clear();
            tableWithRemainingGates.getItems().addAll(DBGroupManager.dbGetAllGates());
        });
        
        comboMenuEdit.setOnAction((e) -> {
            try {
                dataContainer.setCurrentGroupName(getNewGroupName());
                tableWithCurrentGates.setDisable(false);
                List<GroupGate> groupGates = DBGroupManager.dbGetAllGatesFromGroup(dataContainer.getCurrentGroupName());
                if (groupGates != null) {
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
                //TODO: log it
                e1.printStackTrace();
            }
        });
        newGroupTextField.setOnKeyReleased((e) -> {
            try {
                dataContainer.setCurrentGroupName(getNewGroupName());
            } catch (GuiAccessException e1) {
                //TODO: log it
                e1.printStackTrace();
            }
        });
        comboChooseGroup.setOnAction((e) -> {
            gatesNotOnChartList.clear();
            gatesOnChartList.clear();
            
            dataContainer.setChartGroupGates(DBGroupManager.dbGetAllGatesFromGroup(comboChooseGroup.getSelectionModel().getSelectedItem().toString()));
            dataContainer.getAllChartData().clear();
            LinkedList<String> groupGatesNames = new LinkedList<>();
            for (GroupGate gate : dataContainer.getChartGroupGates()) {
                groupGatesNames.add(gate.getDescription());
            }
            
            addComboBoxOptionsFromList(comboNotOnChartGates, gatesNotOnChartList, groupGatesNames);
            loadDataButton.setDisable(false);
            
            progressBar.setProgress(0);
            progressIndicator.setProgress(0);
            //TODO: clear charts
        });
        
        // do dodania na wykresu
        comboNotOnChartGates.setOnHiding((e) -> {
            if (comboNotOnChartGates.getSelectionModel().getSelectedItem() != null) {
                String selectedGateToAdd = comboNotOnChartGates.getSelectionModel().getSelectedItem().toString();
                LinkedList<String> newList = new LinkedList<>();
                newList.add(selectedGateToAdd);
                
                String gateId = dataContainer.getGateIdUsingDescription(selectedGateToAdd);
                for (GateData gateData : GuiDataContainer.getAllChartData()) {
                    if (gateData.getGateId().equals(gateId)) {
                        xyAnalogDataset = Chart.putGateValues(gateData.getValues());
                        //analogChart = Chart.createChart(xyAnalogDataset, "analog", "lol"/*TODO!*/);
                        analogChart.getXYPlot().setDataset(xyAnalogDataset);
                        //analogChart = Chart.createChart(xyAnalogDataset,"analog", "cisnienie");
                        //topChartViewer.setChart(analogChart);
                        
                        
                        
                        break;
                    }
                }
                //TODO: wrzucic dane na wykres na podstawie gateId
                
    
                addComboBoxOptionsFromList(comboOnChartGates, gatesOnChartList, newList);
                comboNotOnChartGates.getItems().remove(selectedGateToAdd);
                comboNotOnChartGates.getSelectionModel().clearSelection();
            }
        });
        // do usuniecia wykresu
        comboOnChartGates.setOnHiding((e) -> {
            //TODO: remove data from chart
            if (comboOnChartGates.getSelectionModel().getSelectedItem() != null) {
                String selectedGateToRemove = comboOnChartGates.getSelectionModel().getSelectedItem().toString();
                LinkedList<String> newList = new LinkedList<>();
                newList.add(selectedGateToRemove);
    
                addComboBoxOptionsFromList(comboNotOnChartGates, gatesNotOnChartList, newList);
                comboOnChartGates.getItems().remove(selectedGateToRemove);
                comboOnChartGates.getSelectionModel().clearSelection();
            }
        });
    }
    
    private void addComboBoxOptionsFromList(ComboBox box, ObservableList<String> list, List<String> newOptions) {
        list.addAll(newOptions);
        list.sort((o1, o2) -> o1.compareTo(o2));
        box.setItems(list);
    }
    
    public synchronized void changeProgress() {
        double oldProgress = progressBar.getProgress();
        double newProgress = oldProgress + 1 / Double.valueOf(GuiDataContainer.getInstance().getChartGroupGates().size());
        progressBar.setProgress(newProgress);
        progressIndicator.setProgress(newProgress);
        if (newProgress == 1) {
            loadDataButton.setDisable(true);
        }
    }
}
