package pl.konradboniecki.servers;

import com.google.common.base.Throwables;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import lombok.Cleanup;
import pl.konradboniecki.general.Configurator;
import pl.konradboniecki.general.MyLogger;
import pl.konradboniecki.structures.GroupGate;
import pl.konradboniecki.structures.MinMax;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

final class TreblinkaFirst {
    private static final String DB_TREBLINKA_1       = "jdbc:sqlserver://" + Configurator.getCurrentSettings().getProperty("Adress-Treblinka-1");
    private static final String USERNAME_TREBLINKA_1 = Configurator.getCurrentSettings().getProperty("User-Treblinka-1");
    private static final String PASSWORD_TREBLINKA_1 = Configurator.getCurrentSettings().getProperty("Password-Treblinka-1");
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static Connection connection;
    private ArrayList<String> instances;
    private static TreblinkaFirst instance = new TreblinkaFirst();
    static TreblinkaFirst getInstance() throws NullPointerException{
        if (instance != null) {
            return instance;
        } else{
            throw new NullPointerException("TreblinkaFirst instance is null");
        }
    }
    
    private TreblinkaFirst(){}
    
    boolean isConnected(){
        try {
            return (connection != null && !connection.isClosed() && connection.isValid(3000)) ? true : false;
        } catch (SQLException e) {
            return false;
        }
    }
    
    void connect() throws Exception {
        if (!isConnected()){
            Class.forName(DRIVER).newInstance();
            connection = DriverManager.getConnection(DB_TREBLINKA_1, USERNAME_TREBLINKA_1, PASSWORD_TREBLINKA_1);
            
            /*try {
                setUpStructuresIfNotExists();
            } catch (SQLException e) {
                MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            }*/
        }
    }
    private void setUpStructuresIfNotExists() throws SQLException{
        @Cleanup Statement statement = connection.createStatement();
        try {
            Path path = Paths.get("src/main/resources/", "Treblinka-groups-1.sql");
            List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
            List<String> queries = new LinkedList<>();
        
            StringBuilder foundQuery = new StringBuilder();
            for (String line : lines){
                foundQuery.append(line);
                if (line.endsWith(";")){
                    queries.add(foundQuery.toString());
                    foundQuery = new StringBuilder();
                }
            }
            for (String fromFileQuery: queries)
                executeCreateIfNotExists(fromFileQuery, statement);
        
        } catch (FileNotFoundException e) {
            MyLogger.getLogger().log(Level.WARNING, "NIE ZNALEZIONO PLIKU Treblinka-groups-1.sql");
        } catch(IOException e) {
            MyLogger.getLogger().log(Level.WARNING, "BLAD ODCZYTU PLIKU Treblinka-groups-1.sql");
        }
    }
    private void executeCreateIfNotExists(String query, Statement statement){
        try {
            statement.execute(query);
        } catch (SQLException e) {
            // One of the scripts from "Treblinka-groups-1.sql" failed (because exists)
        }
    }
    void closeConnection()throws SQLException{
        if (!connection.isClosed()) {
            connection.close();
        }
    }
    
