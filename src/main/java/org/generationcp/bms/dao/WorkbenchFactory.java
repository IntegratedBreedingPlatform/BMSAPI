package org.generationcp.bms.dao;

import java.io.FileNotFoundException;

import javax.annotation.PostConstruct;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
import org.generationcp.middleware.hibernate.SessionFactoryUtil;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

@Configuration
public class WorkbenchFactory {
	
	private String dbNameWorkbench;
	
	private String dbHost;
	private String dbPort;
	private String dbUsername;
	private String dbPassword;
	
	@Autowired
	private Environment environment;
	
	@PostConstruct  
	public void postConstruct() throws FileNotFoundException {
		this.dbHost = environment.getProperty("db.host");
		this.dbPort = environment.getProperty("db.port");
		this.dbUsername = environment.getProperty("db.username");
		this.dbPassword = environment.getProperty("db.password");
		this.dbNameWorkbench = environment.getProperty("db.workbench.name");
	}
	
	@Bean
	@Scope(value = "singleton")
	public WorkbenchDataManager getWorkbenchDataManager() throws FileNotFoundException, MiddlewareQueryException {
		DatabaseConnectionParameters workbenchConnectionParameters = new DatabaseConnectionParameters(
				this.dbHost, this.dbPort, this.dbNameWorkbench, this.dbUsername, this.dbPassword);		
		SessionFactory sessionFactory = SessionFactoryUtil.openSessionFactory("ibpworkbench_hib.cfg.xml", workbenchConnectionParameters);
		HibernateSessionPerRequestProvider sessionProvider = new HibernateSessionPerRequestProvider(sessionFactory);
		WorkbenchDataManagerImpl workbenchDataManager = new WorkbenchDataManagerImpl(sessionProvider);
		return workbenchDataManager;
	}

}
