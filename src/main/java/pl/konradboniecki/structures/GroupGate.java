package pl.konradboniecki.structures;

import javafx.beans.property.SimpleStringProperty;

public final class GroupGate {
    private final SimpleStringProperty description;
    private final SimpleStringProperty gateId;
    private final SimpleStringProperty groupId;
    private final SimpleStringProperty measureType;
    private final SimpleStringProperty gateType;
    private final SimpleStringProperty shortDescription;

    public GroupGate(String desc, String gateid, String groupid, String measure, String gateType, String shortDescription){
        this.description = new SimpleStringProperty(desc);
        this.gateId = new SimpleStringProperty(gateid);
        this.groupId = new SimpleStringProperty(groupid);
        this.measureType = new SimpleStringProperty(measure);
        this.gateType = new SimpleStringProperty(gateType);
        this.shortDescription = new SimpleStringProperty(shortDescription);
    }

    public void setGateId(String gateId) {
        this.gateId.set(gateId);
    }
    public void setDescription(String description) {
        this.description.set(description);
    }
    public void setGroupId(String groupId) {
        this.groupId.set(groupId);
    }
    public void setMeasureType(String measureType) {
        this.measureType.set(measureType);
    }
    public void setGateType(String gateType) {
        this.gateType.set(gateType);
    }
    public void setShortDescription(String shortDescription) {
        this.shortDescription.set(shortDescription);
    }
    
    public String getGateId() {
        return gateId.get();
    }
    public String getDescription() {
        return description.get();
    }
    public String getGroupId() {
        return groupId.get();
    }
    public String getMeasureType() {
        return measureType.get();
    }
    public String getGateType() {
        return gateType.get();
    }
    public String getShortDescription() {
        return shortDescription.get();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        
        GroupGate groupGate = (GroupGate) o;
    
        return getGateId().equals(groupGate.getGateId());
    }
    
    @Override
    public int hashCode() {
        return getGateId().hashCode();
    }
}
