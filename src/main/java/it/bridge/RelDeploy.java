package it.bridge;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import it.bridge.objects.HostAuthentication;
import org.apache.commons.io.FilenameUtils;

public class RelDeploy {
	
	private static String TEMP_BUNDLE_DIR;
	public static HashMap<String, HostAuthentication> HM_RELESE_CHECK = new HashMap<String, HostAuthentication>();
	HashMap<String , String[]> CACHE_DEPLOY_ITEMS  = new HashMap<String , String[]>();
	HashMap<String , String[]> CACHE_DEPLOY_EXCLUDE_ITEMS  = new HashMap<String , String[]>();
	
	private String[] CACHE_GET_ITEMS(String CODE, String FLAG) {
		
		String[] retArray = null;
		
		if (FLAG.equals("EXCLUDE_ITEMS")) {
			
			if (CACHE_DEPLOY_EXCLUDE_ITEMS.get(CODE) != null) {
				retArray = CACHE_DEPLOY_EXCLUDE_ITEMS.get(CODE);
			}else {
				retArray = GetInfo.getItemsList(CODE,"EXCLUDE_ITEMS");
				CACHE_DEPLOY_EXCLUDE_ITEMS.put(CODE, retArray);
			}
			
		}else if (FLAG.equals("ITEMS")) {
			
			if (CACHE_DEPLOY_ITEMS.get(CODE) != null) {
				retArray = CACHE_DEPLOY_ITEMS.get(CODE);
			}else {
				retArray = GetInfo.getItemsList(CODE,"ITEMS");
				CACHE_DEPLOY_ITEMS.put(CODE, retArray);
			}
		}

		return retArray;
	}

	public boolean deploy_artifacts(String BUNDLE_DIR){
		
		boolean RULE_FOUND = false;
		boolean STATUS=true;
		boolean IGNORE_RULE_CHECKER=false;
		
		String tempBundleDir=System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + "TEMP" + File.separator + BUNDLE_DIR;	
		TEMP_BUNDLE_DIR=tempBundleDir;
		
		ArrayList<String> SOURCE_DIRS = GetInfo.getDirectoryList(tempBundleDir);
		if (SOURCE_DIRS == null) {
			System.out.println("");
			loggers.addLogAndPrint("INFO","No artifacts available to deploy",false);
			return true;
		}
		
		loggers.addLog("INFO","Temporary bundle directory name : " + tempBundleDir);
		
		System.out.println("");
		loggers.addLogAndPrint("INFO","Deployment process starting",false);
		
		for (int e=0;e<SOURCE_DIRS.size() ;e++){
			
			ArrayList<String> CAT_PREFIXES_LIST = GetInfo.getCategoryPrefixes(SOURCE_DIRS.get(e), "DEPLOY_KRULE_PREFIXES");
			ArrayList<String> RETURN_ARTIFACTS_LIST = GetInfo.getComponentList(SOURCE_DIRS.get(e),tempBundleDir,false);
			
			if (RETURN_ARTIFACTS_LIST != null ){
				
				System.out.println("");
				loggers.addLogAndPrint("INFO","DEP : " + SOURCE_DIRS.get(e) + " - STARTING DEPLOYMENT PROCESS ...",false);
				
				for (int i=0;i<RETURN_ARTIFACTS_LIST.size() ;i++){
					
					for (int j=0;j<CAT_PREFIXES_LIST.size();j++){
						
						IGNORE_RULE_CHECKER=false;
						boolean FLAG_EXCLUDE = false;
						
						//Get exclude artifacts list
						ArrayList<String> EXCLUDE_ITEM_LIST= null;
						String[] tempExcludeItemList = CACHE_GET_ITEMS(CAT_PREFIXES_LIST.get(j),"EXCLUDE_ITEMS");
						
						if (tempExcludeItemList!=null){
							
							EXCLUDE_ITEM_LIST = new ArrayList<String>(Arrays.asList(tempExcludeItemList) );
							
							if (GetInfo.check_duplicates(EXCLUDE_ITEM_LIST, RETURN_ARTIFACTS_LIST.get(i))){
								FLAG_EXCLUDE=true;
							}
						}
						
						if (FLAG_EXCLUDE==false){
							
							//Get deployable artifacts list
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
										
										boolean bStatus=deploy_uploader(CAT_PREFIXES_LIST.get(j), RETURN_ARTIFACTS_LIST.get(i),SOURCE_DIRS.get(e));
										if ( bStatus == false ){STATUS=false;}
									}
								}
								
							}else{
								
								boolean bStatus=deploy_uploader(CAT_PREFIXES_LIST.get(j), RETURN_ARTIFACTS_LIST.get(i),SOURCE_DIRS.get(e));
								if ( bStatus == false ){STATUS=false;}
								
								RULE_FOUND=true;
							}
														
						}else{
							
							loggers.addLogAndPrint("WARN","DEP : " + SOURCE_DIRS.get(e) + " - " + RETURN_ARTIFACTS_LIST.get(i) + " | STOPPED | " + GetInfo.getConfig(CAT_PREFIXES_LIST.get(j) + "_HOSTNAMES"),false);
							RULE_FOUND=true;
	
						}//End of : check_duplicates - Exclude Rules
						
						
					}//End of for : CAT_PREFIXES_LIST
					
					if (RULE_FOUND == false && IGNORE_RULE_CHECKER == false){
						
						loggers.addLogPrintErrorReport("WARN","DEP : " + SOURCE_DIRS.get(e) + " - ", RETURN_ARTIFACTS_LIST.get(i) + " | FAILED (Deployment Rule Not Found)",false,"DEP-" + SOURCE_DIRS.get(e),"DEP-WARNING");
					
					}
					
					RULE_FOUND=false;
					IGNORE_RULE_CHECKER=false;
					
				}//End of for : RETURN_ARTIFACTS_LIST
				
