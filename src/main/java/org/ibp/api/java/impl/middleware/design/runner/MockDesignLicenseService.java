package org.ibp.api.java.impl.middleware.design.runner;

import org.ibp.api.domain.design.DesignLicenseInfo;
import org.ibp.api.exception.BVLicenseParseException;
import org.ibp.api.java.design.DesignLicenseService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
	value = "design.runner.license.util",
	havingValue = "org.ibp.api.java.impl.middleware.design.runner.MockDesignLicenseService")
public class MockDesignLicenseService implements DesignLicenseService {

	@Override
	public boolean isExpired() {
		return false;
	}

	@Override
	public Integer getExpiryDays() {
		return 100;
	}

}
