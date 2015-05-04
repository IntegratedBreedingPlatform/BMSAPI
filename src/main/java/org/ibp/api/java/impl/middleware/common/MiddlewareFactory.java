package org.ibp.api.java.impl.middleware.common;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
import org.generationcp.middleware.hibernate.SessionFactoryUtil;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.GenotypicDataManagerImpl;
import org.generationcp.middleware.manager.GermplasmDataManagerImpl;
import org.generationcp.middleware.manager.GermplasmListManagerImpl;
import org.generationcp.middleware.manager.InventoryDataManagerImpl;
import org.generationcp.middleware.manager.LocationDataManagerImpl;
import org.generationcp.middleware.manager.OntologyDataManagerImpl;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.generationcp.middleware.manager.UserDataManagerImpl;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.service.DataImportServiceImpl;
import org.generationcp.middleware.service.FieldbookServiceImpl;
import org.generationcp.middleware.service.OntologyManagerServiceImpl;
import org.generationcp.middleware.service.OntologyServiceImpl;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.impl.study.StudyServiceImpl;
import org.generationcp.middleware.service.pedigree.PedigreeDefaultServiceImpl;
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

@Configuration
public class MiddlewareFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(MiddlewareFactory.class);

	private final Map<String, SessionFactory> sessionFactoryCache = new HashMap<>();

	@Autowired
	private ApiEnvironmentConfiguration config;

	@Autowired
	private ContextResolver contextResolver;
	
	private SessionFactory workbenchSessionFactory;
	
	@PreDestroy
	public void preDestroy() {
		
		for (String key : this.sessionFactoryCache.keySet()) {
			LOGGER.info("Closing cached session factory for crop database: " + key);
			this.sessionFactoryCache.get(key).close();
		}
		
		if(this.workbenchSessionFactory != null) {
			LOGGER.info("Closing session factory for workbench database.");
			this.workbenchSessionFactory.close();
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
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public PedigreeService getPedigreeService() throws FileNotFoundException {
		//FIXME - producing the default pedigree service impl. Make it configurable for CIMMYT wheat pedigree generation.
		return new PedigreeDefaultServiceImpl(new HibernateSessionPerRequestProvider(getSessionFactory()));
	}

	@Bean
	public WorkbenchDataManager getWorkbenchDataManager() throws FileNotFoundException {
		return new WorkbenchDataManagerImpl(getWorkbenchSessionProvider());
	}

	@Bean(destroyMethod = "close")
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public HibernateSessionPerRequestProvider getWorkbenchSessionProvider() throws FileNotFoundException {
		if (workbenchSessionFactory == null) {
			DatabaseConnectionParameters workbenchConnectionParameters =
					new DatabaseConnectionParameters(this.config.getDbHost(), this.config.getDbPort(), 
							this.config.getWorkbenchDBName(), this.config.getDbUsername(), this.config.getDbPassword());
			this.workbenchSessionFactory = SessionFactoryUtil.openSessionFactory(workbenchConnectionParameters);
		}
		return new HibernateSessionPerRequestProvider(this.workbenchSessionFactory);
	}
}
