package util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import core.SystemFailureException;

public class PropertyHandler {

	private static final String APP_NAME = "raston";
	
	private static Logger logger = Logger.getLogger(PropertyHandler.class);

	private static final Properties localProperties = getProperties();
	
	public static String getString(String name) {
		return localProperties.getProperty(name);
	}
	
	public static String getNonEmptyString(String name) throws SystemFailureException {
		String value = localProperties.getProperty(name);
		if (StringUtility.isNullOrEmpty(value)) throw new SystemFailureException("Could not get property '%s'", name);
		return value;
	}
	
	public static int getInt(String name, int defaultValue) {
		String rawValue = localProperties.getProperty(name);
		if (!StringUtility.isNullOrEmpty(rawValue)) {
			try {
				return Integer.parseInt(rawValue);
			} catch (NumberFormatException e) {}
		}
		return defaultValue;
	}
	public static int getInt(String name) throws SystemFailureException {
		String rawValue = localProperties.getProperty(name);
		try {
			return Integer.parseInt(rawValue);
		} catch (NumberFormatException e) {
			throw new SystemFailureException("Cannot convert property '%s' value '%s' to an integer.", name, rawValue);
		}
	}
	
	private static Properties getProperties() {
		try {
			//logger.debug("Loading properties file.");
			Properties defaults = new Properties();
			FileInputStream in = new FileInputStream(APP_NAME + ".properties");
			defaults.load(in);
			in.close();
			String loggedInUser = System.getProperty("user.name");
			File overrideFile = new File(APP_NAME + "." + loggedInUser + ".properties");
			if (overrideFile.exists()) {
				//logger.debug("Loading override properties file.");
				Properties overrides = new Properties(defaults);
				in = new FileInputStream(overrideFile);
				overrides.load(in);
				in.close();
				return overrides;
			} else {
				return defaults;
			}
		} catch (Exception e) {
			logger.error("Failed to load properties file", e);
			return null;
		}
	}
}
