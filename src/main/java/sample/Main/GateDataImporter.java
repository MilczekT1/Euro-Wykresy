package sample.Main;

import java.util.LinkedList;

//TODO: implement data
class GateDataImporter extends Thread {
    
    private LinkedList<Integer> values;
    
    public GateDataImporter(LinkedList<Integer> values){
        if (values != null)
            this.values = values;
        else
            this.values = new LinkedList<>();
    }
    
    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
            values.add(i);
        }
    }
}
