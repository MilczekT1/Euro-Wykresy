package sample.Main;

import com.google.common.base.Throwables;
import lombok.Cleanup;
import sample.General.Configurator;
import sample.General.MyLogger;

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

final class DBGroupManager {
    private static Configurator config = Configurator.getInstance();
    private static final String DB = "jdbc:sqlserver://"
                                             + Configurator.getCurrentSettings().getProperty("Server-Adress");
    private static final String USER = Configurator.getCurrentSettings().getProperty("User");
    private static final String USERPW = Configurator.getCurrentSettings().getProperty("Password");
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static Connection connection;
    private static DBGroupManager instance = new DBGroupManager();

    private DBGroupManager(){
        try {
            Class.forName(DRIVER).newInstance();
            connection = DriverManager.getConnection(DB, USER, USERPW);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.SEVERE, Throwables.getStackTraceAsString(e).trim());
            System.exit(1);
        }
    
        try {
            createTablesIfNotExists();
        } catch (SQLException e) {
            //TODO: po dodaniu batchy, zmienic
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
    }
    private void createTablesIfNotExists() throws SQLException {
        @Cleanup
        Statement statement = connection.createStatement();
        
        try {
            Path path = Paths.get("src/main/resources/", "Create_Groups.sql");
            List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
            //TODO: dodaj batcha by jeden error nie gubil reszty, potem zmien obsluge wyjatku w creattablesifnoteist
            //List<String> queries = new LinkedList<>();
            
            String query = "";
            for (String line : lines){
                query += line;
                if (line.endsWith(";")){
                    
                    statement.execute(query);
                    query = "";
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    protected static List<String> dbGetAllExistingGroupNames(){
        try {
            @Cleanup
            Statement statement = connection.createStatement();
            @Cleanup
            ResultSet rs = statement.executeQuery("select Nazwa from KONRAD_GRUPY");
            LinkedList<String> list = new LinkedList<>();
            while(rs.next()){
                list.add(rs.getString("Nazwa"));
            }
            return list;
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
        return Collections.EMPTY_LIST;
    }
    protected static LinkedList<Gate> dbGetAllGatesFromGroup(String groupName){
        try {
            String getGroupIdSQL = "USE wizualizacja2; SELECT Id_grupy FROM KONRAD_GRUPY WHERE Nazwa = ?;";
            @Cleanup
            PreparedStatement preStatement = connection.prepareStatement(getGroupIdSQL);
            preStatement.setString(1,groupName);
            @Cleanup
            ResultSet rs = preStatement.executeQuery();
            if(rs.next()) {
                String wantedId = rs.getString("Id_grupy");
                
                String getDataSQL = "USE wizualizacja2; SELECT description, Slownik.gateId, rodzajPomiaru,rodzajBramki, LongGate FROM Slownik Join KONRAD_BRAMKI ON KONRAD_BRAMKI.GateId=Slownik.gateId WHERE Id_grupy=?;";
                
                preStatement = connection.prepareStatement(getDataSQL);
                preStatement.setString(1,wantedId);
                rs = preStatement.executeQuery();
                
                LinkedList<Gate> gates = new LinkedList<>();
                while (rs.next()) {
                    String[] strings = new String[6];
                    strings[0] = rs.getString("description");
                    strings[1] = rs.getString("gateId");
                    strings[2] = wantedId;
                    strings[3] = rs.getString("rodzajPomiaru");
                    strings[4] = rs.getString("rodzajBramki");
                    strings[5] = rs.getString("LongGate");
                    gates.add(new Gate(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]));
                }
                return gates;
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
        return null;
    }
    protected static LinkedList<Gate> dbGetAllGates(){
        try {
            String sql = "USE wizualizacja2 " +
                                 "SELECT gateId " +
                                 "      ,LongGate AS Skrocona_Nazwa " +
                                 "      ,description AS Opis " +
                                 "      ,rodzajBramki " +
                                 "      ,rodzajPomiaru " +
                                 "  FROM Slownik;";
            @Cleanup
            Statement statement = connection.createStatement();

            @Cleanup
            ResultSet rs = statement.executeQuery(sql);
            LinkedList<Gate> gates = new LinkedList<>();
            while (rs.next()) {
                String[] strings = new String[6];
                strings[0] = rs.getString("Opis");
                strings[1] = rs.getString("GateID");
                strings[2] = null;
                strings[3] = rs.getString("rodzajPomiaru");
                strings[4] = rs.getString("rodzajBramki");
                strings[5] = rs.getString("Skrocona_Nazwa");
                gates.add(new Gate(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]));
            }
            return gates;
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
        return null;
    }
    protected static String dbGetGroupIdUsingGroupName(String groupName){
        try {
            String sql = "USE wizualizacja2; SELECT Id_Grupy FROM Konrad_GRUPY WHERE Nazwa = ?;";
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
        }
        return null;
    }
    protected static Gate dbGetGateUsingGateId(String gateId){
        try {
            String sql = "USE wizualizacja2 "
                                 + "SELECT gateId " +
                                 "      ,LongGate AS Skrocona_Nazwa " +
                                 "      ,description AS Opis " +
                                 "      ,rodzajBramki " +
                                 "      ,rodzajPomiaru " +
                                 "  FROM Slownik WHERE gateId=?;";
            @Cleanup
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,gateId);
            @Cleanup
            ResultSet rs = preparedStatement.executeQuery();
            
            if (rs.next()) {
                String[] strings = new String[6];
                strings[0] = rs.getString("Opis");
                strings[1] = rs.getString("GateID");
                strings[2] = null;
                strings[3] = rs.getString("rodzajPomiaru");
                strings[4] = rs.getString("rodzajBramki");
                strings[5] = rs.getString("Skrocona_Nazwa");
                return new Gate(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]);
            }else{
                throw new SQLException("there is no such gate " + gateId);
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
        return null;
    }
    protected static boolean dbAddGroup(String groupName)throws SQLException{
    
        String findGroupSQL = "USE wizualizacja2;" +
                                      "SELECT Nazwa FROM KONRAD_GRUPY WHERE Nazwa = ?";
        String insertGroupSQL = "USE wizualizacja2;" +
                             "INSERT INTO KONRAD_GRUPY VALUES (?)";
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
            return true;
        }
    }
    protected static void dbInsertCurrentGatesIntoGroup(String gateId, String groupId){
        String findSQL = "USE wizualizacja2;" +
                                   "SELECT * FROM Slownik WHERE gateId=?";
        String insertSQL = "USE wizualizacja2;" +
                                   "INSERT INTO KONRAD_BRAMKI VALUES (?,?)";
        try {
            @Cleanup
            PreparedStatement preparedStatement = connection.prepareStatement(findSQL);
            preparedStatement.setString(1, gateId);
            @Cleanup
            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next()){
                preparedStatement = connection.prepareStatement(insertSQL);
                preparedStatement.setString(1, groupId);
                preparedStatement.setString(2, gateId);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
    }
}