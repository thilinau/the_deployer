package it.bridge;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.bridge.objects.HostAuthentication;
import net.neoremind.sshxcute.exception.TaskExecFailException;

import org.apache.commons.codec.binary.Hex;


public class RelVerify {
	
	public static String TEMP_BUNDLE_DIR;
	static HashMap<String, HashMap<String,String>> hmArtifacts = new HashMap<String, HashMap<String,String>>();

	public static void LoadRelMD5(String BUNDLE_DIR) {
		
		HashMap<String, String> hmMD5 = null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			loggers.addLogEx("SEVE",e1.getMessage(),e1);
		}
		
		String tempBundleDir=System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + "TEMP" +  File.separator + BUNDLE_DIR;
		TEMP_BUNDLE_DIR = tempBundleDir;
		
		ArrayList<String> SOURCE_DIRS = GetInfo.getDirectoryList(tempBundleDir);
		
		if (SOURCE_DIRS == null) {
			System.out.println("");
			loggers.addLogAndPrint("INFO","No artifacts available for verification process",false);
		}
		
		for (int e=0;e<SOURCE_DIRS.size() ;e++){
			
			ArrayList<String> RETURN_ARTIFACTS_LIST = GetInfo.getComponentList(SOURCE_DIRS.get(e),tempBundleDir, false);
			
			if (RETURN_ARTIFACTS_LIST != null ){
				
				hmMD5 = new HashMap<String, String>();
				
				for (int i=0;i<RETURN_ARTIFACTS_LIST.size() ;i++){
					
					FileInputStream newArtifact = null;
					String digest = null;
					
					try {
						newArtifact = new FileInputStream(tempBundleDir + File.separator + SOURCE_DIRS.get(e) + File.separator + RETURN_ARTIFACTS_LIST.get(i));
					} catch (FileNotFoundException e1) {
						loggers.addLogEx("SEVE",e1.getMessage(),e1);
					}
					
					try {
						digest = getDigest(newArtifact, md, 2048);
					} catch (NoSuchAlgorithmException | IOException e1) {
						loggers.addLogEx("SEVE",e1.getMessage(),e1);
					}
					
					hmMD5.put(RETURN_ARTIFACTS_LIST.get(i), digest);
					try {
						newArtifact.close();
					} catch (IOException e1) {
						loggers.addLogEx("SEVE",e1.getMessage(),e1);
					}				
				}
			}
			
			hmArtifacts.put(SOURCE_DIRS.get(e), hmMD5);
			
		}
		
	}
	
	public static String getDigest(InputStream is, MessageDigest md, int byteArraySize)	throws NoSuchAlgorithmException, IOException {

		md.reset();
		byte[] bytes = new byte[byteArraySize];
		int numBytes;
		while ((numBytes = is.read(bytes)) != -1) {
			md.update(bytes, 0, numBytes);
		}
		
		byte[] digest = md.digest();
		String result = new String(Hex.encodeHex(digest));
		return result;
	}
	
	
	
	public static  void CompareRelMD5() throws TaskExecFailException{
		
		ArrayList<String> SOURCE_DIRS = GetInfo.getDirectoryList(TEMP_BUNDLE_DIR);
		HashMap<String, HostAuthentication>  ITEM_MAP = RelDeploy.HM_RELESE_CHECK;
		
		for (int e=0;e<SOURCE_DIRS.size() ;e++){
			
			loggers.addLogAndPrint("INFO","------------ " + SOURCE_DIRS.get(e) + " - ARTIFACT(S) COMPARISON STARTED ------------",false);
			
			for (Map.Entry<String, HostAuthentication> entry : ITEM_MAP.entrySet()) {
				
				String[] item_list = entry.getKey().split("##");
				String DepArtifactMD5 ="";
				
				if (SOURCE_DIRS.get(e).equals(item_list[0])){
					
					try{
						
						HashMap<String, String> getDeployedList =  hmArtifacts.get(SOURCE_DIRS.get(e));
						DepArtifactMD5 = getDeployedList.get(item_list[1]);
						
						String[] shellout =ShellCaller(item_list[0],item_list[1],item_list[3],entry.getValue());
						
						if ( shellout[0] != null && !shellout[0].isEmpty()){

							if (shellout[1].equals(item_list[1])){
								
								String[] pf =  new String[7];
								pf[0]="MD5 : ";
								pf[1]=SOURCE_DIRS.get(e);
								pf[2]=item_list[2];
								pf[3]=item_list[1];
								pf[4]=DepArtifactMD5 + "(REL)";
								pf[5]=shellout[0] + "(DEP)";
								
								if (!shellout[0].equals(DepArtifactMD5)){
									pf[6] = "| FAILED";
								}else{
									pf[6] = "| SUCCESS";
								}
								
								loggers.addLogAndPrintf("INFO", pf,false  );

							}
						}
						
					}catch(Exception e1){
						
						String[] pf =  new String[7];
						pf[0]="MD5 : ";
						pf[1]=SOURCE_DIRS.get(e);
						pf[2]=item_list[2];
						pf[3]=item_list[1];
						pf[4]=DepArtifactMD5 + "(REL)";
						pf[5]="MD5SUM RETRIVAL FAILED (DEP)";
						pf[6] = "| FAILED";
						loggers.addLogAndPrintf("WARN", pf,false  );
						loggers.addLogEx("WARN", e1.getMessage(), e1);
					}
				}
			}
			
			loggers.addLogAndPrint("INFO","------------ END OF " + SOURCE_DIRS.get(e) + " - ARTIFACT(S) COMPARISON ------------\n", false);
		}

	}
	

	
	private static String[] ShellCaller (String CATEGORY,String ARTIFACT,String PATH,  HostAuthentication hA){
		
		String[] out = null;
		try{
			ShellExec ShEx = new ShellExec();
			
		    if (ShEx.connect(CATEGORY,ARTIFACT,PATH,hA)){
		    	
			    out = ShEx.CommandInterface();
			    ShEx.disconnect();
			    
		    }else{
		    	
				loggers.addLogAndPrint("INFO", "MD5 : " + CATEGORY + "  " + hA.getHOSTNAME() + " " + ARTIFACT + "  " + " CONNECTIVITY FAILED | FAILED",false  );

		    }

		}
		catch (Exception e){
			loggers.addLogEx("WARN", e.getMessage(),e);
		}
		
		return out;
	}

	
	public static void PrintMD5ForDeployed() throws FileNotFoundException, IOException{
		
//		String RES = GetInfo.getConfig("PRINT_MD5_FOR_DEPLOYED_ITEMS").toUpperCase();
//		if ( !RES.equals("-1") ){
//			
//			if ( RES.equals("TRUE") ){
//			
//				if (CLT_FLAG == true ){
//					System.out.println("\nPrint MD5SUM of Deployed Client Modules ...");
//					for (Map.Entry<String, String> entry : hmClt.entrySet()) {
//						System.out.println(entry.getKey() + " " + entry.getValue());
//					}			
//				}
//				
//				if (SER_FLAG == true ){
//					System.out.println("\nPrint MD5SUM of Deployed Services ...");
//					for (Map.Entry<String, String> entry : hmSer.entrySet()) {
//						System.out.println(entry.getKey() + " " + entry.getValue());
//					}		
//				}
//				
//				if (LIB_FLAG == true ){
//					System.out.println("\nPrint MD5SUM of Deployed Libraries ...");
//					for (Map.Entry<String, String> entry : hmLib.entrySet()) {
//						System.out.println(entry.getKey() + " " + entry.getValue());
//					}		
//				}
//				
//				if (SCH_FLAG == true ){
//					System.out.println("\nPrint MD5SUM of Deployed Scheduler Resources ...");
//					for (Map.Entry<String, String> entry : hmSch.entrySet()) {
//						System.out.println(entry.getKey() + " " + entry.getValue());
//					}		
//				}
//			}
//		}

	}
}
