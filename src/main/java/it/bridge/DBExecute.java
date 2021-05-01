package it.bridge;

import it.bridge.database.DBConnection;
import it.bridge.database.Pare;
import it.bridge.database.RunnerParameters;
import it.bridge.database.ScriptRunner;
import it.bridge.objects.ParamDBInvalid;
import it.bridge.objects.ParamDBReport;
import it.bridge.objects.ParamFinalDBReport;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import static it.bridge.GetInfo.getPasswordFromKeyFiles;

public class DBExecute {
	
	static String selecEnv;
	static HashMap<String, String> DB_SCHEMA_MAPPER = new HashMap<String, String>();
	static HashMap<String, String> DB_SCHEMA_USER_PASS = new HashMap<String, String>();
	public static List<String> DB_SCHEMAS_LIST=null;
	static HashMap<String, String> RELEASE_AVAIL_DB_SCHEMAS = new HashMap<String, String>();
	public static String DB_HOST,DB_SID;
	public static int DB_PORT;
	public static Map<Integer, ArrayList<String>> AVAILABLE_DB_SCRIPTS = new TreeMap<Integer, ArrayList<String>>();
	public static Map<String, List<Pare>> SCRIPT_REPLACE_MAP = new TreeMap<String, List<Pare>>(); //Hold script replace regex
	public static boolean DB_ENABLE_MULTIPLE_INSTANCES=false;
	public static HashMap<String, RunnerParameters> DB_OVERRIDE_SCHEMAS = new HashMap<String, RunnerParameters>();	
	public static ArrayList<Integer> DB_VALID_OVERRIDE_SCHEMA_INDEXES = new ArrayList<Integer>();
	
	public boolean checkScriptAvailability(String SELECTED_REL){
		
		boolean SCRIPTS_AVILABLE=false;
		
		File f = new File(System.getProperty("BASE_DIR") +  File.separator + "METERIALS" +  File.separator + GetInfo.getReleaseSource() ); 
        File[] files = f.listFiles();
        
        ArrayList<Integer> RELEASES_TO_DEPLOY  = new ArrayList<Integer>();
        
        try{
        	
        	if ( System.getProperty("ADHOC_DEPLOYMENT_FLAG").equals("false") ){
	        	for( int i=0;i<files.length;i++){
	        	
		        	File file = files[i];
		        	int REL_VER;
		        	
	
		        	if (file.isDirectory()){
		            	REL_VER=Integer.parseInt(file.getName().split("_")[1].replaceAll("\\.", ""));
		  
		            	if ( REL_VER > Integer.parseInt(universal_deployer.DB_VERSION.replaceAll("\\.", "")) && REL_VER<=Integer.parseInt(SELECTED_REL.replaceAll("\\.", ""))){
		            		RELEASES_TO_DEPLOY.add(REL_VER);
		            	}
		            }
		        }
	        	
        	}else{
        		
        		for( int i=0;i<files.length;i++){
    	        	
		        	File file = files[i];
		        	int ADHOC_REL_VER;
		        	
		        	if (file.isDirectory()){
		        		
		        		ADHOC_REL_VER=Integer.parseInt(file.getName().split("_")[1].replaceAll("\\.", ""));
		        		if (ADHOC_REL_VER == Integer.parseInt(SELECTED_REL.replaceAll("\\.", ""))){
		        			RELEASES_TO_DEPLOY.add(ADHOC_REL_VER);
		        		}
		        	}
        		}
        	}
        	
            Collections.sort(RELEASES_TO_DEPLOY);
            
            for( int i=0;i<RELEASES_TO_DEPLOY.size();i++){
           		
            	String PARENT_DIR=System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + GetInfo.getReleaseSource() + File.separator + GetInfo.getReleaseDirectory(RELEASES_TO_DEPLOY.get(i)) + File.separator + "DATABASE";
        		
        		File fs = new File(PARENT_DIR ); 
                File[] filess = fs.listFiles();
                
                
                if ( filess != null ){
                	SCRIPTS_AVILABLE=true;
        			break; 
                }   			
            }
            
        }catch(Exception e){
        	
        	SCRIPTS_AVILABLE=false;	
        }
		
		return SCRIPTS_AVILABLE;
	}

