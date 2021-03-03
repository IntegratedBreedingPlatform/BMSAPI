
package org.ibp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitService;
import org.generationcp.middleware.manager.api.PresetService;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.util.Debug;
import org.ibp.api.brapi.v2.validation.CropValidator;
import org.ibp.api.java.crop.CropService;
import org.ibp.api.java.design.runner.DesignRunner;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.design.runner.MockDesignRunnerImpl;
import org.ibp.api.java.impl.middleware.security.SecurityServiceImpl;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.java.rpackage.RPackageService;
import org.ibp.api.java.study.StudyEntryService;
import org.ibp.api.java.study.StudyInstanceService;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class must only be extended by tests which require Spring context to be loaded.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = Main.class)
public abstract class ApiUnitTestBase {

	protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(),
		Charset.forName("utf8"));

	protected final MediaType csvContentType = new MediaType(MediaType.TEXT_PLAIN.getType(), MediaType.TEXT_PLAIN.getSubtype(),
		Charset.forName("utf8"));

	protected final String cropName = "maize";
	protected final String programUuid = UUID.randomUUID().toString();

	protected MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	protected WorkbenchDataManager workbenchDataManager;

	@Autowired
	protected StudyDataManager studyDataManager;

	@Autowired
	protected ObjectMapper jsonMapper;


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public CropValidator cropValidator() {
			return Mockito.mock(CropValidator.class);
		}

		@Bean
		@Primary
		public CropService cropService() {
			return Mockito.mock(CropService.class);
		}

		@Bean
		@Primary
		public WorkbenchDataManager workbenchDataManager() {
			return Mockito.mock(WorkbenchDataManager.class);
		}

		@Bean
		@Primary
		public org.generationcp.middleware.service.api.study.StudyService getStudyServiceMW() {
			return Mockito.mock(org.generationcp.middleware.service.api.study.StudyService.class);
		}

		@Bean
		@Primary
		public StudyDataManager studyDataManager() {
			return Mockito.mock(StudyDataManager.class);
		}

		@Bean
		@Primary
		public UserService userService() {
			return Mockito.mock(UserService.class);
		}

		@Bean
		@Primary
		public FormulaService formulaService() {
			return Mockito.mock(FormulaService.class);
		}

		@Bean
		@Primary
		public PresetService presetService() {
			return Mockito.mock(PresetService.class);
		}

		@Bean
		@Primary
		public DerivedVariableProcessor derivedVariableProcessor() {
			return Mockito.mock(DerivedVariableProcessor.class);
		}

		@Bean
		@Primary
		public TermDataManager termDataManager() {
			return Mockito.mock(TermDataManager.class);
		}

		@Bean
		@Primary
		public OntologyVariableDataManager ontologyVariableDataManager() {
			return Mockito.mock(OntologyVariableDataManager.class);
		}

		@Bean
		@Primary
		public SecurityServiceImpl securityService() {
			return Mockito.mock(SecurityServiceImpl.class);
		}

		@Bean
		@Primary
		public HttpServletRequest httpServletRequest() {
			return Mockito.mock(HttpServletRequest.class);
		}

		@Bean
		@Primary
		public ContextUtil getContextUtil() {
			return Mockito.mock(ContextUtil.class);
		}

		@Bean
		@Primary
		public SearchRequestService searchRequestService() {
			return Mockito.mock(SearchRequestService.class);
		}

		@Bean
		@Primary
		public DatasetTypeService datasetTypeService() {
			return Mockito.mock(DatasetTypeService.class);
		}

		@Bean
		@Primary
		public DesignRunner getDesignRunner() {
			return new MockDesignRunnerImpl();
		}

		@Bean
		@Primary
		public RPackageService rPackageService() {
			return Mockito.mock(RPackageService.class);
		}

		@Bean
		@Primary
		public ObservationUnitService observationUnitService() {
			return Mockito.mock(ObservationUnitService.class);
		}

		@Bean
		@Primary
		public StudyEntryService studyEntryService() {
			return Mockito.mock(StudyEntryService.class);
		}

		@Bean
		@Primary
		public StudyInstanceService studyInstanceService() {
			return Mockito.mock(StudyInstanceService.class);
		}

		@Bean
		@Primary
		public org.generationcp.middleware.service.api.study.StudyInstanceService studyInstanceMiddlewareService() {
			return Mockito.mock(org.generationcp.middleware.service.api.study.StudyInstanceService.class);
		}

		@Bean
		@Primary
		public GermplasmService germplasmService() {
			return Mockito.mock(GermplasmService.class);

		}

		@Bean
		@Primary
		public org.generationcp.middleware.api.germplasm.GermplasmService germplasmMiddlewareService() {
			return Mockito.mock(org.generationcp.middleware.api.germplasm.GermplasmService.class);

		}

		@Bean
		@Primary
		public ProgramService programService() {
			return Mockito.mock(ProgramService.class);
		}

	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
		Mockito.doReturn(new CropType(this.cropName)).when(this.workbenchDataManager).getCropTypeByName(this.cropName);
		this.loadPreAuthorizedRole();
	}

	@After
	public void tearDown() throws Exception {
		this.mockMvc = null;
	}

	public byte[] convertObjectToByte(final Object object) throws JsonProcessingException {
		final ObjectWriter ow = this.jsonMapper.writer().withDefaultPrettyPrinter();
		Debug.println("Request:" + ow.writeValueAsString(object));
		return ow.writeValueAsBytes(object);
	}

	/**
	 * This method load preAuthorized role to test services that PreAuthorize role is required.
	 */
	public void loadPreAuthorizedRole() {
		final List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ADMIN"));
		UsernamePasswordAuthenticationToken loggedInUser =
			new UsernamePasswordAuthenticationToken("User", "Password@##@$@%$%$#^", authorities);
		SecurityContextHolder.getContext().setAuthentication(loggedInUser);
	}
}
