package sample.Main;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;

public class Chart {
    
    public static CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(7445, "JFreeSVG", "Warm-up");
        dataset.addValue(24448, "Batik", "Warm-up");
        dataset.addValue(4297, "JFreeSVG", "Test");
        dataset.addValue(21022, "Batik", "Test");
        return dataset;
    }
    
    public static JFreeChart createChart(CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createLineChart("Analog","Czas","Jednostka",dataset);
        //ChartFactory.createBarChart(
        //        "Performance: JFreeSVG vs Batik", null /* x-axis label*/,
         //       "Milliseconds" /* y-axis label */, dataset);
        chart.addSubtitle(new TextTitle("Wartości analogowe"));
        chart.setBackgroundPaint(Color.white);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        chart.getLegend().setFrame(BlockBorder.NONE);
        return chart;
    }
    
    public void chartMouseClicked(ChartMouseEventFX event) {
        System.out.println(event);
    }
    
    public void chartMouseMoved(ChartMouseEventFX event) {
        System.out.println(event);
    }
    
}
