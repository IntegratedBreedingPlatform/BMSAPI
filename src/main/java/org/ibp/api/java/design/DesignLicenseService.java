package org.ibp.api.java.design;

import org.ibp.api.domain.design.License;

public interface DesignLicenseService {

	boolean isExpired();

	License getLicenseInfo();

}
