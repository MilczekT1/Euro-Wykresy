package pl.konradboniecki.servers;

import pl.konradboniecki.general.Configurator;
import pl.konradboniecki.structures.ChartPoint;
import pl.konradboniecki.structures.GroupGate;
import pl.konradboniecki.structures.MinMax;

import java.sql.SQLException;
import java.util.LinkedList;

class SourceManager {
    
    private static TreblinkaFirst treblinkaFirst = TreblinkaFirst.getInstance();
    private static TreblinkaSecond treblinkaSecond = TreblinkaSecond.getInstance();
    private static Paterek paterek = Paterek.getInstance();
    private static String localisation;
    
    private static SourceManager instance = new SourceManager();
    
    private SourceManager(){
        this.localisation = Configurator.getCurrentProperty("localisation").toLowerCase();
    }
    
    static SourceManager getInstance() throws NullPointerException {
        if (instance != null) {
            return instance;
        } else {
            throw new NullPointerException("SourceManager instance is null");
        }
    }
    
    int connectToAllSources() throws Exception {
        int counter = 0;
        try {
            treblinkaFirst.connect();
            counter++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            treblinkaSecond.connect();
            counter++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (localisation.equals("paterek")){
            try {
                paterek.connect();
                counter++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (counter > 0)
            return counter;
        else
            throw new Exception("Not Connected to any source");
    }
    
    LinkedList<String> getAllAvailableGroupNames() {
        LinkedList<String> allAvailableGroupNames = new LinkedList<>();
        
        if (treblinkaFirst.isConnected())
            allAvailableGroupNames.addAll(treblinkaFirst.dbGetAllExistingGroupNames());
        
        return allAvailableGroupNames;
    }
    LinkedList<GroupGate> getAllGates() {
        LinkedList<GroupGate> allAvailableGates = new LinkedList<>();
        
        if (treblinkaFirst.isConnected())
            allAvailableGates.addAll(treblinkaFirst.dbGetAllGates());
        
        return allAvailableGates;
    }
    LinkedList<GroupGate> getAllGatesFromGroup(String groupName){
        LinkedList<GroupGate> allAvailableGates = new LinkedList<>();
        
        if (treblinkaFirst.isConnected())
            allAvailableGates.addAll(treblinkaFirst.dbGetAllGatesFromGroup(groupName));
        
        return allAvailableGates;
    }
    void addGroup(String groupName) throws SQLException{
        if(treblinkaFirst.isConnected())
            treblinkaFirst.dbAddGroup(groupName);
    }
    boolean editGroup(LinkedList<String> gateIds, String groupId){
        if (treblinkaFirst.isConnected()){
            return treblinkaFirst.dbEditGroup(gateIds, groupId);
        }
        return false;
    }
    String getGroupIdUsingGroupName(String groupName){
        if (treblinkaFirst.isConnected()){
            return treblinkaFirst.dbGetGroupIdUsingGroupName(groupName);
        }
        return null;
    }
    boolean deleteGroupUsingGroupId(String groupId){
        if (treblinkaFirst.isConnected()){
            return treblinkaFirst.dbDeleteGroupUsingGroupId(groupId);
        }
        return false;
    }
    
    MinMax getMinAndMaxAvailableTimePoints() throws SQLException{
        // Opposite direction in constructor
        MinMax minMax1 = new MinMax(Long.MAX_VALUE, Long.MIN_VALUE);
        MinMax minMax2 = new MinMax(Long.MAX_VALUE, Long.MIN_VALUE);
        MinMax minMax3 = new MinMax(Long.MAX_VALUE, Long.MIN_VALUE);
        
        if (treblinkaFirst.isConnected())
            minMax1 = treblinkaFirst.getMinAndMaxTimePoints();
        
        if (treblinkaSecond.isConnected())
            minMax2 = treblinkaSecond.getMinAndMaxTimePoints();
        
        if (localisation.equals("paterek")){
            if (paterek.isConnected()){
                //minMax3 = paterek.getMinAndMaxTimePoints();
            }
        }
        
        MinMax result = new MinMax(minMax1,minMax2);
        result = new MinMax(result,minMax3);
        return result;
    }
    public static LinkedList<ChartPoint> importGateValues(String gateId, long start, long end) throws SQLException {
        LinkedList<ChartPoint> chartPoints = new LinkedList<>();
        if (treblinkaFirst.isConnected())
            chartPoints.addAll(treblinkaFirst.dbImportGateValues(gateId, start, end));
        
        if (treblinkaSecond.isConnected())
            chartPoints.addAll(treblinkaSecond.dbImportGateValues(gateId, start, end));
    
        if (localisation.equals("paterek")){
            if (paterek.isConnected())
                chartPoints.addAll(paterek.dbImportGateValues(gateId, start, end));
        }
        return chartPoints;
    }
}
