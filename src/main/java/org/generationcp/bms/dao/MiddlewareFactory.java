
package org.generationcp.bms.dao;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.bms.Constants;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class MiddlewareFactory {
	
	private Logger LOGGER = LoggerFactory.getLogger(MiddlewareFactory.class); 
	
	private final Map<String, SessionFactory> sessionFactoryCache = new HashMap<String, SessionFactory>();
	
	@Autowired
	private ApiEnvironmentConfiguration config;
	
	@Autowired
	private WorkbenchDataManager workbenchDataManager;
	
	@PreDestroy
	public void preDestroy() {
		LOGGER.info("Closing cached session factories.");
		for(String key : sessionFactoryCache.keySet()) {
			sessionFactoryCache.get(key).close();
		}
	}
	
	private SessionFactory getCentralSessionFactory() throws FileNotFoundException, MiddlewareQueryException {
		String selectedCentralDB = getCurrentlySelectedCentralDBName();
		SessionFactory sessionFactory;

		if (this.sessionFactoryCache.get(selectedCentralDB) == null) {
			DatabaseConnectionParameters centralConnectionParams = new DatabaseConnectionParameters(
					config.getDbHost(), config.getDbPort(), selectedCentralDB, config.getDbUsername(), config.getDbPassword());
			sessionFactory = SessionFactoryUtil.openSessionFactory(centralConnectionParams);
			sessionFactoryCache.put(selectedCentralDB, sessionFactory);
		} else {
			sessionFactory = this.sessionFactoryCache.get(selectedCentralDB);
		}
		return sessionFactory;
	}
	
	private SessionFactory getLocalSessionFactory() throws FileNotFoundException, MiddlewareQueryException {
		String selectedLocalDB = getCurrentlySelectedLocalDBName();
		SessionFactory sessionFactory;

		if (this.sessionFactoryCache.get(selectedLocalDB) == null) {
			DatabaseConnectionParameters localConnectionParams = new DatabaseConnectionParameters(
					config.getDbHost(), config.getDbPort(), selectedLocalDB, config.getDbUsername(), config.getDbPassword());
			sessionFactory = SessionFactoryUtil.openSessionFactory(localConnectionParams);
			sessionFactoryCache.put(selectedLocalDB, sessionFactory);
		} else {
			sessionFactory = this.sessionFactoryCache.get(selectedLocalDB);
		}
		return sessionFactory;
	}
	
	private String getCurrentlySelectedCentralDBName() throws MiddlewareQueryException {
		return resolveProgram().getCentralDbName();
	}
	
	private String getCurrentlySelectedLocalDBName() throws MiddlewareQueryException {
		return resolveProgram().getLocalDbName();
	}
	
	private Project resolveProgram() throws MiddlewareQueryException {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		HttpSession session = request.getSession();
		Project selectedProgram = (Project) session.getAttribute(Constants.PARAM_SELECTED_PROGRAM);
		if(selectedProgram != null) {
			return selectedProgram; 
		}
		else {
			Project lastOpenedProgram = this.workbenchDataManager.getLastOpenedProjectAnyUser();
			if (lastOpenedProgram != null) {
				return lastOpenedProgram;
			}
		}
		throw new RuntimeException("Unable to resolve program in context.");
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyDataManager getStudyDataManager() throws FileNotFoundException, MiddlewareQueryException {		
		return new StudyDataManagerImpl(new HibernateSessionPerRequestProvider(getLocalSessionFactory()), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public FieldbookService getFieldbookService() throws FileNotFoundException, MiddlewareQueryException {
		return new FieldbookServiceImpl(new HibernateSessionPerRequestProvider(getLocalSessionFactory()), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()), getCurrentlySelectedLocalDBName(), getCurrentlySelectedCentralDBName());
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GenotypicDataManager getGenotypicDataManager() throws FileNotFoundException, MiddlewareQueryException {
		return new GenotypicDataManagerImpl(new HibernateSessionPerRequestProvider(getLocalSessionFactory()), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyDataManager getOntologyDataManager() throws FileNotFoundException, MiddlewareQueryException {
		return new OntologyDataManagerImpl(new HibernateSessionPerRequestProvider(getLocalSessionFactory()), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyService getOntologyService() throws FileNotFoundException, MiddlewareQueryException {
		return new OntologyServiceImpl(new HibernateSessionPerRequestProvider(getLocalSessionFactory()), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public InventoryDataManager getInventoryDataManager() throws FileNotFoundException, MiddlewareQueryException {
		return new InventoryDataManagerImpl(new HibernateSessionPerRequestProvider(getLocalSessionFactory()), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public LocationDataManager getLocationDataManager() throws FileNotFoundException, MiddlewareQueryException {
		return new LocationDataManagerImpl(new HibernateSessionPerRequestProvider(getLocalSessionFactory()), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public UserDataManager getUserDataManager() throws FileNotFoundException, MiddlewareQueryException {
		return new UserDataManagerImpl(new HibernateSessionPerRequestProvider(getLocalSessionFactory()), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmListManager getGermplasmListManager() throws FileNotFoundException, MiddlewareQueryException {
		return new GermplasmListManagerImpl(new HibernateSessionPerRequestProvider(getLocalSessionFactory()), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()), getCurrentlySelectedLocalDBName(), getCurrentlySelectedCentralDBName());
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmDataManager getGermplasmDataManager() throws FileNotFoundException, MiddlewareQueryException {
		return new GermplasmDataManagerImpl(new HibernateSessionPerRequestProvider(getLocalSessionFactory()), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()), getCurrentlySelectedLocalDBName(), getCurrentlySelectedCentralDBName());
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public DataImportService getDataImportService() throws FileNotFoundException, MiddlewareQueryException {
		return new DataImportServiceImpl(new HibernateSessionPerRequestProvider(getLocalSessionFactory()), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()));
	}

	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public JdbcTemplate getJDBCTemplate() throws MiddlewareQueryException {
		DriverManagerDataSource dataSource = new DriverManagerDataSource(
				String.format("jdbc:mysql://%s:%s/%s", config.getDbHost(), config.getDbPort(), getCurrentlySelectedCentralDBName()), 
				config.getDbUsername(), config.getDbPassword());
		
		return new JdbcTemplate(dataSource);
	}
}
