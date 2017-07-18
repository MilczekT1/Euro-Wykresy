package pl.konradboniecki.main;

import com.google.common.base.Throwables;
import lombok.Cleanup;
import pl.konradboniecki.general.Configurator;
import pl.konradboniecki.general.MyLogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

final class DBAuthenticator {
    private static final String DB = "jdbc:sqlserver://" + Configurator.getCurrentSettings().getProperty("Adress-Treblinka-1");
    private static final String USER = Configurator.getCurrentSettings().getProperty("User-Treblinka-1");
    private static final String USERPW = Configurator.getCurrentSettings().getProperty("Password-Treblinka-1");
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static Connection connection;
    private static DBAuthenticator instance = new DBAuthenticator();
    
    private DBAuthenticator(){}
    
    public static DBAuthenticator getInstance() throws NullPointerException {
        if (instance != null) {
            return instance;
        } else{
            throw new NullPointerException("DBAuthenticator instance is null");
        }
    }
    public boolean isConnected(){
        try {
            return (connection != null && !connection.isClosed() && connection.isValid(3000)) ? true : false;
        } catch (SQLException e) {
            return false;
        }
    }
    void connect() throws Exception {
        if (!isConnected()){
            Class.forName(DRIVER).newInstance();
            connection = DriverManager.getConnection(DB, USER, USERPW);
    
            try {
                setUpStructuresIfNotExists();
            } catch (SQLException e) {
                MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            }
        }
    }
    private void setUpStructuresIfNotExists() throws SQLException {
        @Cleanup
        Statement statement = connection.createStatement();
        try {
            Path path = Paths.get("src/main/resources/", "Create_Users.sql");
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
            MyLogger.getLogger().log(Level.WARNING, "NIE ZNALEZIONO PLIKU Create_Users.sql");
        } catch(IOException e) {
            MyLogger.getLogger().log(Level.WARNING, "BLAD ODCZYTU PLIKU Create_Users.sql");
        }
    }
    private void executeCreateIfNotExists(String query, Statement statement){
        try {
            statement.execute(query);
        } catch (SQLException e) {
            // One of the scripts from "Create_Groups.sql" failed (because exists)
        }
    }
    
    public void closeConnection() throws SQLException{
        if (!connection.isClosed()) {
            connection.close();
        }
    }
    
    static Boolean tryToLogin(String login, String password, GuiDataContainer gdc){
        String sql  = "USE wizualizacja;" +
                              "SELECT * FROM EW_Uzytkownicy WHERE Login=? AND Haslo=?";
        try {
            @Cleanup
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,login);
            preparedStatement.setString(2,password);
            @Cleanup
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                gdc.setAccessLevel(rs.getInt("Czy_admin"));
                return true;
            }
            else {
                return false;
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.INFO,Throwables.getStackTraceAsString(e).trim());
            return false;
        }
    }
    static boolean tryToRegister(String login, String password) {
        String sql  = "USE wizualizacja;" +
                              "SELECT * FROM EW_Uzytkownicy WHERE Login=?";
        try {
            @Cleanup
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,login);
            @Cleanup
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){
                return false;
            } else {
                String insertSQL = "USE wizualizacja;" +
                                           "INSERT INTO EW_Uzytkownicy(Login, Haslo) VALUES (?,?)";
                preparedStatement = connection.prepareStatement(insertSQL);
                preparedStatement.setString(1,login);
                preparedStatement.setString(2,password);
                preparedStatement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.SEVERE,Throwables.getStackTraceAsString(e).trim());
            return false;
        }
    }
}