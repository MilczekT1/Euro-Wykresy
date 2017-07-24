package pl.konradboniecki.servers;

import pl.konradboniecki.structures.GroupGate;
import pl.konradboniecki.structures.MinMax;

import java.sql.*;
import java.util.LinkedList;

public final class DBGroupManager {
    
    private static SourceManager sourceManager = SourceManager.getInstance();
    
    private static DBGroupManager instance = new DBGroupManager();
    public static DBGroupManager getInstance() throws NullPointerException {
        if (instance != null) {
            return instance;
        } else{
            throw new NullPointerException("DBGroupManager instance is null");
        }
    }
    private DBGroupManager(){
    }
    
    public void connect() throws Exception {
        sourceManager.connectToAllSources();
    }
    
    public static LinkedList<String> getAllAvailableGroupNames(){
        return sourceManager.getAllAvailableGroupNames();
    }
    public static LinkedList<GroupGate> getAllGatesFromGroup(String groupName){
        return sourceManager.getAllGatesFromGroup(groupName);
    }
    public static LinkedList<GroupGate> getAllGates(){
        return sourceManager.getAllGates();
    }
    public static String getGroupIdUsingGroupName(String groupName){
        return sourceManager.getGroupIdUsingGroupName(groupName);
    }
    public static void addGroup(String groupName) throws SQLException{
        sourceManager.addGroup(groupName);
    }
    public static boolean editGroup(LinkedList<String> gateIds, String groupId){
        return sourceManager.editGroup(gateIds, groupId);
    }
    public static boolean deleteGroupUsingGroupId(String groupId){
        return sourceManager.deleteGroupUsingGroupId(groupId);
    }
    public static MinMax getMinAndMax() throws SQLException{
        return sourceManager.getMinAndMaxAvailableTimePoints();
    }
    /*
    public static GroupGate dbGetGateUsingGateId(String gateId){
        try {
            String sql = "USE wizualizacja2 "
                                 + "SELECT gateId " +
                                 "      ,LongGate AS Skrocona_Nazwa " +
                                 "      ,description AS Opis " +
                                 "      ,rodzajBramki " +
                                 "      ,rodzajPomiaru " +
                                 "  FROM Slownik WHERE gateId=?;";
            @Cleanup
            PreparedStatement preparedStatement = connectionWithTreblinka_1.prepareStatement(sql);
            preparedStatement.setString(1,gateId);
            @Cleanup
            ResultSet rs = preparedStatement.executeQuery();
            
            if (rs.next()) {
                String[] strings = new String[6];
                strings[0] = rs.getString("Opis");
                strings[1] = rs.getString("GateID");
                strings[2] = null;
                strings[3] = rs.getString("rodzajPomiaru");
                strings[4] = rs.getString("rodzajBramki");
                strings[5] = rs.getString("Skrocona_Nazwa");
                return new GroupGate(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]);
            }else{
                throw new SQLException("there is no such gate " + gateId);
            }
        } catch (SQLException e) {
            MyLogger.getLogger().log(Level.WARNING, Throwables.getStackTraceAsString(e).trim());
        }
        return null;
    }
    */
}
