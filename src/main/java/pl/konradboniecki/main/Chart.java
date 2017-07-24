package pl.konradboniecki.main;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import pl.konradboniecki.structures.GateData;
import pl.konradboniecki.general.Configurator;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.Date;

class Chart {
    static XYDataset createEmptyDataset( ) {
        TimeSeries series = new TimeSeries("empty");
        return new TimeSeriesCollection(series);
    }
    static XYDataset putGateValues(GateData gateData, String name, String gateType) throws Exception {
        TimeSeries series = new TimeSeries(name);
        RegularTimePeriod time;
        
        for (int i = 0; i < gateData.getTimestamps().length; i++) {
            time = new Second(new Date(gateData.getTimestamps()[i]));
            series.addOrUpdate(time, gateData.getValues()[i]);
        }
        return new TimeSeriesCollection(series);
    }
    
    static JFreeChart createChart(XYDataset dataset,String title, String measureType) {
        return ChartFactory.createTimeSeriesChart(title,"Czas", measureType,dataset,true,true,false);
    }
    
    static CachedRowSet dbImportGateValues(String gateId, long start, long end) throws SQLException {
        //TODO: poprawic  skrypt
        String sql = "use e6_VfiTag;\n" +
                             "SELECT time, value FROM VfiTagNumHistory\n" +
                             "WHERE gateId = ? AND time BETWEEN ? AND ?;";
        CachedRowSet crs = new com.sun.rowset.CachedRowSetImpl();
        crs.setUrl("jdbc:sqlserver://" + Configurator.getCurrentSettings().getProperty("Adress-Treblinka-1"));
        crs.setUsername(Configurator.getCurrentSettings().getProperty("User-Treblinka-1"));
        crs.setPassword(Configurator.getCurrentSettings().getProperty("Password-Treblinka-1"));
        crs.setCommand(sql);
        crs.setString(1,gateId);
        crs.setLong(2,start);
        crs.setLong(3,end);
        crs.execute();
        return crs;
    }
}