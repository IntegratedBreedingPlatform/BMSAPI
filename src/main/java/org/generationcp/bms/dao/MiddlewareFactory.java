package org.generationcp.bms.dao;

import java.io.FileNotFoundException;

import javax.annotation.PostConstruct;

import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
import org.generationcp.middleware.hibernate.SessionFactoryUtil;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.FieldbookServiceImpl;
import org.generationcp.middleware.service.api.FieldbookService;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MiddlewareFactory {
	
	@Autowired
	private Environment environment;
	
	private SessionFactory centralSessionFactory;
	private SessionFactory localSessionFactory;
	
	@PostConstruct  
	public void postConstruct() throws FileNotFoundException {
		
		String centralHost = environment.getProperty("central.host");
		String centralPort = environment.getProperty("central.port");
		String centralDbname = environment.getProperty("central.dbname");
		String centralUsername = environment.getProperty("central.username");
		String centralPassword = environment.getProperty("central.password");

		String localHost = environment.getProperty("local.host");
		String localPort = environment.getProperty("local.port");
		String localDbname = environment.getProperty("local.dbname");
		String localUsername = environment.getProperty("local.username");
		String localPassword = environment.getProperty("local.password");
				
		
		DatabaseConnectionParameters centralConnectionParameters 
					= new DatabaseConnectionParameters(centralHost, centralPort, centralDbname, centralUsername, centralPassword);
		
		DatabaseConnectionParameters localConnectionParameters 
					= new DatabaseConnectionParameters(localHost, localPort, localDbname, localUsername, localPassword);	
		
		centralSessionFactory = SessionFactoryUtil.openSessionFactory(centralConnectionParameters);
		localSessionFactory = SessionFactoryUtil.openSessionFactory(localConnectionParameters);
	}
	
	@Bean
	public StudyDataManager getStudyDataManager() throws FileNotFoundException {		
		return new StudyDataManagerImpl(new HibernateSessionPerRequestProvider(localSessionFactory), 
				new HibernateSessionPerRequestProvider(centralSessionFactory));
	}
	
	@Bean
	public FieldbookService getFieldbookService() {
		return new FieldbookServiceImpl(new HibernateSessionPerRequestProvider(localSessionFactory), 
				new HibernateSessionPerRequestProvider(centralSessionFactory));
	}
}
