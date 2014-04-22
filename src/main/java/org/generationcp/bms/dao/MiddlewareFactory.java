package org.generationcp.bms.dao;

import java.io.IOException;
import java.net.URISyntaxException;

import org.generationcp.middleware.exceptions.ConfigException;
import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
import org.generationcp.middleware.hibernate.SessionFactoryUtil;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MiddlewareFactory {
	
	@Autowired
	private Environment environment;
	
	@Bean
	public StudyDataManager getStudyDataManager() throws ConfigException, URISyntaxException, IOException {
	
		DatabaseConnectionParameters central = new DatabaseConnectionParameters("application.properties", "central");
		DatabaseConnectionParameters local = new DatabaseConnectionParameters("application.properties", "local");
		
		SessionFactory centralSessionFactory = SessionFactoryUtil.openSessionFactory(central);
		SessionFactory localSessionFactory = SessionFactoryUtil.openSessionFactory(local);
		
		StudyDataManagerImpl impl = new StudyDataManagerImpl(new HibernateSessionPerRequestProvider(localSessionFactory), 
				new HibernateSessionPerRequestProvider(centralSessionFactory));
		
		return impl;
	}
}
