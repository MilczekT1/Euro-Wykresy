package pl.konradboniecki.servers;

import lombok.Cleanup;
import pl.konradboniecki.general.Configurator;
import pl.konradboniecki.structures.ChartPoint;
import pl.konradboniecki.structures.MinMax;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Pattern;

class TreblinkaSecond extends SQLServerConnector{
    private static TreblinkaSecond instance = new TreblinkaSecond();
    private ArrayList<String> instances;
    
    private TreblinkaSecond(){
        this.SERVER_ADRESS = "jdbc:sqlserver://" + Configurator.getCurrentSettings().getProperty("Adress-Treblinka-2");
        this.USERNAME = Configurator.getCurrentProperty("User-Treblinka-2");
        this.PASSWORD = Configurator.getCurrentProperty("Password-Treblinka-2");
    }
    
    static TreblinkaSecond getInstance() throws NullPointerException{
        if (instance != null) {
            return instance;
        } else{
            throw new NullPointerException("TreblinkaSecond instance is null");
        }
    }
    
    @Override
    protected void setUpStructuresIfNotExists() throws SQLException {
        ;
    }
    
    MinMax getMinAndMaxTimePoints() throws SQLException {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        setAllInstances();
        
        @Cleanup
        Statement statement = connection.createStatement();
        String findMinSQL = null;
        for (String db : instances) {
            findMinSQL = "USE " + db + "; SELECT MIN(time), MAX(time) FROM dbo.VfiTagNumHistory";
            @Cleanup ResultSet rs = statement.executeQuery(findMinSQL);
            if (rs.next()) {
                min = rs.getLong(1) <= min ? rs.getLong(1) : min;
                max = rs.getLong(2) >= max ? rs.getLong(2) : max;
            }
        }
        return new MinMax(min,max);
    }
    void setAllInstances() throws SQLException {
        String findInstancesSQL = "SELECT DB_NAME(database_id) AS [Database], database_id FROM sys.databases;";
        @Cleanup
        Statement statement = connection.createStatement();
        @Cleanup
        ResultSet resultSet = statement.executeQuery(findInstancesSQL);
        ArrayList<String> allInstances = new ArrayList<>(20);
        while(resultSet.next()){
            allInstances.add(resultSet.getString(1));
        }
        
        instances = new ArrayList<>(20);
        
        for (String db : allInstances){
            if (Pattern.matches("e([1-9]|[1-9][0-9]+)_VfiTag",db))
                instances.add(db);
        }
    }
    LinkedList<ChartPoint> dbImportGateValues(String gateId, long start, long end) throws SQLException {
    
        if (instances == null || instances.size() == 0)
            setAllInstances();
    
        ArrayList<String> selectFromInstanceQuerries = new ArrayList<>(10);
        for (String instance : instances) {
            selectFromInstanceQuerries.add("SELECT time, value FROM " + instance + ".dbo.VfiTagNumHistory WHERE gateId = " + gateId + " AND time BETWEEN " + start + " AND " + end + ";");
        }
        LinkedList<ChartPoint> points = new LinkedList<>();
        @Cleanup Statement statement = connection.createStatement();
    
        for (String query : selectFromInstanceQuerries) {
            @Cleanup ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                points.add(new ChartPoint(resultSet.getLong("time"), resultSet.getDouble("value")));
            }
        }
        return points;
    }
}