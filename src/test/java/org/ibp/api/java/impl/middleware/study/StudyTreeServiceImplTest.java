package org.ibp.api.java.impl.middleware.study;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyTreeValidator;
import org.ibp.api.java.study.StudyTreeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.MapBindingResult;

import java.util.Random;
import java.util.UUID;

public class StudyTreeServiceImplTest {

	private static final Integer FOLDER_ID = new Random().nextInt(Integer.MAX_VALUE);
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
	private org.generationcp.middleware.service.api.study.StudyTreeService studyTreeServiceMW;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void createStudyTreeFolder_OK() {
		this.studyTreeService.createStudyTreeFolder(CROP_NAME, PROGRAM_UUID, FOLDER_ID, FOLDER_NAME);

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

		this.studyTreeService.updateStudyTreeFolder(CROP_NAME, PROGRAM_UUID, FOLDER_ID, FOLDER_NAME);

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
		Mockito.when(study.getName()).thenReturn(FOLDER_NAME);

		Mockito.when(this.studyTreeValidator.validateFolderId(FOLDER_ID)).thenReturn(study);

		this.studyTreeService.updateStudyTreeFolder(CROP_NAME, PROGRAM_UUID, FOLDER_ID, FOLDER_NAME);

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
		Mockito.verify(this.studyTreeValidator).validateFolderHasNoChildren(FOLDER_ID, "study.delete.folder.has.child", PROGRAM_UUID);

		Mockito.verify(this.studyTreeServiceMW).deleteStudyFolder(FOLDER_ID);

		Mockito.verifyNoMoreInteractions(this.studyTreeValidator);
		Mockito.verifyNoMoreInteractions(this.programValidator);
		Mockito.verifyNoMoreInteractions(this.studyTreeServiceMW);
	}

}
