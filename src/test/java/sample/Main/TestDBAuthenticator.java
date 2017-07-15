package sample.Main;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
    public void testConnectIfNullOrClosed(){
        DBAuthenticator.getInstance().connectIfNullOrClosed();
    }
}
