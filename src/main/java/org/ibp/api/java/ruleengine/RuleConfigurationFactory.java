package org.ibp.api.java.ruleengine;

import org.generationcp.middleware.ruleengine.ProcessCodeRuleFactory;
import org.generationcp.middleware.ruleengine.coding.expression.CodingExpressionFactory;
import org.generationcp.middleware.ruleengine.coding.expression.CodingExpressionResolver;
import org.generationcp.middleware.ruleengine.naming.impl.ProcessCodeFactory;
import org.generationcp.middleware.ruleengine.provider.PropertyFileRuleConfigurationProvider;
import org.generationcp.middleware.ruleengine.service.GermplasmNamingProperties;
import org.generationcp.middleware.ruleengine.generator.BreedersCrossIDGenerator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@EnableConfigurationProperties
@PropertySources({
	@PropertySource("classpath:crossing.properties")
})
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

	@Bean
	public CodingExpressionResolver codingExpressionResolver() {
		return new CodingExpressionResolver();
	}

	@Bean(initMethod = "init")
	public CodingExpressionFactory codingExpressionFactory() {
		return new CodingExpressionFactory();
	}

}
