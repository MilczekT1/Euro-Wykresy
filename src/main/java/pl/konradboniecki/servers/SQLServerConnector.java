package pl.konradboniecki.servers;

import com.google.common.base.Throwables;
import pl.konradboniecki.general.MyLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public abstract class SQLServerConnector {
    protected static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    protected static Connection connection;
    protected String SERVER_ADRESS;
    protected String USERNAME;
    protected String PASSWORD;
    
    public boolean isConnected(){
        try {
            return (connection != null && !connection.isClosed() && connection.isValid(3000)) ? true : false;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public void connect() throws Exception {
        if (!isConnected()){
            Class.forName(DRIVER).newInstance();
            connection = DriverManager.getConnection(SERVER_ADRESS, USERNAME, PASSWORD);
            
            try {
                setUpStructuresIfNotExists();
            } catch (SQLException e) {
                MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
            }
        }
    }
    
    public void closeConnection() throws SQLException{
        if (!connection.isClosed()) {
            connection.close();
        }
    }
    
    protected abstract void setUpStructuresIfNotExists() throws SQLException;
    
}
