package it.bridge.database;

import java.util.List;
import java.util.Map;

public class RunnerParameters
{

    private String schemaUser; //TBX/DBAUDIT/CACHE
    private String schemaPassword;
    private String host;
    private String sid;        //cgv4/fdmdb
    private String port;
    private String environment;  //V4QA65/FDMQA
    private String projectCode;
    boolean audit;
	private Map<String, List<Pare>> SCR_REP_MAP ;
    
    public String _getUserName()
    {
        if(environment.equalsIgnoreCase( "DEFAULT" ) )
        {
            if(schemaUser.equals( "DBAUDIT" ) )
            {
                return "DB_AUDIT";
            }
            return schemaUser;
        }
        return schemaUser + environment;
    }
    
    public RunnerParameters()
    {
    	
    }
    
    public RunnerParameters( String schemaUser,String schemaPass, String host, String sid, String port, String environment )
    {
        this.schemaUser = schemaUser;
        this.schemaPassword = schemaPass;
        this.host = host;
        this.sid = sid;
        this.port = port;
        this.environment = environment;
    }
    
    public RunnerParameters( String schemaUser, String schemaPass,String host, String sid, String port, String environment,String projectCode )
    {
        this.schemaUser = schemaUser;
        this.schemaPassword = schemaPass;
        this.host = host;
        this.sid = sid;
        this.port = port;
        this.environment = environment;
        this.projectCode = projectCode;
    }

    public String getSchemaUser()
    {
        return schemaUser;
    }

    public void setSchemaPassword( String schemaPass )
    {
        this.schemaPassword = schemaPass;
    }

    
    public String getSchemaPassword()
    {
        return schemaPassword;
    }
    
    public void setSchemaUser( String schemaUser )
    {
        this.schemaUser = schemaUser;
    }
    
    public String getHost()
    {
        return host;
    }

    public void setHost( String host )
    {
        this.host = host;
    }

    public String getSid()
    {
        return sid;
    }

    public void setSid( String sid )
    {
        this.sid = sid;
    }

    public String getPort()
    {
        return port;
    }

    public void setPort( String port )
    {
        this.port = port;
    }

    public String getEnvironment()
    {
        return environment;
    }

    public void setEnvironment( String environment )
    {
        this.environment = environment;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public void setProjectCode( String projectCode )
    {
        this.projectCode = projectCode;
    }

    public boolean isAudit()
    {
        return audit;
    }

    public void setAudit( boolean audit )
    {
        this.audit = audit;
    }

    @Override public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append( "RunnerParameters" );
        sb.append( "{schemaUser='" ).append( schemaUser ).append( '\'' );
        sb.append( ", host='" ).append( host ).append( '\'' );
        sb.append( ", sid='" ).append( sid ).append( '\'' );
        sb.append( ", port='" ).append( port ).append( '\'' );
        sb.append( ", environment='" ).append( environment ).append( '\'' );
        sb.append( ", projectCode='" ).append( projectCode ).append( '\'' );
        sb.append( ", audit=" ).append( audit );
        sb.append( '}' );
        return sb.toString();
    }

	public Map<String, List<Pare>> getSCR_REP_MAP() {
		return SCR_REP_MAP;
	}

	public void setSCR_REP_MAP(Map<String, List<Pare>> sCR_REP_MAP) {
		SCR_REP_MAP = sCR_REP_MAP;
	}
}
