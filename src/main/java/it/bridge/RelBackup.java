package it.bridge;

import it.bridge.objects.HostAuthentication;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RelBackup {
	
	HashMap<String , String[]> CACHE_BACKUP_ITEMS  = new HashMap<String , String[]>();
	HashMap<String , String[]> CACHE_BACKUP_EXCLUDE_ITEMS  = new HashMap<String , String[]>();
	
	private String[] CACHE_GET_ITEMS(String CODE, String FLAG) {
		
		String[] retArray = null;
		
		if (FLAG.equals("EXCLUDE_ITEMS")) {
			
			if (CACHE_BACKUP_EXCLUDE_ITEMS.get(CODE) != null) {
				retArray = CACHE_BACKUP_EXCLUDE_ITEMS.get(CODE);
			}else {
				retArray = GetInfo.getItemsList(CODE,"EXCLUDE_ITEMS");
				CACHE_BACKUP_EXCLUDE_ITEMS.put(CODE, retArray);
			}
			
		}else if (FLAG.equals("ITEMS")) {
			
			if (CACHE_BACKUP_ITEMS.get(CODE) != null) {
				retArray = CACHE_BACKUP_ITEMS.get(CODE);
			}else {
				retArray = GetInfo.getItemsList(CODE,"ITEMS");
				CACHE_BACKUP_ITEMS.put(CODE, retArray);
			}
		}

		return retArray;
	}
	
	public boolean backup_artifacts(String BUNDLE_DIR){
		
		boolean IGNORE_RULE_CHECKER=false;
		boolean RULE_FOUND = false;
		boolean STATUS=true;
		String tempBundleDir=System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + "TEMP" + File.separator + BUNDLE_DIR;	
		
		ArrayList<String> SOURCE_DIRS = GetInfo.getDirectoryList(tempBundleDir);
		if (SOURCE_DIRS == null) {
			System.out.println("");
			loggers.addLogAndPrint("INFO","No artifacts available to backup",false);
			return true;
		}
		
		loggers.addLog("INFO","Temporary bundle directory name : " + tempBundleDir);
		
		System.out.println("");
		loggers.addLogAndPrint("INFO","Backup process starting",false);
		
		String BKP_LOCATION;
		if ( System.getProperty("ADHOC_DEPLOYMENT_FLAG").equals("false")){
			BKP_LOCATION="BACKUP" + File.separator;
		}
		else{
			BKP_LOCATION="BACKUP" + File.separator + "ADHOC_BACKUP" + File.separator;
		}
		
		String bkpDir=System.getProperty("BASE_DIR") + File.separator +  "METERIALS" + File.separator + BKP_LOCATION + GetInfo.getBackupDirName();
		
		File theDir = new File(bkpDir);
		
		loggers.addLog("INFO","Backup directory name : " + bkpDir);
		
		if ( !theDir.exists() ){
			theDir.mkdir();	
		}
		
		for (int e=0;e<SOURCE_DIRS.size() ;e++){
			
			ArrayList<String> CAT_PREFIXES_LIST = GetInfo.getCategoryPrefixes(SOURCE_DIRS.get(e), "BACKUP_KRULE_PREFIXES");
			ArrayList<String> RETURN_ARTIFACTS_LIST = GetInfo.getComponentList(SOURCE_DIRS.get(e),tempBundleDir, false);
			
			if (RETURN_ARTIFACTS_LIST != null ){
				
				System.out.println("");
				loggers.addLogAndPrint("INFO","BKP : " + SOURCE_DIRS.get(e) + " - STARTING BACKUP PROCESS ...",false);
				
				for (int i=0;i<RETURN_ARTIFACTS_LIST.size() ;i++){
									
					for (int j=0;j<CAT_PREFIXES_LIST.size();j++){
						
						IGNORE_RULE_CHECKER=false;
						boolean FLAG_EXCLUDE = false;
						
						//Get exclude artifacts list
						ArrayList<String> EXCLUDE_ITEM_LIST= null;
						String[] tempExcludeItemList = CACHE_GET_ITEMS(CAT_PREFIXES_LIST.get(j),"EXCLUDE_ITEMS");
						
						if (tempExcludeItemList != null){
							
							EXCLUDE_ITEM_LIST = new ArrayList<String>(Arrays.asList(tempExcludeItemList) );
							
							if (GetInfo.check_duplicates(EXCLUDE_ITEM_LIST, RETURN_ARTIFACTS_LIST.get(i))){
								FLAG_EXCLUDE=true;
							}
						}
						
						if (FLAG_EXCLUDE==false){
							
							//Get backup server authentication details    			
			    			HostAuthentication hA= GetInfo.getHostAuthentication(CAT_PREFIXES_LIST.get(j), "backup", 0);
			    			
			    			String[] RULE_LEVEL_ITEM_LIST = CACHE_GET_ITEMS(CAT_PREFIXES_LIST.get(j),"ITEMS");
			    			List<String> TEMP_RULE_ITEM_LIST = new ArrayList<String>(Arrays.asList(RULE_LEVEL_ITEM_LIST));
			    			
			    			
							if (TEMP_RULE_ITEM_LIST.size()==1 ){
								if ( TEMP_RULE_ITEM_LIST.get(0).equalsIgnoreCase("ANY") ){
									IGNORE_RULE_CHECKER=true;
								}
							}
							
							if (IGNORE_RULE_CHECKER== false){
								
								for (int k=0;k<TEMP_RULE_ITEM_LIST.size();k++){
									
									if (RETURN_ARTIFACTS_LIST.get(i).equals(TEMP_RULE_ITEM_LIST.get(k))){
										
										RULE_FOUND=true;
										
										boolean cpStatus=GetInfo.downloadFiles(RETURN_ARTIFACTS_LIST.get(i),GetInfo.getPath(CAT_PREFIXES_LIST.get(j) + "_PATH",SOURCE_DIRS.get(e)),bkpDir + File.separator + SOURCE_DIRS.get(e),hA);
										
										if (cpStatus == false){
											
											loggers.addLogPrintErrorReport("SEVE","BKP : " + SOURCE_DIRS.get(e) + " - ", RETURN_ARTIFACTS_LIST.get(i) + " | FAILED",false,"BKP-" + SOURCE_DIRS.get(e),"BKP-SEVERE");
											STATUS=false;
											
										}else{
											
											loggers.addLogAndPrint("INFO","BKP : " + SOURCE_DIRS.get(e) + " - " + RETURN_ARTIFACTS_LIST.get(i) + " | SUCCESS | " + GetInfo.getConfig(CAT_PREFIXES_LIST.get(j) + "_HOSTNAME"),false );
										}							
											
									}
								}
								
							}else{
								
								boolean cpStatus=GetInfo.downloadFiles(RETURN_ARTIFACTS_LIST.get(i), GetInfo.getPath(CAT_PREFIXES_LIST.get(j) + "_PATH",SOURCE_DIRS.get(e)),bkpDir + File.separator + SOURCE_DIRS.get(e),hA);
								
								if (cpStatus == false){
									
									loggers.addLogPrintErrorReport("SEVE","BKP : " + SOURCE_DIRS.get(e) + " - ", RETURN_ARTIFACTS_LIST.get(i) + " | FAILED",false,"BKP-" + SOURCE_DIRS.get(e),"BKP-SEVERE");
									STATUS=false;
									
								}else{
									
									loggers.addLogAndPrint("INFO","BKP : " + SOURCE_DIRS.get(e) + " - " + RETURN_ARTIFACTS_LIST.get(i) + " | SUCCESS | " + GetInfo.getConfig(CAT_PREFIXES_LIST.get(j) + "_HOSTNAME"),false);
								}
								
							}
							
						}else {
							
							loggers.addLogAndPrint("WARN","BKP : " + SOURCE_DIRS.get(e) + " - " + RETURN_ARTIFACTS_LIST.get(i) + " | STOPPED | " + GetInfo.getConfig(CAT_PREFIXES_LIST.get(j) + "_HOSTNAME"),false);
							RULE_FOUND=true;
							
						}//End of : check_duplicates - Exclude Rules
						
					} //End of for : CAT_PREFIXES_LIST
				
					if (RULE_FOUND == false && IGNORE_RULE_CHECKER == false ){
						
						loggers.addLogPrintErrorReport("WARN","BKP : " + SOURCE_DIRS.get(e) + " - ", RETURN_ARTIFACTS_LIST.get(i) + " | FAILED (Backup Rule Not Found)",false,"BKP-" + SOURCE_DIRS.get(e),"BKP-WARNING");

					}else{
						
						RULE_FOUND=false;
					}
					
				} //End of for : RETURN_ARTIFACTS_LIST
				
				loggers.addLogAndPrint("INFO","BKP : " + SOURCE_DIRS.get(e) + " - END OF BACKUP PROCESS ...",false);
				
				
				//Backup agent.xml
				if (GetInfo.getConfig("TBX_AGENT_XML_CATEGORY").equals(SOURCE_DIRS.get(e))){
					
					System.out.println("");
					loggers.addLogAndPrint("INFO","BKP : AGENT.XML - STARTING BACKUP PROCESS ...",false);
					
					//Get agent.xml backup server authentication details    			
	    			HostAuthentication hA_agentxml= GetInfo.getHostAuthentication("", "agentxml", 0);
	    			
	    			boolean cpStatus=GetInfo.downloadFiles("agent.xml",GetInfo.getPath("TBX_AGENT_XML_PATH",SOURCE_DIRS.get(e)),bkpDir + File.separator + SOURCE_DIRS.get(e),hA_agentxml);
					
	    			if (cpStatus == false){
	    				
	    				loggers.addLogPrintErrorReport("WARN","BKP : ", "Agent.xml | FAILED",false,"BKP-AGT" ,"BKP-AGT-WARNING");
	    				STATUS=false;
							
					}else{
						
						loggers.addLogAndPrint("INFO","BKP : Agent.xml | SUCCESS",false );
						
					}
					
	    			loggers.addLogAndPrint("INFO","BKP : AGENT.XML - END OF BACKUP PROCESS ...",false);

				}//End : Backup agent.xml
				
			}//End of if : RETURN_ARTIFACTS_LIST != null
			
		}//End of for : SOURCE_DIRS
		
		return STATUS;
	}

	public boolean backup_artifacts_validator(String BUNDLE_DIR){
		
		boolean IGNORE_RULE_CHECKER=false;
		boolean RULE_FOUND = false;
		boolean STATUS=true;
		String tempBundleDir=System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + "TEMP" + File.separator + BUNDLE_DIR;	
		
		ArrayList<String> SOURCE_DIRS = GetInfo.getDirectoryList(tempBundleDir);
		if (SOURCE_DIRS == null) {
			System.out.println("");
			loggers.addLogAndPrint("INFO","BKP-VAL : No artifacts available to validate",false);
			return true;
		}
		
		String BKP_LOCATION;
		if ( System.getProperty("ADHOC_DEPLOYMENT_FLAG").equals("false")){
			BKP_LOCATION="BACKUP" + File.separator;
		}
		else{
			BKP_LOCATION="BACKUP" + File.separator + "ADHOC_BACKUP" + File.separator;
		}
		
		String bkpDir=System.getProperty("BASE_DIR") + File.separator +  "METERIALS" + File.separator + BKP_LOCATION + GetInfo.getBackupDirName();
		
		File theDir = new File(bkpDir);		
		if ( !theDir.exists() ){
			theDir.mkdir();	
		}
		
		for (int e=0;e<SOURCE_DIRS.size() ;e++){
			
			ArrayList<String> CAT_PREFIXES_LIST = GetInfo.getCategoryPrefixes(SOURCE_DIRS.get(e), "BACKUP_KRULE_PREFIXES");
			ArrayList<String> RETURN_ARTIFACTS_LIST = GetInfo.getComponentList(SOURCE_DIRS.get(e),tempBundleDir, false);
			
			if (RETURN_ARTIFACTS_LIST != null ){
						
				for (int i=0;i<RETURN_ARTIFACTS_LIST.size() ;i++){
									
					for (int j=0;j<CAT_PREFIXES_LIST.size();j++){
						
						IGNORE_RULE_CHECKER=false;
						boolean FLAG_EXCLUDE = false;
						
						//Get exclude artifacts list
						ArrayList<String> EXCLUDE_ITEM_LIST= null;
						String[] tempExcludeItemList = CACHE_GET_ITEMS(CAT_PREFIXES_LIST.get(j),"EXCLUDE_ITEMS");
						
						if (tempExcludeItemList != null){
	
							EXCLUDE_ITEM_LIST = new ArrayList<String>(Arrays.asList(tempExcludeItemList) );
							
							if (GetInfo.check_duplicates(EXCLUDE_ITEM_LIST, RETURN_ARTIFACTS_LIST.get(i))){
								FLAG_EXCLUDE=true;
							}
						}
						
						if (FLAG_EXCLUDE==false){
										    			
			    			String[] RULE_LEVEL_ITEM_LIST = CACHE_GET_ITEMS(CAT_PREFIXES_LIST.get(j),"ITEMS");
			    			List<String> TEMP_RULE_ITEM_LIST = new ArrayList<String>(Arrays.asList(RULE_LEVEL_ITEM_LIST));
			    			
			    			
							if (TEMP_RULE_ITEM_LIST.size()==1 ){
								if ( TEMP_RULE_ITEM_LIST.get(0).equalsIgnoreCase("ANY") ){
									IGNORE_RULE_CHECKER=true;
								}
							}
							
							if (IGNORE_RULE_CHECKER== false){
								
								for (int k=0;k<TEMP_RULE_ITEM_LIST.size();k++){
									
									if (RETURN_ARTIFACTS_LIST.get(i).equals(TEMP_RULE_ITEM_LIST.get(k))){
										RULE_FOUND=true;								
									}
								}
								
							}else{
								//IGNORE_RULE_CHECKER=true;
							}
							
						}else {
							
							RULE_FOUND=true;
							
						}//End of : check_duplicates - Exclude Rules
						
					} //End of for : CAT_PREFIXES_LIST
				
					if (RULE_FOUND == false && IGNORE_RULE_CHECKER == false ){
						
						loggers.addLogAndPrint("WARN","BKP-VAL : " + SOURCE_DIRS.get(e) + " - " + RETURN_ARTIFACTS_LIST.get(i) + " | FAILED (Backup Rule Not Found)",false);
						STATUS=false;
						
					}else{
						
						RULE_FOUND=false;
					}
					
				} //End of for : RETURN_ARTIFACTS_LIST
				
			}//End of if : RETURN_ARTIFACTS_LIST != null
			
		}//End of for : SOURCE_DIRS
		
		return STATUS;
	}
}
