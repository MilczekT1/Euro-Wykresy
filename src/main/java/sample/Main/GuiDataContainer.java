package sample.Main;
import lombok.Data;

import javax.sql.rowset.CachedRowSet;
import java.util.ArrayList;
import java.util.LinkedList;

// Container for logic variable in Controller
@Data
final class GuiDataContainer {
    private static GuiDataContainer instance = new GuiDataContainer();
    private GuiDataContainer(){
        chartGroupGates = new LinkedList<>();
        allChartData = new ArrayList<>(20);
    };
    public static GuiDataContainer getInstance(){
        return instance;
    }
    
    protected static String ACCESS_TYPE;
    protected LinkedList<GroupGate> chartGroupGates;
    
    protected static ArrayList<GateData> allChartData;
    
    protected String currentGroupName;
    protected String gateDescriptionToRemove;
    protected String lastAddedGateID;
    protected String gateId;
    protected String groupId;
    protected Integer accessLevel;
    
    protected String getGateIdUsingDescription(String description){
        for (GroupGate gate: chartGroupGates){
            if (gate.getDescription().equals(description)){
                return gate.getGateId();
            }
        }
        //exception?
        return null;
    }
    
    public static ArrayList<GateData> getAllChartData() {
        return allChartData;
    }
    public CachedRowSet findCachedRowSetUsingGateId(String gateId){
    return null;
    }
}
