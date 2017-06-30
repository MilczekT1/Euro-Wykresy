package sample.Main;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;

// Container for logic variable in Controller
@Data
final class GuiDataContainer {
    private static GuiDataContainer instance = new GuiDataContainer();
    private GuiDataContainer(){
        chartGroupGates = new LinkedList<>();
        allChartData = new ArrayList<>(20);
    }
    public static GuiDataContainer getInstance(){
        return instance;
    }
    
    private static String ACCESS_TYPE;
    protected LinkedList<GroupGate> chartGroupGates;
    
    protected static ArrayList<GateData> allChartData;
    
    private String currentGroupName;
    private String gateDescriptionToRemove;
    private String lastAddedGateID;
    private String gateId;
    private String groupId;
    private Integer accessLevel;
    
    String getGateIdUsingDescription(String description){
        for (GroupGate gate: chartGroupGates){
            if (gate.getDescription().equals(description)){
                return gate.getGateId();
            }
        }
        //exception?
        return null;
    }
    
    static ArrayList<GateData> getAllChartData() {
        return allChartData;
    }
}
