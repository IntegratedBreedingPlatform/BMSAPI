package org.generationcp.bms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@EnableAutoConfiguration
@ComponentScan
public class Main extends WebMvcConfigurerAdapter {
	
	private static Logger LOGGER = LoggerFactory.getLogger(Main.class);
	  
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

        return templateResolver;
    }

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**")
				.addResourceLocations("/WEB-INF/static/");
	}

}
