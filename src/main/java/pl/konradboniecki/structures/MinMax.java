package pl.konradboniecki.structures;

import lombok.Getter;

@Getter
public class MinMax {
    private long min;
    private long max;
    
    public MinMax(long min, long max) {
        this.min = min;
        this.max = max;
    }
    
    public void setMin(long min) {
        if (this.min > min)
            this.min = min;
    }
    public void setMax(long max) {
        if (this.max < max)
            this.max = max;
    }
}
