package org.ibp.api.java.design;

import org.ibp.api.domain.design.License;

public interface DesignLicenseService {

	static final String LICENSE_DATE_FORMAT = "dd-MMM-yyyy";

	boolean isExpired();

	License getLicenseInfo();

}
