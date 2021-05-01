package net.neoremind.sshxcute.core;

public class ConnBeanKeyPair {
	
	private String host;
	private String user;
	private String password;
	private String private_key;

	public ConnBeanKeyPair(String host, String user, String password, String private_key){
		this.host = host;
		this.password = password;
		this.user = user;
		this.private_key=private_key;
	}
	
	
	public String getPrivateKey() {
		return private_key;
	}
	
	public void setPrivateKey(String private_key)
	{
		this.private_key= private_key;
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
}
