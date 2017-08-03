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
    public MinMax(MinMax first, MinMax second){
        if (first.getMax() >= second.getMax())
            this.max = first.getMax();
        else
            this.max = second.getMax();
        
        if (first.getMin() <= second.getMin())
            this.min = first.getMin();
        else
            this.min = second.getMin();
    }
}
