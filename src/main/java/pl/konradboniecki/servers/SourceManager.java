package pl.konradboniecki.servers;

import pl.konradboniecki.structures.ChartPoint;
import pl.konradboniecki.structures.GroupGate;
import pl.konradboniecki.structures.MinMax;

import java.sql.SQLException;
import java.util.LinkedList;

class SourceManager {
    
    private static TreblinkaFirst treblinkaFirst = TreblinkaFirst.getInstance();
    private static TreblinkaSecond treblinkaSecond = TreblinkaSecond.getInstance();
    private static Paterek paterek = Paterek.getInstance();
    
    private static SourceManager instance = new SourceManager();
    
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
        }/*
        try {
            treblinkaSecond.connect();
            counter++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            paterek.connect();
            counter++;
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        if (counter > 0)
            return counter;
        else
            throw new Exception("Not Connected to any source");
    }
    
    LinkedList<String> getAllAvailableGroupNames() {
        LinkedList<String> allAvailableGroupNames = new LinkedList<>();
        
        if (treblinkaFirst.isConnected())
            allAvailableGroupNames.addAll(treblinkaFirst.dbGetAllExistingGroupNames());
        //if (treblinkaSecond.isConnected())
        //    allAvailableGroupNames.addAll(treblinkaSecond.dbGetAllExistingGroupNames());
        //if (paterek.isConnected())
        //    allAvailableGroupNames.addAll(paterek.dbGetAllExistingGroupNames());
        
        return allAvailableGroupNames;
    }
    LinkedList<GroupGate> getAllGates() {
        LinkedList<GroupGate> allAvailableGates = new LinkedList<>();
        
        if (treblinkaFirst.isConnected())
            allAvailableGates.addAll(treblinkaFirst.dbGetAllGates());
        //if (treblinkaSecond.isConnected())
        //    allAvailableGates.addAll(treblinkaSecond.dbGetAllGates());
        //if (paterek.isConnected())
        //    allAvailableGates.addAll(paterek.dbGetAllGates());
        
        return allAvailableGates;
    }
    LinkedList<GroupGate> getAllGatesFromGroup(String groupName){
        LinkedList<GroupGate> allAvailableGates = new LinkedList<>();
        
        if (treblinkaFirst.isConnected())
            allAvailableGates.addAll(treblinkaFirst.dbGetAllGatesFromGroup(groupName));
        //if (treblinkaSecond.isConnected())
        //    allAvailableGates.addAll(treblinkaSecond.dbGetAllGatesFromGroup(groupName));
        //if (paterek.isConnected())
        //    allAvailableGates.addAll(paterek.dbGetAllGatesFromGroup(groupName));
        
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
        long max = -1;
        long min = -1;
        if (treblinkaFirst.isConnected()){
            return treblinkaFirst.getMinAndMaxTimePoints();
        }
        return new MinMax(-1,-1);
    }
    public static LinkedList<ChartPoint> importGateValues(String gateId, long start, long end) throws SQLException {
        if (treblinkaFirst.isConnected()){
            return treblinkaFirst.dbImportGateValues(gateId, start, end);
        }
        return null;
    }
}
