package org.ibp.api.java.impl.middleware.common;

import org.generationcp.middleware.liquibase.LiquibaseInitBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(value = "development")
public class LiquibaseInitBeanConfiguration {

	@Bean
	public LiquibaseInitBean getLiquibaseInitBean() {
		return new LiquibaseInitBean();
	}
}
