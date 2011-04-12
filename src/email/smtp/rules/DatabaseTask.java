package email.smtp.rules;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import email.EmailAddress;
import email.Envelope;

import util.DatabaseUtility;
import util.SqlUtility;
import util.StringUtility;

public class DatabaseTask extends SmtpRuleTaskBase implements ISmtpRuleTask {
	
	private static Logger logger = Logger.getLogger(DatabaseTask.class);
	
	private String connectionString;
	private String tableName;
	private ArrayList<Mapping> fieldMappingList;

	@Override
	public void execute(Envelope envelope, Hashtable<String, List<EmailAddress>> emailGroups) {
		this.setTokenMatchingObjects(envelope, emailGroups);
		try {
			String sql = this.buildSql();
			DatabaseUtility.executeUpdateStatement(this.connectionString, sql);
		} catch (SQLException e) {
			logger.error(String.format("Failed to write to database for email-id '%s'", envelope.getEmail().getId()), e);
		}
	}

	@Override
	public void initialise(Element taskElement) {
		this.connectionString = taskElement.getAttribute("connectionString");
		this.tableName = taskElement.getAttribute("tableName");
		this.fieldMappingList = new ArrayList<Mapping>();
		NodeList nodeList = taskElement.getElementsByTagName("fieldMapping");
		for (int i=0; i<nodeList.getLength(); i++) {
			Element mappingElement = (Element)nodeList.item(i);
			Mapping newMapping = new Mapping(mappingElement.getAttribute("field"), mappingElement.getAttribute("source"));
			this.fieldMappingList.add(newMapping);
		}
	}
	
	private String buildSql() {
		StringBuilder sql = new StringBuilder();
		sql.append(String.format("insert into %s (", this.tableName));
		for (int i=0; i<this.fieldMappingList.size(); i++) {
			if (i > 0) sql.append(", ");
			String fieldName = this.fieldMappingList.get(i).fieldName;
			sql.append(StringUtility.doubleQuote(fieldName));
		}
		sql.append(") values (");
		for (int i=0; i<this.fieldMappingList.size(); i++) {
			if (i > 0) sql.append(',');
			String sourceValue = this.replaceTokens(this.fieldMappingList.get(i).source);
			sql.append(SqlUtility.stringQuote(sourceValue));
		}
		sql.append(");");
		return sql.toString();
	}
	
	private class Mapping
	{
		public String fieldName;
		public String source;
		
		public Mapping(String fieldName, String source) {
			this.fieldName = fieldName;
			this.source = source;
		}
	}
}
