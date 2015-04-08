package org.ibp.api.java.impl.middleware.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ApiEnvironmentConfiguration {

	@Autowired
	private Environment environment;

	public String getDbHost() {
		return environment.getProperty("db.host");
	}

	public String getDbPort() {
		return environment.getProperty("db.port");
	}

	public String getDbUsername() {
		return environment.getProperty("db.username");
	}

	public String getDbPassword() {
		return environment.getProperty("db.password");
	}
	
	public String getWorkbenchDBName() {
		return environment.getProperty("db.workbench.name");
	}

}
