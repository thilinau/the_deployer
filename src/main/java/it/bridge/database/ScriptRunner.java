package it.bridge.database;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static org.dbmaintain.database.StoredIdentifierCase.UPPER_CASE;
import static org.dbmaintain.datasource.SimpleDataSource.createDataSource;
import static org.dbmaintain.util.FileUtils.createFile;

import org.dbmaintain.database.Database;
import org.dbmaintain.database.DatabaseConnection;
import org.dbmaintain.database.DatabaseInfo;
import org.dbmaintain.database.Databases;
import org.dbmaintain.database.IdentifierProcessor;
import org.dbmaintain.database.SQLHandler;
import org.dbmaintain.database.impl.DefaultSQLHandler;
import org.dbmaintain.database.impl.OracleDatabase;
import org.dbmaintain.script.Script;
import org.dbmaintain.script.ScriptContentHandle;
import org.dbmaintain.script.ScriptFactory;
import org.dbmaintain.script.executedscriptinfo.ScriptIndexes;
import org.dbmaintain.script.qualifier.Qualifier;
import org.dbmaintain.util.FileUtils;

import it.bridge.DBExecute;
import it.bridge.GetInfo;
import it.bridge.loggers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

public class ScriptRunner {
	
	//private static final Logger logger = Logger.getAnonymousLogger();
	
	public CGError<String> apply( RunnerParameters parameters, File scriptFile, String logName )
    {
		loggers.addLogAndPrint("INFO","DB - " + parameters.toString(),false);
		loggers.addLogAndPrint("INFO", "DB - Applying Script - " + scriptFile.getAbsoluteFile(),false);

        CGError<String> cgError = new CGError<String>( CGError.SUCCESS, "SUCCESS" );
        String url = "jdbc:oracle:thin:@" + parameters.getHost() + ( parameters.getPort() == null ? ":1521:" : ( ":" + parameters.getPort() + ":" ) ) + parameters.getSid();

        if (System.getProperty("enable.tns").equals("true")){
        	url = "jdbc:oracle:thin:@" +  parameters.getSid();
        }
        
        DatabaseInfo databaseInfo = new DatabaseInfo( "oracle1", "oracle", "oracle.jdbc.driver.OracleDriver", url, parameters.getSchemaUser(), parameters.getSchemaPassword(), asList( parameters._getUserName() ), false, true );
        DataSource dataSource = createDataSource( databaseInfo );
        SQLHandler sqlHandler = new DefaultSQLHandler();
        DatabaseConnection databaseConnection = new DatabaseConnection( databaseInfo, sqlHandler, dataSource );
        IdentifierProcessor identifierProcessor = new IdentifierProcessor( UPPER_CASE, "\"", databaseInfo.getDefaultSchemaName() );
        Database database = new OracleDatabase( databaseConnection, identifierProcessor );
        Databases databases = new Databases( database, asList( database ), new ArrayList<String>() );
        CustomSqlPlusScriptRunner defaultScriptRunner = new CustomSqlPlusScriptRunner( databases,  GetInfo.getOracleClientPath() );
        File tempFile;

        try
        {
            tempFile = createTemporaryScriptFile( scriptFile );
        }
        catch( IOException e )
        {
            e.printStackTrace();
            cgError.setNo( CGError.ERROR );
            cgError.setMsg( e.getMessage() );
            return cgError;
        }

    	String db_script_replacer = "FALSE";
    	if (!(db_script_replacer = GetInfo.getConfig("DB_SCRIPT_REPLACE_ENABLE")).equalsIgnoreCase("TRUE")){
    		db_script_replacer = "FALSE";
    	}
        
    	if ( db_script_replacer.equalsIgnoreCase("TRUE")){
    		
    		loggers.addLogAndPrint("INFO", "DB - Script regex replacer enabled",false);
    		
    		String tmpfileName = DBExecute.removeExtension(scriptFile.getName().toString());
    		List<String> break_file_name = new ArrayList<String>(Arrays.asList(tmpfileName.split("_")));
    		
    		List<Pare> pares = new ArrayList<Pare>();
    		
    		Map<String, List<Pare>> SCRIPT_REPLACE_MAP = parameters.getSCR_REP_MAP();
    		if (SCRIPT_REPLACE_MAP != null){
    			
    			pares = SCRIPT_REPLACE_MAP.get(break_file_name.get(1));
    			
    			if (pares != null){
    				
    				loggers.addLogAndPrint("INFO", "DB - Script regex replacer running - " + scriptFile.getName(),false);
    				
    				cgError = replaceAllContent( tempFile, pares );

    				if( cgError._isError() )
    				{
    					loggers.addLogAndPrint("INFO", "DB - replaceAllContent Error " + cgError.getMsg() , false);
		               	return cgError;
    				}
    			}
    		}
    	}
        
    	
    	
//        List<Pare> pares = new ArrayList<Pare>();
//
//        if( parameters.audit && !parameters.getEnvironment().equals( "DEFAULT" ) )
//        {
//            pares.add( new Pare( "\\bDB_AUDIT\\b", "DBAUDIT" + parameters.getEnvironment() ) );
//            pares.add( new Pare( "\\bTBX\\b", "TBX" + parameters.getEnvironment() ) );
//        }
//        if( !parameters.getEnvironment().equals( "DEFAULT" ) )
//        {
//            pares.add( new Pare( "\\bACC_SEARCH_CTX\\b", "ACC_SEARCH_CTX" + parameters.getEnvironment() ) );
//        }
//
//        cgError = replaceAllContent( tempFile, pares );
//
//        if( cgError._isError() )
//        {
//            logger.info( "replaceAllContent Error " + cgError.getMsg() );
//            return cgError;
//        }

    	
        Script script = createScript( tempFile );
        
        loggers.addLogAndPrint("INFO", "DB - Temp Script - " + tempFile.getAbsoluteFile() , false);
        
        try
        {
            defaultScriptRunner.execScript(script, logName); 
            //logger.info( defaultScriptRunner.getOutput() );
            //cgError.setReturnData( defaultScriptRunner.getOutput() );
        }
        catch( Exception e )
        {
            cgError.setNo( CGError.ERROR );
            cgError.setMsg( e.getMessage() );
            StringWriter errors = new StringWriter();
            e.printStackTrace( new PrintWriter( errors ) );
            cgError.setReturnData( errors.toString() );
            e.printStackTrace();
        }
        finally
        {
            sqlHandler.closeAllConnections();
        }
        return cgError;
    }
	

	
    private Script createScript( File file )
    {
        ScriptFactory scriptFactory = createScriptFactory();
        return scriptFactory.createScriptWithContent( file.getName(), 0L, new ScriptContentHandle.UrlScriptContentHandle( FileUtils.getUrl( file ), "ISO-8859-1", false ) );
    }
	
