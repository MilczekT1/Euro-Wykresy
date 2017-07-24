package pl.konradboniecki.main;

import com.google.common.base.Throwables;
import lombok.Cleanup;
import pl.konradboniecki.structures.GateData;
import pl.konradboniecki.general.MyLogger;
import pl.konradboniecki.general.ThreadPool;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.logging.Level;

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
        try {
            @Cleanup
            CachedRowSet rows = Chart.dbImportGateValues(gateId,start,end);
            synchronized (GuiDataContainer.getAllChartData()){
                GuiDataContainer.getAllChartData().add(new GateData(gateId,rows));
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            ThreadPool.getInstance().shutdownNow();
        }
        
        //Controller instance, because progressbar cant be static
        Controller.getInstance().changeProgress();
    }
}
