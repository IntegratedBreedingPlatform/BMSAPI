
package org.ibp.api.java.impl.middleware.common;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.rits.cloning.Cloner;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.service.BreedingViewImportService;
import org.generationcp.commons.service.CsvExportSampleListService;
import org.generationcp.commons.service.impl.BreedingViewImportServiceImpl;
import org.generationcp.commons.service.impl.CsvExportSampleListServiceImpl;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.hibernate.DatasourceUtilities;
import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
import org.generationcp.middleware.manager.GenotypicDataManagerImpl;
import org.generationcp.middleware.manager.GermplasmDataManagerImpl;
import org.generationcp.middleware.manager.GermplasmListManagerImpl;
import org.generationcp.middleware.manager.InventoryDataManagerImpl;
import org.generationcp.middleware.manager.LocationDataManagerImpl;
import org.generationcp.middleware.manager.OntologyDataManagerImpl;
import org.generationcp.middleware.manager.PedigreeDataManagerImpl;
import org.generationcp.middleware.manager.PresetServiceImpl;
import org.generationcp.middleware.manager.SearchRequestServiceImpl;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.generationcp.middleware.manager.UserDataManagerImpl;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.manager.api.PresetService;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.OntologyMethodDataManagerImpl;
import org.generationcp.middleware.manager.ontology.OntologyPropertyDataManagerImpl;
import org.generationcp.middleware.manager.ontology.OntologyScaleDataManagerImpl;
import org.generationcp.middleware.manager.ontology.OntologyVariableDataManagerImpl;
import org.generationcp.middleware.manager.ontology.TermDataManagerImpl;
import org.generationcp.middleware.manager.ontology.api.OntologyMethodDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.operation.saver.ListDataProjectSaver;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.operation.transformer.etl.StandardVariableTransformer;
import org.generationcp.middleware.pojos.KeySequenceRegister;
import org.generationcp.middleware.service.DataImportServiceImpl;
import org.generationcp.middleware.service.FieldbookServiceImpl;
import org.generationcp.middleware.service.MethodServiceImpl;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.service.api.KeySequenceRegisterService;
import org.generationcp.middleware.service.api.MethodService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.SampleListService;
import org.generationcp.middleware.service.api.SampleService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.derived_variables.DerivedVariableService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.generationcp.middleware.service.api.study.MeasurementVariableService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.service.api.user.UserServiceImpl;
import org.generationcp.middleware.service.impl.GermplasmGroupingServiceImpl;
import org.generationcp.middleware.service.impl.KeySequenceRegisterServiceImpl;
import org.generationcp.middleware.service.impl.dataset.DatasetServiceImpl;
import org.generationcp.middleware.service.impl.dataset.DatasetTypeServiceImpl;
import org.generationcp.middleware.service.impl.derived_variables.DerivedVariableServiceImpl;
import org.generationcp.middleware.service.impl.derived_variables.FormulaServiceImpl;
import org.generationcp.middleware.service.impl.study.MeasurementVariableServiceImpl;
import org.generationcp.middleware.service.impl.study.SampleListServiceImpl;
import org.generationcp.middleware.service.impl.study.SampleServiceImpl;
import org.generationcp.middleware.service.impl.study.StudyServiceImpl;
import org.generationcp.middleware.service.pedigree.PedigreeFactory;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

@Configuration
@EnableTransactionManagement
public class MiddlewareFactory {

	@Autowired
	private ContextResolver contextResolver;

	@Autowired
	@Qualifier("WORKBENCH_SessionFactory")
	private SessionFactory WORKBENCH_SessionFactory;

	@Autowired
	private ApplicationContext applicationContext;

	public MiddlewareFactory() {
		super();
	}

	private SessionFactory getSessionFactory() {
		return (SessionFactory) this.applicationContext.getBean(DatasourceUtilities.computeSessionFactoryName(this
			.getCurrentlySelectedCropDBName()));
	}

	private String getCurrentlySelectedCropDBName() {
		return this.contextResolver.resolveDatabaseFromUrl();
	}

	@Bean
	public UserTransaction userTransaction() throws Throwable {
		final UserTransactionImp userTransactionImp = new UserTransactionImp();
		userTransactionImp.setTransactionTimeout(1000);
		return userTransactionImp;
	}

	@Bean(initMethod = "init", destroyMethod = "close")
	public TransactionManager transactionManager() throws Throwable {
		final UserTransactionManager userTransactionManager = new UserTransactionManager();
		userTransactionManager.setForceShutdown(false);
		return userTransactionManager;
	}

