package pl.konradboniecki.servers;

import com.google.common.base.Throwables;
import lombok.Cleanup;
import pl.konradboniecki.general.Configurator;
import pl.konradboniecki.general.MyLogger;
import pl.konradboniecki.main.GroupGate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

final class TreblinkaFirst {
    private static final String DB_TREBLINKA_1       = "jdbc:sqlserver://" + Configurator.getCurrentSettings().getProperty("Adress-Treblinka-1");
    private static final String USERNAME_TREBLINKA_1 = Configurator.getCurrentSettings().getProperty("User-Treblinka-1");
    private static final String PASSWORD_TREBLINKA_1 = Configurator.getCurrentSettings().getProperty("Password-Treblinka-1");
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static Connection connection;
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
            
            try {
                setUpStructuresIfNotExists();
            } catch (SQLException e) {
                MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            }
        }
    }
    private void setUpStructuresIfNotExists() throws SQLException{
        @Cleanup Statement statement = connection.createStatement();
        try {
            Path path = Paths.get("src/main/resources/", "Create_Groups.sql");
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
            MyLogger.getLogger().log(Level.WARNING, "NIE ZNALEZIONO PLIKU Create_Groups.sql");
        } catch(IOException e) {
            MyLogger.getLogger().log(Level.WARNING, "BLAD ODCZYTU PLIKU Create_Groups.sql");
        }
    }
    private void executeCreateIfNotExists(String query, Statement statement){
        try {
            statement.execute(query);
        } catch (SQLException e) {
            // One of the scripts from "Create_Groups.sql" failed (because exists)
        }
    }
    void closeConnection()throws SQLException{
        if (!connection.isClosed()) {
            connection.close();
        }
    }
    
    List<String> dbGetAllExistingGroupNames(){
        List<String> list = new LinkedList<>();
        try {
            @Cleanup
            Statement statement = connection.createStatement();
            @Cleanup ResultSet rs = statement.executeQuery("select Nazwa from KONRAD_GRUPY");
            while(rs.next()){
                list.add(rs.getString("Nazwa"));
            }
            return list;
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            return Collections.emptyList();
        }
    }
    List<GroupGate> dbGetAllGates() {
        try {
            String sql = "USE wizualizacja2 " + "SELECT gateId " + "      ,LongGate AS Skrocona_Nazwa " + "      ,description AS Opis " + "      ,rodzajBramki " + "      ,rodzajPomiaru " + "  FROM Slownik;";
            @Cleanup Statement statement = connection.createStatement();
    
            @Cleanup ResultSet rs = statement.executeQuery(sql);
            LinkedList<GroupGate> groupGates = new LinkedList<>();
            while (rs.next()) {
                String[] strings = new String[6];
                strings[0] = rs.getString("Opis");
                strings[1] = rs.getString("GateID");
                strings[2] = null;
                strings[3] = rs.getString("rodzajPomiaru");
                strings[4] = rs.getString("rodzajBramki");
                strings[5] = rs.getString("Skrocona_Nazwa");
                groupGates.add(new GroupGate(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]));
            }
            return groupGates;
        } catch (SQLException e){
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            return Collections.EMPTY_LIST;
        }
    }
}