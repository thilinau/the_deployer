package it.bridge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
	
	public static final String AES = "AES";
	
	public static String DecryptPassword(String user, String password) 
	{
		String base_dir = System.getProperty("BASE_DIR") + File.separator + "conf"  + File.separator + System.getProperty("hostname")  ;
		String key_file = GetInfo.getConfig("DB_PASSWORD_KEY_FILE");
			
		if (key_file != "-1")
		{
			
			try{
				SecretKeySpec sks = getSecretKeySpec(new File(base_dir + File.separator + key_file));
				Cipher cipher = Cipher.getInstance(CryptoUtils.AES);
				cipher.init(Cipher.DECRYPT_MODE, sks);
				byte[] decrypted = cipher.doFinal(hexStringToByteArray(password));
				return new String(decrypted);
			}
			catch(Exception e){
				
				loggers.addLogAndPrint("SERV","DB Schema " + user + " Password decrypion | FAILED", false);
				loggers.addLogEx("SERV", e.getMessage(), e);
				return password;
			}
						
		}else{
			
			return password;
		}
				
			
	}

 	private static SecretKeySpec getSecretKeySpec(File keyFile) throws NoSuchAlgorithmException, IOException {
		 
	    byte [] key = readKeyFile(keyFile);
	    SecretKeySpec sks = new SecretKeySpec(key, CryptoUtils.AES);
	    return sks;
	    
 	}
 	
 	
	private static byte [] readKeyFile(File keyFile) throws FileNotFoundException  {
 		
	    Scanner scanner = new Scanner(keyFile);
	    scanner.useDelimiter("\\Z");
	    String keyValue = scanner.next();
	    scanner.close();
	    return hexStringToByteArray(keyValue);
	    
 	}


 	@SuppressWarnings("unused")
	private static String byteArrayToHexString(byte[] b){
 		
	    StringBuffer sb = new StringBuffer(b.length * 2);
	    for (int i = 0; i < b.length; i++){
	      int v = b[i] & 0xff;
	      if (v < 16) {
	        sb.append('0');
	      }
	      sb.append(Integer.toHexString(v));
	    }
	    
	    return sb.toString().toUpperCase();
 	}

 	
 	private static byte[] hexStringToByteArray(String s) {
 		
	    byte[] b = new byte[s.length() / 2];
	    for (int i = 0; i < b.length; i++){
	      int index = i * 2;
	      int v = Integer.parseInt(s.substring(index, index + 2), 16);
	      b[i] = (byte)v;
	    }
	    return b;
 	}
}
