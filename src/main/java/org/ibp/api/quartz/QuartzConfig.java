package org.ibp.api.quartz;

import org.generationcp.middleware.hibernate.XABeanDefinition;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
public class QuartzConfig {

	@Value("${workbench.driver}")
	private String workbenchDriver;

	@Value("${workbench.url}")
	private String workbenchDBURL;

	@Value("${workbench.dbname}")
	private String workbenchDBname;

	@Value("${workbench.username}")
	private String workbenchDBUsername;

	@Value("${workbench.password}")
	private String workbenchDBPassword;

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	public SpringBeanJobFactory springBeanJobFactory() {
		AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		return jobFactory;
	}

	@Bean
	public Scheduler scheduler() throws SchedulerException, IOException {
		final Properties properties = this.getProperties();
		final StdSchedulerFactory schedulerFactory = new StdSchedulerFactory(properties);
		schedulerFactory.initialize();
		final Scheduler scheduler = schedulerFactory.getScheduler();
		scheduler.setJobFactory(springBeanJobFactory());
		return scheduler;
	}

	private Properties getProperties() throws IOException {
		final InputStream input = this.getClass().getClassLoader().getResourceAsStream("quartz.properties");
		final Properties prop = new Properties();
		prop.load(input);

		final String workbenchDataSourceName = XABeanDefinition.getDataSourceName(this.workbenchDBname.toUpperCase());
		prop.setProperty("org.quartz.jobStore.dataSource", workbenchDataSourceName);
		prop.setProperty("org.quartz.dataSource." + workbenchDataSourceName + ".URL", this.workbenchDBURL);
		prop.setProperty("org.quartz.dataSource." + workbenchDataSourceName + ".driver", this.workbenchDriver);
		prop.setProperty("org.quartz.dataSource." + workbenchDataSourceName + ".user", this.workbenchDBUsername);
		prop.setProperty("org.quartz.dataSource." + workbenchDataSourceName + ".password", this.workbenchDBPassword);

		return prop;
	}

}
