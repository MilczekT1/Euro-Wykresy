package pl.konradboniecki.structures;

public class ChartPoint {
    private long timestamp;
    private double value;
    
    public ChartPoint(long timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setValue(double value) {
        this.value = value;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public double getValue() {
        return value;
    }
}
