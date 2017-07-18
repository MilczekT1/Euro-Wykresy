package pl.konradboniecki.servers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;


public class TestTreblinkaFirst {
    @Test
    public void testGetInstance() {
        try{
            TreblinkaFirst dbt1 = TreblinkaFirst.getInstance();
            Assertions.assertNotNull(dbt1);
        } catch (NullPointerException exc){
            ;
        }
    }
    @Test
    public void testIsConnected(){
        TreblinkaFirst dbt1 = TreblinkaFirst.getInstance();
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
            TreblinkaFirst.getInstance().connect();
            if (!TreblinkaFirst.getInstance().isConnected()){
                Assertions.fail("DBTreblinka hasn't got a connection");
            }
        } catch(Exception e){
            ;
        }
    }
    @Test
    public void testCloseConnection(){
        TreblinkaFirst dbt = TreblinkaFirst.getInstance();
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
