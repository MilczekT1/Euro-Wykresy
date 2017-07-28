package pl.konradboniecki.main;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import pl.konradboniecki.structures.GateData;

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
    
}