package it.bridge;

import it.bridge.objects.ParamDBInvalid;
import it.bridge.objects.ParamDBReport;
import it.bridge.objects.ParamDepErrors;
import it.bridge.objects.ParamFinalDBReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class ShowInfo {
	
	public void dispaly_release_content(String SELECTED_REL ) throws IOException{
			
		String PARENT_DIR = "" ;
		PARENT_DIR=System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + GetInfo.getReleaseSource() + File.separator + SELECTED_REL;

		File f = new File(PARENT_DIR ); 
        File[] files = f.listFiles();
        
        for( int i=0;i<files.length;i++){
        	File file = files[i];
        	if (file.isDirectory()){
        		System.out.printf("%-10s %1s %1s",file.getName() , ":","" );
        		File Childf = new File(PARENT_DIR + File.separator + file.getName() ); 
        		File[] ChildFiles = Childf.listFiles();
        		for( int h=0;h<ChildFiles.length;h++){
        			File fileChild = ChildFiles[h];
        			if (!fileChild.isDirectory()){
        				System.out.printf(fileChild.getName());
        				if (h != ChildFiles.length-1){
        					System.out.printf(",");
        				}
        					
        			}
        		}
        		System.out.printf("\n");
        	}
        }
	}
	
	
	public void dispaly_release_content_db(String SELECTED_REL , int DIR_CODE) throws IOException{
		
		ArrayList<String> tmpScrptList = null;
		String PARENT_DIR = "" ;
		PARENT_DIR=System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + GetInfo.getReleaseSource() + File.separator + SELECTED_REL + File.separator + "DATABASE";
		String log_script_list="";
		
		File f = new File(PARENT_DIR ); 
        File[] files = f.listFiles();
        
        
        if ( files != null ){
        	
        	tmpScrptList= new ArrayList<String>();
        	System.out.printf("%-10s %1s %1s","DATABASE" , ":","" );
	        
        	Arrays.sort(files);
        	
        	for( int i=0;i<files.length;i++){
	        	
	        	File file = files[i];
	        	System.out.printf(file.getName());
	        	tmpScrptList.add(file.getName());
	        	log_script_list = log_script_list + file.getName();
	        	
	        	if (i != files.length-1){
					System.out.printf(",");
					log_script_list = log_script_list + ",";
				}
	        }
        	
        	loggers.addLog("INFO",SELECTED_REL + " Scripts - " + log_script_list);
        	
        	DBExecute.AVAILABLE_DB_SCRIPTS.put(DIR_CODE, tmpScrptList);
        	System.out.printf("\n");
        	
        }else{
        	
        	loggers.addLog("INFO",SELECTED_REL + " - No database scripts found");
        	System.out.println("No database scripts found");
        }
	}
	
	public boolean availability_release_content_db(String SELECTED_REL , int DIR_CODE) throws IOException{
		
		boolean DB_SCRIPTS_AVAILABLE=false;
		
		ArrayList<String> tmpScrptList = null;
		String PARENT_DIR = "" ;
		PARENT_DIR=System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + GetInfo.getReleaseSource() + File.separator + SELECTED_REL + File.separator + "DATABASE";
		String log_script_list="";
		
		File f = new File(PARENT_DIR ); 
        File[] files = f.listFiles();
        
        
        if ( files != null ){
        	
        	tmpScrptList= new ArrayList<String>();
	        
        	Arrays.sort(files);
        	
        	for( int i=0;i<files.length;i++){
	        	
	        	File file = files[i];
	        	System.out.printf(file.getName());
	        	tmpScrptList.add(file.getName());
	        	log_script_list = log_script_list + file.getName();
	        	
	        	if (i != files.length-1){
					System.out.printf(",");
					log_script_list = log_script_list + ",";
				}
	        }
        	
        	
        }else{
        	
        	DB_SCRIPTS_AVAILABLE=false;
        }
        
        return DB_SCRIPTS_AVAILABLE;
	}
	
	public static boolean cleaningTempDir(File directory){
			
		if (directory == null)
			return false;
		if (!directory.exists())
			return true;
		if (!directory.isDirectory())
		    return false;

		String[] list = directory.list();

		if (list != null) {
			 for (int i = 0; i < list.length; i++) {
			      File entry = new File(directory, list[i]);

			      if (entry.isDirectory())
			      {
			        if (!cleaningTempDir(entry))
			          return false;
			      }
			      else
			      {
			        if (!entry.delete())
			          return false;
			      }
			    }
		}

		return directory.delete();
	}
	
	
	public static boolean cleaningTempFile(File file){
		
		if (file == null)
			return false;
		if (!file.exists())
			return true;
		if (file.isDirectory())
		    return false;

		try{
			file.delete();
			return true;
		}catch(Exception e){
			loggers.addLogAndPrint("SERV", e.getMessage(), false);
			return false;
		}
		
	}
	
	
	public static void DispatchNotification(String Status, boolean db_flag , ParamFinalDBReport dbReport){
		
		if (GetInfo.getConfig("send_mail").equals("true"))
		{
			System.out.println("");
			loggers.addLogAndPrint("INFO","Sending notification mail ...",false);
			
			String tmpZip="";
			SendMail sm = new SendMail();
			
			if (dbReport != null){
				tmpZip=System.getProperty("BASE_DIR") +  File.separator + "METERIALS" + File.separator + "TEMP" + File.separator + dbReport.getZipFile();
				if (dbReport.getZipFile() != "" && dbReport.getZipFile() != null ){
					sm.SendM(loggers.GetLogName(), Status,tmpZip,generateMailBody(dbReport));
				}else{
					sm.SendM(loggers.GetLogName(), Status,null,generateMailBody(dbReport));
				}
			}else{
				sm.SendM(loggers.GetLogName(), Status,null,null);
			}
			

		}
	}
	
	private static StringBuilder generateMailBody(ParamFinalDBReport dbReport){
		
		ParamFinalDBReport dbRep =  dbReport;
		Map<String, ParamDBReport> dbErr= new TreeMap<String, ParamDBReport>();
		dbErr = dbRep.getErrorRep();
		
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
	    sb.append("<head>");
	    sb.append("<style type=\"text/css\">");
	    sb.append("body.format {font-family: verdana,arial,sans-serif;	font-size:14px;	}");
	    sb.append("table.gridtable {font-family: verdana,arial,sans-serif;font-size:11px; color:#333333;border-width: 1px;border-color: #666666;border-collapse: collapse;}");
	    sb.append("table.gridtable th {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #dedede;}");
	    sb.append("table.gridtable td {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;}");
	    sb.append("tr.red {background-color: #FF0066;}");
	    sb.append("tr.amber {background-color: #FF9900;}");
    	sb.append("tr.yellow {background-color: #FFFF66;}");
		sb.append("td.red {background-color: #FF0066;}");
		sb.append("	td.amber {background-color: #FF9900;}");
		sb.append("td.yellow {background-color: #FFFF66;}");
		sb.append("td.green {background-color: #009900;}");
	    sb.append("</style>");
	    sb.append("<title>Database Script Applier Report</title>");
	    sb.append("</head>");
	    sb.append("<body class=\"format\">");
	    
	    HashMap<String, String> HOLD_VERSIONS = new HashMap<String, String>();
	    HOLD_VERSIONS= dbRep.getHOLD_VERSIONS();
	    
	    //Release Summary
	    sb.append("<h3>CODEGEN RELEASE - VERSION SUMMARY</h3>");

	    if (System.getProperty("LOGGED_USER")!= null && System.getProperty("LOGGED_USER")!="") {
			sb.append("<h5>Deployer Executed By : " + System.getProperty("LOGGED_USER").toUpperCase() + " </h5>");
		}

	    sb.append("<table class=\"gridtable\"><tr><th></th><th>Application Version</th><th>Date</th><th>Database Version</th><th>Date</th></tr>");
	    sb.append("<tr><td>Before Release</td><td>" + HOLD_VERSIONS.get("REL_VER") + "</td><td>" + HOLD_VERSIONS.get("REL_LU") + "</td><td>" + HOLD_VERSIONS.get("DB_VER") + "</td><td>" + HOLD_VERSIONS.get("DB_LU") + "</td></tr>");
	    
	    String tmpVer = HOLD_VERSIONS.get("ADHOC");
	    if (tmpVer == null){tmpVer = "FALSE";}
	    
	    String tmpDBFlg= HOLD_VERSIONS.get("DB_ONLY_FLAG");
	    if (tmpDBFlg == null){tmpDBFlg = "FALSE";}
	    
	    if (!tmpVer.equals("TRUE")){
	    	if (tmpDBFlg.equals("TRUE"))
	    	{
	    		sb.append("<tr><td>After Release</td><td>" + HOLD_VERSIONS.get("REL_VER") + "</td><td>" + HOLD_VERSIONS.get("REL_LU") + "</td><td class=\"yellow\">" + HOLD_VERSIONS.get("DB_VER_NEW") + "</td><td class=\"yellow\">" + HOLD_VERSIONS.get("DB_LU_NEW") + "</td></tr>");
	    	}else{
	    		sb.append("<tr><td>After Release</td><td>" + HOLD_VERSIONS.get("REL_VER_NEW") + "</td><td>" + HOLD_VERSIONS.get("REL_LU_NEW") + "</td><td>" + HOLD_VERSIONS.get("DB_VER_NEW") + "</td><td>" + HOLD_VERSIONS.get("DB_LU_NEW") + "</td></tr>");
	    	}
	    }else{
		    sb.append("<tr><td>After Release</td><td>" + HOLD_VERSIONS.get("REL_VER") + "</td><td>" + HOLD_VERSIONS.get("REL_LU") + "</td><td>" + HOLD_VERSIONS.get("DB_VER") + "</td><td>" + HOLD_VERSIONS.get("DB_LU") + "</td></tr>");
		    
	    	if (tmpDBFlg.equals("TRUE"))
	    	{	
	    		if (HOLD_VERSIONS.get("DB_ONLY_FLAG").equals("TRUE")){
	    			sb.append("<tr><td>ADHOC Release</td><td>-</td><td>-</td><td class=\"yellow\">" + HOLD_VERSIONS.get("SEL_RELEASE") + "</td><td class=\"yellow\">" + HOLD_VERSIONS.get("SEL_RELEASE_DT") + "</td></tr>");
	    		}
	    	}else{
	    		if (HOLD_VERSIONS.get("DB_SUCCESS_FLAG").equals("TRUE")){
	    			sb.append("<tr><td>ADHOC Release</td><td>" + HOLD_VERSIONS.get("SEL_RELEASE") + "</td><td>" + HOLD_VERSIONS.get("SEL_RELEASE_DT")  +"</td><td>" + HOLD_VERSIONS.get("SEL_RELEASE") + "</td><td>" + HOLD_VERSIONS.get("SEL_RELEASE_DT") + "</td></tr>");
	    		}else{
	    			sb.append("<tr><td>ADHOC Release</td><td class=\"yellow\">" + HOLD_VERSIONS.get("SEL_RELEASE") + "</td><td class=\"yellow\">" + HOLD_VERSIONS.get("SEL_RELEASE_DT")  +"</td><td>-</td><td>-</td></tr>");
	    		}
	    	}
	    }
	    
	    sb.append("</table>");
	    
	    //Backup and Deployment Errors
	    boolean bkpHeader =false,depHeader =false;
	    
	    if ( universal_deployer.ARR_ALL_DEP_ERRORS.size()>0){
	    	
	    	ArrayList<ParamDepErrors> DEP_ERR_LST = universal_deployer.ARR_ALL_DEP_ERRORS;
	    	
	    	sb.append("<h3>CODEGEN RELEASE - DEPLOYMENT ERRORS</h3>");
	    	
	    	//Backup Errors
	    	for (int z=0; z<DEP_ERR_LST.size();z++){
	    		
	    		ParamDepErrors pde = DEP_ERR_LST.get(z);

	    		if (pde.getERR_CODE().toUpperCase().contains("BKP-")) {
	    			
	    			if (bkpHeader == false) {
	    		    	sb.append("<h5><font color=\"red\">Backup error(s) found, Please refer the attached logs</font></h5>");
	    		    	sb.append("<table class=\"gridtable\"><tr><th>Error Code</th><th>Description</th></tr>");
	    		    	bkpHeader=true;
	    			}
	    			
	    			sb.append("<tr><td>" + pde.getERR_CODE() + "</td><td>" + pde.getERR_DESC() + "</td></tr>");
	    		}
	    	}
	    	
	    	if (bkpHeader==true) {
	    		sb.append("</table>");
	    	}
	    	
	    	sb.append("</br>");
	    	//Deployment Errors
	    	for (int z=0; z<DEP_ERR_LST.size();z++){
	    		
	    		ParamDepErrors pde = DEP_ERR_LST.get(z);
	    		
	    		if (pde.getERR_CODE().toUpperCase().contains("DEP-")) {
	    			
	    			if (depHeader == false) {
	    		    	sb.append("<h5><font color=\"red\">Deployment error(s) found, Please refer the attached logs</font></h5>");
	    		    	sb.append("<table class=\"gridtable\"><tr><th>Error Code</th><th>Description</th></tr>");
	    		    	depHeader=true;
	    			}
	    			
	    			sb.append("<tr><td>" + pde.getERR_CODE() + "</td><td>" + pde.getERR_DESC() + "</td></tr>");
	    		}	
	    	}
	    	
	    	if (depHeader==true) {
	    		sb.append("</table>");
	    	}
	    }
	    
	    
	    //Database script applier report
	    if (dbErr != null){
	    	
		    if ( dbErr.size() >0 ){
		    	sb.append("<h3>CODEGEN RELEASE - DATABASE SCRIPT APPLIER REPORT</h3>");
		    }
		    
			for( String key : dbErr.keySet() )
		    {
				ParamDBReport scripts = dbErr.get( key );
				
				String relref= scripts.getReleaseRef();
				String scriptName = scripts.getScriptName();
				Map<Integer, String> scriptErros = new TreeMap<Integer, String>(scripts.getErrors());
					
				if (scriptErros.size() >0){
					
					sb.append("<h5>Release Ref : " +  relref + "  |  Script Name : " + scriptName + "  |  Error Count : " + scriptErros.size() + "</h5>");
					loggers.addLog("INFO", "DB Scripts | Release Ref:" +  relref + " | Script:" + scriptName + " | Errors:" + scriptErros.size() );
					
					sb.append("<table class=\"gridtable\"><tr><th>LINE NUMBER</th><th>ERROR INFORMATION</th></tr>");
					
						for( Integer keyLine : scriptErros.keySet() )
					    {
							 String row = scriptErros.get( keyLine );
							 sb.append("<tr><td>" + keyLine + "</td><td>" + row + "</td></tr>");
					    }
						
					sb.append("</table>");
				}
				
		    }
	    }
	    
		ArrayList<ParamDBInvalid> before=null;
		ArrayList<ParamDBInvalid> after=null;
		ArrayList<ParamDBInvalid> disabled_obj_after_rel=null;
		
		//Invalid objects before release
		if (dbReport.getInvalidBefore()!=null){
			before = dbReport.getInvalidBefore();
			sb.append("<h5>Database Invalid object(s) before release</h5>");
			sb.append("<table class=\"gridtable\"><tr><th>Schema</th><th>Object Type</th><th>Object Name</th><th>Invalid Since</th></tr>");
			
			for (int i=0; i<before.size();i++){
				ParamDBInvalid dbi = before.get(i); 
				sb.append("<tr><td>" + dbi.getOwner() + "</td><td>" + dbi.getObjectType() + "</td><td>" + dbi.getObjectName() + "</td><td>" + dbi.getLast_ddl_time() + "</td></tr>");
			}
			sb.append("</table>");
		}
		
		//Invalid objects after release
		if (dbReport.getInvalidAfter()!=null){
			after = dbReport.getInvalidAfter();
			
			sb.append("<h5>Database Invalid object(s) after release</h5>");
			sb.append("<table class=\"gridtable\"><tr><th>Schema</th><th>Object Type</th><th>Object Name</th><th>Invalid Since</th></tr>");
			
			for (int i=0; i<after.size();i++){
				ParamDBInvalid dbi = after.get(i); 
				sb.append("<tr><td>" + dbi.getOwner() + "</td><td>" + dbi.getObjectType() + "</td><td>" + dbi.getObjectName() + "</td><td>" + dbi.getLast_ddl_time() + "</td></tr>");
			}
			sb.append("</table>");
		}
		
		//Disabled objects after release
		if (dbReport.getRs_disabled_obj_after_rel()!=null){
			disabled_obj_after_rel = dbReport.getRs_disabled_obj_after_rel(); 
			
			sb.append("<h5>Database Disabled Constraint(s) and Trigger(s) after release</h5>");
			sb.append("<table class=\"gridtable\"><tr><th>Schema</th><th>Object Type</th><th>Object Name</th><th>Table Name</th></tr>");
			
			for (int i=0; i<disabled_obj_after_rel.size();i++){
				ParamDBInvalid dbi = disabled_obj_after_rel.get(i); 
				sb.append("<tr><td>" + dbi.getOwner() + "</td><td>" + dbi.getObjectType() + "</td><td>" + dbi.getObjectName() + "</td><td>" + dbi.getTable_name() + "</td></tr>");	
			}
			
			sb.append("</table>");
			
		}
		
		
		sb.append("<p style=\"font-size:9px\"> <br><br><br><br>The Deployer - Version " + "1.0.2" + "<br>By: Team ARGUS, 2021 </p>");
		sb.append("</body>");
		sb.append("</html>");

		try{
			WriteReportToDisk(sb);
		}catch(Exception e){
			loggers.addLogAndPrint("SERV", "Error in report saving to disk", false);
			loggers.addLogEx("SERV",e.getMessage(), e);
		}

		return sb;
		
	}

	private static void WriteReportToDisk(StringBuilder sb) {

		String tmpDir=System.getProperty("BASE_DIR") +  File.separator + "METERIALS" + File.separator + "LOGS" + File.separator + "REPORTS";
		File theDir = new File(tmpDir);

		if (!theDir.exists()) {

			try{
				theDir.mkdir();
			} catch(SecurityException se){
				loggers.addLogEx("SERV", "WriteReportToDisk : Error in creating REPORTS directory : " + se.getMessage(), se);
			}
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
		Date date = new Date();
		String LogFileName  = tmpDir +  File.separator +  dateFormat.format(date) + "_" + System.getProperty("hostname") + "_" + System.getProperty("selecEnv") + ".html";

		try {

			FileOutputStream outputStream = new FileOutputStream(LogFileName);
			byte[] strToBytes = sb.toString().getBytes();
			outputStream.write(strToBytes);
			outputStream.close();
			loggers.addLogAndPrint("INFO", "Final Report Successfully Saved to Disk : " + LogFileName , false);

		} catch(Exception se){
			loggers.addLogAndPrint("SERV", "Error in report saving to disk", false);
			loggers.addLogEx("SERV", "WriteReportToDisk : Error in final report saving to disk" + se.getMessage(), se);
		}

	}

	
			
}
