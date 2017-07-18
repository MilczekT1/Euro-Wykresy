package pl.konradboniecki.servers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class TestTreblinkaSecond {
    @Test
    public void testGetInstance() {
        try{
            TreblinkaSecond dbt2 = TreblinkaSecond.getInstance();
            Assertions.assertNotNull(dbt2);
        } catch (NullPointerException exc){
            ;
        }
    }
    @Test
    public void testIsConnected(){
        TreblinkaSecond dbt1 = TreblinkaSecond.getInstance();
        try {
            if (dbt1.isConnected()) {
                ;
            }
        } catch(Exception e){
            Assertions.fail("Exception has been thrown");
        }
    }
    @Test
    public void testConnect(){
        try {
            TreblinkaSecond.getInstance().connect();
            if (!TreblinkaSecond.getInstance().isConnected()){
                Assertions.fail("DBTreblinka hasn't got a connection");
            }
        } catch(Exception e){
            ;
        }
    }
    @Test
    public void testCloseConnection(){
        TreblinkaSecond dbt = TreblinkaSecond.getInstance();
        try {
            dbt.connect();
            try {
                dbt.closeConnection();
            } catch(SQLException e){
                ;
            } finally{
                if (dbt.isConnected()){
                    Assertions.fail("Connection has not been closed");
                }
            }
        } catch (Exception e) {
            ;
        }
    }
}