	public boolean setParams(boolean DBOnlyFlag) {
		boolean con_flag = true;
		
		if (DBOnlyFlag == true){
			selecEnv = universal_deployer.profile_selection();
			universal_deployer.setSelecEnv(selecEnv);
		}else{
			selecEnv = System.getProperty("selecEnv");
		}
			
		
		if (selecEnv.equals("Back")){
			
			try {
				universal_deployer.main(null);
			} catch (IOException e) {
				con_flag=false;
				loggers.addLogEx("SERV", e.getMessage(), e);
			} catch (Exception e) {
				con_flag=false;
				loggers.addLogEx("SERV", e.getMessage(), e);
			}
			
		}else{
			
			if (DBOnlyFlag == true){
				
				while( true )
				{
					try {
						
						String selec=selectMode();
						
						if (selec.equals("1")){
							
							loggers.addLog("INFO", "SELECTED PROFILE (DATABASE) : " +  selecEnv);					
							loggers.addLog("INFO", "DEPLOYER VERSION - (DATABASE) " + universal_deployer.DEPLOYER_REL_VER);
							loggers.addLog("INFO", "DEPLOYER LAST UPDATED DATE - (DATABASE) " + universal_deployer.DEPLOYER_REL_DATE);
							loggers.addLog("INFO", "Selected Mode (DATABASE) - GENERAL");
							
							if (universal_deployer.load_profile(selecEnv)==true){
								
						        System.setProperty("ADHOC_DEPLOYMENT_FLAG", "false");
								break;	
							}
							
						}else if (selec.equals("2")){
													
							loggers.addLog("INFO", "SELECTED PROFILE (DATABASE) : " +  selecEnv);					
							loggers.addLog("INFO", "DEPLOYER VERSION - (DATABASE) " + universal_deployer.DEPLOYER_REL_VER);
							loggers.addLog("INFO", "DEPLOYER LAST UPDATED DATE - (DATABASE) " + universal_deployer.DEPLOYER_REL_DATE);
							loggers.addLog("INFO", "Selected Mode (DATABASE) - ADHOC");
							
							if (universal_deployer.load_profile(selecEnv)==true){

								System.setProperty("ADHOC_DEPLOYMENT_FLAG", "true");
								break;
							}
						}
						else if (selec.equals("3"))
						{
							break;				
						}
						else if (selec.equals("4"))
						{
							System.out.println("Bye");
							System.exit(0);
							
						}else{
							
							System.out.println("Info!! Invalid Selection");
							continue;
						}		
						
					} catch (IOException e) {
						con_flag=false;
						loggers.addLogEx("SERV", e.getMessage(), e);
					}
				}
			}
			
			String PROCEED_CONFIRMATION="-1";
			
			while (true){
				
				if (DBOnlyFlag == true){
					universal_deployer.SEL_RELEASE=universal_deployer.available_releases("1");
				}
				
				loggers.addLog("INFO", "Selected release (DB) " + universal_deployer.SEL_RELEASE );
				
				try {
					PROCEED_CONFIRMATION=universal_deployer.show_release_summary(universal_deployer.SEL_RELEASE.trim(),true);
				} catch (IOException e) {
					con_flag=false;
					loggers.addLogEx("SERV", e.getMessage(), e);
				}
				
				if (PROCEED_CONFIRMATION.equals("-1")){
					continue;
				}else if (PROCEED_CONFIRMATION.toUpperCase().equals("Y")){
					break;
				}else if (PROCEED_CONFIRMATION.toUpperCase().equals("B")){
					continue;
				}else if (PROCEED_CONFIRMATION.toUpperCase().equals("S")){
					
					loggers.addLogAndPrint("INFO", "DB Script applier - Skipped...",false );
					con_flag=false;
					break;
					
				}else{
					System.out.println("Invalid selection");
					continue;
				}		
			}
		}
		
		if (con_flag == true){
			if (!loadDBConfigurations()){
				con_flag=false;
			}
		}
		
		return con_flag;
	}
	
	public boolean checkDBConnectivity() {

		boolean conn_flag=true;
		
		loggers.addLogAndPrint("INFO", "DB - Checking connectivity ... ",false);
		
		Iterator<Entry<String, String>> it = RELEASE_AVAIL_DB_SCHEMAS.entrySet().iterator();
	    while (it.hasNext()) {

	        Map.Entry<String,String> entry = (Map.Entry<String,String>) it.next();
	        String user_pass[] = getSchemaUserPass(entry.getKey());
	        
			RunnerParameters parameters = null;
			if ((parameters=DB_OVERRIDE_SCHEMAS.get(user_pass[0])) != null) {
				parameters.setSchemaUser(user_pass[0]);
				parameters.setSchemaPassword(user_pass[1]);
			} else {
				parameters = new RunnerParameters(user_pass[0], user_pass[1], DB_HOST, DB_SID,Integer.toString(DB_PORT), "");
			}
			
			loggers.addLog("INFO","DB - Connectivity : " + parameters.toString());
	        DBConnection db_conn =  new  DBConnection(parameters );	
	        
			if (db_conn.connect()){
				loggers.addLogAndPrint("INFO", "DB - Connectivity for " + user_pass[0] + " Schema - Success",false);
				db_conn.CloseDBConnection();
			}else{
				loggers.addLogAndPrint("INFO", "DB - Connectivity for " + user_pass[0] + " Schema - FAILED",false);
				conn_flag=false;
			}
			
	        it.remove(); 
	    }
		    

		return conn_flag;
	}

