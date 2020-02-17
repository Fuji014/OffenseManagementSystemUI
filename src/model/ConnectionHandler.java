package model;

import java.sql.*;

public class ConnectionHandler {
    public Connection conn;
    private boolean isConnected = false;
    public Connection getConnection() throws ClassNotFoundException, SQLException { // get connection
        String myUrl = "jdbc:mysql://"+Config.server+":"+Config.port+"/"+Config.database+"?autoReconnect=true&useSSL=false";
        String myDriver = "com.mysql.jdbc.Driver";
        Class.forName(myDriver);
        conn = DriverManager.getConnection(myUrl,Config.username,Config.password);
        return conn;
    }
    public boolean isConnected(){ // get connection
        try {
            String myUrl = "jdbc:mysql://"+Config.server+":"+Config.port+"/"+Config.database+"?autoReconnect=true&useSSL=false";
            String myDriver = "com.mysql.jdbc.Driver";
            Class.forName(myDriver);
            conn = DriverManager.getConnection(myUrl,Config.username,Config.password);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public void close(Connection connection, PreparedStatement prs, ResultSet rs) throws SQLException { // close connection
        if(connection != null){
            connection.close();
        }if(prs != null){
            prs.close();
        }if(rs != null){
            rs.close();
        }
    }
}
