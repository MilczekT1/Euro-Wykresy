package sample.Main;

import com.google.common.base.Throwables;
import lombok.Cleanup;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import sample.General.Configurator;
import sample.General.MyLogger;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;

class Chart {

    public static XYDataset createDefaultDataset( ) {
        /*
        TimeSeries series = new TimeSeries( "Random Data" );
        Second current = new Second();
        double value = 100.0;
    
        for (int i = 0; i < 1000; i++) {
            try {
                value = value + Math.random( ) - 0.5;
                series.add(current, new Double( value ) );
                current = ( Second ) current.next( );
            } catch ( SeriesException e ) {
                System.err.println("Error adding to series");
            }
        }
        */
        TimeSeries series = new TimeSeries("default");
        return new TimeSeriesCollection(series);
    }
    public static XYDataset
    putGateValues(GateData gateData){
        TimeSeries series = new TimeSeries("lolek");
        RegularTimePeriod time;
        for(int i=0; i< gateData.getTimestamps().length; i++){
            time = new Second(new Date(gateData.getTimestamps()[i]));
            //series.add(new Millisecond(new Date(crs.getLong("time"))),crs.getDouble("value"));
            series.addOrUpdate(time, gateData.getValues()[i]);
        }
        return new TimeSeriesCollection(series);
    }
    
    public static JFreeChart createChart(XYDataset dataset,String title, String measureType) {
        return ChartFactory.createTimeSeriesChart(title,"Czas", measureType,dataset,false,false,false);
    }
    
    public static CachedRowSet dbImportGateValues(String gateId, long start, long end) throws SQLException{
        String sql = "use e6_VfiTag;\n" +
                             "SELECT time, value FROM VfiTagNumHistory\n" +
                             "WHERE gateId = ? AND time BETWEEN ? AND ?;";
        CachedRowSet crs = new com.sun.rowset.CachedRowSetImpl();
        crs.setUrl("jdbc:sqlserver://" + Configurator.getCurrentSettings().getProperty("Server-Adress"));
        crs.setUsername(Configurator.getCurrentSettings().getProperty("User"));
        crs.setPassword(Configurator.getCurrentSettings().getProperty("Password"));
        crs.setCommand(sql);
        crs.setString(1,gateId);
        crs.setLong(2,start);
        crs.setLong(3,end);
        crs.execute();
        return crs;
    }
}