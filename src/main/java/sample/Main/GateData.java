package sample.Main;

import javax.sql.rowset.CachedRowSet;

public class GateData {
    private String gateId;
    private CachedRowSet values;
    
    public GateData(String gateId, CachedRowSet values) {
        this.gateId = gateId;
        this.values = values;
    }
    
    public CachedRowSet getValues() {
        return values;
    }
    
    public String getGateId() {
        return gateId;
    }
}