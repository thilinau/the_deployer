package it.bridge.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.bridge.loggers;

public class DBConnection {
	
	private  Connection _CommonConn;
	private static String conn_url="";
	private static String current_schema="";
	private static String current_schema_pass="";
	
	public DBConnection(RunnerParameters runParam) 
	{		

		try
		{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			
		}catch (Exception e) {
			loggers.addLogAndPrint("SEVE", "Connection Driver Failed..", false);
			loggers.addLogEx("SEVE", e.getMessage(), e);
		}

		if (System.getProperty("enable.tns").equals("true")){
			conn_url="jdbc:oracle:thin:@" + runParam.getSid();
		}else {
			conn_url="jdbc:oracle:thin:@" + runParam.getHost()  + ":" + runParam.getPort() + ":" + runParam.getSid();
		}
		current_schema= runParam.getSchemaUser();
		current_schema_pass=runParam.getSchemaPassword();
		
	}
  
	
	public boolean connect(){
		
		try {
			_CommonConn = DriverManager.getConnection(conn_url,current_schema,current_schema_pass);

		} catch (SQLException e) {
			
			loggers.addLog("SEVE", "Connection Status " + current_schema + " - FAILED");
			loggers.addLogEx("SEVE", e.getMessage(), e);
		} 
		
		if (_CommonConn != null) {
			return true;
		} else {
			return false;
		}	
	}

   public boolean GetDBConnectionStatus()
   {
		if (_CommonConn != null) {
			return true;
		} else {
			return false;
		}	
   }
	   
	   

    public Connection GetDBConnection()
    {
        return _CommonConn;
    }


    public void CloseDBConnection()
    {
    	try {
    		_CommonConn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    
	public ResultSet getData(String strSQL) throws SQLException
    {
    	ResultSet retRS= null;
		PreparedStatement preStatement = null;

		try {
			
			preStatement = _CommonConn.prepareStatement(strSQL);
			retRS = preStatement.executeQuery();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		} finally {

			//if (preStatement != null) {
			//	preStatement.close();
			//}
		}
		
		return retRS;
    	
    }
}
