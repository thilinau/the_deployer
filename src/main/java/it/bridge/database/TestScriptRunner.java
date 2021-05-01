package it.bridge.database;

import java.io.File;
import java.io.IOException;

public class TestScriptRunner {
	public static <T> void main( String[] args ) throws IOException
    {
        String scriptBase = "E:\\Personal\\java_workspace\\ScriptRunner\\test\\it\\codegen\\scriptrunner\\sql\\";

        ScriptRunner runner = new ScriptRunner();
        RunnerParameters parameters = new RunnerParameters( "TBX", "TBX","192.168.2.16", "archers11", "1521", "" );
        //File file = new File( scriptBase + "create_table_11_12.sql" );
        File file = new File( scriptBase + "accom_package.sql" );
		runner.apply( parameters, file,scriptBase + "accom_package.log" );
		
/*
        File scriptFile = new File( scriptBase + "1_ishara_SD100_T.sql" );
        File auditFile = new File( scriptBase + "DBAUDIT_2_1_ishara_SD100_T.sql" );
        File triggerFile = new File( scriptBase + "TBX_3_TRI_1_ishara_SD100_T.sql" );
        parameters.setSchemaUser( "DBAUDIT" );
        GeneratedScripts generatedScripts = new GeneratedScripts( scriptFile, auditFile, triggerFile );

        runner.apply( parameters, generatedScripts, true );
*/

    }
}