    private static ScriptFactory createScriptFactory()
    {
        return createScriptFactory( null );
    }
    
    private static ScriptFactory createScriptFactory( ScriptIndexes baseLineRevision )
    {
        return new ScriptFactory( "^([0-9]+)_", "(?:\\\\G|_)@([a-zA-Z0-9]+)_", "(?:\\\\G|_)#([a-zA-Z0-9]+)_", new HashSet<Qualifier>(), qualifiers( "patch" ), "postprocessing", baseLineRevision );
    }

    private static Set<Qualifier> qualifiers( String... qualifierNames )
    {
        Set<Qualifier> result = new HashSet<Qualifier>();
        for( String qualifierStr : qualifierNames )
        {
            result.add( new Qualifier( qualifierStr ) );
        }
        return result;
    }
    
    protected File createTemporaryScriptFile( File script ) throws IOException
    {
        File temporaryScriptsDir = createTemporaryScriptsDir();
        File temporaryScriptFile = new File( temporaryScriptsDir, getTemporaryScriptName( script ) );
        temporaryScriptFile.deleteOnExit();

        Reader scriptContentReader = new FileReader( script );
        try
        {
            createFile( temporaryScriptFile, scriptContentReader );
        }
        finally
        {
            scriptContentReader.close();
        }
        return temporaryScriptFile;
    }

    protected File createTemporaryScriptsDir()
    {
        String tempDir = System.getProperty( "java.io.tmpdir" );
        File temporaryScriptsDir = new File( tempDir, "dbmaintain_scriptrunner" );
        temporaryScriptsDir.mkdirs();
        return temporaryScriptsDir;
    }
    
    protected String getTemporaryScriptName( File script )
    {
        return currentTimeMillis() + script.getName();
    }
    
    protected CGError<String> replaceAllContent( File file, List<Pare> list )
    {
        CGError<String> cgError = new CGError<String>( CGError.SUCCESS, "SUCCESS" );
        try
        {
            BufferedReader reader = new BufferedReader( new FileReader( file ) );
            String line = "", oldText = "";
            while( ( line = reader.readLine() ) != null )
            {
                oldText += line + "\r\n";
            }
            reader.close();

            boolean found = false;
            for( Pare pare : list )
            {
                Pattern pattern = Pattern.compile( pare.getRegex() );
                Matcher m = pattern.matcher( oldText );
                if( m.find() )
                {
                    found = true;
                    break;
                }
            }
            if( !found )
            {
                return cgError;
            }
            String newText = oldText;
            for( Pare pare : list )
            {
                newText = newText.replaceAll( pare.getRegex(), pare.getReplacement() );
            }

            FileWriter writer = new FileWriter( file );
            writer.write( newText );
            writer.close();
        }
        catch( IOException e )
        {
            cgError.setNo( CGError.ERROR );
            cgError.setMsg( e.getMessage() );
            e.printStackTrace();
        }
        return cgError;
    }

}
