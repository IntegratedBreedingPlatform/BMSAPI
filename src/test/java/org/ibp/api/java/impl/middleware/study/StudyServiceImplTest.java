
package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.name.validator.GermplasmNameTypeValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class StudyServiceImplTest {

	private static final int NAME_TYPE_ID = new Random().nextInt(Integer.MAX_VALUE);
	@InjectMocks
	private StudyServiceImpl studyServiceImpl;

	@Mock
	private StudyService mockMiddlewareStudyService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private GermplasmNameTypeValidator germplasmNameTypeValidator;

	@Test
	public void testGetStudyReference() {
		final int studyId = 101;
		this.studyServiceImpl.getStudyReference(studyId);
		Mockito.verify(this.studyDataManager).getStudyReference(studyId);
	}

	@Test
	public void testGetGermplasmStudies_Success() {
		final Integer gid = 1;
		this.studyServiceImpl.getGermplasmStudies(gid);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(gid)));
		Mockito.verify(this.mockMiddlewareStudyService).getGermplasmStudies(gid);
	}


	@Test
	public void testGetGermplasmStudies_ThrowsException_WhenGIDIsInvalid() {
		final Integer gid = 999;
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(Collections.singletonList(gid)));
			this.studyServiceImpl.getGermplasmStudies(gid);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
				ArgumentMatchers.eq(Collections.singletonList(gid)));
			Mockito.verify(this.mockMiddlewareStudyService, Mockito.never()).getGermplasmStudies(gid);
		}
	}

	@Test
	public void testDeleteNameTypeFromStudies_OK() {
		this.studyServiceImpl.deleteNameTypeFromStudies(NAME_TYPE_ID);
		Mockito.verify(this.germplasmNameTypeValidator).validate(NAME_TYPE_ID);
		Mockito.verify(this.mockMiddlewareStudyService).deleteNameTypeFromStudies(NAME_TYPE_ID);
		Mockito.verifyNoMoreInteractions(this.germplasmNameTypeValidator);
		Mockito.verifyNoMoreInteractions(this.mockMiddlewareStudyService);
	}

}
