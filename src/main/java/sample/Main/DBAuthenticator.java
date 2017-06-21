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
import java.util.List;
import java.util.logging.Level;

public final class DBAuthenticator {
    private static Configurator config = Configurator.getInstance();
    private static final String DB = "jdbc:sqlserver://"
                                             + Configurator.getCurrentSettings().getProperty("Server-Adress");
    private static final String USER = Configurator.getCurrentSettings().getProperty("User");
    private static final String USERPW = Configurator.getCurrentSettings().getProperty("Password");
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static Connection connection;
    private static DBAuthenticator instance = new DBAuthenticator();
    
    public static DBAuthenticator getInstance(){
        return instance;
    }
    
    private DBAuthenticator(){
        try {
            Class.forName(DRIVER).newInstance();
            connection = DriverManager.getConnection(DB, USER, USERPW);
        } catch (InstantiationException e) {
            MyLogger.getLogger().log(Level.SEVERE,Throwables.getStackTraceAsString(e));
        } catch (IllegalAccessException e) {
            MyLogger.getLogger().log(Level.SEVERE,Throwables.getStackTraceAsString(e).trim());
        } catch (ClassNotFoundException e) {
            MyLogger.getLogger().log(Level.SEVERE,Throwables.getStackTraceAsString(e).trim());
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.SEVERE,Throwables.getStackTraceAsString(e));
            System.exit(1);
        }
        try {
            createTablesIfNotExists();
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING,Throwables.getStackTraceAsString(e));
        }
    }
    private void createTablesIfNotExists() throws SQLException {
        @Cleanup
        Statement statement = connection.createStatement();
        try {
            Path path = Paths.get("src/main/resources/", "Create_Users.sql");
            List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
            //TODO: batch
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
            MyLogger.getLogger().log(Level.INFO,Throwables.getStackTraceAsString(e));
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
            MyLogger.getLogger().log(Level.SEVERE,Throwables.getStackTraceAsString(e));
            return false;
        }
    }
    public static String hashPassword(String password){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashArray = digest.digest(password.getBytes("UTF-8"));
            return DatatypeConverter.printHexBinary(hashArray);
        } catch (NoSuchAlgorithmException e) {
            MyLogger.getLogger().log(Level.WARNING,Throwables.getStackTraceAsString(e));
        } catch (UnsupportedEncodingException e) {
            MyLogger.getLogger().log(Level.WARNING,Throwables.getStackTraceAsString(e));
        }
        return null;
    }
}
