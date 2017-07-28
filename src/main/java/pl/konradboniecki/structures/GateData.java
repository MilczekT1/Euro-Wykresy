package pl.konradboniecki.structures;

import lombok.Getter;

import java.sql.SQLException;
import java.util.LinkedList;

@Getter
public class GateData {
    private String gateId;
    private long[] timestamps;
    private double[] values;
    
    public GateData(String gateId, LinkedList<ChartPoint> chartPoints) throws SQLException {
        this.gateId = gateId;
        timestamps = new long[chartPoints.size()];
        values = new double[chartPoints.size()];
        int counter = 0;
        for (ChartPoint point : chartPoints){
            timestamps[counter] = point.getTimestamp();
            values[counter] = point.getValue();
            counter++;
        }
    }
}