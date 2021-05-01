package it.bridge;

import java.io.*;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import it.bridge.objects.HostAuthentication;
import it.bridge.objects.ParamDepErrors;
import it.codegen.util.CGPrivateKey;
import it.codegen.util.Password;
import org.apache.commons.io.FilenameUtils;

import javax.crypto.Cipher;


public class GetInfo {

	public static Object readFromFile(String filename) {

		Object object = null;
		ObjectInputStream input = null;

		try {

			File file = new File(filename);
			if (file.exists()) {

				input = new ObjectInputStream(new FileInputStream(file));
				object = input.readObject();

			}

		} catch (IOException e) {
			loggers.addLogEx("SEVE",e.getMessage(),e);
			return null;
		} catch (ClassNotFoundException e) {
			loggers.addLogEx("SEVE",e.getMessage(),e);
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					//Do nothing
				}
			}
		}

		return object;

	}

	public static String getPasswordFromKeyFiles(String PasswordFileName) {

		try {

			String fileLoc =  System.getProperty("BASE_DIR") + File.separator + "conf" + File.separator + System.getProperty("hostname") ;

			String keyfile = fileLoc + File.separator + PasswordFileName + "_pvtkey";
			String passwordfile = fileLoc + File.separator + PasswordFileName;

			File kfile = new File(keyfile);
			File pfile = new File(passwordfile);

			if (kfile.exists() && pfile.exists()) {

				loggers.addLog("INFO", "Reading Private Key File...");

				CGPrivateKey cgPrivateKey = (CGPrivateKey) readFromFile(keyfile);
				PrivateKey privateKey = cgPrivateKey.getPrivateKey();
				loggers.addLog("INFO", "Private Key Obtained : " + keyfile);

				loggers.addLog("INFO","Reading Password File...");

				Password cgPassword = (Password) readFromFile(passwordfile);
				byte[] password = cgPassword.getEncryptedPassword();
				loggers.addLog("INFO", "Password Obtained : " + passwordfile);

				Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.DECRYPT_MODE, privateKey);

				byte[] decryptedPassword = cipher.doFinal(password);
				return new String(decryptedPassword).toString();

			}else{

				return null;
			}

		} catch (Exception e) {
			loggers.addLogEx("SEVE",e.getMessage(),e);
			return null;
		}
	}

	public static ArrayList<String> getCategoryPrefixes(String CATEGORY, String Config){
		
		ArrayList<String> CAT_PRE_LIST = new ArrayList<String>();
		List<String> KRULE_PREFIX = new ArrayList<String>(Arrays.asList(GetInfo.getConfig(Config).split(",")) );
		
		for (int i=0;i<KRULE_PREFIX.size();i++){
			
			if (GetInfo.getConfig(KRULE_PREFIX.get(i) + "_CATEGORY").equals(CATEGORY)){
				CAT_PRE_LIST.add(KRULE_PREFIX.get(i));
			}

		}
		
		return CAT_PRE_LIST;
	}
	
	public static ArrayList<String> getDirectoryList(String BUNDLE_DIR){
		
		ArrayList<String> RETURN_LIST = null;
		
		File theDir = new File(BUNDLE_DIR);
		if ( theDir.exists() ){
			
			RETURN_LIST = new ArrayList<String>();
			
			File[] files = theDir.listFiles();
			
			if (files.length != 0){
				
				for( int q=0;q<files.length;q++){
		        	
					File file = files[q];
			        			
		        	if (file.isDirectory()){
		        		RETURN_LIST.add(files[q].getName());
		        	}					
			    }
				
			 }else{
				 RETURN_LIST=null;
			 }
			 
		}else{
			
			RETURN_LIST=null;
		}
		
		return RETURN_LIST;
	}
	
	public static void recBackupDeployErr(String ERR_CODE ,String ERR_DESC,String ERR_CATEGORY){
		
		ParamDepErrors nErr = new ParamDepErrors();
		nErr.setERR_CODE(ERR_CODE);
		nErr.setERR_DESC(ERR_DESC);
		nErr.setERR_CATEGORY(ERR_CATEGORY);
		
		universal_deployer.ARR_ALL_DEP_ERRORS.add(nErr);
	}

	public static HostAuthentication getHostAuthentication(String key, String type, int index){
		
		HostAuthentication  ha = new HostAuthentication();
		
		if (type.equals("backup")){
			
			ha.setHOSTNAME(getConfig(key + "_HOSTNAME"));
			
			String AUTH_T;
			if (!getConfig(key + "_AUTH_TYPE").equals("-1")){
				AUTH_T=getConfig(key + "_AUTH_TYPE");
			}else{
				AUTH_T = getConfig("DEFAULT_AUTH_TYPE");
			}
			
			ha.setAUTH_TYPE(AUTH_T);
			
			if (AUTH_T.equals("userpass")){
				
				if (getConfig(key + "_USERNAME").equals("-1")){
					ha.setUSERNAME(getConfig("DEFAULT_APP_USER"));
				}else{				
					ha.setUSERNAME(getConfig(key + "_USERNAME"));
				}
				
				
				if (getConfig(key + "_PASSWORD").equals("-1")){
					ha.setPASSWORD(getConfig("DEFAULT_APP_PASSWORD"));				
				}else{
					ha.setPASSWORD(getConfig(key + "_PASSWORD"));					
				}
				
			}else if (AUTH_T.equals("keyfile")){
				
				if (!getConfig(key + "_KEYFILE").equals("-1")){
					ha.setKEYFILE(getConfig(key + "_KEYFILE"));
				}else{
					ha.setKEYFILE(getConfig("DEFAULT_KEYFILE"));
				}
				
				
				if (getConfig(key + "_USERNAME").equals("-1")){
					ha.setUSERNAME(getConfig("DEFAULT_APP_USER"));
				}else{
					ha.setUSERNAME(getConfig(key + "_USERNAME"));
				}
				
				
				if (getConfig(key + "_PASSWORD").equals("-1")){
					ha.setPASSWORD(null);
				}else{
					ha.setPASSWORD(getConfig(key + "_PASSWORD"));
				}
			}
			
			if (!getConfig(key + "_SSH_PORT").equals("-1")){
				ha.setPORT(getConfig(key + "_SSH_PORT"));
			}else if(!getConfig("DEFAULT_SSH_PORT").equals("-1")){
				ha.setPORT(getConfig("DEFAULT_SSH_PORT"));
			}else{
				ha.setPORT("22");
			}
			
			
		}else if (type.equals("agentxml")){
			
			ha.setHOSTNAME(getConfig("TBX_AGENT_XML_HOSTNAME"));
			
			String AUTH_T;
			if (!getConfig("TBX_AGENT_XML_AUTH_TYPE").equals("-1")){
				AUTH_T=getConfig("TBX_AGENT_XML_AUTH_TYPE");
			}else{
				AUTH_T = getConfig("DEFAULT_AUTH_TYPE");
			}
			
			ha.setAUTH_TYPE(AUTH_T);
			
			if (AUTH_T.equals("userpass")){
				
				if (getConfig("TBX_AGENT_XML_USERNAME").equals("-1")){
					ha.setUSERNAME(getConfig("DEFAULT_APP_USER"));
				}else{
					ha.setUSERNAME(getConfig("TBX_AGENT_XML_USERNAME"));
				}
				
				
				if (getConfig("TBX_AGENT_XML_PASSWORD").equals("-1")){
					ha.setPASSWORD(getConfig("DEFAULT_APP_PASSWORD"));
				}else{
					ha.setPASSWORD(getConfig("TBX_AGENT_XML_PASSWORD"));
				}
				
			}else if (AUTH_T.equals("keyfile")){
				
				if (!getConfig("TBX_AGENT_XML_KEYFILE").equals("-1")){
					ha.setKEYFILE(getConfig("TBX_AGENT_XML_KEYFILE"));
				}else{
					ha.setKEYFILE(getConfig("DEFAULT_KEYFILE"));
				}
				
				if (getConfig("TBX_AGENT_XML_USERNAME").equals("-1")){
					ha.setUSERNAME(getConfig("DEFAULT_APP_USER"));
				}else{
					ha.setUSERNAME(getConfig("TBX_AGENT_XML_USERNAME"));
				}
				
				
				if (getConfig("TBX_AGENT_XML_PASSWORD").equals("-1")){
					ha.setPASSWORD(null);
				}else{
					ha.setPASSWORD(getConfig("TBX_AGENT_XML_PASSWORD"));
				}
			}
			
			
			if (!getConfig("TBX_AGENT_XML_SSH_PORT").equals("-1")){
				ha.setPORT(getConfig("TBX_AGENT_XML_SSH_PORT"));
			}else if(!getConfig("DEFAULT_SSH_PORT").equals("-1")){
				ha.setPORT(getConfig("DEFAULT_SSH_PORT"));
			}else{
				ha.setPORT("22");
			}
			
			
		}else if (type.equals("deploy")){
			
			List<String> DEP_HOST = new ArrayList<String>(Arrays.asList(GetInfo.getConfig(key + "_HOSTNAMES").split(",")));
			ha.setHOSTNAME(DEP_HOST.get(index));
			
			String AUTH_T;
			if (!getConfig(key + "_AUTH_TYPE_" + (index + 1)).equals("-1")){
				AUTH_T=getConfig(key + "_AUTH_TYPE_" + (index + 1));
			}else{
				AUTH_T = getConfig("DEFAULT_AUTH_TYPE");
			}
			
			ha.setAUTH_TYPE(AUTH_T);
			
			if (AUTH_T.equals("userpass")){
				if (getConfig(key + "_USERNAME_" + (index + 1)).equals("-1")){
					ha.setUSERNAME(getConfig("DEFAULT_APP_USER"));
				}else{
					ha.setUSERNAME(getConfig(key + "_USERNAME_" + (index + 1)));
				}
				
				if (getConfig(key + "_PASSWORD_" + (index + 1)).equals("-1")){
					ha.setPASSWORD(getConfig("DEFAULT_APP_PASSWORD"));
				}else{
					ha.setPASSWORD(getConfig(key + "_PASSWORD_" + (index + 1)));
				}
				
			}else if (AUTH_T.equals("keyfile")){
				
				if (!getConfig(key + "_KEYFILE_" + ( index + 1)).equals("-1")){
					ha.setKEYFILE(getConfig(key + "_KEYFILE_" + ( index + 1)));
				}else{
					ha.setKEYFILE(getConfig("DEFAULT_KEYFILE"));
				}
				
				if (getConfig(key + "_USERNAME_" + (index + 1)).equals("-1")){
					ha.setUSERNAME(getConfig("DEFAULT_APP_USER"));
				}else{
					ha.setUSERNAME(getConfig(key + "_USERNAME_" + (index + 1)));
				}
				
				if (getConfig(key + "_PASSWORD_" + (index + 1)).equals("-1")){
					ha.setPASSWORD(null);
				}else{
					ha.setPASSWORD(getConfig(key + "_PASSWORD_" + (index + 1)));
				}
			}
			
			if (!getConfig(key + "_SSH_PORT_" + (index + 1)).equals("-1")){
				ha.setPORT(getConfig(key + "_SSH_PORT_" + (index + 1)));
			}else if(!getConfig("DEFAULT_SSH_PORT").equals("-1")){
				ha.setPORT(getConfig("DEFAULT_SSH_PORT"));
			}else{
				ha.setPORT("22");
			}
			
		}
		
		return ha;
	}

	public static boolean getFileAvailability(List<String> tempFil){
		
		boolean STATUS = true;
		
		for (int x=0; x<tempFil.size();x++) {
			
			String tmpDir=System.getProperty("BASE_DIR") +  File.separator + "conf" + File.separator + tempFil.get(x);
			File theDir = new File(tmpDir);

			if (!theDir.exists()) {
				
				loggers.addLogAndPrint("INFO", "Data loading file not found : " + tmpDir, false);
				STATUS=false;
				break;
				
			 }else {
				 
				 try{
						
					String strLine = null;
					
		            FileInputStream fstream = new FileInputStream(tmpDir);
		            DataInputStream in = new DataInputStream(fstream);
		            BufferedReader br = new BufferedReader(new InputStreamReader(in));

		            StringBuffer buffer = new StringBuffer("");
		            while ((strLine = br.readLine()) != null)   {
		            	 buffer.append(strLine);
		            }
		            
		            if (buffer.toString()== null || buffer.toString().isEmpty()) {
		            	
		            	loggers.addLogAndPrint("INFO", "Data loading file is blank : " + tmpDir, false);
						STATUS=false;
						break;
		            }
		            
		            in.close();
		            
		        }
				catch (Exception e)
		        {
		            System.err.println("getFileAvailability : " + e.getMessage());
		        }
			 }
		}
		
		return STATUS;
	}
	
	public static String getReleaseDirectory(int REL_ID){
		
		String REL_DIRECTORY = "-1";
			
		File f = new File(System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + GetInfo.getReleaseSource() ); 
        File[] files = f.listFiles();
        
        for( int i=0;i<files.length;i++){
        	
        	File file = files[i];
        	int REL_VER;
        	
        	if (file.isDirectory()){
            	REL_VER=Integer.parseInt(file.getName().split("_")[1].replaceAll("\\.", ""));
            	
            	if ( REL_VER == REL_ID){
            		REL_DIRECTORY=file.getName();            		
            	}	          	
            }
        }
		
		return REL_DIRECTORY;
	}
	
	public static List<String> getOraErrPrefixes(){
		List<String> OraErrors = new ArrayList<String>();

			OraErrors.add("DBV-"); //DBV	DB Verify errors
			OraErrors.add("DBA-"); //DBA	SQL*DBA errors
			OraErrors.add("ORA-"); //ORA	RDBMS errors
			OraErrors.add("TNS-"); //TNS	SQL*Net errors
			OraErrors.add("EXP-"); //EXP	Export errors
			OraErrors.add("PLS-"); //PLS	PL/SQL errors
			OraErrors.add("PLW-"); //PLW	PL/SQL warnings
			OraErrors.add("IMP-"); //IMP	Import errors
			OraErrors.add("FRM-"); //FRM	Oracle Forms errors
			OraErrors.add("LCC-"); //LCC	Parameter parsing errors
			OraErrors.add("OSD-"); //OSD	OSD level errors
			OraErrors.add("PCC-"); //PCC	Precompiler errors
			OraErrors.add("SQL-"); //SQL	SQLLIB runtime errors
			OraErrors.add("REP-"); //REP	Oracle Reports errors
			OraErrors.add("RMAN-"); //RMAN	RMAN error messages
			OraErrors.add("SP2-"); //SQL*Plus Error Messages
						
		return OraErrors;
		
	}
	
	public static String getBackupDirName(){
		
		String DIR_NAME="";
		ArrayList<Integer> tempAl = universal_deployer.DEP_REL_ARR;
		
		
		int x=0;
		File theDir;
		
		while (true){

			DIR_NAME= System.getProperty("hostname") + "_" + System.getProperty("selecEnv");
			 
			if (System.getProperty("ADHOC_DEPLOYMENT_FLAG").equals("false")){
				DIR_NAME=DIR_NAME + "_" + getReleaseDirectory(tempAl.get(0)) + "__" + getReleaseDirectory(tempAl.get(tempAl.size()-1)) + "_" + x ;
				theDir = new File( System.getProperty("BASE_DIR") +  File.separator + "METERIALS" + File.separator + "BACKUP" +  File.separator + DIR_NAME);
			}else{
				DIR_NAME=DIR_NAME + "_" + getReleaseDirectory(tempAl.get(0)) + "__" + getReleaseDirectory(tempAl.get(tempAl.size()-1)) + "_" + x ;
				theDir = new File( System.getProperty("BASE_DIR") +  File.separator + "METERIALS" +  File.separator +  "BACKUP" +  File.separator + "ADHOC_BACKUP" +  File.separator + DIR_NAME);
			}
			
			if ( !theDir.exists()){
				break;
			}else{
				x++;;
			}
		}
		
		return DIR_NAME;
	}

	public static String getDBLogBackupDirName(String release_dir){
		
		String DIR_NAME="";
		String base_dir= System.getProperty("BASE_DIR") +  File.separator + "METERIALS" + File.separator + "LOGS" ;
		
		int x=0;
		File theDir;
		
		while (true){

			DIR_NAME=System.getProperty("hostname")  + "_" + System.getProperty("selecEnv");
			DIR_NAME=DIR_NAME + "_" +  release_dir + "_" + x ;
			
			if (System.getProperty("ADHOC_DEPLOYMENT_FLAG").equals("false")){
				theDir = new File(base_dir +  File.separator + "DB_LOGS" + File.separator + DIR_NAME);
			}else{				
				theDir = new File( base_dir +  File.separator + "DB_LOGS_ADHOC" +  File.separator + DIR_NAME);
			}
			
			if ( !theDir.exists()){
				
				try{
			        theDir.mkdirs();
			    } 
			    catch(Exception se){
			        
			    	loggers.addLogAndPrint("INFO", "Logs output directory cannot be created", false);
			    }
				
				break;
				
			}else{
				x++;;
			}
		}
		
		return theDir.getAbsolutePath();
	}
	
	public static String[] getItemsList(String RULE_PREFIX, String FLAG) {
		
		String ret_list = null;
		String[] result = null;
		
		if (FLAG.equals("ITEMS")) {
			
			if ( !GetInfo.getConfig(RULE_PREFIX + "_ITEMS_LOAD_FROM_FILE").equals("-1")){
				
				List<String> tempFil=Arrays.asList(GetInfo.getConfig(RULE_PREFIX + "_ITEMS_LOAD_FROM_FILE").split(","));
				
				for (int c=0;c<tempFil.size();c++ ) {
					
					String tmpDir=System.getProperty("BASE_DIR") +  File.separator + "conf" + File.separator + tempFil.get(c);
					StringBuffer sbRead = readFile(tmpDir);
					
		            if (ret_list == null) {
		            	ret_list =  sbRead.toString();
		            }else {
		            	ret_list = ret_list + "," + sbRead.toString();
		            }
				}
				
			}else {
				
				ret_list = GetInfo.getConfig(RULE_PREFIX + "_ITEMS");
			}
			

	        
		}else if (FLAG.equals("EXCLUDE_ITEMS")) {
			
			if ( !GetInfo.getConfig(RULE_PREFIX + "_EXCLUDE_ITEMS_LOAD_FROM_FILE").equals("-1")){
				
				List<String> tempFil=Arrays.asList(GetInfo.getConfig(RULE_PREFIX + "_EXCLUDE_ITEMS_LOAD_FROM_FILE").split(","));
				
				for (int c=0;c<tempFil.size();c++ ) {
					
					String tmpDir=System.getProperty("BASE_DIR") +  File.separator + "conf" + File.separator + tempFil.get(c);
					StringBuffer sbRead = readFile(tmpDir);
					
		            if (ret_list == null) {
		            	ret_list =  sbRead.toString();
		            }else {
		            	ret_list = ret_list + "," + sbRead.toString();
		            }
				}
				
			}else {
				
				ret_list = GetInfo.getConfig(RULE_PREFIX + "_EXCLUDE_ITEMS");
				if (ret_list.equals("-1")) {
					ret_list = null;
				}
			}
		}
		
		if (ret_list != null) {
	        List<String> list = Arrays.asList(ret_list.split(","));
	        Set<String> set = new HashSet<String>(list);     
	        result = new String[set.size()];
	        set.toArray(result);
		}
		
		return result;
	}
	
	private static StringBuffer readFile(String fileName) {
		
		StringBuffer buffer = null;
		try{
			
			String tmpDir=fileName;				
			String strLine = null;
			
            FileInputStream fstream = new FileInputStream(tmpDir);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            buffer = new StringBuffer("");
            while ((strLine = br.readLine()) != null)   {
            	 buffer.append(strLine);
            }
                       
            in.close();
            
        }
		catch (Exception e)
        {
            System.err.println("getItemsList : " + e.getMessage());
        }
		
		return buffer;
	}
	
	public static boolean isNumeric(String str) 	{
	  try  
	  {  
	    @SuppressWarnings("unused")
		Integer d = Integer.parseInt(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}

	public static String getCommonConfig(String code) {

		String	ret = "-1";
		Properties props = new Properties();

		try
		{
			String proPath = System.getProperty("BASE_DIR") + File.separator + "conf"  + File.separator + System.getProperty("hostname") + File.separator + "common.conf";
			props.load(new FileInputStream(proPath));

			ret = props.getProperty(code);
			if (ret == null){
				ret="-1";
			}
			else{
				if (ret.isEmpty()){
					ret="-1";
				}
			}

		}catch (FileNotFoundException e) {
			ret="-1";
		} catch (IOException e) {
			ret="-1";
		}

		return ret;
	}

	public static String getConfig(String code) {
		
		String	ret = "-1";
		Properties props = new Properties();	 	
		
		try
		{		
			String proPath = System.getProperty("BASE_DIR") + File.separator + "conf"  + File.separator + System.getProperty("hostname") + File.separator + System.getProperty("selecEnv") + ".properties";
			props.load(new FileInputStream(proPath));
			
			ret = props.getProperty(code);
			if (ret == null){
				ret="-1";
			}
			else{
				if (ret.isEmpty()){
					ret="-1";
				}
			}
			
		}catch (FileNotFoundException e) {
			ret="-1";
			loggers.addLogEx("SEVE","getConfig.FileNotFoundException : " + e.getMessage() , e);
		} catch (IOException e) {
			ret="-1";
			loggers.addLogEx("SEVE","getConfig.IOException : " + e.getMessage() , e);
		}
	
		return ret;
	}
	
	public static ArrayList<String> getSourceDirectories(){
		
		String TEMP_CONFIG;
		ArrayList<String> SOURCES = new ArrayList<String>();
		List<String> BKP_KRULE_PREFIX = new ArrayList<String>(Arrays.asList(GetInfo.getConfig("BACKUP_KRULE_PREFIXES").split(",")) );
		
		for (int i=0;i<BKP_KRULE_PREFIX.size();i++){
			
			TEMP_CONFIG=GetInfo.getConfig(BKP_KRULE_PREFIX.get(i) + "_CATEGORY");
	
			if (!TEMP_CONFIG.equals("-1")){
				if (check_duplicates(SOURCES,TEMP_CONFIG) == false){
					
					SOURCES.add(TEMP_CONFIG);
				}
				
			}else{
				
				loggers.addLogAndPrint("WARN","Value not found for config " + BKP_KRULE_PREFIX.get(i) + "_CATEGORY \nBye",false);
				System.exit(0);
			}
		}
		
		List<String> DEP_KRULE_PREFIX = new ArrayList<String>(Arrays.asList(GetInfo.getConfig("DEPLOY_KRULE_PREFIXES").split(",")) );
		
		for (int k=0;k<DEP_KRULE_PREFIX.size();k++){
			
			TEMP_CONFIG=GetInfo.getConfig(DEP_KRULE_PREFIX.get(k) + "_CATEGORY");
			List<String> DEP_HOST = new ArrayList<String>(Arrays.asList(TEMP_CONFIG.split(",")) );
						
			if (!TEMP_CONFIG.equals("-1")){
				for (int l=0;l<DEP_HOST.size();l++){
					
					if (check_duplicates(SOURCES,TEMP_CONFIG) == false){
						
						SOURCES.add(TEMP_CONFIG);
					}
				}
			}else{
				
				loggers.addLogAndPrint("WARN","Value(s) not found for config " + DEP_KRULE_PREFIX.get(k) + "_CATEGORY \nBye",false);
				System.exit(0);
			}
		}
		
		return SOURCES;
	}
	
	public static boolean check_duplicates(ArrayList<String> itemArr, String key){
		
		boolean RET_STATUS=false;
		
		for (int u=0; u<itemArr.size();u++ ){
			if ( itemArr.get(u).equals(key)){
				RET_STATUS=true;
				break;
			}
		}
		
		return RET_STATUS;
		
	}

	public static boolean check_duplicates(ArrayList<Integer> itemArr, int key){
		
		boolean RET_STATUS=false;
		
		for (int u=0; u<itemArr.size();u++ ){
			if ( itemArr.get(u).equals(key)){
				RET_STATUS=true;
				break;
			}
		}
		
		return RET_STATUS;
		
	}
	
	public static String getPath(String code, String category) {
		
		String path = getConfig(code);
		
		if (path.equals("-1")){
			
			if (!getConfig("DEFAULT_" + category.toUpperCase()+ "_PATH").equals("-1")){
				
				path = getConfig("DEFAULT_" + category.toUpperCase()+ "_PATH");
				
			}else{
				
				loggers.addLogAndPrint("INFO","Configuration " + code + " not found",false);
				System.exit(0);
			}
		}
	
		return path;
	}

	public static String getConfigVersion(String code) throws FileNotFoundException, IOException{
		
		Properties props = new Properties();	 	
		
		String proPath = System.getProperty("BASE_DIR") +  File.separator + "conf" + File.separator + System.getProperty("hostname") + File.separator + System.getProperty("selecEnv") + ".version";
		props.load(new FileInputStream(proPath));
		
		String	ret = props.getProperty(code.trim());
		if (ret == null){
			ret="-1";
		}
		else{
			if (ret.isEmpty()){
				ret="-1";
			}
		}
		
		return ret;
	
	}
	
	public static String getOracleClientPath(){
		
		String ora_path= getConfig("ORACLE_HOME");
		
		if (ora_path.equals("-1")){
			
			try
		    {
		        final String os = System.getProperty("os.name");

		        if (os.contains("Windows"))
		        {
					String ora_client_base_dir= getConfig("ORACLE_CLIENT_BASE_DIR");
					if (ora_client_base_dir.equals("-1"))
					{
						ora_client_base_dir = "instantclient-sqlplus-nt-11.2.0.4.0";
					}
		        	ora_path = System.getProperty("BASE_DIR") +  File.separator + "conf" + File.separator + "tools" + File.separator + ora_client_base_dir + File.separator + "sqlplus";
		        }
		        else
		        {
		        	ora_path="sqlplus";
		        }
		    }
		    catch (final Exception e)
		    {
		       	loggers.addLogAndPrint("SERV", "Oracle home not found", false);
		       	loggers.addLogEx("SERV", e.getMessage(), e);
		    }
		}
		
		return ora_path;
		
	}
	
	public static boolean downloadFiles(String fName,String srcLoc,String desLoc,HostAuthentication hA){
		
		boolean ret_status = true;
			
		FileTransferHandler FH = new FileTransferHandler();
		FH.SFTPHandler(hA);
		
		try{
			FH.connect();	
		}
		catch(Exception ex)
		{
			ret_status=false;
			loggers.addLogEx("SEVE","downloadFiles.connect : " + ex.getMessage() , ex);
		}
		
		if ( FH.isConnected()){
			
			try{
				
				boolean sta=FH.downloadFile(desLoc, srcLoc, fName);
				if ( sta == false ){
					ret_status=false;
				}
					
			}catch (Exception ex){
				
				ret_status=false;
				loggers.addLogEx("SEVE","downloadFiles.downloadFile : " + ex.getMessage() , ex);
			}
			
		}else{
			
			ret_status=false;
		}

		return ret_status;
	}

	public static boolean deleteFile(String fName,HostAuthentication hA) {
		
		boolean ret_status = true;

		FileTransferHandler FH = new FileTransferHandler();
		FH.SFTPHandler(hA);
		
		try{
			FH.connect();			
		}
		catch(Exception ex)
		{
			ret_status=false;
			loggers.addLogEx("SEVE","deleteFile.connect : " + ex.getMessage() , ex);
		}
		
		if ( FH.isConnected()){
			
			try{
				
				boolean sta=FH.deleteFile(fName);
				if ( sta == false ){
					ret_status=false;
				}
					
			}catch (Exception ex){
				
				ret_status=false;
				loggers.addLogEx("SEVE","deleteFile.deleteFile : " + ex.getMessage() , ex);
				
			}
		}
		else{
			
			ret_status=false;
		}
			
		return ret_status;
	}

	public static boolean deleteDir(String dName,String fName,HostAuthentication hA) {
		
		boolean ret_status = true;
			
		FileTransferHandler FH = new FileTransferHandler();
		FH.SFTPHandler(hA);
		
		try{
			FH.connect();			
		}
		catch(Exception ex)
		{
			ret_status=false;
			loggers.addLogEx("SEVE","deleteDir.connect : " + ex.getMessage() , ex);
		}
		
		if ( FH.isConnected()){
			
			try{
				
				boolean sta=FH.deleteDir(dName,fName);
				if ( sta == false ){
					ret_status=false;
				}
					
			}catch (Exception ex){
				
				ret_status=false;
				loggers.addLogEx("SEVE","deleteDir.deleteDir : " + ex.getMessage() , ex);
			}
			
		}else{
			
			ret_status=false;
		}
		
		return ret_status;
	}	
	
	public static boolean uploadFile(String fName,String srcLoc,String desLoc,HostAuthentication hA){
		
		boolean ret_status = true;
		
		FileTransferHandler FH = new FileTransferHandler();
		FH.SFTPHandler(hA);
		
		try{
			FH.connect();			
		}
		catch(Exception ex)
		{
			ret_status=false;
			loggers.addLogEx("SEVE","uploadFile.connect : " +ex.getMessage() , ex);
		}
		
		if ( FH.isConnected()){
			
			try{
				boolean sta=FH.uploadFile(srcLoc, desLoc, fName);
				if ( sta == false ){
					ret_status=false;
				}
					
			}catch (Exception ex){
				
				ret_status=false;
				loggers.addLogEx("SEVE","uploadFile.uploadFile : " + ex.getMessage() , ex);
			}
		}
		else
		{	
			ret_status=false;
		}
		
		return ret_status;
	}

	public static boolean createTempDirectory(String tempFileName){
		
		boolean status=false;
		String tmpDir=System.getProperty("BASE_DIR") +  File.separator + "METERIALS" + File.separator + "TEMP" + File.separator + tempFileName;
		File theDir = new File(tmpDir);

		if (!theDir.exists()) {

		    try{
		        theDir.mkdir();
		        status = true;
		     } catch(SecurityException se){
		    	 status = false;
		    	 loggers.addLogEx("SERV", "createTempDirectory.mkdir : " + se.getMessage(), se);
		     }        
		  }
		
		return status;
	}
	
	public static String getRandomKey(){
		
		Random ran = new Random();
		return String.valueOf(ran.nextInt(Integer.MAX_VALUE));
		
	}

	public static ArrayList<String> getComponentList(String COMP_DIR,String BUNDLE_DIR, boolean RemoveExtension){
		
		ArrayList<String> RETURN_LIST = null;
		
		String tempDir= BUNDLE_DIR + File.separator + COMP_DIR;
		File theDir = new File(tempDir);
		if ( theDir.exists() ){
			
			RETURN_LIST = new ArrayList<String>();
			
			File[] files = theDir.listFiles();
			if (files.length != 0){
				for( int q=0;q<files.length;q++){
					
					if (RemoveExtension == false){
						RETURN_LIST.add(files[q].getName());
					}else{
						RETURN_LIST.add( FilenameUtils.removeExtension(files[q].getName()));
					}
			    }
			 }else{
				 RETURN_LIST=null;
			 }
			 
		}else{
			
			RETURN_LIST=null;
		}
		
		return RETURN_LIST;
	}
	
	public static void setConfigVersion(String code, String value) throws FileNotFoundException, IOException{
		Properties props = new Properties();	 	
		
		String proPath = System.getProperty("BASE_DIR") + File.separator +  "conf" + File.separator + System.getProperty("hostname") + File.separator + System.getProperty("selecEnv") + ".version";
		props.load(new FileInputStream(proPath));
		
		props.setProperty(code, value.trim());
		FileOutputStream out = new FileOutputStream(proPath);
		props.store(out,null);
		out.close();
	}

	public static void adhocDirCheck(){
		
		String ADHOC_BACKUP_DIR=System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + "BACKUP" + File.separator + "ADHOC_BACKUP" + File.separator;
		File theDir = new File(ADHOC_BACKUP_DIR);
		if ( !theDir.exists() ){
			theDir.mkdirs();
		}
		
	}
	
	public static String getReleaseSource(){
		
		String REL_SOURCE;
		
		if ( System.getProperty("ADHOC_DEPLOYMENT_FLAG").equals("false")){
			REL_SOURCE="RELEASES";
		}
		else{
			REL_SOURCE="ADHOCREL";
		}
		return REL_SOURCE;
	}
		
}