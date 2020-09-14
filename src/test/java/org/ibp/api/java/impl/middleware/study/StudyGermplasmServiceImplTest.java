package org.ibp.api.java.impl.middleware.study;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyGermplasmValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StudyGermplasmServiceImplTest {

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Mock
	private StudyGermplasmValidator studyGermplasmValidator;

	@Mock
	private GermplasmListValidator germplasmListValidator;

	@Mock
	private org.ibp.api.java.germplasm.GermplamListService germplasmListService;

	@Mock
	private TermValidator termValidator;

	@Mock
	private org.generationcp.middleware.service.api.study.StudyGermplasmService middlewareStudyGermplasmService;

	@InjectMocks
	private final StudyGermplasmServiceImpl studyGermplasmService = new StudyGermplasmServiceImpl();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	private final Random random = new Random();

	@Test
	public void testReplaceStudyGermplasm() {
		final Integer studyId = random.nextInt();
		final Integer entryId = random.nextInt();
		final Integer newGid = random.nextInt();
		final String crossExpansion = RandomStringUtils.randomAlphabetic(20);
		Mockito.doReturn(crossExpansion).when(this.pedigreeService).getCrossExpansion(newGid, this.crossExpansionProperties);
		final StudyGermplasmDto dto = new StudyGermplasmDto();
		dto.setGermplasmId(newGid);
		this.studyGermplasmService.replaceStudyGermplasm(studyId, entryId, dto);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.studyGermplasmValidator).validate(studyId, entryId, newGid);
		Mockito.verify(this.middlewareStudyGermplasmService).replaceStudyGermplasm(studyId, entryId, newGid, crossExpansion);
	}

	@Test
	public void testCreateStudyGermplasmList() {

		final GermplasmList germplasmList = new GermplasmList();
		List<GermplasmListData> listData = new ArrayList<>();
		germplasmList.setListData(listData);

		final Integer studyId = random.nextInt();
		final Integer germplasmListId = random.nextInt();

		Mockito.when(this.germplasmListService.getGermplasmList(germplasmListId)).thenReturn(germplasmList);

		this.studyGermplasmService.createStudyGermplasmList(studyId, germplasmListId);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(germplasmListId);
		Mockito.verify(this.studyGermplasmValidator).validateStudyAlreadyHasStudyGermplasm(studyId);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.middlewareStudyGermplasmService).saveStudyGermplasm(ArgumentMatchers.eq(studyId), ArgumentMatchers.anyList());

	}

	@Test
	public void testDeleteStudyGermplasm() {
		final Integer studyId = random.nextInt();
		this.studyGermplasmService.deleteStudyGermplasm(studyId);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.middlewareStudyGermplasmService).deleteStudyEntries(studyId);
	}

	@Test
	public void testUpdateStudyEntryProperty() {

		final Integer studyId = random.nextInt();
		final Integer variableId = random.nextInt();
		final Integer studyEntryPropertyId = random.nextInt();
		final StudyEntryPropertyData studyEntryPropertyData = new StudyEntryPropertyData();
		studyEntryPropertyData.setVariableId(variableId);
		studyEntryPropertyData.setStudyEntryPropertyId(studyEntryPropertyId);
		this.studyGermplasmService.updateStudyEntryProperty(studyId, studyEntryPropertyData);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.termValidator).validate(variableId);
		Mockito.verify(this.studyGermplasmValidator).validateStudyEntryProperty(studyEntryPropertyId);
		Mockito.verify(this.middlewareStudyGermplasmService).updateStudyEntryProperty(studyId, studyEntryPropertyData);
	}

}
