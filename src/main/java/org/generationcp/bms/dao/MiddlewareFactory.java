package org.generationcp.bms.dao;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
import org.generationcp.middleware.hibernate.SessionFactoryUtil;
import org.generationcp.middleware.manager.GenotypicDataManagerImpl;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.FieldbookServiceImpl;
import org.generationcp.middleware.service.api.FieldbookService;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class MiddlewareFactory {
	
	@Autowired
	private Environment environment;
	
	private SessionFactory localSessionFactory;
	
	private String dbHost;
	private String dbPort;
	private String dbUsername;
	private String dbPassword;
	
	private String dbNameLocal;
	private String dbNameCentralDefault;
	
	private final Map<String, SessionFactory> sessionFactoryCache = new HashMap<String, SessionFactory>();
	
	@PostConstruct  
	public void postConstruct() throws FileNotFoundException {
		
		this.dbHost = environment.getProperty("db.host");
		this.dbPort = environment.getProperty("db.port");
		this.dbUsername = environment.getProperty("db.username");
		this.dbPassword = environment.getProperty("db.password");

		this.dbNameLocal = environment.getProperty("db.crop.local");
		this.dbNameCentralDefault = environment.getProperty("db.crop.central.default");
		
		localSessionFactory = SessionFactoryUtil.openSessionFactory(null, this.dbHost, this.dbPort,
				this.dbUsername, this.dbPassword, this.dbNameLocal);
		
	}
	
	private SessionFactory getCentralSessionFactory() throws FileNotFoundException {

		String selectedCentralDB = getCurrentlySelectedCropDBName();
		SessionFactory sessionFactory;

		if (this.sessionFactoryCache.get(selectedCentralDB) == null) {
			sessionFactory = SessionFactoryUtil.openSessionFactory(null, this.dbHost, this.dbPort,
					this.dbUsername, this.dbPassword, selectedCentralDB);
			sessionFactoryCache.put(selectedCentralDB, sessionFactory);
		} else {
			sessionFactory = this.sessionFactoryCache.get(selectedCentralDB);
		}
		return sessionFactory;
	}
	
	private String getCurrentlySelectedCropDBName() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		HttpSession session = request.getSession();
		String selectedCropDB = (String) session.getAttribute("selectedCropDB");
		if(selectedCropDB != null) {
			return selectedCropDB; 
		}
		session.setAttribute("selectedCropDB", this.dbNameCentralDefault);
		return this.dbNameCentralDefault;
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyDataManager getStudyDataManager() throws FileNotFoundException {		
		return new StudyDataManagerImpl(new HibernateSessionPerRequestProvider(localSessionFactory), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public FieldbookService getFieldbookService() throws FileNotFoundException {
		return new FieldbookServiceImpl(new HibernateSessionPerRequestProvider(localSessionFactory), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()));
	}
	
	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GenotypicDataManager getGenotypicDataManager() throws FileNotFoundException {
		return new GenotypicDataManagerImpl(new HibernateSessionPerRequestProvider(localSessionFactory), 
				new HibernateSessionPerRequestProvider(getCentralSessionFactory()));
	}

	@Bean
	@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public JdbcTemplate getJDBCTemplate() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource(
				String.format("jdbc:mysql://%s:%s/%s", this.dbHost, this.dbPort, getCurrentlySelectedCropDBName()), 
				this.dbUsername, 
				this.dbPassword);
		
		return new JdbcTemplate(dataSource);
	}
}