	public ArrayList<ParamDBInvalid> checkDisableObjects(){

		loggers.addLogAndPrint("INFO", "DB - Checking disabled constraints and triggers ...",false);

		ArrayList<ParamDBInvalid> retDisabledObjects= new ArrayList<ParamDBInvalid>();
		ResultSet rs=null;
		List<String> tempFil=null;
		List<String> schemaList=new ArrayList<String>();

		if (!GetInfo.getConfig("DB_SCHEMAS").equals("-1")){

			tempFil=Arrays.asList(GetInfo.getConfig("DB_SCHEMAS").split(","));

			if (GetInfo.getConfig("DB_ENABLE_MULTIPLE_INSTANCES").equalsIgnoreCase("TRUE")) {
				if (!DB_OVERRIDE_SCHEMAS.isEmpty()) {
					for (int i = 0; i < tempFil.size(); i++) {
						if (DB_OVERRIDE_SCHEMAS.get(tempFil.get(i)) == null) {
							schemaList.add(tempFil.get(i));
						}
					}
				} else {
					schemaList = tempFil;
				}
			} else {
				schemaList = tempFil;
			}
		}

		String strSQL="SELECT TRIGGER_NAME AS OBJECT_NAME, 'TRIGGER' AS OBJECT_TYPE,TABLE_NAME,STATUS FROM USER_TRIGGERS WHERE STATUS !='ENABLED' UNION ALL " +
					"SELECT CONSTRAINT_NAME AS OBJECT_NAME,'CONSTRAINT' AS OBJECT_TYPE,TABLE_NAME ,STATUS FROM USER_CONSTRAINTS where STATUS !='ENABLED' " +
					"ORDER BY OBJECT_TYPE";

		for (int x=0;x <schemaList.size();x++) {

			String user_pass[] = getSchemaUserPass(schemaList.get(x));

			RunnerParameters parameters = new RunnerParameters(user_pass[0], user_pass[1], DB_HOST, DB_SID, Integer.toString(DB_PORT), "");
			DBConnection db_conn = new DBConnection(parameters);

			try {

				if (db_conn.connect()) {

					rs = db_conn.getData(strSQL);

					if (rs != null) {

						if (!rs.next()) {

							loggers.addLog("INFO", "DB - NO Disabled objects found for : " + schemaList.get(x));

						} else {

							loggers.addLog("INFO", "DB - Disabled objects found : " + schemaList.get(x));

							do {
								ParamDBInvalid pdi = new ParamDBInvalid(schemaList.get(x), rs.getString("OBJECT_TYPE"), rs.getString("OBJECT_NAME"), rs.getString("TABLE_NAME"), null);
								retDisabledObjects.add(pdi);
							} while (rs.next());
						}

					} else {

						loggers.addLogAndPrint("INFO", "DB - Disabled objects check - FAILED : " + schemaList.get(x), false);
						loggers.addLog("INFO", "Disabled object list - null");
					}

				} else {

					loggers.addLogAndPrint("INFO", "DB - Disabled objects check - " + schemaList.get(x) + " schema connection - FAILED", false);
				}

			} catch (SQLException e) {
				loggers.addLogAndPrint("INFO", "DB - Disabled objects check for " + schemaList.get(x) + " Schema - FAILED", false);
			} finally {
				db_conn.CloseDBConnection();
			}
		}

		if (GetInfo.getConfig("DB_ENABLE_MULTIPLE_INSTANCES").equalsIgnoreCase("TRUE")) {

			if (DB_VALID_OVERRIDE_SCHEMA_INDEXES.size()>0) {

				for (int r = 0; r < DB_VALID_OVERRIDE_SCHEMA_INDEXES.size(); r++) {

					List<String> MultischemaList=null;

					if (!GetInfo.getConfig("DB_SCHEMAS_" + DB_VALID_OVERRIDE_SCHEMA_INDEXES.get(r)).equals("-1")) {
						MultischemaList = Arrays.asList(GetInfo.getConfig("DB_SCHEMAS_" + DB_VALID_OVERRIDE_SCHEMA_INDEXES.get(r)).split(","));
					}

					for (int x=0;x <MultischemaList.size();x++) {

						String user_pass[] = getSchemaUserPass(MultischemaList.get(x));

						RunnerParameters parameters = DB_OVERRIDE_SCHEMAS.get(user_pass[0]);
						parameters.setSchemaUser(user_pass[0]);
						parameters.setSchemaPassword(user_pass[1]);

						DBConnection db_conn = new DBConnection(parameters);

						try {

							if (db_conn.connect()) {

								rs = db_conn.getData(strSQL);

								if (rs != null) {

									if (!rs.next()) {

										loggers.addLog("INFO", "MULTI DB - NO Disabled objects found for : " + schemaList.get(x));

									} else {

										loggers.addLog("INFO", "MULTI DB - Disabled objects found : " + schemaList.get(x));

										do {
											ParamDBInvalid pdi = new ParamDBInvalid(MultischemaList.get(x), rs.getString("OBJECT_TYPE"), rs.getString("OBJECT_NAME"), rs.getString("TABLE_NAME"), null);
											retDisabledObjects.add(pdi);
										} while (rs.next());
									}

								} else {

									loggers.addLogAndPrint("INFO", "MULTI DB - Disabled objects check - FAILED : " + schemaList.get(x), false);
									loggers.addLog("INFO", "Disabled object list - null");
								}

							} else {
								loggers.addLogAndPrint("INFO", "MULTI DB - Disabled objects check - " + schemaList.get(x) + " schema connection - FAILED", false);
							}

						} catch (SQLException e) {
							loggers.addLogAndPrint("INFO", "MULTI DB - Disabled objects check for " + schemaList.get(x) + " Schema - FAILED", false);
						} finally {
							db_conn.CloseDBConnection();
						}
					}
				}
			}
		}

		if (retDisabledObjects.size()==0){
			return null;
		}else {
			return retDisabledObjects;
		}

	}

