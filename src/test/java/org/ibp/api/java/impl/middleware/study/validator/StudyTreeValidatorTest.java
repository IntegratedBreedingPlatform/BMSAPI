package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.service.api.study.StudyService;
import org.hamcrest.MatcherAssert;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.fail;

public class StudyTreeValidatorTest {

	private static final Integer FOLDER_ID = new Random().nextInt(Integer.MAX_VALUE);
	private static final String PROGRAM_UUID = UUID.randomUUID().toString();

	@InjectMocks
	private StudyTreeValidator studyTreeValidator;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private StudyService studyService;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void validateFolderName_OK() {
		this.studyTreeValidator.validateFolderName("folderName");
	}

	@Test
	public void validateFolderName_FAIL_invalidTooLongFolderName() {
		final String folderName = StringUtils.repeat("a", StudyTreeValidator.NAME_MAX_LENGTH + 1);

		try {
			this.studyTreeValidator.validateFolderName(folderName);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));

			final ObjectError objectError = ((ApiRequestValidationException) e).getErrors().get(0);
			MatcherAssert.assertThat(Arrays.asList(objectError.getCodes()), hasItem("study.folder.name.too.long"));

			final Object[] arguments = objectError.getArguments();
			MatcherAssert.assertThat(arguments.length, is(1));
			MatcherAssert.assertThat(arguments[0], is(StudyTreeValidator.NAME_MAX_LENGTH));
		}
	}

	@Test
	public void validateFolderName_FAIL_invalidNullFolderName() {
		try {
			this.studyTreeValidator.validateFolderName(null);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert
				.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
					hasItem("study.folder.empty"));
		}
	}

	@Test
	public void validateFolderId_OK() {
		this.mockGetStudy(true, PROGRAM_UUID);

		this.studyTreeValidator.validateFolderId(FOLDER_ID, PROGRAM_UUID);

		Mockito.verify(this.studyDataManager).getProject(FOLDER_ID);

		Mockito.verifyNoMoreInteractions(this.studyDataManager);
	}

	@Test
	public void validateFolderId_FAIL_nullFolder() {
		Mockito.when(this.studyDataManager.getStudy(FOLDER_ID)).thenReturn(null);

		try {
			this.studyTreeValidator.validateFolderId(FOLDER_ID, PROGRAM_UUID);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));

			final ObjectError objectError = ((ApiRequestValidationException) e).getErrors().get(0);

			MatcherAssert
				.assertThat(Arrays.asList(objectError.getCodes()), hasItem("study.folder.id.not.exist"));

			final Object[] arguments = objectError.getArguments();
			MatcherAssert.assertThat(arguments.length, is(1));
			MatcherAssert.assertThat(arguments[0], is(FOLDER_ID));
		}

		Mockito.verify(this.studyDataManager).getProject(FOLDER_ID);

		Mockito.verifyNoMoreInteractions(this.studyDataManager);
	}

	@Test
	public void validateFolderId_FAIL_notFolder() {
		this.mockGetStudy(false, PROGRAM_UUID);

		try {
			this.studyTreeValidator.validateFolderId(FOLDER_ID, PROGRAM_UUID);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));

			final ObjectError objectError = ((ApiRequestValidationException) e).getErrors().get(0);
			MatcherAssert
				.assertThat(Arrays.asList(objectError.getCodes()), hasItem("study.folder.id.not.exist"));

			final Object[] arguments = objectError.getArguments();
			MatcherAssert.assertThat(arguments.length, is(1));
			MatcherAssert.assertThat(arguments[0], is(FOLDER_ID));
		}

		Mockito.verify(this.studyDataManager).getProject(FOLDER_ID);

		Mockito.verifyNoMoreInteractions(this.studyDataManager);
	}

	@Test
	public void validateFolderId_FAIL_folderNotBelongToProgram() {
		this.mockGetStudy(false, RandomStringUtils.randomAlphabetic(10));

		try {
			this.studyTreeValidator.validateFolderId(FOLDER_ID, PROGRAM_UUID);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));

			final ObjectError objectError = ((ApiRequestValidationException) e).getErrors().get(0);
			MatcherAssert
				.assertThat(Arrays.asList(objectError.getCodes()), hasItem("study.folder.id.not.exist"));

			final Object[] arguments = objectError.getArguments();
			MatcherAssert.assertThat(arguments.length, is(1));
			MatcherAssert.assertThat(arguments[0], is(FOLDER_ID));
		}

		Mockito.verify(this.studyDataManager).getProject(FOLDER_ID);

		Mockito.verifyNoMoreInteractions(this.studyDataManager);
	}

	@Test
	public void validateNotSameFolderNameInParent_OK() {
		final String folderName = "newFolderName";
		final Integer parentId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.when(this.studyService.getFolderByParentAndName(parentId, folderName, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		this.studyTreeValidator.validateNotSameFolderNameInParent(folderName, parentId, PROGRAM_UUID);

		Mockito.verify(this.studyService).getFolderByParentAndName(parentId, folderName, PROGRAM_UUID);
		Mockito.verifyNoMoreInteractions(this.studyService);
	}

	@Test
	public void validateNotSameFolderNameInParent_FAIL_InvalidSameFolderNameInParent() {

		final String folderName = "newFolderName";
		final Integer parentId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.when(this.studyService.getFolderByParentAndName(parentId, folderName, PROGRAM_UUID))
			.thenReturn(Optional.of(Mockito.mock(FolderReference.class)));

		try {
			this.studyTreeValidator.validateNotSameFolderNameInParent(folderName, parentId, PROGRAM_UUID);
			fail("Should have failed");
		} catch (final Exception e) {
			final ObjectError objectError = ((ApiRequestValidationException) e).getErrors().get(0);
			MatcherAssert.assertThat(Arrays.asList(objectError.getCodes()),
				hasItem("study.folder.name.exists"));

			final Object[] arguments = objectError.getArguments();
			MatcherAssert.assertThat(arguments.length, is(1));
			MatcherAssert.assertThat(arguments[0], is(folderName));
		}

		Mockito.verify(this.studyService).getFolderByParentAndName(parentId, folderName, PROGRAM_UUID);
		Mockito.verifyNoMoreInteractions(this.studyService);
	}

	@Test
	public void validateFolderHasNoChildren_OK() {

		Mockito.when(this.studyDataManager.getChildrenOfFolder(FOLDER_ID, PROGRAM_UUID))
			.thenReturn(new ArrayList<>());

		this.studyTreeValidator.validateFolderHasNoChildren(FOLDER_ID, "some.message", PROGRAM_UUID);

		Mockito.verify(this.studyDataManager).getChildrenOfFolder(FOLDER_ID, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.studyDataManager);
	}

	@Test
	public void validateFolderHasNoChildren_FAIL_listHasChildren() {

		Mockito.when(this.studyDataManager.getChildrenOfFolder(FOLDER_ID, PROGRAM_UUID))
			.thenReturn(Collections.singletonList(Mockito.mock(Reference.class)));

		try {
			this.studyTreeValidator.validateFolderHasNoChildren(FOLDER_ID, "some.message", PROGRAM_UUID);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));

			final ObjectError objectError = ((ApiRequestValidationException) e).getErrors().get(0);
			MatcherAssert.assertThat(
				Arrays.asList(objectError.getCodes()),
				hasItem("some.message"));

			final Object[] arguments = objectError.getArguments();
			MatcherAssert.assertThat(arguments.length, is(1));
			MatcherAssert.assertThat(arguments[0], is(FOLDER_ID.toString()));
		}

		Mockito.verify(this.studyDataManager).getChildrenOfFolder(FOLDER_ID, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.studyDataManager);
	}

	private void mockGetStudy(final boolean isFolder, final String programUUID) {
		final DmsProject study = Mockito.mock(DmsProject.class);
		Mockito.when(study.isFolder()).thenReturn(isFolder);
		Mockito.when(study.getProgramUUID()).thenReturn(programUUID);
		Mockito.when(this.studyDataManager.getProject(FOLDER_ID)).thenReturn(study);
	}

}
