package it.bridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {
	
	public static String zipi(String[] srcFolders)   {
		
		String base_dir = System.getProperty("BASE_DIR") +  File.separator + "METERIALS" + File.separator + "TEMP";
		ZipOutputStream zip = null;
	    FileOutputStream fileWriter = null;
	    boolean noScript=true;
	    String tempZip = GetInfo.getRandomKey() + ".zip";
	    
	    try{
	    	
	    	fileWriter = new FileOutputStream(base_dir +  File.separator + tempZip );
		    zip = new ZipOutputStream(fileWriter);
		    
			for (int i = 0; i < srcFolders.length; i++){
				try {
					
					addFolderToZip("", srcFolders[i], zip);
					noScript=false;
					
				} catch (Exception e) {
					loggers.addLogAndPrint("INFO", "DB - logs compress - Failed", false);
					loggers.addLogEx("INFO", e.getMessage(), e);
				}
			}
			
			zip.flush();
		    zip.close();
		    
	    }catch(Exception e){
	    	
	    	loggers.addLogAndPrint("INFO", "DB - Creating log zip - Failed", false);
			loggers.addLogEx("INFO", e.getMessage(), e);
	    }
		    
		if (noScript == true){
			tempZip="";
		}
		return tempZip;
		
	}
	

	  static private void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception {
		  
		  File folder = new File(srcFile);
		  if (folder.isDirectory()) {
			  
			  addFolderToZip(path, srcFile, zip);
			  
		  } else {
			  
	    	 byte[] buf = new byte[1024];
	    	 int len;
	    	 FileInputStream in = new FileInputStream(srcFile);
	    	 zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
	    	 
	    	 while ((len = in.read(buf)) > 0) {
	    		 zip.write(buf, 0, len);
	    	 }
	    	 
	    	 in.close();
	    }
	  }

	  static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
		  
		  File folder = new File(srcFolder);

		  for (String fileName : folder.list()) {
		      if (path.equals("")) {
		        addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
		      } else {
		        addFileToZip(path + "/" + folder.getName(), srcFolder + "/" +   fileName, zip);
		      }
		  }
	  }
}
