
package org.ibp.api.java.impl.middleware.common;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.rits.cloning.Cloner;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.ruleengine.impl.RulesServiceImpl;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.generationcp.commons.service.BreedingViewImportService;
import org.generationcp.commons.service.CsvExportSampleListService;
import org.generationcp.commons.service.GermplasmNamingService;
import org.generationcp.commons.service.StockService;
import org.generationcp.commons.service.impl.BreedingViewImportServiceImpl;
import org.generationcp.commons.service.impl.CsvExportSampleListServiceImpl;
import org.generationcp.commons.service.impl.GermplasmNamingServiceImpl;
import org.generationcp.commons.service.impl.StockServiceImpl;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.api.attribute.AttributeService;
import org.generationcp.middleware.api.attribute.AttributeServiceImpl;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitService;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitServiceImpl;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodServiceImpl;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchServiceImpl;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.api.germplasmlist.GermplasmListServiceImpl;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsService;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsServiceImpl;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.LocationServiceImpl;
import org.generationcp.middleware.hibernate.DatasourceUtilities;
import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
import org.generationcp.middleware.manager.*;
import org.generationcp.middleware.manager.api.*;
import org.generationcp.middleware.manager.ontology.*;
import org.generationcp.middleware.manager.ontology.api.*;
import org.generationcp.middleware.operation.builder.DataSetBuilder;
import org.generationcp.middleware.operation.builder.StockBuilder;
import org.generationcp.middleware.operation.builder.TrialEnvironmentBuilder;
import org.generationcp.middleware.operation.builder.WorkbookBuilder;
import org.generationcp.middleware.operation.saver.WorkbookSaver;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.operation.transformer.etl.StandardVariableTransformer;
import org.generationcp.middleware.service.DataImportServiceImpl;
import org.generationcp.middleware.service.FieldbookServiceImpl;
import org.generationcp.middleware.service.InventoryServiceImpl;
import org.generationcp.middleware.service.MethodServiceImpl;
import org.generationcp.middleware.service.api.*;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.derived_variables.DerivedVariableService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.service.api.permission.PermissionServiceImpl;
import org.generationcp.middleware.service.api.rpackage.RPackageService;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.api.study.generation.ExperimentDesignService;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceService;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.service.impl.GermplasmGroupingServiceImpl;
import org.generationcp.middleware.service.impl.KeySequenceRegisterServiceImpl;
import org.generationcp.middleware.service.impl.dataset.DatasetServiceImpl;
import org.generationcp.middleware.service.impl.dataset.DatasetTypeServiceImpl;
import org.generationcp.middleware.service.impl.derived_variables.DerivedVariableServiceImpl;
import org.generationcp.middleware.service.impl.derived_variables.FormulaServiceImpl;
import org.generationcp.middleware.service.impl.inventory.LotServiceImpl;
import org.generationcp.middleware.service.impl.inventory.PlantingServiceImpl;
import org.generationcp.middleware.service.impl.inventory.TransactionServiceImpl;
import org.generationcp.middleware.service.impl.rpackage.RPackageServiceImpl;
import org.generationcp.middleware.service.impl.study.*;
import org.generationcp.middleware.service.impl.study.generation.ExperimentDesignServiceImpl;
import org.generationcp.middleware.service.impl.study.generation.ExperimentModelGenerator;
import org.generationcp.middleware.service.impl.study.germplasm.source.GermplasmStudySourceServiceImpl;
import org.generationcp.middleware.service.impl.user.UserServiceImpl;
import org.generationcp.middleware.service.pedigree.PedigreeFactory;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

	@Autowired
	private Environment environment;

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
	public GermplasmListManager getGermplasmListManager() {
		return new GermplasmListManagerImpl(this.getCropDatabaseSessionProvider(), this.getCurrentlySelectedCropDBName());
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
	@DependsOn("WORKBENCH_SessionFactory")
	public UserService getUserService() {
		return new UserServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	@DependsOn("WORKBENCH_SessionFactory")
	public PermissionServiceImpl getPermissionService() {
		return new PermissionServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	public CrossExpansionProperties getCrossExpansionProperties() {
		final String defaultLevel = this.environment.getProperty("default.generation.level");
		final String pedigreeProfile = this.environment.getProperty("pedigree.profile");
		final List<String> list = Arrays.asList(this.environment.getProperty("hybrid.breeding.methods").split(","));

		final CrossExpansionProperties crossExpansionProperties = new CrossExpansionProperties();
		crossExpansionProperties.setDefaultLevel(Integer.parseInt(defaultLevel));
		crossExpansionProperties.setProfile(pedigreeProfile);
		crossExpansionProperties
			.setHybridBreedingMethods(list.stream().map(Integer::valueOf).collect(Collectors.toSet()));
		return crossExpansionProperties;
	}

	@Bean
	public Cloner cloner() {
		return new Cloner();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmNamingService getGermplasmNamingService() {
		return new GermplasmNamingServiceImpl();
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
	public TrialEnvironmentBuilder trialEnvironmentBuilder() {
		return new TrialEnvironmentBuilder(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public DataSetBuilder dataSetBuilder() {
		return new DataSetBuilder(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StockBuilder stockBuilder() {
		return new StockBuilder(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public WorkbookBuilder workbookBuilder() {
		return new WorkbookBuilder(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public WorkbookSaver workbookSaver() {
		return new WorkbookSaver(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public ExperimentDesignService experimentDesignMiddlewareService() {
		return new ExperimentDesignServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public LotService getLotService() {
		return new LotServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public TransactionServiceImpl getTransactionService() {
		return new TransactionServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public PlantingServiceImpl getPlantingService() {
		return new PlantingServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyTransactionsService getStudyTransactionsServiceImpl() {
		return new StudyTransactionsServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyInstanceService studyInstanceMiddlewareService() {
		return new StudyInstanceServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public RPackageService rPackageMiddlewareService() {
		return new RPackageServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StockService getStockService() {
		return new StockServiceImpl();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public RulesService getRulesService() {
		return new RulesServiceImpl();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public InventoryService getInventoryService() {
		return new InventoryServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public ObservationUnitService getObservationUnitService() {
		return new ObservationUnitServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyEntryService getStudyEntryService() {
		return new StudyEntryServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmStudySourceService getGermplasmStudySourceService() {
		return new GermplasmStudySourceServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmListService getGermplasmListService() {
		return new GermplasmListServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmSearchService getGermplasmSearchService() {
		return new GermplasmSearchServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public AttributeService getAttributeService() {
		return new AttributeServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public BreedingMethodService getBreedingMethodService() {
		return new BreedingMethodServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public LocationService getLocationService() {
		return new LocationServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public ExperimentModelGenerator getExperimentModelGenerator() {
		return new ExperimentModelGenerator(this.getCropDatabaseSessionProvider());
	}

	private HibernateSessionPerRequestProvider getWorkbenchSessionProvider() {
		return new HibernateSessionPerRequestProvider(this.WORKBENCH_SessionFactory);
	}

}
