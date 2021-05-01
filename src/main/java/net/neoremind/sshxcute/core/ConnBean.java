package net.neoremind.sshxcute.core;

/**
 * This is a Javabean class that used to construct a connection. It stores host, usernamd and password for a connection.
 * <br>
 * The three parameters has getter and setter method.
 * 
 * @author zxucdl
 *
 */
public class ConnBean {

	private String host;	
	private String user;	
	private String password;
	private String private_key=null;
	
	
	public ConnBean(String host, String user, String password){
		this.host = host;
		this.password = password;
		this.user = user;
	}
	
	public ConnBean(String host, String user, String password, String private_key){
		this.host = host;
		this.password = password;
		this.user = user;
		this.private_key=private_key;
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPrivateKey() {
		return private_key;
	}
	
	public void setPrivateKey(String private_key)
	{
		this.private_key= private_key;
	}
	
}

