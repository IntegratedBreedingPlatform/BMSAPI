package org.ibp.api.java.impl.middleware.germplasm.brapi;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.ibp.api.brapi.StudyServiceBrapi;
import org.ibp.api.brapi.v2.study.StudyImportResponse;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.study.validator.StudyImportRequestValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.nullValue;

@RunWith(MockitoJUnitRunner.class)
public class StudyServiceBrapiImplTest {

	@Mock
	private SecurityService securityService;

	@Mock
	private StudyImportRequestValidator studyImportRequestValidator;

	@Mock
	private org.generationcp.middleware.api.brapi.StudyServiceBrapi middlewareStudyServiceBrapi;

	@InjectMocks
	private StudyServiceBrapi studyServiceBrapi;

	private WorkbenchUser testUser;

	@Before
	public void init() {
		if (this.testUser == null) {
			this.testUser = new WorkbenchUser(new Random().nextInt(100));
			Mockito.doReturn(this.testUser).when(this.securityService).getCurrentlyLoggedInUser();
		}
	}

	@Test
	public void testCreateStudies_AllCreated(){
		final String crop = RandomStringUtils.randomAlphabetic(10);
		final BindingResult result = Mockito.mock(BindingResult.class);
		Mockito.doReturn(false).when(result).hasErrors();
		Mockito.doReturn(result).when(this.studyImportRequestValidator).pruneStudiesInvalidForImport(ArgumentMatchers.anyList());

		final List<StudyImportRequestDTO> importList = new ArrayList<>();
		final StudyImportRequestDTO request1 = new StudyImportRequestDTO();
		request1.setTrialDbId(RandomStringUtils.randomAlphabetic(20));
		final StudyImportRequestDTO request2 = new StudyImportRequestDTO();
		request2.setTrialDbId(RandomStringUtils.randomAlphabetic(20));
		importList.add(request1);
		importList.add(request2);
		final StudyInstanceDto study1 = new StudyInstanceDto();
		study1.setTrialDbId(request1.getTrialDbId());
		study1.setStudyDbId(String.valueOf(new Random().nextInt()));
		final StudyInstanceDto study2 = new StudyInstanceDto();
		study2.setTrialDbId(request2.getTrialDbId());
		study2.setStudyDbId(String.valueOf(new Random().nextInt()));
		final List<StudyInstanceDto> studies = Arrays.asList(study1, study2);
		Mockito.doReturn(studies).when(this.middlewareStudyServiceBrapi).saveStudyInstances(crop, importList, this.testUser.getUserid());

		final StudyImportResponse importResponse = this.studyServiceBrapi.createStudies(crop, importList);
		final int size = studies.size();
		Assert.assertThat(importResponse.getCreatedSize(), is(size));
		Assert.assertThat(importResponse.getImportListSize(), is(size));
		Assert.assertThat(importResponse.getEntityList(), iterableWithSize(studies.size()));
		Assert.assertThat(importResponse.getErrors(), nullValue());
	}

	@Test
	public void testCreateStudies_InvalidNotCreated(){
		final String crop = RandomStringUtils.randomAlphabetic(10);
		final BindingResult result = Mockito.mock(BindingResult.class);
		final ObjectError error = Mockito.mock(ObjectError.class);
		Mockito.doReturn(true).when(result).hasErrors();
		Mockito.doReturn(Lists.newArrayList(error)).when(result).getAllErrors();
		Mockito.doReturn(result).when(this.studyImportRequestValidator).pruneStudiesInvalidForImport(ArgumentMatchers.anyList());

		final List<StudyImportRequestDTO> importList = new ArrayList<>();
		final StudyImportRequestDTO request1 = new StudyImportRequestDTO();
		request1.setTrialDbId(RandomStringUtils.randomAlphabetic(20));
		final StudyImportRequestDTO request2 = new StudyImportRequestDTO();
		request2.setTrialDbId(RandomStringUtils.randomAlphabetic(20));
		importList.add(request1);
		importList.add(request2);
		final StudyInstanceDto study1 = new StudyInstanceDto();
		study1.setTrialDbId(request1.getTrialDbId());
		study1.setStudyDbId(String.valueOf(new Random().nextInt()));
		final List<StudyInstanceDto> studies = Collections.singletonList(study1);
		Mockito.doReturn(studies).when(this.middlewareStudyServiceBrapi).saveStudyInstances(crop, importList, this.testUser.getUserid());

		final StudyImportResponse importResponse = this.studyServiceBrapi.createStudies(crop, importList);
		final int size = studies.size();
		Assert.assertThat(importResponse.getCreatedSize(), is(size));
		Assert.assertThat(importResponse.getImportListSize(), is(importList.size()));
		Assert.assertThat(importResponse.getEntityList(), iterableWithSize(studies.size()));
		Assert.assertThat(importResponse.getErrors(), is(Lists.newArrayList(error)));
	}

}
