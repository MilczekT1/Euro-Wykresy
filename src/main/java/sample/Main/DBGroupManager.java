package sample.Main;

import com.google.common.base.Throwables;
import com.microsoft.sqlserver.jdbc.SQLServerException;
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
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            MyLogger.getLogger().log(Level.SEVERE,Throwables.getStackTraceAsString(e).trim());
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.SEVERE, Throwables.getStackTraceAsString(e).trim());
            System.exit(1);
        }
    
        try {
            createTablesIfNotExists();
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
    }
    private void createTablesIfNotExists() throws SQLException {
        @Cleanup
        Statement statement = connection.createStatement();
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
            MyLogger.getLogger().log(Level.INFO, "Nie utworzono jednej z tabel");
        }
    }
    
    static List dbGetAllExistingGroupNames(){
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
    static LinkedList<GroupGate> dbGetAllGatesFromGroup(String groupName){
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
                
                LinkedList<GroupGate> groupGates = new LinkedList<>();
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
                return groupGates;
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
        return null;
    }
    static LinkedList<GroupGate> dbGetAllGates(){
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
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            throw new NullPointerException("Nieudana proba poboru bazy slownika");
        }
    }
    static String dbGetGroupIdUsingGroupName(String groupName){
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
            return null;
        }
    }
    protected static GroupGate dbGetGateUsingGateId(String gateId){
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
                return new GroupGate(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]);
            }else{
                throw new SQLException("there is no such gate " + gateId);
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
        return null;
    }
    static void dbAddGroup(String groupName)throws SQLException{
    
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
        }
    }
    static boolean dbDeleteGroupUsingGroupId(String groupId){
        String sql = "USE wizualizacja2;" +
                             "DELETE FROM KONRAD_GRUPY " +
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
    static void dbInsertCurrentGatesIntoGroup(String gateId, String groupId){
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
