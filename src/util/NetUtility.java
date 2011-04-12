package util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetUtility {

	private NetUtility() {}
	
	public static String hostName() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			return addr.getHostName();
		} catch (UnknownHostException e) {
			return "???";
		}
	}
	
	public static String ipAddress() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			return addr.getHostAddress();
		} catch (UnknownHostException e) {
			return "???";
		}		
	}
}
