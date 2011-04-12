package util;

import java.io.*;
import java.nio.channels.FileChannel;
import org.apache.log4j.Logger;

public class FileUtility {
	
	private static Logger logger = Logger.getLogger(FileUtility.class);

	public static boolean copy(String sourceFilePath, String destinationFilePath) {
		try {
	        FileChannel inChannel = new FileInputStream(sourceFilePath).getChannel();
		    FileChannel outChannel = new FileOutputStream(destinationFilePath).getChannel();
		    inChannel.transferTo(0, inChannel.size(), outChannel);
		    inChannel.close();
		    outChannel.close();
		    return true;
		} catch (IOException e) {
			logger.error(String.format("Failed to copy from '%s' to '%s'", sourceFilePath, destinationFilePath), e);
			return false;
		}
	}
	
	public static String getUnique(String filePath) {
		String testFilePath = filePath;
		int index = 1;
		while (exists(testFilePath)) {
			testFilePath = filePath + "_" + index++; 
		}
		return testFilePath;
	}
	
	public static String combinePath(String path1, String path2) {
		File f1 = new File(path1);
		File f2 = new File(f1, path2);
		return f2.getAbsolutePath();
	}
	
	public static boolean exists(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}
	
	public static String removeExtension(String filePath) {
		int index = filePath.lastIndexOf(".");
		if (index >= 0) {
			return filePath.substring(0, index);
		} else {
			return filePath;
		}
	}

	public static boolean saveObjectToFile(String filePath, Object objectToSave) {
		return saveAsciiFile(filePath, objectToSave.toString());
	}
	
	public static boolean saveAsciiFile(String filePath, String fileContents) {
		File file = new File(filePath);
		File folder = new File(file.getParent());
		if (!folder.exists()) folder.mkdirs();
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "US-ASCII"));
			out.write(fileContents);
			out.close();
			return true;
		} catch (IOException e) {
			logger.error(e);
		}
		return false;
	}
	
	public static String loadAsciiFile(String filePath) {
		return loadAsciiFile(filePath, null);
	}
	public static String loadAsciiFile(String filePath, String terminatorLine) {
		File file = new File(filePath);
		if (file.exists()) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "US-ASCII"));
				String line;
				StringBuilder result = new StringBuilder();
				while ((line = in.readLine()) != null) {
					if ((terminatorLine != null) && line.equalsIgnoreCase(terminatorLine)) {
						logger.debug("Terminator line detected.");
						break;
					}
					result.append(line);
					result.append(StringUtility.CRLF);
				}
				in.close();
				return result.toString();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return null;
	}
	
	public static String getFilename(String filePath) {
		File theFile = new File(filePath);
		return theFile.getName();
	}
	
	public static String[] listFiles(String folderPath, String suffix) {
		final String suffixLowerCase = suffix.toLowerCase();
		File folder = new File(folderPath);
		return folder.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(suffixLowerCase);
			}
		});
	}
	
	public static boolean deleteFileOnly(String filePath) {
		File f1 = new File(filePath);
		if (f1.isFile()) {
			return f1.delete();
		} else {
			return false;
		}
	}
	
	public static boolean deleteFileOrTree(String path) {
		File f1 = new File(path);
		if (f1.isDirectory()) {
			for (File file : f1.listFiles()) {
				deleteFileOrTree(file.getAbsolutePath());
			}
		}
		return f1.delete();
	}
	
	public static long getLastModifiedTime(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			return file.lastModified();
		} else {
			return 0;
		}
	}
}
