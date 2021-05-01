package it.bridge.database;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;


@XmlType(name = "CGError", namespace="http://codegen.it")
public class CGError<T> implements Serializable
{

	private static final long serialVersionUID = 1L;
	/** 1 */
    public static final int SUCCESS = 1;
    /** -1 */
    public static final int ERROR = -1;
    /** 0 */
    public static final int WARNING =   0;
    /** -2 */
    public static final int SESSION_EXPIRED = -2;

    private long no;
	private String msg;
    private T returnData;
    public static final long UNKNOWN_ERROR = -1;

   
    public  CGError()
	{
    	
	}

    public CGError( long no, String msg )
    {
        this.no = no;
        this.msg = msg;
    }

    public CGError( String msg, long no )
    {
        this.msg = msg;
        this.no = no;
    }

    public CGError( long no, String msg, T returnData )
    {
        this.no = no;
        this.msg = msg;
        this.returnData = returnData;
    }

    public String getMsg()
	{
		return msg;
	}
	public void setMsg(String msg)
	{
		this.msg = msg;
	}

    public T getReturnData()
    {
        return returnData;
    }

    public void setReturnData( T returnData )
    {
        this.returnData = returnData;
    }

    public long getNo()
    {
        return no;
    }

    public void setNo( long no )
    {
        this.no = no;
    }

    public boolean _isSuccess()
    {
        return this.no == CGError.SUCCESS;
    }

    public boolean _isError()
    {
        return this.no == CGError.ERROR;
    }

    public boolean _isWarning()
    {
        return this.no == CGError.WARNING;
    }

    public String _getErrorStatus()
    {
        if( no == CGError.SUCCESS )
        {
            return "SUCCESS";
        }
        if( no == CGError.ERROR )
        {
            return "ERROR";
        }
        if( no == CGError.WARNING )
        {
            return "WARNING";
        }
        return "" + no;
    }
}
