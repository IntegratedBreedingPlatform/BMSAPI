
package org.ibp;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.ibp.api.java.impl.middleware.common.validator.CropNameValidationInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.File;

import javax.annotation.PostConstruct;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
@EnableSwagger2
@Configuration
public class Main extends WebMvcConfigurerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	@Value("${swagger.enable}")
	private boolean enableSwagger;

	@Autowired
	com.fasterxml.jackson.databind.ObjectMapper objectMapper;

	@Value("${bms.version}")
	private String bmsVersion;

	public static void main(final String[] args) {
		SpringApplication.run(Main.class, args);
		Main.LOGGER.info("Startup Complete!");
	}

	@Bean
	public TemplateResolver templateResolver() {
		final ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
		templateResolver.setPrefix("/WEB-INF/html/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode("HTML5");
		templateResolver.setCacheable(false);

		return templateResolver;
	}

	@Bean
	public SpringTemplateEngine templateEngine() {
		final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(this.templateResolver());
		templateEngine.addDialect(this.securityDialect());
		return templateEngine;
	}

	@Bean
	public SpringSecurityDialect securityDialect() {
		return new SpringSecurityDialect();
	}

	@Bean
	public CropNameValidationInterceptor getCropNameValidationInterceptor() {
		return new CropNameValidationInterceptor();
	}

	@Override
	public void configurePathMatch(final PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false);
	}

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations("/WEB-INF/static/");
	}

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(this.getCropNameValidationInterceptor()).addPathPatterns("/**");
	}

	@Bean
	public Docket customImplementation() {

		TypeResolver typeResolver = new TypeResolver();
		final ResolvedType fileSystemResourceType =
				typeResolver.resolve(
						File.class);
		final ResolvedType objectType =
				typeResolver.resolve(
						Object.class);

		return new Docket(DocumentationType.SWAGGER_2)
				.alternateTypeRules(
						new AlternateTypeRule(
								fileSystemResourceType,
								objectType)
				)
				.apiInfo(this.apiInfo())
				.enable(this.enableSwagger)
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any())
				.build()
				.forCodeGeneration(true);
	}

	@Bean
	public ResourceBundleMessageSource getResourceBundleMessageSource() {
		final ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
		resourceBundleMessageSource.setBasenames("CommonMessages", "mw_messages_en", "messages_en");
		return resourceBundleMessageSource;
	}

	private ApiInfo apiInfo() {
		return new ApiInfo(
				"BMSAPI",
				"Try out the Breeding Management System API methods listed below!",
				this.bmsVersion,
				"https://www.integratedbreeding.net/1855/terms-of-use",
				new Contact("BMS Support", "", "support@integratedbreeding.net"),
				"GNU General Public License",
				"https://www.gnu.org/licenses/licenses.html#GPL");
	}

	protected void setEnableSwagger(final boolean enableSwagger) {
		this.enableSwagger = enableSwagger;
	}

	@PostConstruct
	private void setTimeZone() {
		objectMapper.setTimeZone(LocaleContextHolder.getTimeZone());
	}

}
