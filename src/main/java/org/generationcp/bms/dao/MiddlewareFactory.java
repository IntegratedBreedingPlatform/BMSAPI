
package org.generationcp.bms.dao;

import org.generationcp.bms.context.ContextResolver;
import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
import org.generationcp.middleware.hibernate.SessionFactoryUtil;
import org.generationcp.middleware.manager.*;
import org.generationcp.middleware.manager.api.*;
import org.generationcp.middleware.service.DataImportServiceImpl;
import org.generationcp.middleware.service.FieldbookServiceImpl;
import org.generationcp.middleware.service.OntologyServiceImpl;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.annotation.PreDestroy;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MiddlewareFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MiddlewareFactory.class); 
	
	private final Map<String, SessionFactory> sessionFactoryCache = new HashMap<String, SessionFactory>();
	
	@Autowired
	private ApiEnvironmentConfiguration config;
	
	@Autowired
	private ContextResolver contextResolver;
	
	@PreDestroy
	public void preDestroy() {
		LOGGER.info("Closing cached session factories.");
		for(String key : sessionFactoryCache.keySet()) {
			sessionFactoryCache.get(key).close();
		}
	}
	
	private SessionFactory getSessionFactory() throws FileNotFoundException {
		String selectedCropDB = getCurrentlySelectedCropDBName();
		SessionFactory sessionFactory;

		if (this.sessionFactoryCache.get(selectedCropDB) == null) {
			DatabaseConnectionParameters connectionParams = new DatabaseConnectionParameters(
					config.getDbHost(), config.getDbPort(), selectedCropDB, config.getDbUsername(), config.getDbPassword());
			sessionFactory = SessionFactoryUtil.openSessionFactory(connectionParams);
			sessionFactoryCache.put(selectedCropDB, sessionFactory);
		} else {
			sessionFactory = this.sessionFactoryCache.get(selectedCropDB);
		}
		return sessionFactory;
	}
	
	private String getCurrentlySelectedCropDBName() {
		return this.contextResolver.resolveProgram().getDatabaseName();
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyDataManager getStudyDataManager() throws FileNotFoundException {		
		return new StudyDataManagerImpl(new HibernateSessionPerRequestProvider(getSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public FieldbookService getFieldbookService() throws FileNotFoundException {
		return new FieldbookServiceImpl(new HibernateSessionPerRequestProvider(getSessionFactory()), getCurrentlySelectedCropDBName());
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GenotypicDataManager getGenotypicDataManager() throws FileNotFoundException {
		return new GenotypicDataManagerImpl(new HibernateSessionPerRequestProvider(getSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyDataManager getOntologyDataManager() throws FileNotFoundException {
		return new OntologyDataManagerImpl(new HibernateSessionPerRequestProvider(getSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyService getOntologyService() throws FileNotFoundException {
		return new OntologyServiceImpl(new HibernateSessionPerRequestProvider(getSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public InventoryDataManager getInventoryDataManager() throws FileNotFoundException {
		return new InventoryDataManagerImpl(new HibernateSessionPerRequestProvider(getSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public LocationDataManager getLocationDataManager() throws FileNotFoundException {
		return new LocationDataManagerImpl(new HibernateSessionPerRequestProvider(getSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public UserDataManager getUserDataManager() throws FileNotFoundException {
		return new UserDataManagerImpl(new HibernateSessionPerRequestProvider(getSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmListManager getGermplasmListManager() throws FileNotFoundException {
		return new GermplasmListManagerImpl(new HibernateSessionPerRequestProvider(getSessionFactory()), getCurrentlySelectedCropDBName());
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmDataManager getGermplasmDataManager() throws FileNotFoundException {
		return new GermplasmDataManagerImpl(new HibernateSessionPerRequestProvider(getSessionFactory()), getCurrentlySelectedCropDBName());
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public DataImportService getDataImportService() throws FileNotFoundException {
		return new DataImportServiceImpl(new HibernateSessionPerRequestProvider(getSessionFactory()));
	}

	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public JdbcTemplate getJDBCTemplate() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource(
				String.format("jdbc:mysql://%s:%s/%s", config.getDbHost(), config.getDbPort(), getCurrentlySelectedCropDBName()), 
				config.getDbUsername(), config.getDbPassword());
		
		return new JdbcTemplate(dataSource);
	}
}
