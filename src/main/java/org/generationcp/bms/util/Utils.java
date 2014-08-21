package org.generationcp.bms.util;

import javax.servlet.http.HttpServletRequest;

public abstract class Utils {
	
	public static String getBaseUrl(HttpServletRequest httpRequest) {
		if(httpRequest == null) {
			return null;
		}		
		return String.format("%s://%s:%s", httpRequest.getScheme(), httpRequest.getServerName(), httpRequest.getServerPort());	
	}

}