	public ArrayList<ParamDBInvalid> checkInvaldObjects_multidb() {

		loggers.addLogAndPrint("INFO", "DB - Checking invalid objects for multi db configurations ...", false);

		ArrayList<ParamDBInvalid> retInvalidObjects = null;
		ResultSet rs=null;

		if (GetInfo.getConfig("DB_ENABLE_MULTIPLE_INSTANCES").equalsIgnoreCase("TRUE")) {

			retInvalidObjects = new ArrayList<ParamDBInvalid>();

			if (DB_VALID_OVERRIDE_SCHEMA_INDEXES.size()>0) {

				for (int r = 0; r < DB_VALID_OVERRIDE_SCHEMA_INDEXES.size(); r++) {

					List<String> schemaList=null;

					if (!GetInfo.getConfig("DB_SCHEMAS_" + DB_VALID_OVERRIDE_SCHEMA_INDEXES.get(r)).equals("-1")) {
						schemaList = Arrays.asList(GetInfo.getConfig("DB_SCHEMAS_" + DB_VALID_OVERRIDE_SCHEMA_INDEXES.get(r)).split(","));
					}

					String strSQL="select object_type,object_name,last_ddl_time from user_objects " +
							"where status != 'VALID' OR object_name in (SELECT INDEX_NAME FROM USER_INDEXES WHERE STATUS='UNUSABLE') order by object_type";

					for (int x=0;x <schemaList.size();x++) {

						String user_pass[] = getSchemaUserPass(schemaList.get(x));

						RunnerParameters parameters = DB_OVERRIDE_SCHEMAS.get(user_pass[0]);
						parameters.setSchemaUser(user_pass[0]);
						parameters.setSchemaPassword(user_pass[1]);

						DBConnection db_conn = new DBConnection(parameters);

						loggers.addLog("INFO", "DB - Checking invalid objects : " + parameters.toString());

						try {

							if (db_conn.connect()) {

								rs = db_conn.getData(strSQL);

								if (rs != null) {

									if (!rs.next()) {

										loggers.addLog("INFO", "MULTI DB : NO Invalid objects found for : " + schemaList.get(x));

									} else {

										loggers.addLog("INFO", "MULTI DB : Invalid objects found : " + schemaList.get(x));

										do {
											ParamDBInvalid pdi = new ParamDBInvalid(schemaList.get(x), rs.getString("object_type"), rs.getString("object_name"), rs.getDate("last_ddl_time"));
											retInvalidObjects.add(pdi);
										} while (rs.next());
									}

								} else {

									loggers.addLogAndPrint("INFO", "MULTI DB : Invalid objects check - FAILED : " + schemaList.get(x), false);
									loggers.addLog("INFO", "DB - MULTI : Invalid object list - null");
								}

							} else {
								loggers.addLogAndPrint("INFO", "MULTI DB : Invalid objects check - " + schemaList.get(x) + " schema connection - FAILED", false);
							}

						} catch (SQLException e) {
							loggers.addLogAndPrint("INFO", "MULTI DB : Invalid objects check for " + schemaList.get(x) + " Schema - FAILED", false);
						} finally {
							db_conn.CloseDBConnection();
						}
					}
				}
			}
		}

		return retInvalidObjects;
	}

