package sample.Main;

import com.google.common.base.Throwables;
import lombok.Cleanup;
import sample.General.Configurator;
import sample.General.MyLogger;

import javax.xml.bind.DatatypeConverter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

final class DBAuthenticator {
    private static final String DB = "jdbc:sqlserver://"
                                             + Configurator.getCurrentSettings().getProperty("Server-Adress");
    private static final String USER = Configurator.getCurrentSettings().getProperty("User");
    private static final String USERPW = Configurator.getCurrentSettings().getProperty("Password");
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static Connection connection;
    private static DBAuthenticator instance = new DBAuthenticator();
    
    private DBAuthenticator(){}
    
    public static DBAuthenticator getInstance() {
        return instance;
    }
    public void connectIfNull() {
        if (DBAuthenticator.getInstance() != null) {
            ;
        }
        else{
            try {
                Class.forName(DRIVER).newInstance();
                connection = DriverManager.getConnection(DB, USER, USERPW);
            } catch (InstantiationException | IllegalAccessException | SQLException | ClassNotFoundException e) {
                MyLogger.getLogger().log(Level.SEVERE,Throwables.getStackTraceAsString(e).trim());
            }
    
            try {
                createTablesIfNotExists();
            } catch (SQLException e) {
                MyLogger.getLogger().log(Level.WARNING,Throwables.getStackTraceAsString(e).trim());
            }
        }
    }
    
    private void createTablesIfNotExists() throws SQLException {
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
    
    public static Boolean tryToLoginAndReturnAccessType(String login, String password, GuiDataContainer gdc){
        String sql  = "USE wizualizacja2;" +
                              "SELECT * FROM KONRAD_UZYTKOWNICY WHERE Login=? AND Haslo=?";
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
    public static boolean tryToRegister(String login, String password) {
        String sql  = "USE wizualizacja2;" +
                              "SELECT * FROM KONRAD_UZYTKOWNICY WHERE Login=?";
        try {
            @Cleanup
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,login);
            @Cleanup
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){
                return false;
            } else {
                String insertSQL = "USE wizualizacja2;" +
                                           "INSERT INTO KONRAD_UZYTKOWNICY(Login, Haslo) VALUES (?,?)";
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
    public static String hashPassword(String password){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashArray = digest.digest(password.getBytes("UTF-8"));
            return DatatypeConverter.printHexBinary(hashArray);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            MyLogger.getLogger().log(Level.WARNING,Throwables.getStackTraceAsString(e).trim());
        }
        return null;
    }
}