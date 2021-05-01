package it.bridge.objects;

public class HostAuthentication {

	private String AUTH_TYPE;
	private String USERNAME;
	private String PASSWORD;
	private String KEYFILE;
	private String HOSTNAME;
	private String PORT;
	
	public HostAuthentication() {
		// TODO Auto-generated constructor stub
	}

	public String getAUTH_TYPE() {
		return AUTH_TYPE;
	}

	public void setAUTH_TYPE(String aUTH_TYPE) {
		AUTH_TYPE = aUTH_TYPE;
	}

	public String getUSERNAME() {
		return USERNAME;
	}

	public void setUSERNAME(String uSERNAME) {
		USERNAME = uSERNAME;
	}

	public String getPASSWORD() {
		return PASSWORD;
	}

	public void setPASSWORD(String pASSWORD) {
		PASSWORD = pASSWORD;
	}

	public String getKEYFILE() {
		return KEYFILE;
	}

	public void setKEYFILE(String kEYFILE) {
		KEYFILE = kEYFILE;
	}

	public String getHOSTNAME() {
		return HOSTNAME;
	}

	public void setHOSTNAME(String hOSTNAME) {
		HOSTNAME = hOSTNAME;
	}

	public String getPORT() {
		return PORT;
	}

	public void setPORT(String pORT) {
		PORT = pORT;
	}

}
