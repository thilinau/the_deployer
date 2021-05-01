package it.bridge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;


public class BundleReleases {
	
	public String BuildSingleRelease(ArrayList<Integer> DEP_REL_ARR){
		
		Boolean STATUS=true;
		String tempDir = "", RET="-1";
		
		while (true){

			tempDir = GetInfo.getRandomKey();
			if (GetInfo.createTempDirectory(tempDir)){
				break;
			}	
		}	
		
		ArrayList<String> SOURCE_DIRS = GetInfo.getSourceDirectories();
		
		for (int i=0;i<DEP_REL_ARR.size() ;i++){
			if (DEP_REL_ARR.get(i)!=0){
				
				String REL_DIR=GetInfo.getReleaseDirectory(DEP_REL_ARR.get(i));
				
				for (int e=0;e<SOURCE_DIRS.size() ;e++){
					if (checkArtifactsAvailability(REL_DIR,SOURCE_DIRS.get(e) )){
						if (!CopyArtifactsToTemp(REL_DIR,tempDir,SOURCE_DIRS.get(e))){
							STATUS=false;
							break;
						}
					}
				}				
			}			
		}
		
		if (STATUS == false){
			RET="-1";
		}else{
			RET=tempDir;
		}
		return RET;
	}
	
	public boolean checkArtifactsAvailability(String REL_DIR, String ARTIFACS_DIR){
		
		boolean status=false;
		
		String tmpDir=System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + GetInfo.getReleaseSource() + File.separator + REL_DIR + File.separator + ARTIFACS_DIR;
		File theDir = new File(tmpDir);

		if (theDir.exists()) {
			status=true;
		}
		
		return status;
	}
	
	public boolean CopyArtifactsToTemp(String REL_DIR,String TEMP_DIR, String ARTIFACS_DIR ){
		
		boolean status=true;
		String artifacsDir = System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + GetInfo.getReleaseSource() + File.separator + REL_DIR + File.separator + ARTIFACS_DIR;
		String tmpDir = System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + "TEMP" + File.separator + TEMP_DIR;
		String tmpArtiDir=tmpDir + File.separator + ARTIFACS_DIR;
		File theDir = new File(tmpArtiDir);

		if (!theDir.exists()) {
			try{
				theDir.mkdir();
				status = true;
			} catch(SecurityException se){
				status = false;
				loggers.addLogEx("SEVE","Error in creating temporary directory" , se);
			}catch(Exception ex){
				status = false;
				loggers.addLogEx("SEVE","Error in creating temporary directory" , ex);
			}
		}
		
		if (status == true){
			
			File srcDir= new File(artifacsDir);
			File destDir= new File(tmpArtiDir);
			
			try{
				
				FileUtils.copyDirectory(srcDir, destDir);
				
			}catch(IOException e){
				status=false;
				loggers.addLogEx("SEVE","Error in copying artifacts to temporary directory" , e);
			}
		}
		
		return status;
	}

}
