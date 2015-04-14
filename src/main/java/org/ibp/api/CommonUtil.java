package org.ibp.api;

public class CommonUtil {

	public static Integer tryParseSafe(String value){
		if(value == null){
		  	return null;
		}

		try {
		  	return Integer.valueOf(value);
		} catch (Exception ignored){

		}
		return null;
	}

	// Will Capitalize first character of error message
	public static String capitalizeFirstLetterOfErrorMessage(String errorMessage){
	  	return errorMessage.substring(0, 1).toUpperCase() + errorMessage.substring(1);
	}
}
