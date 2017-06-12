package sample.Groups;

import javafx.beans.property.SimpleStringProperty;
import sample.Groups.GateThreads.GateDataImporter;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Gate {
    
    public static void main(String[] args) {
        LinkedList<Integer> list = new LinkedList<>();
        LinkedList<Integer> list2 = new LinkedList<>();
        list.add(1); list.add(2); list.add(3);
        list2.add(4); list2.add(5); list2.add(6);
        ExecutorService exs = Executors.newFixedThreadPool(2);
        exs.execute(new GateDataImporter(list));
        exs.execute(new GateDataImporter(list2));
        exs.shutdown();
        while (!exs.isTerminated()) {
        }
        System.out.println("end");
    }
    ////////////////////////////////////
    private final SimpleStringProperty description;
    private final SimpleStringProperty gateId;
    private final SimpleStringProperty groupId;
    private final SimpleStringProperty measureType;
    private final SimpleStringProperty gateType;
    private final SimpleStringProperty shortDescription;

    public Gate(String desc, String gateid, String groupid, String measure, String gateType, String shortDescription){
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
        
        Gate gate = (Gate) o;
    
        return getGateId().equals(gate.getGateId());
    }
    
    @Override
    public int hashCode() {
        return getGateId().hashCode();
    }
}
