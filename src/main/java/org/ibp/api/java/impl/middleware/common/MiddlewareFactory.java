package org.ibp.api.java.impl.middleware.common;

import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
import org.generationcp.middleware.hibernate.SessionFactoryUtil;
import org.generationcp.middleware.manager.*;
import org.generationcp.middleware.manager.api.*;
import org.generationcp.middleware.service.DataImportServiceImpl;
import org.generationcp.middleware.service.FieldbookServiceImpl;
import org.generationcp.middleware.service.OntologyManagerServiceImpl;
import org.generationcp.middleware.service.OntologyServiceImpl;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.impl.study.StudyServiceImpl;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PreDestroy;

@Configuration
public class MiddlewareFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(MiddlewareFactory.class);

	private final Map<String, SessionFactory> sessionFactoryCache = new HashMap<>();

	@Autowired
	private ApiEnvironmentConfiguration config;

	@Autowired
	private ContextResolver contextResolver;

	@PreDestroy
	public void preDestroy() {
		MiddlewareFactory.LOGGER.info("Closing cached session factories.");
		for (String key : this.sessionFactoryCache.keySet()) {
			this.sessionFactoryCache.get(key).close();
		}
	}

	private SessionFactory getSessionFactory() throws FileNotFoundException {
		String selectedCropDB = this.getCurrentlySelectedCropDBName();
		SessionFactory sessionFactory;

		if (this.sessionFactoryCache.get(selectedCropDB) == null) {

			DatabaseConnectionParameters connectionParams = new DatabaseConnectionParameters(
					this.config.getDbHost(), this.config.getDbPort(), selectedCropDB,
					this.config.getDbUsername(), this.config.getDbPassword());

			sessionFactory = SessionFactoryUtil.openSessionFactory(connectionParams);
			this.sessionFactoryCache.put(selectedCropDB, sessionFactory);
		} else {
			sessionFactory = this.sessionFactoryCache.get(selectedCropDB);
		}
		return sessionFactory;
	}

	private String getCurrentlySelectedCropDBName() {
		return this.contextResolver.resolveDatabaseFromUrl();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyDataManager getStudyDataManager() throws FileNotFoundException {
		return new StudyDataManagerImpl(new HibernateSessionPerRequestProvider(
				this.getSessionFactory()));
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public FieldbookService getFieldbookService() throws FileNotFoundException {
		return new FieldbookServiceImpl(new HibernateSessionPerRequestProvider(
				this.getSessionFactory()), this.getCurrentlySelectedCropDBName());
	}
	
	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyService getStudyService() throws FileNotFoundException {
		return new StudyServiceImpl(new HibernateSessionPerRequestProvider(this.getSessionFactory()));
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GenotypicDataManager getGenotypicDataManager() throws FileNotFoundException {
		return new GenotypicDataManagerImpl(new HibernateSessionPerRequestProvider(
				this.getSessionFactory()));
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyDataManager getOntologyDataManager() throws FileNotFoundException {
		return new OntologyDataManagerImpl(new HibernateSessionPerRequestProvider(
				this.getSessionFactory()));
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyService getOntologyService() throws FileNotFoundException {
		return new OntologyServiceImpl(new HibernateSessionPerRequestProvider(
				this.getSessionFactory()));
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyManagerService getOntologyManagerService() throws FileNotFoundException {
		return new OntologyManagerServiceImpl(new HibernateSessionPerRequestProvider(
				this.getSessionFactory()));
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public InventoryDataManager getInventoryDataManager() throws FileNotFoundException {
		return new InventoryDataManagerImpl(new HibernateSessionPerRequestProvider(
				this.getSessionFactory()));
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public LocationDataManager getLocationDataManager() throws FileNotFoundException {
		return new LocationDataManagerImpl(new HibernateSessionPerRequestProvider(
				this.getSessionFactory()));
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public UserDataManager getUserDataManager() throws FileNotFoundException {
		return new UserDataManagerImpl(new HibernateSessionPerRequestProvider(
				this.getSessionFactory()));
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmListManager getGermplasmListManager() throws FileNotFoundException {
		return new GermplasmListManagerImpl(new HibernateSessionPerRequestProvider(
				this.getSessionFactory()), this.getCurrentlySelectedCropDBName());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmDataManager getGermplasmDataManager() throws FileNotFoundException {
		return new GermplasmDataManagerImpl(new HibernateSessionPerRequestProvider(
				this.getSessionFactory()), this.getCurrentlySelectedCropDBName());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public DataImportService getDataImportService() throws FileNotFoundException {
		return new DataImportServiceImpl(new HibernateSessionPerRequestProvider(
				this.getSessionFactory()));
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public JdbcTemplate getJDBCTemplate() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource(String.format(
				"jdbc:mysql://%s:%s/%s", this.config.getDbHost(), this.config.getDbPort(),
				this.getCurrentlySelectedCropDBName()), this.config.getDbUsername(),
				this.config.getDbPassword());

		return new JdbcTemplate(dataSource);
	}

	@Bean
	@Scope(value = "singleton")
	public WorkbenchDataManager getWorkbenchDataManager() throws FileNotFoundException {
		DatabaseConnectionParameters workbenchConnectionParameters = new DatabaseConnectionParameters(
				this.config.getDbHost(), this.config.getDbPort(), this.config.getWorkbenchDBName(),
				this.config.getDbUsername(), this.config.getDbPassword());
		SessionFactory sessionFactory = SessionFactoryUtil
				.openSessionFactory(workbenchConnectionParameters);
		HibernateSessionPerRequestProvider sessionProvider = new HibernateSessionPerRequestProvider(
				sessionFactory);
		return new WorkbenchDataManagerImpl(sessionProvider);
	}
}
