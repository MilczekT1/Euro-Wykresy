package sample.Main;

import lombok.Getter;
import javax.sql.rowset.CachedRowSet;

@Getter
class GateData {
    private final String gateId;
    private CachedRowSet values;
    
    public GateData(String gateId, CachedRowSet values) {
        this.gateId = gateId;
        this.values = values;
    }
}