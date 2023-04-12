package org.ibp.api.java.impl.middleware.germplasm.brapi;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.TrialServiceBrapi;
import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.domain.dms.TrialSummary;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.brapi.v2.trial.TrialImportResponse;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.study.brapi.TrialServiceBrapiImpl;
import org.ibp.api.java.impl.middleware.study.validator.TrialImportRequestValidator;
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
public class TrialServiceBrapiImplTest {

	@Mock
	private TrialImportRequestValidator trialImportRequestDtoValidator;

	@Mock
	private TrialServiceBrapi middlewareTrialServiceBrapi;

	@Mock
	private SecurityService securityService;

	@InjectMocks
	private org.ibp.api.brapi.TrialServiceBrapi trialServiceBrapi = new TrialServiceBrapiImpl();

	private WorkbenchUser testUser;


	@Before
	public void setup() {
		if (this.testUser == null) {
			this.testUser = new WorkbenchUser(new Random().nextInt(100));
			Mockito.doReturn(this.testUser).when(this.securityService).getCurrentlyLoggedInUser();
		}
	}

	@Test
	public void testCreateTrials_AllCreated(){
		final String crop = RandomStringUtils.randomAlphabetic(10);
		final BindingResult result = Mockito.mock(BindingResult.class);
		Mockito.doReturn(false).when(result).hasErrors();
		Mockito.doReturn(result).when(this.trialImportRequestDtoValidator).pruneTrialsInvalidForImport(ArgumentMatchers.anyList(), ArgumentMatchers.eq(crop));

		final List<TrialImportRequestDTO> importList = new ArrayList<>();
		final TrialImportRequestDTO request1 = new TrialImportRequestDTO();
		request1.setTrialName(RandomStringUtils.randomAlphabetic(20));
		final TrialImportRequestDTO request2 = new TrialImportRequestDTO();
		request2.setTrialName(RandomStringUtils.randomAlphabetic(20));
		importList.add(request1);
		importList.add(request2);
		final TrialSummary trial1 = new TrialSummary();
		trial1.setName(request1.getTrialName());
		trial1.setTrialDbId(new Random().nextInt());
		final TrialSummary trial2 = new TrialSummary();
		trial2.setName(request2.getTrialName());
		trial2.setTrialDbId(new Random().nextInt());
		final List<TrialSummary> trialList = Arrays.asList(trial1, trial2);
		Mockito.doReturn(trialList).when(this.middlewareTrialServiceBrapi).saveTrials(crop, importList, this.testUser.getUserid());

		final TrialImportResponse importResponse = this.trialServiceBrapi.createTrials(crop, importList);
		final int size = trialList.size();
		Assert.assertThat(importResponse.getCreatedSize(), is(size));
		Assert.assertThat(importResponse.getImportListSize(), is(size));
		Assert.assertThat(importResponse.getEntityList(), iterableWithSize(trialList.size()));
		Assert.assertThat(importResponse.getErrors(), nullValue());
	}

	@Test
	public void testCreateTrials_InvalidNotCreated(){
		final String crop = RandomStringUtils.randomAlphabetic(10);
		final BindingResult result = Mockito.mock(BindingResult.class);
		final ObjectError error = Mockito.mock(ObjectError.class);
		Mockito.doReturn(true).when(result).hasErrors();
		Mockito.doReturn(Lists.newArrayList(error)).when(result).getAllErrors();
		Mockito.doReturn(result).when(this.trialImportRequestDtoValidator).pruneTrialsInvalidForImport(ArgumentMatchers.anyList(), ArgumentMatchers.eq(crop));

		final List<TrialImportRequestDTO> importList = new ArrayList<>();
		final TrialImportRequestDTO request1 = new TrialImportRequestDTO();
		request1.setTrialName(RandomStringUtils.randomAlphabetic(20));
		final TrialImportRequestDTO request2 = new TrialImportRequestDTO();
		request2.setTrialName(RandomStringUtils.randomAlphabetic(20));
		importList.add(request1);
		importList.add(request2);
		final TrialSummary trial1 = new TrialSummary();
		trial1.setName(request1.getTrialName());
		trial1.setTrialDbId(new Random().nextInt());
		final List<TrialSummary> trialList = Collections.singletonList(trial1);
		Mockito.doReturn(trialList).when(this.middlewareTrialServiceBrapi).saveTrials(crop, importList, this.testUser.getUserid());

		final TrialImportResponse importResponse = this.trialServiceBrapi.createTrials(crop, importList);
		final int size = trialList.size();
		Assert.assertThat(importResponse.getCreatedSize(), is(size));
		Assert.assertThat(importResponse.getImportListSize(), is(importList.size()));
		Assert.assertThat(importResponse.getEntityList(), iterableWithSize(trialList.size()));
		Assert.assertThat(importResponse.getErrors(), is(Lists.newArrayList(error)));
	}

}