	// We do not want the platform transaction manager created per request but in order to handle different corps we need to seaarch for it
	// per request. A hash map to cache
	@Bean
	public PlatformTransactionManager platformTransactionManager() throws Throwable {

		return new JtaTransactionManager(this.userTransaction(), this.transactionManager());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyDataManager getStudyDataManager() {
		return new StudyDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public FieldbookService getFieldbookService() {
		return new FieldbookServiceImpl(this.getCropDatabaseSessionProvider(), this.getCurrentlySelectedCropDBName());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public MethodService getMethodService() {
		return new MethodServiceImpl(this.getCropDatabaseSessionProvider(), this.getCurrentlySelectedCropDBName());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyService getStudyService() {
		return new StudyServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GenotypicDataManager getGenotypicDataManager() {
		return new GenotypicDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public TermDataManager getTermDataManager() {
		return new TermDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyMethodDataManager getOntologyMethodDataManager() {
		return new OntologyMethodDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyPropertyDataManager getOntologyPropertyDataManager() {
		return new OntologyPropertyDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyScaleDataManager getOntologyScaleDataManager() {
		return new OntologyScaleDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyVariableDataManager getOntologyVariableDataManager() {
		return new OntologyVariableDataManagerImpl(this.getOntologyMethodDataManager(), this.getOntologyPropertyDataManager(),
			this.getOntologyScaleDataManager(), this.getFormulaService(), this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public MeasurementVariableTransformer getMeasurementVariableTransformer() {
		return new MeasurementVariableTransformer(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public InventoryDataManager getInventoryDataManager() {
		return new InventoryDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public LocationDataManager getLocationDataManager() {
		return new LocationDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public UserDataManager getUserDataManager() {
		return new UserDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmListManager getGermplasmListManager() {
		return new GermplasmListManagerImpl(this.getCropDatabaseSessionProvider(), this.getCurrentlySelectedCropDBName());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public ListDataProjectSaver getListDataProjectSaver() {
		return new ListDataProjectSaver(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmDataManager getGermplasmDataManager() {
		return new GermplasmDataManagerImpl(this.getCropDatabaseSessionProvider(), this.getCurrentlySelectedCropDBName());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmGroupingService getGermplasmGroupingService() {
		return new GermplasmGroupingServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public PedigreeDataManager getPedigreeDataManager() {
		final PedigreeDataManagerImpl pedigreeDataManager = new PedigreeDataManagerImpl(this.getCropDatabaseSessionProvider());
		pedigreeDataManager.setGermplasmDataManager(this.getGermplasmDataManager());
		return pedigreeDataManager;
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public DataImportService getDataImportService() {
		return new DataImportServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public PedigreeService getPedigreeService() {
		return PedigreeFactory.getPedigreeService(this.getCropDatabaseSessionProvider(),
				this.getCrossExpansionProperties().getProfile(), this.contextResolver.resolveCropNameFromUrl());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public HibernateSessionPerRequestProvider getCropDatabaseSessionProvider() {
		return new HibernateSessionPerRequestProvider(this.getSessionFactory());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public SampleService getSampleService() {
		return new SampleServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public CsvExportSampleListService getCsvExportSampleListService() {
		return new CsvExportSampleListServiceImpl();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public FormulaService getFormulaService() {
		return new FormulaServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public DerivedVariableProcessor getDerivedVariableProcessor() {
		return new DerivedVariableProcessor();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public SampleListService getSampleListService() {
		return new SampleListServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@DependsOn("WORKBENCH_SessionFactory")
	public WorkbenchDataManager getWorkbenchDataManager() {
		return new WorkbenchDataManagerImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	public CrossExpansionProperties getCrossExpansionProperties() {
		return new CrossExpansionProperties();
	}

	@Bean
	public Cloner cloner() {
		return new Cloner();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public BreedingViewImportService getBreedingViewImportService() {
		return new BreedingViewImportServiceImpl();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyDataManager getOntologyDataManager() {
		return new OntologyDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StandardVariableTransformer standardVariableTransformer() {
		return new StandardVariableTransformer(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public DatasetService getDatasetService() {
		return new DatasetServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public DerivedVariableService getDerivedVariableService() {
		return new DerivedVariableServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public MeasurementVariableService getMeasurementVariableService() {
		return new MeasurementVariableServiceImpl(this.getCropDatabaseSessionProvider().getSession());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public PresetService getPresetService() {
		return new PresetServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public SearchRequestService getSearchRequestService() {
		return new SearchRequestServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public ContextUtil contextUtil() {
		return new ContextUtil();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public KeySequenceRegisterService keySequenceRegister() {
		return new KeySequenceRegisterServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public DatasetTypeService getDatasetTypeService() {
		return new DatasetTypeServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public UserService getUserService() {
		return new UserServiceImpl(this.getWorkbenchSessionProvider());
	}

	private HibernateSessionPerRequestProvider getWorkbenchSessionProvider() {
		return new HibernateSessionPerRequestProvider(this.WORKBENCH_SessionFactory);
	}

}
