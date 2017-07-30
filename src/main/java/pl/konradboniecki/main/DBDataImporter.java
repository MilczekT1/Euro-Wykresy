package pl.konradboniecki.main;

import com.google.common.base.Throwables;
import pl.konradboniecki.general.MyLogger;
import pl.konradboniecki.general.ThreadPool;
import pl.konradboniecki.general.Utils;
import pl.konradboniecki.servers.DBGroupManager;
import pl.konradboniecki.structures.ChartPoint;
import pl.konradboniecki.structures.GateData;

import java.sql.SQLException;
import java.util.LinkedList;
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
            LinkedList<ChartPoint> chartPoints = DBGroupManager.importGateValues(gateId,start,end);
            synchronized (GuiDataContainer.getAllChartData()){
                GuiDataContainer.getAllChartData().add(new GateData(gateId,chartPoints));
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            Controller.getInstance().changeProgress(0);
            Utils.showMessageDialog("Blad importu danych do wykresu!");
            ThreadPool.getInstance().shutdownNow();
            ThreadPool.turnOnAfterShutdown();
            return;
        }
        
        //Controller instance, because progressbar cant be static
        Controller.getInstance().incrementProgress();
    }
}
