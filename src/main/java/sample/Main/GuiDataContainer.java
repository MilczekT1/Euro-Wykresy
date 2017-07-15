package sample.Main;
import lombok.Data;
import org.omg.CORBA.IntHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

// Container for logic variable in Controller
@Data
final class GuiDataContainer {
    private static GuiDataContainer instance = new GuiDataContainer();
    public static GuiDataContainer getInstance(){
        return instance;
    }
    private GuiDataContainer(){
        chartGroupGates = new LinkedList<>();
        allChartData = new ArrayList<>(20);
        amountOfProcessedThreads = new IntHolder(0);
    }
    
    LinkedList<GroupGate> chartGroupGates;
    private static ArrayList<GateData> allChartData;
    private IntHolder amountOfProcessedThreads;
    
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
    String getGateTypeUsingDescription(String description){
        for (GroupGate gate: chartGroupGates){
            if (gate.getDescription().equals(description)){
                return gate.getGateType();
            }
        }
        //exception?
        return null;
    }
    
    static ArrayList<GateData> getAllChartData() {
        return allChartData;
    }
    
    public IntHolder getAmountOfProcessedThreads() {
        return amountOfProcessedThreads;
    }
}
