package sample.Main;
import lombok.Data;

// Container for logic variable in Controller
@Data
final class GuiDataContainer {
    private static GuiDataContainer instance = new GuiDataContainer();
    
    private GuiDataContainer(){};
    public static GuiDataContainer getInstance(){
        return instance;
    }
    
    protected static String ACCESS_TYPE;
    
    protected String currentGroupName;
    protected String gateDescriptionToRemove;
    protected String lastAddedGateID;
    protected String gateId;
    protected String groupId;
    protected Integer accessLevel;
}
