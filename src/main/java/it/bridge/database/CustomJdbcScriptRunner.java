package it.bridge.database;

import java.util.Map;

import org.dbmaintain.database.Databases;
import org.dbmaintain.database.SQLHandler;
import org.dbmaintain.script.parser.ScriptParserFactory;
import org.dbmaintain.script.runner.impl.JdbcScriptRunner;

public class CustomJdbcScriptRunner extends JdbcScriptRunner {

	public CustomJdbcScriptRunner(Map<String, ScriptParserFactory> databaseDialectScriptParserFactoryMap,
			Databases databases, SQLHandler sqlHandler) {
		super(databaseDialectScriptParserFactoryMap, databases, sqlHandler);

	}

}
