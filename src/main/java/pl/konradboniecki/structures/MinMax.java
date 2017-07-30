package pl.konradboniecki.structures;

import lombok.Data;

@Data
public class MinMax {
    private long min;
    private long max;
    
    public MinMax(long min, long max) {
        this.min = min;
        this.max = max;
    }
}
