package pl.konradboniecki.servers;

import java.sql.Connection;

public abstract class SQLServerConnector {
    protected static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    protected static Connection connection;
    protected String SERVER_ADRESS;
    protected String USERNAME;
    protected String PASSWORD;
}
