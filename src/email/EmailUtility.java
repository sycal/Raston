package email;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

public class EmailUtility {

	public static boolean isValidEmailAddress(String emailAddress) {
		Pattern p = Pattern.compile("^<?\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b>?$", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(emailAddress);
		return m.find();
	}
	
	public static String stripNameFromEmail(String email) {
		return email.split("@")[0];
	}
	
	public static String combineEmailAddresses(List<EmailAddress> addressList) {
		StringBuilder result = new StringBuilder();
		for (EmailAddress address : addressList) {
			if (result.length() > 0) result.append(';');
			result.append(address.toSimpleAddress());
		}
		return result.toString();
	}
	
	public static List<EmailAddress> splitEmailAddresses(String emailList) {
		List<EmailAddress> list = new ArrayList<EmailAddress>();
		for (String email : emailList.split(";")) {
			list.add(EmailAddress.parse(email));
		}
		return list;
	}
}
