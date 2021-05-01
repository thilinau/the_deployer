package it.bridge.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ParamFinalDBReport {
	
	private Map<String, ParamDBReport> errorReport;
	private boolean scriptStatus;
	private String zipFilePath;
	private ArrayList<ParamDBInvalid> rs_before_rel;
	private ArrayList<ParamDBInvalid> rs_after_rel;
	private ArrayList<ParamDBInvalid> rs_disabled_obj_after_rel;
	private HashMap<String, String> HOLD_VER = new HashMap<>() ;
	
	public ParamFinalDBReport( Map<String, ParamDBReport> dbRep,boolean status, String zipPath )
    {
        this.errorReport = dbRep;
        this.scriptStatus = status;
        this.zipFilePath = zipPath;

    }
 	
    public Map<String, ParamDBReport> getErrorRep()
    {
        return errorReport;
    }
    
    public boolean getStatus()
    {
        return scriptStatus;
    }
    
    public String getZipFile()
    {
        return zipFilePath;
    }
    
    public void setInvalidBefore( ArrayList<ParamDBInvalid> al )
    {
        this.rs_before_rel = al;
    }
    
    public void setInvalidAfter( ArrayList<ParamDBInvalid> al )
    {
        this.rs_after_rel = al;
    }
    
    
    public ArrayList<ParamDBInvalid> getInvalidBefore()
    {
        return rs_before_rel;
    }
    
    public ArrayList<ParamDBInvalid> getInvalidAfter()
    {
        return rs_after_rel;
    }

	public ArrayList<ParamDBInvalid> getRs_disabled_obj_after_rel() {
		return rs_disabled_obj_after_rel;
	}

	public void setRs_disabled_obj_after_rel(ArrayList<ParamDBInvalid> rs_disabled_obj_after_rel) {
		this.rs_disabled_obj_after_rel = rs_disabled_obj_after_rel;
	}

	public HashMap<String, String> getHOLD_VERSIONS() {
		return HOLD_VER;
	}

	public void setHOLD_VERSIONS(HashMap<String, String> hOLD_VERSIONS) {
		this.HOLD_VER = hOLD_VERSIONS;
	}
    
}
