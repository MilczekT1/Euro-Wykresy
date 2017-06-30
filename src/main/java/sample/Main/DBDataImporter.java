package sample.Main;

import javax.sql.rowset.CachedRowSet;

final class DBDataImporter extends Thread {
    
    private final String gateId;
    private final long start;
    private final long end;
    
    public DBDataImporter(String gateId, long start, long end) {
        super("DataImporter");
        this.gateId = gateId;
        this.start = start;
        this.end = end;
    }
    
    @Override
    public void run() {
        CachedRowSet rows = Chart.importGateValues(gateId,start,end);
        synchronized (GuiDataContainer.getAllChartData()){
            GuiDataContainer.getAllChartData().add(new GateData(gateId,rows));
        }
        //solution for: progressbar cant be static
        Controller.getInstance().changeProgress();
        System.out.println("koniec");
    }
}
