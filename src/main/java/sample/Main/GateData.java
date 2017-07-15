package sample.Main;

import lombok.Getter;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;

@Getter
class GateData {
    private String gateId;
    private long[] timestamps;
    private double[] values;
    
    public GateData(String gateId, CachedRowSet crs) {
        this.gateId = gateId;
        timestamps = new long[crs.size()];
        values = new double[crs.size()];
        int counter = 0;
        try {
            while (crs.next()){
                timestamps[counter] = crs.getLong("time");
                values[counter] = crs.getDouble("value");
                counter++;
            }
        } catch (SQLException e) {
            e.printStackTrace(); //TODO: log it
        }
    }
}