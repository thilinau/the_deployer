
//Store Invalid Database Objects

package it.bridge.objects;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ParamDBInvalid {

	private String owner;
	private String object_type;
	private String object_name;
	private String table_name;
	private String last_ddl_time;
	
 	public ParamDBInvalid( String ow,String ot, String on,Date lst_ddl_tim)
    {
        this.owner = ow;
        this.object_type = ot;
        this.object_name = on;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        if (lst_ddl_tim != null)
        {
            this.last_ddl_time = simpleDateFormat.format( lst_ddl_tim );
        }
    }

 	public ParamDBInvalid( String ow,String ot, String on, String tb,Date lst_ddl_tim )
    {
        this.owner = ow;
        this.object_type = ot;
        this.object_name = on;
        this.table_name = tb;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        if (lst_ddl_tim != null)
        {
            this.last_ddl_time = simpleDateFormat.format( lst_ddl_tim );
        }
    }

	public String getOwner()
    {
        return owner;
    }
    
    public String getObjectType()
    {
        return object_type;
    }
    
    public String getObjectName()
    {
        return object_name;
    }

	public String getTable_name() {
		return table_name;
	}

    public String getLast_ddl_time() {
        return last_ddl_time;
    }

}
