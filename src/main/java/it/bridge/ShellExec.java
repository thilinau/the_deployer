package it.bridge;

import java.util.ArrayList;

import it.bridge.objects.HostAuthentication;
import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.IOptionName;
import net.neoremind.sshxcute.core.Logger;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

public class ShellExec {
	

	static SSHExec sshDeployer = null;
	static String ITEMS;
	static String username;
	static String password;
	static String authmode;
	static String keyfile;
	static String connect_ip;
	static int port = 22;
	static String category;
	static String artifact_path;
	

	public boolean connect(String CATEGORY, String ARTIFACT, String PATH, HostAuthentication hA){

		boolean RET_STATUS = true;
		
		ITEMS=ARTIFACT;
		
		connect_ip=hA.getHOSTNAME();
		authmode=hA.getAUTH_TYPE();
		
		if (authmode.equals("userpass")){
			username = hA.getUSERNAME();
			password=hA.getPASSWORD();
		}else if(authmode.equals("keyfile")){
			keyfile= hA.getKEYFILE();
			username = hA.getUSERNAME();
			password=hA.getPASSWORD();
		}
		
		port =Integer.parseInt( hA.getPORT());
		category = CATEGORY;
		artifact_path = PATH;
		
		try{

			SSHExec.setOption(IOptionName.SSH_PORT_NUMBER, port);
			ConnBean cb = null;
			
			if (authmode.equals("userpass")){
				
				cb = new ConnBean(connect_ip, username,password);
				
			}else if(authmode.equals("keyfile")){

				cb = new ConnBean(connect_ip, username,password,keyfile);
			}
			
			
			sshDeployer = SSHExec.getInstance(cb); 
			
			Logger log = Logger.getLogger();
			log.setThreshold(Logger.ERROR);
			
			sshDeployer.connect();
			
		}catch(Exception e){
			
			loggers.addLogAndPrint("INFO","MD5-" + category + " : Connection for " + connect_ip + " | FAILED | " ,false);
			RET_STATUS=false;
		}
		
		return RET_STATUS;
	}
	
	
	public boolean disconnect(){
		boolean RET_STATUS = true;
		
		try{
			sshDeployer.disconnect();
		}catch(Exception e){
			RET_STATUS=false;
		}
		
		return RET_STATUS;
	}
	
	
	public String[] CommandInterface(){
		
		ArrayList<String> build_command ;
		String[] retMD5 = null;
		
		build_command = new ArrayList<String>();
		build_command.add("cd " + artifact_path);
		build_command.add("md5sum " + ITEMS);
		
		try{
			
			String res= CommandExec(build_command);
			
			if ( !res.isEmpty()){
				retMD5= res.split("\\s+");
			}
			
		}catch (Exception e){
			e.printStackTrace();
			loggers.addLogAndPrint("INFO","MD5-EXC : " + ITEMS + " | FAILED" ,false);
			loggers.addLogEx("INFO", e.getMessage(), e);
		}
		
		return retMD5;

	}
	
	
	private String CommandExec(ArrayList<String> commands) throws TaskExecFailException{
		
		String RET="";
		
		String[] exc_cmd = new String[commands.size()];
		exc_cmd = commands.toArray(exc_cmd);
		
		CustomTask csTsk = new ExecCommand(exc_cmd);
		Result res = sshDeployer.exec(csTsk);

		if (res.isSuccess)
		{
			RET= res.sysout;
		}

		else
		{
			loggers.addLogAndPrint("INFO","MD5-EXC : " + res.rc + " | " + res.error_msg ,false);
		}
		
		return RET;
	}
}
