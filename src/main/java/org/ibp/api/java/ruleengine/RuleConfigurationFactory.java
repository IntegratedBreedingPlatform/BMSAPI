package org.ibp.api.java.ruleengine;

import org.generationcp.commons.ruleengine.provider.PropertyFileRuleConfigurationProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class RuleConfigurationFactory {

	@Bean
	@ConfigurationProperties(prefix = "rule-engine")
	public PropertyFileRuleConfigurationProvider propertyFileRuleConfigurationProvider() {
		return new PropertyFileRuleConfigurationProvider();
	}
}