	public ArrayList<ParamDBInvalid> checkInvaldObjects(){

		loggers.addLogAndPrint("INFO", "DB - Checking invalid objects ...",false);

		ArrayList<ParamDBInvalid> retInvalidObjects = new ArrayList<ParamDBInvalid>();;
		ResultSet rs=null;
		List<String> tempFil=null;
		List<String> schemaList=new ArrayList<String>();

		if (!GetInfo.getConfig("DB_SCHEMAS").equals("-1")){

			tempFil=Arrays.asList(GetInfo.getConfig("DB_SCHEMAS").split(","));

			if (GetInfo.getConfig("DB_ENABLE_MULTIPLE_INSTANCES").equalsIgnoreCase("TRUE")) {
				if (!DB_OVERRIDE_SCHEMAS.isEmpty()) {
					for (int i = 0; i < tempFil.size(); i++) {
						if (DB_OVERRIDE_SCHEMAS.get(tempFil.get(i)) == null) {
							schemaList.add(tempFil.get(i));
						}
					}
				} else {
					schemaList = tempFil;
				}
			} else {
				schemaList = tempFil;
			}
		}

		String strSQL="select object_type,object_name,last_ddl_time from user_objects " +
			"where status != 'VALID' OR object_name in (SELECT INDEX_NAME FROM USER_INDEXES WHERE STATUS='UNUSABLE') order by object_type";

		for (int x=0;x <schemaList.size();x++) {

			String user_pass[] = getSchemaUserPass(schemaList.get(x));

			RunnerParameters parameters = new RunnerParameters(user_pass[0], user_pass[1], DB_HOST, DB_SID, Integer.toString(DB_PORT), "");
			DBConnection db_conn = new DBConnection(parameters);

			loggers.addLog("INFO", "DB - Checking invalid objects : " + parameters.toString());

			try {

				if (db_conn.connect()) {

					rs = db_conn.getData(strSQL);

					if (rs != null) {

						if (!rs.next()) {

							loggers.addLog("INFO", "DB - NO Invalid objects found for : " + schemaList.get(x));

						} else {

							loggers.addLog("INFO", "DB - Invalid objects found : " + schemaList.get(x));

							do {
								ParamDBInvalid pdi = new ParamDBInvalid(schemaList.get(x), rs.getString("object_type"), rs.getString("object_name"), rs.getDate("last_ddl_time"));
								retInvalidObjects.add(pdi);
							} while (rs.next());
						}

					} else {

						loggers.addLogAndPrint("INFO", "DB - Invalid objects check - FAILED : " + schemaList.get(x), false);
						loggers.addLog("INFO", "Invalid object list - null");
					}

				} else {

					loggers.addLogAndPrint("INFO", "DB - Invalid objects check - " + schemaList.get(x) + " schema connection - FAILED", false);
				}

			} catch (SQLException e) {
				loggers.addLogAndPrint("INFO", "DB - Invalid objects check for " + schemaList.get(x) + " Schema - FAILED", false);
			} finally {
				db_conn.CloseDBConnection();
			}
		}

		if (GetInfo.getConfig("DB_ENABLE_MULTIPLE_INSTANCES").equalsIgnoreCase("TRUE")) {
			ArrayList<ParamDBInvalid> checkInvald_multidb = checkInvaldObjects_multidb();
			if (checkInvald_multidb != null) {

				if (retInvalidObjects != null) {
					retInvalidObjects.addAll(checkInvald_multidb);
				} else {
					retInvalidObjects = checkInvald_multidb;
				}
			}
		}

		return retInvalidObjects;
	}

