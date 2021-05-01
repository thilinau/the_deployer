package it.bridge.database;

import java.io.File;
import java.io.IOException;

import org.dbmaintain.database.Database;
import org.dbmaintain.database.DatabaseInfo;
import org.dbmaintain.database.Databases;
import org.dbmaintain.script.Script;
import org.dbmaintain.script.runner.impl.Application;
import org.dbmaintain.script.runner.impl.SqlPlusScriptRunner;
import org.dbmaintain.util.DbMaintainException;

import it.bridge.loggers;

import static java.lang.System.currentTimeMillis;
import static org.dbmaintain.util.FileUtils.createFile;


public class CustomSqlPlusScriptRunner extends SqlPlusScriptRunner
{
    String output;
    String logname;
    
    public CustomSqlPlusScriptRunner( Databases databases, String sqlPlusCommand )
    {
        super( databases, sqlPlusCommand );
    }

    @Override
    protected void executeScript( File scriptFile, Database targetDatabase ) throws Exception
    {
        File wrapperScriptFile = generateWrapperScriptFile( targetDatabase.getDatabaseInfo(), scriptFile );
        String[] arguments = {"/nolog", "@" + wrapperScriptFile.getPath()};
        Application.ProcessOutput processOutput = application.executeScr( logname,arguments);    
        output = processOutput.getOutput();

    }

    
    public void execScript(Script script, String logName) {
        
    	this.logname= logName;
    	
    	try {

            Database targetDatabase = getTargetDatabaseDatabase(script);
            if (targetDatabase == null) {
                loggers.addLog("INFO","Script " + script.getFileName() + " has target database " + script.getTargetDatabaseName() + ". This database is disabled, so the script is not executed.");
                return;
            }

            File scriptFile = createTemporaryScriptFile(script);
            executeScript(scriptFile, targetDatabase);

        } catch (Exception e) {
            throw new DbMaintainException("Error executing script " + script.getFileName(), e);
        }
    }
    
    
    @Override protected String getDatabaseConfigFromJdbcUrl( String url )
    {

        String ipPortSidString = super.getDatabaseConfigFromJdbcUrl( url );
        String[] strings = ipPortSidString.split( ":" );
        StringBuilder sb = new StringBuilder();
       
        if (System.getProperty("enable.tns").equals("true")) {
        	sb.append(strings[0]);
        }else {
	    	sb.append( "(DESCRIPTION =(ADDRESS= (PROTOCOL = TCP)" ).
	        append( "(HOST=" ).append( strings[0] ).append( " )" ).
	        append( "(PORT = " ).append( strings[1] ).append( "))" ).
	    	append( "(CONNECT_DATA=(SID = " ).append( strings[2] ).append( ")))" );
        }
        
        return sb.toString();
    }

    public String getOutput()
    {
        return output;
    }
    
    protected File generateWrapperScriptFile(DatabaseInfo databaseInfo, File targetScriptFile) throws IOException
    {
        boolean FLAG_LOCAL_AUTH = false;
        if (System.getProperty("db_script_run_with_local_authentication").equalsIgnoreCase("true")){
            FLAG_LOCAL_AUTH = true;
        }

        File temporaryScriptsDir = createTemporaryScriptsDir();
        File temporaryScriptWrapperFile = new File(temporaryScriptsDir, "wrapper-" + currentTimeMillis() + targetScriptFile.getName());
        temporaryScriptWrapperFile.deleteOnExit();

        String lineSeparator = System.getProperty("line.separator");
        StringBuilder content = new StringBuilder();
        content.append("set echo off");
        content.append(lineSeparator);
        //content.append("whenever sqlerror exit sql.sqlcode rollback");
        content.append("whenever sqlerror continue none");
        content.append(lineSeparator);
        content.append("whenever oserror exit sql.sqlcode rollback");
        content.append(lineSeparator);
        content.append("connect ");

        if (FLAG_LOCAL_AUTH == true){
            content.append('/');
            content.append('@');
            content.append(databaseInfo.getUserName());
        }else{
            content.append(databaseInfo.getUserName());
            content.append('/');
            content.append('"' + databaseInfo.getPassword() + '"');
            content.append('@');
            content.append(getDatabaseConfigFromJdbcUrl(databaseInfo.getUrl()));
        }

        content.append(lineSeparator);
        content.append("alter session set current_schema=");
        content.append(databaseInfo.getDefaultSchemaName());
        content.append(";");
        content.append(lineSeparator);
        content.append("ALTER SESSION SET ddl_lock_timeout=60;");
        content.append(lineSeparator);
        content.append("set echo on");
        content.append(lineSeparator);
        content.append("SELECT systimestamp ||' - ' || GLOBAL_NAME ||' - ' || sys_context('userenv','instance_name') from dual, global_name;");
        content.append(lineSeparator);     
        content.append("@@");
        //content.append(targetScriptFile.getName());
        content.append(targetScriptFile.getAbsolutePath());
        content.append(lineSeparator);
        content.append("exit sql.sqlcode");
        content.append(lineSeparator);
        createFile(temporaryScriptWrapperFile, content.toString());
        return temporaryScriptWrapperFile;
    }
}
