package org.ibp.api.rest.dataset.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.user.ContactDto;
import org.ibp.api.java.impl.middleware.study.validator.TrialImportRequestValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class TrialImportRequestValidatorTest {

	private static final String CROP = "maize";
	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphabetic(10);
	private static final String TRIAL_NAME = RandomStringUtils.randomAlphabetic(10);

	@Mock
	private ProgramService programService;

	@Mock
	private FieldbookService fieldbookService;

	@InjectMocks
	private TrialImportRequestValidator trialImportRequestValidator;

	@Before
	public void setUp() {
		Mockito.when(this.programService.getProjectByUuidAndCrop(PROGRAM_UUID, CROP)).thenReturn(new Project());
		Mockito.when(this.fieldbookService.getProjectIdByNameAndProgramUUID(TRIAL_NAME, PROGRAM_UUID)).thenReturn(null);
	}

	@Test
	public void testPruneTrialsInvalidForImport_Success() {
		final List<TrialImportRequestDTO> trialImportRequestDTOList = this.createTrialImportRequestDTOList();
		// Valid external references
		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(200));
		externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(200));
		trialImportRequestDTOList.get(0).setExternalReferences(Collections.singletonList(externalReferenceDTO));
		// Valid additional Info
		trialImportRequestDTOList.get(0).setAdditionalInfo(Collections.singletonMap(RandomStringUtils.randomAlphabetic(50), RandomStringUtils.randomAlphabetic(200)));
		// Valid Contact
		trialImportRequestDTOList.get(0).setContacts(Collections.singletonList(
			new ContactDto(RandomStringUtils.randomAlphabetic(50), RandomStringUtils.randomAlphabetic(50),
				RandomStringUtils.randomAlphabetic(50), RandomStringUtils.randomAlphabetic(50))));
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(trialImportRequestDTOList, CROP);
		Assert.assertFalse(result.hasErrors());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereTrialNameIsNull() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		dtoList.get(0).setTrialName(null);
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.name.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereTrialNameIsDuplicateInImport() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		dtoList.addAll(this.createTrialImportRequestDTOList());
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.name.duplicate.import", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereTrialNameExceedsMaxLength() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		dtoList.get(0).setTrialName(RandomStringUtils.randomAlphabetic(226));
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.name.exceed.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereProgramDbIdIsInvalid() {
		Mockito.when(this.programService.getProjectByUuidAndCrop(PROGRAM_UUID, CROP)).thenReturn(null);
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.program.dbid.invalid", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereProgramDbIdIsNull() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		dtoList.get(0).setProgramDbId(null);
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.program.dbid.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereDescriptionIsNull() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		dtoList.get(0).setTrialDescription(null);
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.description.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereDescriptionExceedsMaxLength() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		dtoList.get(0).setTrialDescription(RandomStringUtils.randomAlphabetic(226));
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.description.exceed.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereStartDateIsNull() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		dtoList.get(0).setStartDate(null);
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.start.date.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereStartDateIsInvalid() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		dtoList.get(0).setStartDate("20210421");
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.start.date.invalid.format", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereEndDateIsInvalid() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		dtoList.get(0).setEndDate("20210524");
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.end.date.invalid.format", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereEndDateIsBeforeStartDate() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		dtoList.get(0).setEndDate("2021-05-24");
		dtoList.get(0).setStartDate("2021-05-25");
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.end.date.invalid.date", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereAdditionalInfoHasEmptyStringKey() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		final Map<String, String> additionalInfo = new HashMap<>();
		additionalInfo.put("", "value");
		dtoList.get(0).setAdditionalInfo(additionalInfo);
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.additional.info.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereAdditionalInfoHasNullKey() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		final Map<String, String> additionalInfo = new HashMap<>();
		additionalInfo.put(null, "value");
		dtoList.get(0).setAdditionalInfo(additionalInfo);
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.additional.info.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereAdditionalInfoValueExceedsMaxLength() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		final Map<String, String> additionalInfo = new HashMap<>();
		additionalInfo.put("key", RandomStringUtils.randomAlphabetic(256));
		dtoList.get(0).setAdditionalInfo(additionalInfo);
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.additional.info.value.exceeded.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereExternalReferenceHasMissingInfo() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
		externalReferenceDTOS.add(new ExternalReferenceDTO());
		dtoList.get(0).setExternalReferences(externalReferenceDTOS);
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.reference.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereExternalReferenceIdExceedsLength() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(2001));
		externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(200));
		externalReferenceDTOS.add(externalReferenceDTO);
		dtoList.get(0).setExternalReferences(externalReferenceDTOS);
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.reference.id.exceeded.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereExternalReferenceSourceExceedsLength() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(200));
		externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(256));
		externalReferenceDTOS.add(externalReferenceDTO);
		dtoList.get(0).setExternalReferences(externalReferenceDTOS);
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.reference.source.exceeded.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereContactIsNull() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		dtoList.get(0).setContacts(Collections.singletonList(null));
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.contact.info.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereContactFieldsAreEmpty() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		dtoList.get(0).setContacts(Collections.singletonList(new ContactDto()));
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.contact.info.empty.values", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereContactNameExceedsLength() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		final ContactDto contact = new ContactDto();
		contact.setName(RandomStringUtils.randomAlphabetic(260));
		dtoList.get(0).setContacts(Collections.singletonList(contact));
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.contact.info.value.exceeded.length", result.getAllErrors().get(0).getCode());
		Assert.assertEquals(new String [] {"1", "name"}, result.getAllErrors().get(0).getArguments());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereContactEmailExceedsLength() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		final ContactDto contact = new ContactDto();
		contact.setEmail(RandomStringUtils.randomAlphabetic(260));
		dtoList.get(0).setContacts(Collections.singletonList(contact));
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.contact.info.value.exceeded.length", result.getAllErrors().get(0).getCode());
		Assert.assertEquals(new String [] {"1", "email"}, result.getAllErrors().get(0).getArguments());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereContactInstituteExceedsLength() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		final ContactDto contact = new ContactDto();
		contact.setInstituteName(RandomStringUtils.randomAlphabetic(260));
		dtoList.get(0).setContacts(Collections.singletonList(contact));
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.contact.info.value.exceeded.length", result.getAllErrors().get(0).getCode());
		Assert.assertEquals(new String [] {"1", "instituteName"}, result.getAllErrors().get(0).getArguments());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereContactTypeExceedsLength() {
		final List<TrialImportRequestDTO> dtoList = this.createTrialImportRequestDTOList();
		final ContactDto contact = new ContactDto();
		contact.setType(RandomStringUtils.randomAlphabetic(260));
		dtoList.get(0).setContacts(Collections.singletonList(contact));
		final BindingResult result = this.trialImportRequestValidator.pruneTrialsInvalidForImport(dtoList, CROP);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("trial.import.contact.info.value.exceeded.length", result.getAllErrors().get(0).getCode());
		Assert.assertEquals(new String [] {"1", "type"}, result.getAllErrors().get(0).getArguments());
	}

	public List<TrialImportRequestDTO> createTrialImportRequestDTOList() {
		final List<TrialImportRequestDTO> trialImportRequestDTOList = new ArrayList<>();
		final TrialImportRequestDTO dto = new TrialImportRequestDTO();
		dto.setTrialName(TRIAL_NAME);
		dto.setTrialDescription(RandomStringUtils.randomAlphabetic(10));
		dto.setStartDate("2021-05-25");
		dto.setProgramDbId(PROGRAM_UUID);
		trialImportRequestDTOList.add(dto);
		return trialImportRequestDTOList;
	}

}
