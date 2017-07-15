package sample.Main;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class TestDBAuthenticator {
    
    @Test
    public void testGetInstance() {
        try{
            DBAuthenticator dba = DBAuthenticator.getInstance();
            Assertions.assertNotNull(dba);
        } catch (NullPointerException exc){
            ;
        }
    }
    @Test
    public void testIsConnected(){
        DBAuthenticator dba = DBAuthenticator.getInstance();
        try {
            if (dba.isConnected()) {
                ;
            }
        } catch(Exception exc){
            Assertions.fail("Exception has been thrown");
        }
    }
    @Test
    public void testConnectIfNullOrClosed(){
        try {
            DBAuthenticator.getInstance().connectIfNullOrClosed();
            if (!DBAuthenticator.getInstance().isConnected()){
                Assertions.fail("DBAuthenticator hasn't got a connection");
            }
        } catch(Exception exc){
            ;
        }
    }
    
    @Test
    public void testCloseConnection(){
        DBAuthenticator dba = DBAuthenticator.getInstance();
        try {
            dba.connectIfNullOrClosed();
            try {
                dba.closeConnection();
            } catch(SQLException e){
                if (dba.isConnected()){
                    Assertions.fail("DBAuthenticator should be connected");
                }
            }
        } catch (Exception e) {
            ;
        }
    }
    
}