	public boolean loadDBConfigurations() 	{
		boolean conf_flag=true;
		
		String DB_SCHEMA_MAP = GetInfo.getConfig("DB_SCHEMA_MAPPER");
		if ( DB_SCHEMA_MAP.equals("-1" ))
		{	
			loggers.addLog("INFO","Configuration DB_SCHEMA_MAPPER not found");
			
		}else{
			
			List<String> SCHEMA_MAP = new ArrayList<String>(Arrays.asList(DB_SCHEMA_MAP.split(",")));
			for (int i=0;i<SCHEMA_MAP.size();i++){
				 String temp[]= SCHEMA_MAP.get(i).split(":");
				 DB_SCHEMA_MAPPER.put(temp[0],temp[1]);
			}
		}
		
		String DB_SCHEMA = GetInfo.getConfig("DB_SCHEMAS");
		if ( DB_SCHEMA.equals("-1" ))
		{	
			loggers.addLogAndPrint("INFO","Configuration DB_SCHEMAS not found",false);
			conf_flag=false;
		}else{
			
			DB_SCHEMAS_LIST = new ArrayList<String>(Arrays.asList(DB_SCHEMA.split(",")));
			
			for (int i=0;i<DB_SCHEMAS_LIST.size();i++){

				String tmpPass = null;
				DB_SCHEMA_USER_PASS.put(DB_SCHEMAS_LIST.get(i),"");

				//Load password from key files
				String tmpKeyFile = GetInfo.getConfig("DB_SCHEMA_PASSWORD_FILE_" + ( i + 1) );

				if (!tmpKeyFile.equals("-1")){
					tmpPass = getPasswordFromKeyFiles(tmpKeyFile);
				}

				if (tmpPass != null){

					DB_SCHEMA_USER_PASS.put(DB_SCHEMAS_LIST.get(i), tmpPass);

				}else {

					tmpPass = GetInfo.getConfig("DB_SCHEMA_PASSWORD_" + (i + 1));

					if (tmpPass.equals("-1")) {

						loggers.addLogAndPrint("INFO", "Configuration DB_SCHEMA_PASSWORD_" + (i + 1) + " not found", false);
						conf_flag = false;
						break;

					} else {

						DB_SCHEMA_USER_PASS.put(DB_SCHEMAS_LIST.get(i), CryptoUtils.DecryptPassword(DB_SCHEMAS_LIST.get(i), tmpPass));
					}
				}
			}
		}

		if (System.getProperty("enable.tns").equals("false")) {

			DB_HOST = GetInfo.getConfig("DB_HOSTNAME");
			if (DB_HOST.equals("-1")) {
				loggers.addLogAndPrint("INFO", "Configuration DB_HOST not found", false);
				conf_flag = false;
			}

			String tmp_db_port = GetInfo.getConfig("DB_PORT");
			if (tmp_db_port.equals("-1")) {
				loggers.addLogAndPrint("INFO", "Configuration DB_PORT not found", false);
				conf_flag = false;
			} else {
				DB_PORT = Integer.parseInt(tmp_db_port);
			}

		} else {

			loggers.addLogAndPrint("INFO", "DB - Connection load from tnsnames.ora - ENABLED", false);

		}

		DB_SID = GetInfo.getConfig("DB_SID");
		if ( DB_SID.equals("-1" ))
		{	
			loggers.addLogAndPrint("INFO","Configuration DB_SID not found",false);
			conf_flag=false;
		}
		
		//Script replacer regex validator 
		if (GetInfo.getConfig("DB_SCRIPT_REPLACE_ENABLE").equalsIgnoreCase("TRUE")){
			
			List<Pare> pares = null;
			
			int rule_counter = 10;
			String rule_count  = GetInfo.getConfig("DB_SCRIPT_REPLACE_RULE_COUNTER");
			if ( !rule_count.equals("-1" ))
			{	
				rule_counter=Integer.parseInt(rule_count);
			}else{
				rule_counter=10;
			}
			
			String tmpMapSch="";
			for (int i=0;i<DB_SCHEMAS_LIST.size();i++){
				
				for( String key : DB_SCHEMA_MAPPER.keySet() ){
					tmpMapSch = DB_SCHEMA_MAPPER.get(key);
					
					if ( tmpMapSch.equals(DB_SCHEMAS_LIST.get(i))){
						tmpMapSch = key;
						break;
					}
				}
			       
				String tmpRule="";
				pares = new ArrayList<Pare>();
				
				for (int t=1 ; t <=rule_counter;t++){
					
					tmpRule = GetInfo.getConfig("DB_SCRIPT_REPLACE_REGEX_" + tmpMapSch + "_" + t);
					if (!tmpRule.equals("-1")){
						String tmpR[] = tmpRule.split(",");
						pares.add( new Pare( tmpR[0],tmpR[1] ));
					}
				}
				
				if (pares.size() != 0){
					SCRIPT_REPLACE_MAP.put(tmpMapSch,pares);
				}
			}
		}
		
		//Enable multiple database instances
		
		if (GetInfo.getConfig("DB_ENABLE_MULTIPLE_INSTANCES").equalsIgnoreCase("TRUE")){
			
			loggers.addLogAndPrint("INFO", "DB - Apply scripts to multiple database instances - ENABLED",false);
			
			DB_ENABLE_MULTIPLE_INSTANCES = true;
			
			int instance_counter = 5;
			String instance_count  = GetInfo.getConfig("DB_MULTIPLE_INSTANCES_COUNT");
			if ( !instance_count.equals("-1" ))
			{	
				instance_counter=Integer.parseInt(instance_count);
			}else{
				instance_counter=5;
			}
			
			String tmpDBSchemas ="";
			for (int u=1 ; u <=instance_counter;u++){
				
				List<String> DB_MULTI_SCHEMAS_LIST = null;
				String strMultiDbSid="",strMultiDbPort="",strMultiDbHostname="";
				RunnerParameters runParm= new RunnerParameters( );
				
				strMultiDbSid = GetInfo.getConfig("DB_SID_" + u);
				if (!strMultiDbSid.equalsIgnoreCase("-1")){
					if ((tmpDBSchemas=GetInfo.getConfig("DB_SCHEMAS_" + u)).equalsIgnoreCase("-1")) {
						loggers.addLogAndPrint("INFO","Configuration DB_SCHEMAS_" + u + " not found",false);
						conf_flag=false;
					}else {
						
						DB_MULTI_SCHEMAS_LIST=new ArrayList<String>(Arrays.asList(tmpDBSchemas.split(",")));
						
						if (System.getProperty("enable.tns").equals("false")) {
							
							if ((strMultiDbHostname=GetInfo.getConfig("DB_HOSTNAME_" + u)).equalsIgnoreCase("-1")) {
								loggers.addLogAndPrint("INFO","Configuration DB_HOSTNAME_" + u + " not found",false);
								conf_flag=false;	
							}
							
							if ((strMultiDbPort=GetInfo.getConfig("DB_PORT_" + u)).equalsIgnoreCase("-1")) {
								loggers.addLogAndPrint("INFO","Configuration DB_PORT_" + u + " not found",false);
								conf_flag=false;	
							}	
						}	
					}
				}
				
				runParm.setHost(strMultiDbHostname);
				runParm.setPort(strMultiDbPort);
				runParm.setSid(strMultiDbSid);
				
				if (DB_MULTI_SCHEMAS_LIST != null) {
					for (int i = 0; i < DB_MULTI_SCHEMAS_LIST.size(); i++) {
						if (DB_OVERRIDE_SCHEMAS.get(DB_MULTI_SCHEMAS_LIST.get(i)) != null) {
							loggers.addLogAndPrint("INFO",
									"Under DB_ENABLE_MULTIPLE_INSTANCES function, same schema has defined multipe times.Please check schema name "
											+ DB_MULTI_SCHEMAS_LIST.get(i),
									false);
							conf_flag = false;
						} else {
							DB_OVERRIDE_SCHEMAS.put(DB_MULTI_SCHEMAS_LIST.get(i), runParm);
							if (!GetInfo.check_duplicates(DB_VALID_OVERRIDE_SCHEMA_INDEXES, u)){
								DB_VALID_OVERRIDE_SCHEMA_INDEXES.add(u);
							}
						}
					}
				}
			}
			
			if (DB_OVERRIDE_SCHEMAS.size()>0 ) {
				
				Iterator<Entry<String, RunnerParameters>> it = DB_OVERRIDE_SCHEMAS.entrySet().iterator();
			    while (it.hasNext()) {

			        Map.Entry<String,RunnerParameters> entry = (Map.Entry<String,RunnerParameters>) it.next();
			        String tmpMSchema = entry.getKey();
			        
			        boolean override_flag = false;
			        for (int u=0; u<DB_SCHEMAS_LIST.size();u++) {
			        	if (tmpMSchema.equals(DB_SCHEMAS_LIST.get(u))) {
			        		override_flag=true;
			        		break;
			        	}
			        }
					
			        if ( override_flag == false) {
						loggers.addLogAndPrint("INFO","DB : Override schemas defined in DB_SCHEMAS_X is not a part of the configuration DB_SCHEMAS",false);
						conf_flag=false;
			        }
			        //it.remove(); 
			    }
			}
			
		}else {
			DB_ENABLE_MULTIPLE_INSTANCES=false;
		}
		
		return conf_flag;
	}

