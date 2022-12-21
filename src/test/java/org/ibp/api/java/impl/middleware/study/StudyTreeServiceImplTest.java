package org.ibp.api.java.impl.middleware.study;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyTreeValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StudyTreeServiceImplTest {

	private static final Integer FOLDER_ID = new Random().nextInt(Integer.MAX_VALUE);
	private static final Integer NEW_PARENT_FOLDER_ID = new Random().nextInt(Integer.MAX_VALUE);
	private static final String FOLDER_NAME = RandomStringUtils.randomAlphabetic(10);
	private static final String CROP_NAME = RandomStringUtils.randomAlphabetic(10);
	private static final String PROGRAM_UUID = UUID.randomUUID().toString();

	@InjectMocks
	private StudyTreeServiceImpl studyTreeService;

	@Mock
	private StudyTreeValidator studyTreeValidator;

	@Mock
	public ProgramValidator programValidator;

	@Mock
	public StudyDataManager studyDataManager;

	@Mock
	private org.generationcp.middleware.service.api.study.StudyTreeService studyTreeServiceMW;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void createStudyTreeFolder_OK() {
		Mockito.when(this.studyTreeServiceMW.createStudyTreeFolder(FOLDER_ID, FOLDER_NAME, PROGRAM_UUID)).thenReturn(FOLDER_ID);

		final Integer createdFolderId = this.studyTreeService.createStudyTreeFolder(CROP_NAME, PROGRAM_UUID, FOLDER_ID, FOLDER_NAME);
		assertThat(createdFolderId, is(FOLDER_ID));

		Mockito.verify(this.studyTreeValidator).validateFolderName(FOLDER_NAME);
		Mockito.verify(this.studyTreeValidator).validateFolderId(FOLDER_ID);
		Mockito.verify(this.programValidator)
			.validate(ArgumentMatchers.any(ProgramDTO.class), ArgumentMatchers.any(MapBindingResult.class));
		Mockito.verify(this.studyTreeValidator).validateNotSameFolderNameInParent(FOLDER_NAME, FOLDER_ID, PROGRAM_UUID);

		Mockito.verify(this.studyTreeServiceMW).createStudyTreeFolder(FOLDER_ID, FOLDER_NAME, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.studyTreeValidator);
		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.studyTreeServiceMW);
	}

	@Test
	public void updateStudyTreeFolder_OK() {
		Mockito.when(this.studyTreeValidator.validateFolderId(FOLDER_ID)).thenReturn(Mockito.mock(Study.class));

		Mockito.when(this.studyTreeServiceMW.updateStudyTreeFolder(FOLDER_ID, FOLDER_NAME)).thenReturn(FOLDER_ID);

		final Integer updatedFolderId = this.studyTreeService.updateStudyTreeFolder(CROP_NAME, PROGRAM_UUID, FOLDER_ID, FOLDER_NAME);
		assertThat(updatedFolderId, is(FOLDER_ID));

		Mockito.verify(this.studyTreeValidator).validateFolderName(FOLDER_NAME);
		Mockito.verify(this.studyTreeValidator).validateFolderId(FOLDER_ID);
		Mockito.verify(this.programValidator)
			.validate(ArgumentMatchers.any(ProgramDTO.class), ArgumentMatchers.any(MapBindingResult.class));
		Mockito.verify(this.studyTreeValidator).validateNotSameFolderNameInParent(FOLDER_NAME, FOLDER_ID, PROGRAM_UUID);

		Mockito.verify(this.studyTreeServiceMW).updateStudyTreeFolder(FOLDER_ID, FOLDER_NAME);

		Mockito.verifyNoMoreInteractions(this.studyTreeValidator);
		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.studyTreeServiceMW);
	}

	@Test
	public void updateStudyTreeFolder_OK_preventEditionUsingFolderSame() {
		final Study study = Mockito.mock(Study.class);
		Mockito.when(study.getId()).thenReturn(FOLDER_ID);
		Mockito.when(study.getName()).thenReturn(FOLDER_NAME);

		Mockito.when(this.studyTreeValidator.validateFolderId(FOLDER_ID)).thenReturn(study);

		final Integer folderId = this.studyTreeService.updateStudyTreeFolder(CROP_NAME, PROGRAM_UUID, FOLDER_ID, FOLDER_NAME);
		assertThat(folderId, is(FOLDER_ID));

		Mockito.verify(this.studyTreeValidator).validateFolderName(FOLDER_NAME);
		Mockito.verify(this.studyTreeValidator).validateFolderId(FOLDER_ID);

		Mockito.verifyNoMoreInteractions(this.studyTreeValidator);
		Mockito.verifyNoInteractions(this.programValidator);
		Mockito.verifyNoInteractions(this.studyTreeServiceMW);
	}

	@Test
	public void deleteStudyTreeFolder_OK() {
		this.studyTreeService.deleteStudyFolder(CROP_NAME, PROGRAM_UUID, FOLDER_ID);

		Mockito.verify(this.studyTreeValidator).validateFolderId(FOLDER_ID);
		Mockito.verify(this.programValidator)
			.validate(ArgumentMatchers.any(ProgramDTO.class), ArgumentMatchers.any(MapBindingResult.class));
		Mockito.verify(this.studyTreeValidator).validateFolderHasNoChildren(FOLDER_ID, "study.folder.delete.has.child", PROGRAM_UUID);

		Mockito.verify(this.studyTreeServiceMW).deleteStudyFolder(FOLDER_ID);

		Mockito.verifyNoMoreInteractions(this.studyTreeValidator);
		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.studyTreeServiceMW);
	}

	@Test
	public void moveStudyFolder_OK() {
		final Study folderToMove = mockStudy(PROGRAM_UUID);
		Mockito.when(this.studyTreeValidator.validateFolderId(FOLDER_ID)).thenReturn(folderToMove);

		final Study newParentFolder = mockStudy(PROGRAM_UUID);
		Mockito.when(this.studyTreeValidator.validateFolderId(NEW_PARENT_FOLDER_ID)).thenReturn(newParentFolder);

		Mockito.when(this.studyTreeServiceMW.moveStudyFolder(FOLDER_ID, NEW_PARENT_FOLDER_ID)).thenReturn(FOLDER_ID);

		final Reference reference = Mockito.mock(Reference.class);
		Mockito.when(reference.getId()).thenReturn(FOLDER_ID);
		Mockito.when(reference.getName()).thenReturn(FOLDER_NAME);
		Mockito.when(reference.getProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(reference.isFolder()).thenReturn(true);
		Mockito.when(this.studyDataManager.getChildrenOfFolder(FOLDER_ID, PROGRAM_UUID)).thenReturn(Arrays.asList(reference));

		final TreeNode treeNode = this.studyTreeService.moveStudyFolder(CROP_NAME, PROGRAM_UUID, FOLDER_ID, NEW_PARENT_FOLDER_ID);
		assertNotNull(treeNode);
		assertThat(treeNode.getKey(), is(FOLDER_ID.toString()));
		assertThat(treeNode.getTitle(), is(FOLDER_NAME));
		assertThat(treeNode.getProgramUUID(), is(PROGRAM_UUID));
		assertTrue(treeNode.getIsFolder());

		Mockito.verify(this.programValidator)
			.validate(ArgumentMatchers.any(ProgramDTO.class), ArgumentMatchers.any(MapBindingResult.class));

		Mockito.verify(this.studyTreeValidator).validateFolderId(FOLDER_ID);
		Mockito.verify(this.studyTreeValidator).validateFolderId(NEW_PARENT_FOLDER_ID);
		Mockito.verify(this.studyTreeValidator).validateFolderHasNoChildren(FOLDER_ID, "study.folder.move.has.child", PROGRAM_UUID);
		Mockito.verify(this.studyTreeValidator).validateNotSameFolderNameInParent(FOLDER_NAME, NEW_PARENT_FOLDER_ID, PROGRAM_UUID);

		Mockito.verify(this.studyDataManager).getChildrenOfFolder(FOLDER_ID, PROGRAM_UUID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.studyTreeValidator);
		Mockito.verifyNoMoreInteractions(this.studyDataManager);
	}

	@Test
	public void moveStudyFolder_FAIL_invalidFolderId() {
		try {
			this.studyTreeService.moveStudyFolder(CROP_NAME, PROGRAM_UUID, null, NEW_PARENT_FOLDER_ID);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("study.folder.id.invalid"));
		}

		Mockito.verifyNoInteractions(this.programValidator);
		Mockito.verifyNoInteractions(this.studyTreeValidator);
		Mockito.verifyNoInteractions(this.studyDataManager);
	}

	@Test
	public void moveStudyFolder_FAIL_invalidNewParentFolderId() {
		try {
			this.studyTreeService.moveStudyFolder(CROP_NAME, PROGRAM_UUID, FOLDER_ID, null);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("study.parent.folder.id.invalid"));
		}

		Mockito.verifyNoInteractions(this.programValidator);
		Mockito.verifyNoInteractions(this.studyTreeValidator);
		Mockito.verifyNoInteractions(this.studyDataManager);
	}

	@Test
	public void moveStudyFolder_FAIL_folderAndParentAreTheSame() {
		try {
			this.studyTreeService.moveStudyFolder(CROP_NAME, PROGRAM_UUID, FOLDER_ID, FOLDER_ID);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("study.folder.move.id.same.values"));
		}

		Mockito.verifyNoInteractions(this.programValidator);
		Mockito.verifyNoInteractions(this.studyTreeValidator);
		Mockito.verifyNoInteractions(this.studyDataManager);
	}

	@Test
	public void moveStudyFolder_FAIL_folderNotBelongsToGivenProgramUUID() {
		final Study folderToMove = mockStudy(RandomStringUtils.randomAlphabetic(10));
		Mockito.when(this.studyTreeValidator.validateFolderId(FOLDER_ID)).thenReturn(folderToMove);

		try {
			this.studyTreeService.moveStudyFolder(CROP_NAME, PROGRAM_UUID, FOLDER_ID, NEW_PARENT_FOLDER_ID);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));

			final ObjectError objectError = ((ApiRequestValidationException) e).getErrors().get(0);
			MatcherAssert.assertThat(Arrays.asList(objectError.getCodes()),
				hasItem("study.folder.id.not.exist"));

			final Object[] arguments = objectError.getArguments();
			MatcherAssert.assertThat(arguments.length, CoreMatchers.is(1));
			MatcherAssert.assertThat(arguments[0], CoreMatchers.is(FOLDER_ID.toString()));
		}

		Mockito.verify(this.programValidator)
			.validate(ArgumentMatchers.any(ProgramDTO.class), ArgumentMatchers.any(MapBindingResult.class));

		Mockito.verify(this.studyTreeValidator).validateFolderId(FOLDER_ID);
		Mockito.verify(this.studyTreeValidator).validateFolderId(NEW_PARENT_FOLDER_ID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.studyTreeValidator);
		Mockito.verifyNoInteractions(this.studyDataManager);
	}

	@Test
	public void moveStudyFolder_FAIL_parentFolderNotBelongsToGivenProgramUUID() {
		final Study folderToMove = mockStudy(PROGRAM_UUID);
		Mockito.when(this.studyTreeValidator.validateFolderId(FOLDER_ID)).thenReturn(folderToMove);

		final Study parentFolderToMove = mockStudy(RandomStringUtils.randomAlphabetic(10));
		Mockito.when(this.studyTreeValidator.validateFolderId(NEW_PARENT_FOLDER_ID)).thenReturn(parentFolderToMove);

		try {
			this.studyTreeService.moveStudyFolder(CROP_NAME, PROGRAM_UUID, FOLDER_ID, NEW_PARENT_FOLDER_ID);
			fail("Should have failed");
		} catch (final Exception e) {
			final ObjectError objectError = ((ApiRequestValidationException) e).getErrors().get(0);
			MatcherAssert.assertThat(Arrays.asList(objectError.getCodes()),
				hasItem("study.folder.id.not.exist"));

			final Object[] arguments = objectError.getArguments();
			MatcherAssert.assertThat(arguments.length, CoreMatchers.is(1));
			MatcherAssert.assertThat(arguments[0], CoreMatchers.is(NEW_PARENT_FOLDER_ID.toString()));
		}

		Mockito.verify(this.programValidator)
			.validate(ArgumentMatchers.any(ProgramDTO.class), ArgumentMatchers.any(MapBindingResult.class));

		Mockito.verify(this.studyTreeValidator).validateFolderId(FOLDER_ID);
		Mockito.verify(this.studyTreeValidator).validateFolderId(NEW_PARENT_FOLDER_ID);

		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.studyTreeValidator);
		Mockito.verifyNoInteractions(this.studyDataManager);
	}

	private Study mockStudy(final String programUUID) {
		final Study study = Mockito.mock(Study.class);
		Mockito.when(study.getName()).thenReturn(FOLDER_NAME);
		Mockito.when(study.getProgramUUID()).thenReturn(programUUID);
		return study;
	}

}
