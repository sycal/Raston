package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.rowset.CachedRowSet;

import org.apache.log4j.Logger;

//import com.sun.rowset.CachedRowSetImpl;

public class DatabaseUtility {
	
	private static Logger logger = Logger.getLogger(DatabaseUtility.class);
	
	private DatabaseUtility() {}

	public static void checkJdbcDriver(String connectionString) {
		try {
			if (connectionString.contains("postgresql")) {
				Class.forName("org.postgresql.Driver");
			} else if (connectionString.contains("jtds:sqlserver")) {
				Class.forName("net.sourceforge.jtds.jdbc.Driver");
			}
		} catch (ClassNotFoundException e) {
			logger.error(String.format("Failed to load driver for '%s'", connectionString), e);
		}
	}
	
	public static int executeUpdateStatement(String connectionString, String sql) throws SQLException {
		checkJdbcDriver(connectionString);
		Connection con = null;
		Statement stmt = null;
		try {
			con = DriverManager.getConnection(connectionString);
			//logger.debug(String.format("Connected to %s", con.getMetaData().getDatabaseProductName()));
			stmt = con.createStatement();
			return stmt.executeUpdate(sql);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if (con != null) {
				con.close();
			}
		}
	}
	
	public static CachedRowSet executeSelectStatement(String connectionString, String sql) throws SQLException {
		checkJdbcDriver(connectionString);
		Connection con = null;
		Statement stmt = null;
		try {
			con = DriverManager.getConnection(connectionString);
			//logger.debug(String.format("Connected to %s", con.getMetaData().getDatabaseProductName()));
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			CachedRowSet crs = createCachedRowSet();
			crs.populate(rs);
			return crs;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if (con != null) {
				con.close();
			}
		}
	}
	
	public static String[] getRowData(CachedRowSet row, String... columnLabels) throws SQLException {
		String[] result = new String[columnLabels.length];
		for (int i=0; i<columnLabels.length; i++) {
			String columnLabel = columnLabels[i];
			result[i] = row.getString(columnLabel);
		}
		return result;
	}
	
	public static String[] getColumnLabels(CachedRowSet row) throws SQLException {
		ResultSetMetaData metadata = row.getMetaData();
		String[] columnLabels = new String[metadata.getColumnCount()];
		int columnIndex = 0;
		for (int c=1; c<=metadata.getColumnCount(); c++) {
			columnLabels[columnIndex++] = metadata.getColumnLabel(c);
		}
		return columnLabels;
	}
	
    private static CachedRowSet createCachedRowSet() throws SQLException {
	    try {
	        return (CachedRowSet) Class.forName("com.sun.rowset.CachedRowSetImpl").newInstance();
	    }
	    catch (Exception e) {
	        throw new SQLException("Could not create a CachedRowSet", e);
	    }
    }
}