				loggers.addLogAndPrint("INFO","DEP : " + SOURCE_DIRS.get(e) + " - END OF DEPLOYMENT PROCESS ...",false);
				
				//Call agent.xml updator
				if (GetInfo.getConfig("TBX_AGENT_XML_CATEGORY").equals(SOURCE_DIRS.get(e))){
					
		    		System.out.println("");
		    		loggers.addLogAndPrint("INFO","DEP : AGENT.XML - STARTING DEPLOYMENT PROCESS ...",false);
		    		
					AgentXMLUpdator ax= new AgentXMLUpdator();
					boolean agentStatus = ax.updateAgentXML(TEMP_BUNDLE_DIR, SOURCE_DIRS.get(e));
					if ( agentStatus == false ){
						
						loggers.addLogPrintErrorReport("INFO","DEP : ","Agent.xml | FAILED",false,"DEP-AGT","DEP-AGT-INFO");
						STATUS=false;
					}
					
					loggers.addLogAndPrint("INFO","DEP : AGENT.XML - END OF DEPLOYMENT PROCESS ...",false);
				}
				
			}//End of if : RETURN_ARTIFACTS_LIST != null
			
		} //End of for : SOURCE_DIRS
			
		return STATUS;
	}
	
	
	private boolean deploy_uploader(String DEP_KRULE_PREFIX,String RETURN_ARTIFACTS_LIST, String CATEGORY) {
		
		boolean RET_STAUS =true;
		
		String HOST_LIST=GetInfo.getConfig(DEP_KRULE_PREFIX + "_HOSTNAMES");
		List<String> TEMP_HOST_LIST = new ArrayList<String>(Arrays.asList(HOST_LIST.split(",")) );
		
		for ( int l=0;l<TEMP_HOST_LIST.size();l++ ){
			
			//Get deployment server authentication details    			
			HostAuthentication hA= GetInfo.getHostAuthentication(DEP_KRULE_PREFIX, "deploy", l);
			
			//Delete existing item
			boolean cpStatus=GetInfo.deleteFile(GetInfo.getPath(DEP_KRULE_PREFIX + "_PATH_" + (l+1), CATEGORY) + File.separator + RETURN_ARTIFACTS_LIST , hA);
			if (cpStatus == false){
				loggers.addLogPrintErrorReport("INFO","DEL : " + CATEGORY + " - ", RETURN_ARTIFACTS_LIST + " | FAILED | " + TEMP_HOST_LIST.get(l) ,false,"DEL-" + CATEGORY,"DEL-INFO");
				RET_STAUS=false;
			}
			
			//Delete existing deployed directories
			cpStatus=GetInfo.deleteDir(GetInfo.getPath(DEP_KRULE_PREFIX + "_PATH_" + (l + 1), CATEGORY) ,  FilenameUtils.removeExtension(RETURN_ARTIFACTS_LIST) , hA);
			if (cpStatus == false){
				loggers.addLogPrintErrorReport("INFO","DLD : " + CATEGORY + " - ", RETURN_ARTIFACTS_LIST + " | FAILED | " + TEMP_HOST_LIST.get(l) ,false,"DLD-" + CATEGORY,"DLD-INFO");
				RET_STAUS=false;
			}
			
			//Deploy new artifacts
			cpStatus=GetInfo.uploadFile(RETURN_ARTIFACTS_LIST, TEMP_BUNDLE_DIR + File.separator + CATEGORY, GetInfo.getPath(DEP_KRULE_PREFIX + "_PATH_" + (l + 1), CATEGORY),hA);
			if (cpStatus == false){
				loggers.addLogPrintErrorReport("INFO","DEP : " + CATEGORY + " - ", RETURN_ARTIFACTS_LIST + " | FAILED | " + TEMP_HOST_LIST.get(l) ,false,"DEP-" + CATEGORY,"DEP-INFO");
				RET_STAUS=false;
			}
			else
			{
				loggers.addLogAndPrint("INFO","DEP : " + CATEGORY + " - " + RETURN_ARTIFACTS_LIST + " | SUCCESS | " + TEMP_HOST_LIST.get(l),false );
				HM_RELESE_CHECK.put(CATEGORY + "##" + RETURN_ARTIFACTS_LIST + "##" + TEMP_HOST_LIST.get(l) + "##" +  GetInfo.getPath(DEP_KRULE_PREFIX + "_PATH_" + (l + 1), CATEGORY) , hA);
			}
		}
		       
		return RET_STAUS;
	}
	
	public boolean deploy_artifacts_validator(String BUNDLE_DIR){
		
		boolean RULE_FOUND = false;
		boolean STATUS=true;
		boolean IGNORE_RULE_CHECKER=false;
		
		String tempBundleDir=System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + "TEMP" + File.separator + BUNDLE_DIR;	
		TEMP_BUNDLE_DIR=tempBundleDir;
		
		ArrayList<String> SOURCE_DIRS = GetInfo.getDirectoryList(tempBundleDir);
		if (SOURCE_DIRS == null) {
			System.out.println("");
			loggers.addLogAndPrint("INFO","BKP-VAL : No artifacts available to validate",false);
			return true;
		}
		
		
		for (int e=0;e<SOURCE_DIRS.size() ;e++){
			
			ArrayList<String> CAT_PREFIXES_LIST = GetInfo.getCategoryPrefixes(SOURCE_DIRS.get(e), "DEPLOY_KRULE_PREFIXES");
			ArrayList<String> RETURN_ARTIFACTS_LIST = GetInfo.getComponentList(SOURCE_DIRS.get(e),tempBundleDir,false);
			
			if (RETURN_ARTIFACTS_LIST != null ){
							
				for (int i=0;i<RETURN_ARTIFACTS_LIST.size() ;i++){
					
					for (int j=0;j<CAT_PREFIXES_LIST.size();j++){
						
						IGNORE_RULE_CHECKER=false;
						boolean FLAG_EXCLUDE = false;
						
						//Get exclude artifacts list
						ArrayList<String> EXCLUDE_ITEM_LIST= null;
						String[] tempExcludeItemList = CACHE_GET_ITEMS(CAT_PREFIXES_LIST.get(j),"EXCLUDE_ITEMS");
						
						if (tempExcludeItemList!=null){
							
							EXCLUDE_ITEM_LIST = new ArrayList<String>(Arrays.asList(tempExcludeItemList) );
							
							if (GetInfo.check_duplicates(EXCLUDE_ITEM_LIST, RETURN_ARTIFACTS_LIST.get(i))){
								FLAG_EXCLUDE=true;
							}
						}
						
						if (FLAG_EXCLUDE==false){
							
							//Get deployable artifacts list
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
								
								RULE_FOUND=true;
							}
														
						}else{
							
							//loggers.addLogAndPrint("WARN","DEP : " + SOURCE_DIRS.get(e) + " - " + RETURN_ARTIFACTS_LIST.get(i) + " | STOPPED | " + GetInfo.getConfig(CAT_PREFIXES_LIST.get(j) + "_HOSTNAMES"),false);
							RULE_FOUND=true;
	
						}//End of : check_duplicates - Exclude Rules
						
						
					}//End of for : CAT_PREFIXES_LIST
					
					if (RULE_FOUND == false && IGNORE_RULE_CHECKER == false){
						
						loggers.addLogAndPrint("WARN","DEP-VAL : " + SOURCE_DIRS.get(e) + " - " + RETURN_ARTIFACTS_LIST.get(i) + " | FAILED (Deployment Rule Not Found)",false);
						STATUS=false;
					}
					
					RULE_FOUND=false;
					IGNORE_RULE_CHECKER=false;
					
				}//End of for : RETURN_ARTIFACTS_LIST
				
			}//End of if : RETURN_ARTIFACTS_LIST != null
			
		} //End of for : SOURCE_DIRS
			
		return STATUS;
	}
}
