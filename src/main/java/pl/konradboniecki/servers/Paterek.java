package pl.konradboniecki.servers;

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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

final class Paterek {
    private static final String DB_PATEREK             = "jdbc:sqlserver://" + Configurator.getCurrentSettings().getProperty("Paterek-Adress");
    private static final String USERNAME_PATEREK       = Configurator.getCurrentSettings().getProperty("User-Paterek");
    private static final String PASSWORD_PATEREK       = Configurator.getCurrentSettings().getProperty("Password-Paterek");
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static Connection connection;
    private static Paterek instance = new Paterek();
    static Paterek getInstance() throws NullPointerException{
        if (instance != null) {
            return instance;
        } else{
            throw new NullPointerException("Paterek instance is null");
        }
    }
    
    private Paterek(){}
    
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
            connection = DriverManager.getConnection(DB_PATEREK, USERNAME_PATEREK, PASSWORD_PATEREK);
            
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
    public void closeConnection()throws SQLException{
        if (!connection.isClosed()) {
            connection.close();
        }
    }
}
