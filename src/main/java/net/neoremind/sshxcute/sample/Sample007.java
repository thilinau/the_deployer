package net.neoremind.sshxcute.sample;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

public class Sample007 {

	public static void main(String[] args) throws TaskExecFailException {
		SSHExec ssh = null;
		try {
			
			ConnBean cb = new ConnBean("192.168.1.170", "archers","unidep2015","E:\\Personal\\java_workspace\\universal_deployer\\conf\\KEYS\\withPassphrase\\openkey.ppk");
			ssh = SSHExec.getInstance(cb);		
			ssh.connect();
			CustomTask echo = new ExecCommand("date");
			Result res = ssh.exec(echo);
			if (res.isSuccess)
			{
				System.out.println("Return code: " + res.rc);
				System.out.println("sysout: " + res.sysout);
			}
			else
			{
				System.out.println("Return code: " + res.rc);
				System.out.println("error message: " + res.error_msg);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			ssh.disconnect();	
		}
	}

}
