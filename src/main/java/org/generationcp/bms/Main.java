package org.generationcp.bms;

import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@EnableAutoConfiguration
@ComponentScan
@EnableSwagger
public class Main extends WebMvcConfigurerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	@Autowired
	private SpringSwaggerConfig springSwaggerConfig;
	
	public static void main(String[] args) throws Exception {	
		SpringApplication.run(Main.class, args);	    
		LOGGER.info("Startup Complete!");
	}
	
    @Bean
    public TemplateResolver templateResolver(){
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
        templateResolver.setPrefix("/WEB-INF/html/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCacheable(false);

        return templateResolver;
    }

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**")
				.addResourceLocations("/WEB-INF/static/");
	}
	
	@Bean
	public SwaggerSpringMvcPlugin customImplementation() {
		return new SwaggerSpringMvcPlugin(this.springSwaggerConfig).apiInfo(apiInfo());
	}

	private ApiInfo apiInfo() {
		return new ApiInfo(
            "Welcome!",
            "Try out the Breeding Management System API methods listed below!",
            "http://bit.ly/KQX1nL",
            "naymesh@leafnode.io",
            "GNU General Public License",
            "http://bit.ly/8Ztv8M");
	}
}
