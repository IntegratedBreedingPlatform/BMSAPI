package org.ibp.api.java.impl.middleware.design.runner;

import org.ibp.api.domain.design.BVDesignLicenseInfo;
import org.ibp.api.exception.BVLicenseParseException;
import org.ibp.api.java.design.DesignLicenseUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
	value = "design.runner.license.util",
	havingValue = "org.ibp.api.java.impl.middleware.design.runner.MockBVDesignLicenseUtil")
public class MockBVDesignLicenseUtil implements DesignLicenseUtil {

	@Override
	public boolean isExpired(final BVDesignLicenseInfo bvDesignLicenseInfo) {
		return false;
	}

	@Override
	public boolean isExpiringWithinThirtyDays(final BVDesignLicenseInfo bvDesignLicenseInfo) {
		return false;
	}

	@Override
	public BVDesignLicenseInfo retrieveLicenseInfo() throws BVLicenseParseException {
		return null;
	}
}
