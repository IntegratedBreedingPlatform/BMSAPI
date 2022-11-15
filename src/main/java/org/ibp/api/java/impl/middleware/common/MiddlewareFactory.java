
package org.ibp.api.java.impl.middleware.common;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.rits.cloning.Cloner;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.service.BreedingViewImportService;
import org.generationcp.commons.service.CsvExportSampleListService;
import org.generationcp.commons.service.StockService;
import org.generationcp.commons.service.impl.BreedingViewImportServiceImpl;
import org.generationcp.commons.service.impl.CsvExportSampleListServiceImpl;
import org.generationcp.commons.service.impl.StockServiceImpl;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.api.brapi.GermplasmListServiceBrapi;
import org.generationcp.middleware.api.brapi.GermplasmListServiceBrapiImpl;
import org.generationcp.middleware.api.brapi.GermplasmServiceBrapi;
import org.generationcp.middleware.api.brapi.GermplasmServiceBrapiImpl;
import org.generationcp.middleware.api.brapi.ObservationServiceBrapi;
import org.generationcp.middleware.api.brapi.ObservationServiceBrapiImpl;
import org.generationcp.middleware.api.brapi.PedigreeServiceBrapi;
import org.generationcp.middleware.api.brapi.PedigreeServiceBrapiImpl;
import org.generationcp.middleware.api.brapi.SampleServiceBrapi;
import org.generationcp.middleware.api.brapi.SampleServiceBrapiImpl;
import org.generationcp.middleware.api.brapi.StudyServiceBrapi;
import org.generationcp.middleware.api.brapi.StudyServiceBrapiImpl;
import org.generationcp.middleware.api.brapi.TrialServiceBrapi;
import org.generationcp.middleware.api.brapi.TrialServiceBrapiImpl;
import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableServiceBrapiImpl;
import org.generationcp.middleware.api.brapi.v2.attribute.AttributeValueServiceBrapi;
import org.generationcp.middleware.api.brapi.v2.attribute.AttributeValueServiceBrapiImpl;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitService;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitServiceImpl;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodServiceImpl;
import org.generationcp.middleware.api.crop.CropService;
import org.generationcp.middleware.api.crop.CropServiceImpl;
import org.generationcp.middleware.api.cropparameter.CropParameterService;
import org.generationcp.middleware.api.cropparameter.CropParameterServiceImpl;
import org.generationcp.middleware.api.file.FileMetadataService;
import org.generationcp.middleware.api.file.FileMetadataServiceImpl;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeServiceImpl;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.germplasm.GermplasmNameServiceImpl;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.germplasm.GermplasmServiceImpl;
import org.generationcp.middleware.api.germplasm.pedigree.GermplasmPedigreeService;
import org.generationcp.middleware.api.germplasm.pedigree.GermplasmPedigreeServiceImpl;
import org.generationcp.middleware.api.germplasm.pedigree.cop.CopService;
import org.generationcp.middleware.api.germplasm.pedigree.cop.CopServiceImpl;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchServiceImpl;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.api.germplasmlist.GermplasmListServiceImpl;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataService;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataServiceImpl;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsService;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsServiceImpl;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.LocationServiceImpl;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeServiceImpl;
import org.generationcp.middleware.api.ontology.OntologyVariableService;
import org.generationcp.middleware.api.ontology.OntologyVariableServiceImpl;
import org.generationcp.middleware.api.program.ProgramFavoriteService;
import org.generationcp.middleware.api.program.ProgramFavoriteServiceImpl;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.api.program.ProgramServiceImpl;
import org.generationcp.middleware.api.role.RoleService;
import org.generationcp.middleware.api.role.RoleServiceImpl;
import org.generationcp.middleware.api.role.RoleTypeService;
import org.generationcp.middleware.api.role.RoleTypeServiceImpl;
import org.generationcp.middleware.api.study.MyStudiesService;
import org.generationcp.middleware.api.study.MyStudiesServiceImpl;
import org.generationcp.middleware.api.study.StudyEntryObservationService;
import org.generationcp.middleware.api.study.StudyEntryObservationServiceImpl;
import org.generationcp.middleware.hibernate.DatasourceUtilities;
import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
import org.generationcp.middleware.manager.GenotypicDataManagerImpl;
import org.generationcp.middleware.manager.GermplasmDataManagerImpl;
import org.generationcp.middleware.manager.GermplasmListManagerImpl;
import org.generationcp.middleware.manager.InventoryDataManagerImpl;
import org.generationcp.middleware.manager.OntologyDataManagerImpl;
import org.generationcp.middleware.manager.PedigreeDataManagerImpl;
import org.generationcp.middleware.manager.PresetServiceImpl;
import org.generationcp.middleware.manager.SearchRequestServiceImpl;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.generationcp.middleware.manager.UserProgramStateDataManagerImpl;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.manager.api.PresetService;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.UserProgramStateDataManager;
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
import org.generationcp.middleware.operation.builder.DataSetBuilder;
import org.generationcp.middleware.operation.builder.StockBuilder;
import org.generationcp.middleware.operation.builder.TrialEnvironmentBuilder;
import org.generationcp.middleware.operation.builder.WorkbookBuilder;
import org.generationcp.middleware.operation.saver.WorkbookSaver;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.operation.transformer.etl.StandardVariableTransformer;
import org.generationcp.middleware.ruleengine.generator.SeedSourceGenerator;
import org.generationcp.middleware.ruleengine.impl.RulesServiceImpl;
import org.generationcp.middleware.ruleengine.newnaming.impl.GermplasmNamingServiceImpl;
import org.generationcp.middleware.ruleengine.newnaming.service.GermplasmNamingService;
import org.generationcp.middleware.ruleengine.service.RulesService;
import org.generationcp.middleware.service.DataImportServiceImpl;
import org.generationcp.middleware.service.FieldbookServiceImpl;
import org.generationcp.middleware.service.MethodServiceImpl;
import org.generationcp.middleware.service.OntologyServiceImpl;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.service.api.KeySequenceRegisterService;
import org.generationcp.middleware.service.api.MethodService;
import org.generationcp.middleware.service.api.NamingConfigurationService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.SampleListService;
import org.generationcp.middleware.service.api.SampleService;
import org.generationcp.middleware.service.api.analysis.SiteAnalysisService;
import org.generationcp.middleware.service.api.audit.GermplasmAuditService;
import org.generationcp.middleware.service.api.crop.CropGenotypingParameterService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.derived_variables.DerivedVariableService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.generationcp.middleware.service.api.feedback.FeedbackService;
import org.generationcp.middleware.service.api.inventory.LotAttributeService;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.service.api.ontology.BreedingMethodValidator;
import org.generationcp.middleware.service.api.ontology.LocationValidator;
import org.generationcp.middleware.service.api.ontology.PersonValidator;
import org.generationcp.middleware.service.api.ontology.VariableDataValidatorFactory;
import org.generationcp.middleware.service.api.ontology.VariableDataValidatorFactoryImpl;
import org.generationcp.middleware.service.api.permission.PermissionServiceImpl;
import org.generationcp.middleware.service.api.releasenote.ReleaseNoteService;
import org.generationcp.middleware.service.api.rpackage.RPackageService;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.api.study.advance.AdvanceService;
import org.generationcp.middleware.service.api.study.generation.ExperimentDesignService;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceService;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.service.impl.GermplasmGroupingServiceImpl;
import org.generationcp.middleware.service.impl.KeySequenceRegisterServiceImpl;
import org.generationcp.middleware.service.impl.NamingConfigurationServiceImpl;
import org.generationcp.middleware.service.impl.analysis.SiteAnalysisServiceImpl;
import org.generationcp.middleware.service.impl.audit.GermplasmAuditServiceImpl;
import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterServiceImpl;
import org.generationcp.middleware.service.impl.dataset.DatasetServiceImpl;
import org.generationcp.middleware.service.impl.dataset.DatasetTypeServiceImpl;
import org.generationcp.middleware.service.impl.derived_variables.DerivedVariableServiceImpl;
import org.generationcp.middleware.service.impl.derived_variables.FormulaServiceImpl;
import org.generationcp.middleware.service.impl.feedback.FeedbackServiceImpl;
import org.generationcp.middleware.service.impl.inventory.LotAttributeServiceImpl;
import org.generationcp.middleware.service.impl.inventory.LotServiceImpl;
import org.generationcp.middleware.service.impl.inventory.PlantingServiceImpl;
import org.generationcp.middleware.service.impl.inventory.TransactionServiceImpl;
import org.generationcp.middleware.service.impl.releasenote.ReleaseNoteServiceImpl;
import org.generationcp.middleware.service.impl.rpackage.RPackageServiceImpl;
import org.generationcp.middleware.service.impl.study.SampleListServiceImpl;
import org.generationcp.middleware.service.impl.study.SampleServiceImpl;
import org.generationcp.middleware.service.impl.study.StudyEntryServiceImpl;
import org.generationcp.middleware.service.impl.study.StudyInstanceServiceImpl;
import org.generationcp.middleware.service.impl.study.StudyServiceImpl;
import org.generationcp.middleware.service.impl.study.advance.AdvanceServiceImpl;
import org.generationcp.middleware.service.impl.study.generation.ExperimentDesignServiceImpl;
import org.generationcp.middleware.service.impl.study.generation.ExperimentModelGenerator;
import org.generationcp.middleware.service.impl.study.germplasm.source.GermplasmStudySourceServiceImpl;
import org.generationcp.middleware.service.impl.user.UserServiceImpl;
import org.generationcp.middleware.service.impl.workbench.WorkbenchServiceImpl;
import org.generationcp.middleware.service.pedigree.PedigreeFactory;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hibernate.SessionFactory;
import org.ibp.api.java.germplasm.GermplasmCodeGenerationService;
import org.ibp.api.java.impl.middleware.germplasm.GermplasmCodeGenerationServiceImpl;
import org.ibp.api.java.impl.middleware.germplasm.cop.CopServiceAsync;
import org.ibp.api.java.impl.middleware.germplasm.cop.CopServiceAsyncImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Note: this configuration now contains beans not only from Middleware, so it's used as a general purpose factory
 * TODO rename
 */
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
	public CropParameterService getCropParameterService() {
		return new CropParameterServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyDataManager getStudyDataManager() {
		return new StudyDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public FieldbookService getFieldbookService() {
		return new FieldbookServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public MethodService getMethodService() {
		return new MethodServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyService getStudyService() {
		return new StudyServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public MyStudiesService getMyStudiesService() {
		return new MyStudiesServiceImpl(this.getCropDatabaseSessionProvider());
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
	public OntologyVariableService getOntologyVariableService() {
		return new OntologyVariableServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public OntologyService getOntologyService() {
		return new OntologyServiceImpl(this.getCropDatabaseSessionProvider());
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
		return new MeasurementVariableTransformer();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public InventoryDataManager getInventoryDataManager() {
		return new InventoryDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmListManager getGermplasmListManager() {
		return new GermplasmListManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmDataManager getGermplasmDataManager() {
		return new GermplasmDataManagerImpl(this.getCropDatabaseSessionProvider());
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

	/**
	 * can be used to create async beans in thread context, not bound to request
	 * Beans that inject these "async" beans must have request context,
	 * to be able to resolve connection parameters (cropName, programUUID)
	 */
	@Bean
	@Scope(value = "prototype")
	public HibernateSessionPerRequestProvider getCropDatabaseSessionPrototypeProvider() {
		/*
		 * There is no much difference with HibernateSessionPerThreadProvider, perhaps it's not even needed anymore
		 * TODO review HibernateSessionProvider classes
		 */
		return new HibernateSessionPerRequestProvider(this.getSessionFactory());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public SampleService getSampleService() {
		return new SampleServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public SampleServiceBrapi getSampleServiceBrapi() {
		return new SampleServiceBrapiImpl(this.getCropDatabaseSessionProvider());
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
	public RoleService getRoleService() {
		return new RoleServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	@DependsOn("WORKBENCH_SessionFactory")
	public UserService getUserService() {
		return new UserServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	@DependsOn("WORKBENCH_SessionFactory")
	public ProgramService getProgramService() {
		return new ProgramServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	@DependsOn("WORKBENCH_SessionFactory")
	public PermissionServiceImpl getPermissionService() {
		return new PermissionServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Autowired
	private Environment environment;

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
		return new StandardVariableTransformer();
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
	public LotAttributeService getLotAttributeService() {
		return new LotAttributeServiceImpl(this.getCropDatabaseSessionProvider());
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
	public GermplasmService getGermplasmService() {
		return new GermplasmServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmServiceBrapi getGermplasmServiceBrapi() {
		return new GermplasmServiceBrapiImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmListServiceBrapi getGermplasmListServiceBrapi() {
		return new GermplasmListServiceBrapiImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmPedigreeService getGermplasmPedigreeService() {
		return new GermplasmPedigreeServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public CopService getCopServiceMiddleware() {
		return new CopServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public CopServiceAsync getCopServiceAsync() {
		return new CopServiceAsyncImpl(this.getCropDatabaseSessionPrototypeProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmAttributeService getGermplasmAttributeService() {
		return new GermplasmAttributeServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmNameTypeService getGermplasmNameTypeService() {
		return new GermplasmNameTypeServiceImpl(this.getCropDatabaseSessionProvider());
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

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public VariableDataValidatorFactory getVariableDataValidatorFactory() {
		return new VariableDataValidatorFactoryImpl();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public PersonValidator getPersonValidator() {
		return new PersonValidator();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public BreedingMethodValidator getBreedingMethodValidator() {
		return new BreedingMethodValidator();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public LocationValidator getLocationValidator() {
		return new LocationValidator();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmNameService getGermplasmNameService() {
		return new GermplasmNameServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public UserProgramStateDataManager getUserProgramDataManager() {
		return new UserProgramStateDataManagerImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmAuditService getAuditService() {
		return new GermplasmAuditServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@DependsOn("WORKBENCH_SessionFactory")
	public WorkbenchServiceImpl getWorkbenchService() {
		return new WorkbenchServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	@DependsOn("WORKBENCH_SessionFactory")
	public ReleaseNoteService getReleaseNoteService() {
		return new ReleaseNoteServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmCodeGenerationService getGermplasmCodeGenerationService() {
		return new GermplasmCodeGenerationServiceImpl();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public NamingConfigurationService getNamingConfigurationService() {
		return new NamingConfigurationServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public FileMetadataService getFileMetadataService() {
		return new FileMetadataServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public TrialServiceBrapi getTrialServiceBrapi() {
		return new TrialServiceBrapiImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public VariableServiceBrapi getVariableServiceBrapi() {
		return new VariableServiceBrapiImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyServiceBrapi getStudyServiceBrapi() {
		return new StudyServiceBrapiImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public ObservationServiceBrapi getObservationServiceBrapi() {
		return new ObservationServiceBrapiImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public ProgramFavoriteService getProgramFavoriteService() {
		return new ProgramFavoriteServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public GermplasmListDataService getGermplasmListDataSearchService() {
		return new GermplasmListDataServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public AttributeValueServiceBrapi getAttributeValueService() {
		return new AttributeValueServiceBrapiImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public SiteAnalysisService getSiteAnalysisService() {
		return new SiteAnalysisServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public CropGenotypingParameterService getCropGenotypingParameterService() {
		return new CropGenotypingParameterServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public PedigreeServiceBrapi getPedigreeServiceBrapi() {
		return new PedigreeServiceBrapiImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@DependsOn("WORKBENCH_SessionFactory")
	public FeedbackService getFeedbackService() {
		return new FeedbackServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	@DependsOn("WORKBENCH_SessionFactory")
	public CropService getCropService() {
		return new CropServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	@DependsOn("WORKBENCH_SessionFactory")
	public RoleTypeService getRoleTypeService() {
		return new RoleTypeServiceImpl(this.getWorkbenchSessionProvider());
	}

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StudyEntryObservationService getStudyEntryObservationService() {
		return new StudyEntryObservationServiceImpl(this.getCropDatabaseSessionProvider());
	}

	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public AdvanceService getAdvanceService() {
		return new AdvanceServiceImpl(this.getCropDatabaseSessionProvider());
	}

	private HibernateSessionPerRequestProvider getWorkbenchSessionProvider() {
		return new HibernateSessionPerRequestProvider(this.WORKBENCH_SessionFactory);
	}

	@Bean
	public SeedSourceGenerator getSeedSourceGenerator() {
		return new SeedSourceGenerator();
	}

}
