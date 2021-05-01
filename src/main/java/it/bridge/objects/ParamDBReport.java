package it.bridge.objects;

import java.util.Map;
import java.util.TreeMap;

public class ParamDBReport {
 	private String releaseRef;
 	private String scriptName;
 	private Map<Integer, String> scriptErros = new TreeMap<Integer, String>();

 	public ParamDBReport( String release,String script, Map<Integer, String> error )
    {
        this.releaseRef = release;
        this.scriptName = script;
        this.scriptErros = error;

    }
 	
    public void setReleaseRef( String relRef )
    {
        this.releaseRef = relRef;
    }
 	
    public String getReleaseRef()
    {
        return releaseRef;
    }
    
    public void setScript( String script )
    {
        this.scriptName = script;
    }
 	
    public String getScriptName()
    {
        return scriptName;
    }
    
    public void setErrors( Map<Integer, String> errorList )
    {
        this.scriptErros = errorList;
    }
 	
    public Map<Integer, String> getErrors()
    {
        return scriptErros;
    }
}
