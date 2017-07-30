package pl.konradboniecki.structures;

import lombok.Data;

@Data
public class ChartPoint {
    private long timestamp;
    private double value;
    
    public ChartPoint(long timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }
}
