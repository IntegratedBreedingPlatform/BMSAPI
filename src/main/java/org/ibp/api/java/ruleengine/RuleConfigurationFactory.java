package org.ibp.api.java.ruleengine;

import org.generationcp.commons.ruleengine.ProcessCodeRuleFactory;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.naming.expression.ComponentPostProcessor;
import org.generationcp.commons.ruleengine.naming.impl.ProcessCodeFactory;
import org.generationcp.commons.ruleengine.provider.PropertyFileRuleConfigurationProvider;
import org.generationcp.commons.service.GermplasmNamingProperties;
import org.generationcp.commons.service.impl.BreedersCrossIDGenerator;
import org.generationcp.middleware.pojos.KeySequenceRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class RuleConfigurationFactory {

	@Bean
	@ConfigurationProperties(prefix = "rule-engine")
	public PropertyFileRuleConfigurationProvider propertyFileRuleConfigurationProvider() {
		return new PropertyFileRuleConfigurationProvider();
	}

	@Bean(initMethod = "init")
	public ProcessCodeRuleFactory processCodeRuleFactory() {
        return new ProcessCodeRuleFactory();
	}

	@Bean(initMethod = "init")
	public ProcessCodeFactory processCodeFactory() {
        return new ProcessCodeFactory();
	}

	@Bean
	public BreedersCrossIDGenerator breedersCrossIDGenerator() {
		return new BreedersCrossIDGenerator();
	}

	@Bean
	@ConfigurationProperties(prefix = "germplasm-naming-properties")
	public GermplasmNamingProperties germplasmNamingProperties() {
        return new GermplasmNamingProperties();
	}
}
