package org.ibp.api.java.impl.middleware.design.runner;

import org.ibp.api.domain.design.License;
import org.ibp.api.java.design.DesignLicenseService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
	value = "design.runner.license.service",
	havingValue = "org.ibp.api.java.impl.middleware.design.runner.MockDesignLicenseService")
public class MockDesignLicenseService implements DesignLicenseService {

	@Override
	public boolean isExpired() {
		return false;
	}

	@Override
	public License getLicenseInfo() {
		return new License("Succesful license checkout", "73", "30-NOV-2022");
	}

}
