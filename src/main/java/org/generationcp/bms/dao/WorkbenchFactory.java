package org.generationcp.bms.dao;

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

import java.io.FileNotFoundException;

@Configuration
public class WorkbenchFactory {
	
	@Autowired
	private ApiEnvironmentConfiguration config;
	
	@Bean
	@Scope(value = "singleton")
	public WorkbenchDataManager getWorkbenchDataManager() throws FileNotFoundException, MiddlewareQueryException {
		DatabaseConnectionParameters workbenchConnectionParameters = new DatabaseConnectionParameters(
				config.getDbHost(), config.getDbPort(), config.getWorkbenchDBName(), config.getDbUsername(), config.getDbPassword());		
		SessionFactory sessionFactory = SessionFactoryUtil.openSessionFactory("ibpworkbench_hib.cfg.xml", workbenchConnectionParameters);
		HibernateSessionPerRequestProvider sessionProvider = new HibernateSessionPerRequestProvider(sessionFactory);
		WorkbenchDataManagerImpl workbenchDataManager = new WorkbenchDataManagerImpl(sessionProvider);
		return workbenchDataManager;
	}

}