	public static String selectMode() throws IOException	{
		System.out.println("=================================================================================");
		System.out.println("           	   Welcome To Universal Deployer - DB Package Applier");		
		System.out.println("=================================================================================");
		System.out.println("(1) DB - General Release");
		System.out.println("(2) DB - Adhoc Release");
		System.out.println("(3) Back to Main");
		System.out.println("(4) Quit");
		System.out.println("");
		System.out.println("Enter Your Choice [ 1 - 4 ] :");
		
		String select="-1";  
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));		
		select = br.readLine();
		
		return select;
	}

	public boolean scriptValidator(){
		
		loggers.addLogAndPrint("INFO", "DB - Running script validator ... ",false);
		
		boolean status =  true;
		
		outerloop :
			
        for( Integer key : AVAILABLE_DB_SCRIPTS.keySet() )
        {
        	List<String> scripts = AVAILABLE_DB_SCRIPTS.get( key );
        	for( int i=0;i<scripts.size();i++){
        		
        		String tmpScript = removeExtension(scripts.get(i));
        		List<String> break_file_name = new ArrayList<String>(Arrays.asList(tmpScript.split("_")));

        		String ext = FilenameUtils.getExtension(scripts.get(i));
        		if (!ext.equals("sql")){
        			loggers.addLogAndPrint("INFO","DB - Invalid file extension - " + tmpScript ,false);
        			status = false;
    		    	break outerloop;
        		}
        		
    			try { 
    		        Integer.parseInt(break_file_name.get(0)); 
    		    } catch(Exception e) { 
    		    	loggers.addLogAndPrint("INFO","DB - Invalid File Format - " + tmpScript ,false);
    		    	loggers.addLogEx("INFO",e.getMessage(),e);
    		    	status = false;
    		    	break outerloop;
    		    }
    			
    			String tmpSchema = break_file_name.get(1);
    			if (DB_SCHEMA_MAPPER.get(tmpSchema) != null){
    				if (DB_SCHEMA_USER_PASS.get(DB_SCHEMA_MAPPER.get(tmpSchema)) == null){
        		    	loggers.addLogAndPrint("INFO","DB - Mapped Schema not found - " + tmpScript ,false);
        		    	status = false;
        		    	break outerloop;
    				}
	
    			}else{
    				
    				if (DB_SCHEMA_USER_PASS.get(tmpSchema) == null){
        		    	loggers.addLogAndPrint("INFO","DB - Matched schema not found - " + tmpScript ,false);
        		    	status = false;
        		    	break outerloop;
    				}
    			}
    			
    			RELEASE_AVAIL_DB_SCHEMAS.put(tmpSchema, ""); 
        	}
        }

        return status;
	}
	
	public ParamFinalDBReport scriptApplier(){
		
		String[] LogDirs =new String[AVAILABLE_DB_SCRIPTS.size()];
		int pos = 0;
		Map<String, ParamDBReport> TotalScriptErros = new TreeMap<String, ParamDBReport>();
		ParamFinalDBReport returnReport = null;
		
        for( Integer key : AVAILABLE_DB_SCRIPTS.keySet() )
        {
        	String scriptBase= System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + GetInfo.getReleaseSource() + File.separator + GetInfo.getReleaseDirectory(key) + File.separator + "DATABASE";
        	String logBase = GetInfo.getDBLogBackupDirName(GetInfo.getReleaseDirectory(key));
        	List<String> scripts = AVAILABLE_DB_SCRIPTS.get( key );
        	LogDirs[pos] = logBase;
        	
        	
        	for( int i=0;i<scripts.size();i++){
        		
        		String user_pass[]= getUserPass(scripts.get(i));
      		
	        	ScriptRunner runner = new ScriptRunner();
                RunnerParameters parameters = null;

				if ((parameters=DB_OVERRIDE_SCHEMAS.get(user_pass[0])) != null) {
					parameters.setSchemaUser(user_pass[0]);
					parameters.setSchemaPassword(user_pass[1]);
					parameters.setEnvironment("");
				} else {
					parameters = new RunnerParameters(user_pass[0], user_pass[1], DB_HOST, DB_SID,Integer.toString(DB_PORT), "");
				}
	        	
	        	File file = new File( scriptBase + File.separator + scripts.get(i) );
	        	String logPath= logBase + File.separator + removeExtension(scripts.get(i)) + ".log";
	        	parameters.setSCR_REP_MAP(SCRIPT_REPLACE_MAP);
				runner.apply( parameters, file,logPath );
				
				Map<Integer, String> ScriptErros = new TreeMap<Integer, String>(logAnalyser(logPath));
				ParamDBReport pdbr= new ParamDBReport(GetInfo.getReleaseDirectory(key), scripts.get(i), ScriptErros);
				TotalScriptErros.put(key + "_" + i, pdbr);
        	}
        	
        	pos++;
        }
        
        String zipFile = "";

        try{
        	
        	zipFile=Zipper.zipi(LogDirs);
        	returnReport = new ParamFinalDBReport(TotalScriptErros, true,zipFile);   	
        	loggers.addLog("INFO", "DB - Scripts zip package - " + zipFile );
        	
        }catch(Exception e){
        	
        	returnReport = new ParamFinalDBReport(TotalScriptErros, false,"");
        	loggers.addLog("SERV", e.getMessage());
        }
        
        return returnReport;
        
	}
	
	private Map<Integer, String> logAnalyser(String logFileName){
		
		Map<Integer, String> ErrorsFound = new TreeMap<Integer, String>();
		List<String> OraErr = new ArrayList<String>(GetInfo.getOraErrPrefixes());
		int lineNumber=1;
		
		try(BufferedReader br = new BufferedReader(new FileReader(logFileName))) {
		    for(String line; (line = br.readLine()) != null; ) {
		    	for(String keyword : OraErr){
		    	      if(line.contains(keyword)){
		    	    	  ErrorsFound.put(lineNumber, line);
		    	      }
		    	   }
		    	lineNumber++;
		    }

		} catch (FileNotFoundException e) {
			loggers.addLogEx("SERV", "DB - Log file not found to analyze", e);
		} catch (IOException e) {
			loggers.addLogEx("SERV", "DB - Log file analyzer | FAILED", e);
		}
		
		return ErrorsFound;
	}
	
	public static String removeExtension(String s) {

	    String separator = System.getProperty("file.separator");
	    String filename;

	    // Remove the path upto the filename.
	    int lastSeparatorIndex = s.lastIndexOf(separator);
	    if (lastSeparatorIndex == -1) {
	        filename = s;
	    } else {
	        filename = s.substring(lastSeparatorIndex + 1);
	    }

	    // Remove the extension.
	    int extensionIndex = filename.lastIndexOf(".");
	    if (extensionIndex == -1)
	        return filename;

	    return filename.substring(0, extensionIndex);
	}

	private String[] getUserPass(String fileName){
		
		String retLst[] = new String[2] ;
		
		String tmpfileName = removeExtension(fileName);
		List<String> break_file_name = new ArrayList<String>(Arrays.asList(tmpfileName.split("_")));

		if (DB_SCHEMA_MAPPER.get(break_file_name.get(1)) != null){
			if (DB_SCHEMA_USER_PASS.get(DB_SCHEMA_MAPPER.get(break_file_name.get(1))) != null){
				retLst[0] = DB_SCHEMA_MAPPER.get(break_file_name.get(1));
				retLst[1]= DB_SCHEMA_USER_PASS.get(DB_SCHEMA_MAPPER.get(break_file_name.get(1)));
			}

		}else{
			
			if (DB_SCHEMA_USER_PASS.get(break_file_name.get(1)) != null){
				retLst[0] = break_file_name.get(1);
				retLst[1]= DB_SCHEMA_USER_PASS.get(break_file_name.get(1));
			}
		}
		
		return retLst;
	}
	
	private String[] getSchemaUserPass(String schemaref){
		
		String retLst[] = new String[2] ;
		
		if (DB_SCHEMA_MAPPER.get(schemaref) != null){
			if (DB_SCHEMA_USER_PASS.get(DB_SCHEMA_MAPPER.get(schemaref)) != null){
				retLst[0] = DB_SCHEMA_MAPPER.get(schemaref);
				retLst[1]= DB_SCHEMA_USER_PASS.get(DB_SCHEMA_MAPPER.get(schemaref));
			}

		}else{
			
			if (DB_SCHEMA_USER_PASS.get(schemaref) != null){
				retLst[0] = schemaref;
				retLst[1]= DB_SCHEMA_USER_PASS.get(schemaref);
			}
		}
		
		
		return retLst;
	}
}
