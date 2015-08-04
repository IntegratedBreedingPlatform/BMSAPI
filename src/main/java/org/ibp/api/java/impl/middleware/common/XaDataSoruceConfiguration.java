package org.ibp.api.java.impl.middleware.common;

import org.generationcp.middleware.hibernate.XADataSources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XaDataSoruceConfiguration {

	@Bean
	public XADataSources getXADataSources() {
		return new XADataSources();
	}
}