    LinkedList<String> dbGetAllExistingGroupNames(){
        LinkedList<String> list = new LinkedList<>();
        try {
            @Cleanup
            Statement statement = connection.createStatement();
            @Cleanup
            ResultSet rs = statement.executeQuery("SELECT Nazwa FROM wizualizacja.dbo.EW_Grupy");
            while(rs.next()){
                list.add(rs.getString("Nazwa"));
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        } finally {
            return list;
        }
    }
    LinkedList<GroupGate> dbGetAllGates() {
        LinkedList<GroupGate> groupGates = new LinkedList<>();
        try {
            String sql = "SELECT gateId " +
                                 "      ,LongGate AS Skrocona_Nazwa " +
                                 "      ,description AS Opis " +
                                 "      ,rodzajBramki " +
                                 "      ,rodzajPomiaru " +
                                 " FROM wizualizacja.dbo.Slownik;";
            @Cleanup Statement statement = connection.createStatement();
    
            @Cleanup ResultSet rs = statement.executeQuery(sql);
            String[] strings = new String[6];
            while (rs.next()) {
                strings[0] = rs.getString("Opis");
                strings[1] = rs.getString("GateID");
                strings[2] = null;
                strings[3] = rs.getString("rodzajPomiaru");
                strings[4] = rs.getString("rodzajBramki");
                strings[5] = rs.getString("Skrocona_Nazwa");
                groupGates.add(new GroupGate(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]));
            }
        } catch (SQLException e){
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        } finally {
            return groupGates;
        }
    }
    LinkedList<GroupGate> dbGetAllGatesFromGroup(String groupName){
        LinkedList<GroupGate> groupGates = new LinkedList<>();
        try {
            String getGroupIdSQL = "SELECT Id_grupy FROM wizualizacja.dbo.EW_Grupy WHERE Nazwa = ?;";
            @Cleanup
            PreparedStatement preStatement = connection.prepareStatement(getGroupIdSQL);
            preStatement.setString(1,groupName);
            @Cleanup
            ResultSet rs = preStatement.executeQuery();
            if(rs.next()) {
                String wantedId = rs.getString("Id_grupy");
            
                String getDataSQL = " SELECT description, " +
                                            " Slownik.gateId, " +
                                            " rodzajPomiaru," +
                                            " rodzajBramki, " +
                                            " LongGate " +
                                            " FROM Slownik" +
                                            " Join wizualizacja.dbo.EW_Bramki ON wizualizacja.dbo.EW_Bramki.GateId=Slownik.gateId WHERE Id_grupy=?;";
            
                preStatement = connection.prepareStatement(getDataSQL);
                preStatement.setString(1,wantedId);
                rs = preStatement.executeQuery();
                
                while (rs.next()) {
                    String[] strings = new String[6];
                    strings[0] = rs.getString("description");
                    strings[1] = rs.getString("gateId");
                    strings[2] = wantedId;
                    strings[3] = rs.getString("rodzajPomiaru");
                    strings[4] = rs.getString("rodzajBramki");
                    strings[5] = rs.getString("LongGate");
                    groupGates.add(new GroupGate(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]));
                }
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        } finally {
            return groupGates;
        }
    }
    void dbAddGroup(String groupName) throws SQLException{
    
        String findGroupSQL = "SELECT Nazwa FROM wizualizacja.dbo.EW_Grupy WHERE Nazwa = ?";
        String insertGroupSQL = "INSERT INTO wizualizacja.dbo.EW_Grupy VALUES (?)";
        @Cleanup
        PreparedStatement preparedStatement = connection.prepareStatement(findGroupSQL);
        preparedStatement.setString(1,groupName);
        @Cleanup
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()){
            throw new SQLException("Grupa już istnieje w bazie");
        } else {
            preparedStatement = connection.prepareStatement(insertGroupSQL);
            preparedStatement.setString(1,groupName);
            preparedStatement.executeUpdate();
        }
    }
    boolean dbEditGroup(LinkedList<String> gateIds, String groupId){
        String delSQL = "DELETE FROM wizualizacja.dbo.EW_Bramki " +
                                "WHERE Id_Grupy =" + groupId + ";";
        String findSQL = "SELECT * FROM wizualizacja.dbo.Slownik WHERE gateId=?;";
    
        boolean autoCommit = false;
        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            return false;
        }
    
        try{
            @Cleanup
            Statement stat = connection.createStatement();
            stat.addBatch(delSQL);
            
            @Cleanup
            PreparedStatement preparedStatement = connection.prepareStatement(findSQL);
            for (String gateId: gateIds) {
                preparedStatement.setString(1, gateId);
                @Cleanup
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    stat.addBatch("INSERT INTO wizualizacja.dbo.EW_Bramki VALUES (" + groupId + "," + gateId + ");");
                }
            }
            
            stat.executeBatch();
            try {
                connection.commit();
                connection.setAutoCommit(autoCommit);
            } catch (SQLException e1) {
                connection.rollback();
                MyLogger.getLogger().log(Level.SEVERE, Throwables.getStackTraceAsString(e1).trim());
                return false;
            }
            return true;
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            return false;
        }
    }
    String dbGetGroupIdUsingGroupName(String groupName){
        try {
            String sql = "SELECT Id_Grupy FROM wizualizacja.dbo.EW_Grupy WHERE Nazwa = ?;";
            @Cleanup
            PreparedStatement preStatement = connection.prepareStatement(sql);
            preStatement.setString(1,groupName);
            @Cleanup
            ResultSet preRS = preStatement.executeQuery();
            if(preRS.next()) {
                return preRS.getString("Id_Grupy");
            }else{
                throw new SQLException("Brak szukanej bramki!");
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            return null;
        }
    }
    boolean dbDeleteGroupUsingGroupId(String groupId){
        String sql = "DELETE FROM wizualizacja.dbo.EW_Grupy " +
                             "WHERE Id_Grupy = ?; ";
        try {
            @Cleanup
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,groupId);
            @Cleanup
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.wasNull()){
                return true;
            }
            else{
                throw new SQLException("Nieudana proba usuniecia grupy z numerem: " + groupId);
            }
        } catch (SQLException e) {
            if (e instanceof SQLServerException) return true;
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            return false;
        }
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
    
        if (instances == null)
            instances = new ArrayList<>(20);
        else
            instances.clear();
        
        for (String db : allInstances){
            if (Pattern.matches("e([1-9]|[1-9]{1}[0-9]+)_VfiTag",db))
                instances.add(db);
        }
    }
}