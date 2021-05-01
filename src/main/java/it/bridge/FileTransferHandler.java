package it.bridge;

import java.io.File;

import com.jscape.inet.sftp.Sftp;
import com.jscape.inet.sftp.SftpConfiguration;
import com.jscape.inet.sftp.SftpException;
import com.jscape.inet.ssh.util.SshParameters;
import it.bridge.objects.HostAuthentication;

public class FileTransferHandler {
	
	Sftp ftp;
	
	private SftpConfiguration getSftpConfiguration() {
		
		SftpConfiguration configuration = new SftpConfiguration();
		configuration.getSshConfiguration().getTransportConfiguration().getAlgorithmFactory().addCipher( "aes128-cbc", "AES/CBC/NoPadding", 16 );
		configuration.getSshConfiguration().getTransportConfiguration().getAlgorithmFactory().addCipher( "aes192-cbc", "AES/CBC/NoPadding", 24 );
		configuration.getSshConfiguration().getTransportConfiguration().getAlgorithmFactory().addCipher( "aes256-cbc", "AES/CBC/NoPadding", 32 );
		configuration.getSshConfiguration().getTransportConfiguration().getAlgorithmFactory().addCipher( "aes128-ctr", "AES/CTR/NoPadding", 16 );
		configuration.getSshConfiguration().getTransportConfiguration().getAlgorithmFactory().addCipher( "aes192-ctr", "AES/CTR/NoPadding", 24 );
		configuration.getSshConfiguration().getTransportConfiguration().getAlgorithmFactory().addCipher( "aes256-ctr", "AES/CTR/NoPadding", 32 );
		
		return configuration;

	}
	
	public void SFTPHandler(HostAuthentication haAuth)
	{
		if (haAuth.getAUTH_TYPE().equals("userpass")){
			
			ftp = new Sftp( new SshParameters( haAuth.getHOSTNAME(),Integer.parseInt(haAuth.getPORT()), haAuth.getUSERNAME(), haAuth.getPASSWORD()),getSftpConfiguration());
		
		}else if(haAuth.getAUTH_TYPE().equals("keyfile")){
			
			try{
				
				File privateKeyFile =  new File(haAuth.getKEYFILE());
				ftp = new Sftp( new SshParameters( haAuth.getHOSTNAME(),Integer.parseInt(haAuth.getPORT()), haAuth.getUSERNAME(), haAuth.getPASSWORD(),privateKeyFile),getSftpConfiguration());

			}
			catch (Exception fe)
			{
				loggers.addLog("SEVE","Keyfile or Passphrase Error : " + fe.getMessage());
			}
		}
	}
	
	public void SFTPHandler( String host, int port, String loginName, String password, String privateKey )
	{
		if (privateKey.equals("-1"))
		{
			ftp = new Sftp( new SshParameters( host, port, loginName, password ),getSftpConfiguration() );
		}
		else
		{
			try
			{
				File privateKeyFile =  new File(privateKey);
				ftp = new Sftp( new SshParameters( host, port, loginName, password,privateKeyFile),getSftpConfiguration() );

			}
			catch (Exception fe)
			{
				loggers.addLog("SEVE","Key file or Passphrase Error : " + fe.getMessage());
			}
		}
	}
	
	public boolean isConnected()
	{
		boolean status = false;
		
		try
		{
			status= ftp.isConnected();
		}
		catch (Exception e)
		{
			status=false;
			loggers.addLogEx("SEVE","isConnected : " + e.getMessage() , e);
		}
		
		return status;
	}
	
	public void connect() 
	{
		try{
			ftp.connect();
		}
		catch (Exception e)
		{
			loggers.addLogEx("SEVE","ftp.connect : " + e.getMessage() , e);
		}
	}
		
	public void disconnect() throws Exception
	{
		ftp.disconnect();
	}
	
	public boolean deleteFile( String path ) 
	{
		boolean FLG_SUCESS=true;
		
		try{
			
			ftp.deleteFile( path );
			
		}catch(Exception e){
			
			FLG_SUCESS=false;
			loggers.addLogEx("SEVE","deleteFile : " + e.getMessage() , e);
		}
		
		return FLG_SUCESS;
	}
	
	public boolean deleteDir( String path , String fileName ) 
	{
		boolean FLG_SUCESS=true;
		
		try{
			ftp.setDir(path);
			
			if (ftp.isValidPath(path + File.separator + fileName )){
				ftp.deleteDir(fileName,true );
			}
			

		}catch(Exception e){
			
			FLG_SUCESS=false;
			loggers.addLogEx("SEVE","deleteDir : " + e.getMessage() , e);
		}
		
		return FLG_SUCESS;
	}
	
    public boolean  downloadFile( String localDir, String remoteDir,String fileName ) throws SftpException
    {
    	boolean FLG_SUCESS=true;
    	
    	if ( ftp.isValidPath(remoteDir) ){
    	
			try
			{
				ftp.setDir(remoteDir);
				
				File localPath = new File (localDir);
				if ( !localPath.exists() ){
					localPath.mkdir();
				}
				
				ftp.setLocalDir( new File( localDir ) );				
				ftp.setAuto( true );
				ftp.download( fileName );
				
			}
			catch ( Exception e )
			{
				FLG_SUCESS=false;
				loggers.addLogEx("SEVE","downloadFile : " + e.getMessage() , e);
			}
    	}else{
    		FLG_SUCESS=false;
    		loggers.addLog("SEVE","Invalid remote path " + remoteDir );
    	}
    	
    	return FLG_SUCESS;
    }
    
    
    public boolean  uploadFile( String localDir, String remoteDir,String fileName ) throws SftpException{
    	
    	boolean FLG_SUCESS=true;
    	
    	if ( ftp.isValidPath(remoteDir) ){
        	
			try
			{
				ftp.setDir(remoteDir);
				ftp.setLocalDir( new File( localDir ) );				
				ftp.setAuto( true );
				ftp.upload(fileName);
				
				try{
					
					String FILE_PERMISSION =GetInfo.getConfig("SET_DEP_FILE_PERMISSION");
					
					if ( FILE_PERMISSION.equals("-1")){	
						FILE_PERMISSION = "755";
					}
									
					ftp.setFilePermissions( fileName, Integer.parseInt(FILE_PERMISSION));

				}catch( Exception ex ){
					
					loggers.addLog("SEVE","setFilePermissions Failed for " + fileName );
					loggers.addLogEx("SEVE","setFilePermissions : " + ex.getMessage() , ex);

				}
								
			}catch ( Exception e ){
				
				FLG_SUCESS=false;
				loggers.addLogEx("SEVE","uploadFile : " + e.getMessage() , e);
			}
			
    	}else{
    		loggers.addLog("SEVE","RemoteDirectory Validation Failed : " + remoteDir );
    		FLG_SUCESS=false;
    	}
    	
    	return FLG_SUCESS;
    }
    
}
