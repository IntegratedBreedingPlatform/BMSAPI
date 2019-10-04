package org.ibp.api.java.design;

import org.ibp.api.domain.design.BVDesignLicenseInfo;
import org.ibp.api.exception.BVLicenseParseException;

public interface DesignLicenseUtil {

	public boolean isExpired(BVDesignLicenseInfo bvDesignLicenseInfo);

	public boolean isExpiringWithinThirtyDays(BVDesignLicenseInfo bvDesignLicenseInfo);

	public BVDesignLicenseInfo retrieveLicenseInfo() throws BVLicenseParseException;

}
