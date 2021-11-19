package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.api.germplasmlist.GermplasmListMetadataRequest;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.hamcrest.MatcherAssert;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.fail;

public class GermplasmListValidatorTest {

	private static final int LIST_ID = new Random().nextInt(Integer.MAX_VALUE);
	private static final String PROGRAM_UUID = UUID.randomUUID().toString();
	private static final String GERMPLASM_LIST_TYPE = "LST";

	@InjectMocks
	private GermplasmListValidator germplasmListValidator;

	@Mock
	private GermplasmListService germplasmListService;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);

		final UserDefinedField userDefinedField = new UserDefinedField();
		userDefinedField.setFcode(GERMPLASM_LIST_TYPE);
		userDefinedField.setFldno(new Random().nextInt());
		userDefinedField.setFname("GERMPLASM LISTS");

		Mockito.when(this.germplasmListManager.getGermplasmListTypes()).thenReturn(Collections.singletonList(userDefinedField));
	}

	@Test
	public void validateGermplasmList_OK() {
		final GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
		Mockito.when(this.germplasmListService.getGermplasmListById(LIST_ID)).thenReturn(Optional.of(germplasmList));

		this.germplasmListValidator.validateGermplasmList(LIST_ID);

		Mockito.verify(this.germplasmListService).getGermplasmListById(LIST_ID);
		Mockito.verifyNoMoreInteractions(this.germplasmListService);
		Mockito.verifyNoInteractions(this.germplasmListManager);
	}

	@Test
	public void validateGermplasmList_invalidNegativeGermplasmListId() {
		try {
			this.germplasmListValidator.validateGermplasmList(-1);
			Assert.fail("Should have thrown validation exception but did not.");
		} catch (final ResourceNotFoundException e) {
			Assert.assertThat(e.getError().getCode(), is("list.id.invalid"));
		}

		Mockito.verifyNoInteractions(this.germplasmListService);
		Mockito.verifyNoInteractions(this.germplasmListManager);
	}

	@Test
	public void validateGermplasmList_invalidNullGermplasmListId() {
		try {
			this.germplasmListValidator.validateGermplasmList(null);
			Assert.fail("Should have thrown validation exception but did not.");
		} catch (final ResourceNotFoundException e) {
			Assert.assertThat(e.getError().getCode(), is("list.id.invalid"));
		}

		Mockito.verifyNoInteractions(this.germplasmListService);
		Mockito.verifyNoInteractions(this.germplasmListManager);
	}

	@Test
	public void validateGermplasmList_germplasmListNotFound() {
		Mockito.when(this.germplasmListService.getGermplasmListById(LIST_ID)).thenReturn(Optional.empty());

		try {
			this.germplasmListValidator.validateGermplasmList(LIST_ID);
			Assert.fail("Should have thrown validation exception but did not.");
		} catch (final ResourceNotFoundException e) {
			Assert.assertThat(e.getError().getCode(), is("list.id.invalid"));
		}

		Mockito.verify(this.germplasmListService).getGermplasmListById(LIST_ID);
		Mockito.verifyNoMoreInteractions(this.germplasmListService);
		Mockito.verifyNoInteractions(this.germplasmListManager);
	}

	@Test
	public void validateFolderName_OK() {
		this.germplasmListValidator.validateFolderName("folderName");
	}

	@Test
	public void validateFolderName_invalidNullFolderName() {
		try {
			this.germplasmListValidator.validateFolderName(null);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert
				.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.empty"));
		}
	}

	@Test
	public void validateFolderName_invalidEmptyFolderName() {
		try {
			this.germplasmListValidator.validateFolderName("");
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert
				.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()), hasItem("list.folder.empty"));
		}
	}

	@Test
	public void validateFolderName_invalidTooLongFolderName() {
		final String folderName = StringUtils.repeat("a", GermplasmListValidator.NAME_MAX_LENGTH + 1);

		try {
			this.germplasmListValidator.validateFolderName(folderName);
			fail("Should have failed");
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.name.too.long"));
		}
	}

	@Test
	public void validateNotSameFolderNameInParent_OK() {
		final String folderName = "newFolderName";
		final Integer parentId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.when(this.germplasmListService.getGermplasmListByParentAndName(folderName, parentId, PROGRAM_UUID))
			.thenReturn(Optional.empty());

		this.germplasmListValidator.validateNotSameFolderNameInParent(folderName, parentId, PROGRAM_UUID);

		Mockito.verify(this.germplasmListService).getGermplasmListByParentAndName(folderName, parentId, PROGRAM_UUID);
		Mockito.verifyNoMoreInteractions(this.germplasmListService);
	}

	@Test
	public void validateNotSameFolderNameInParent_InvalidSameFolderNameInParent() {

		final String folderName = "newFolderName";
		final Integer parentId = new Random().nextInt(Integer.MAX_VALUE);

		Mockito.when(this.germplasmListService.getGermplasmListByParentAndName(folderName, parentId, PROGRAM_UUID))
			.thenReturn(Optional.of(Mockito.mock(GermplasmList.class)));

		try {
			this.germplasmListValidator.validateNotSameFolderNameInParent(folderName, parentId, PROGRAM_UUID);
			fail("Should have failed");
		} catch (final ApiRequestValidationException e) {
			MatcherAssert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("list.folder.name.exists"));
		}

		Mockito.verify(this.germplasmListService).getGermplasmListByParentAndName(folderName, parentId, PROGRAM_UUID);
		Mockito.verifyNoMoreInteractions(this.germplasmListService);
	}

	@Test
	public void validateListMetadata_EmptyMetadataRequest() {
		try {
			this.germplasmListValidator.validateListMetadata(null, RandomStringUtils.randomAlphabetic(20), null);
			fail("Should have failed");
		} catch (final ApiRequestValidationException e) {
			MatcherAssert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("param.null"));
			Assert.assertEquals(new String[] {"request"}, e.getErrors().get(0).getArguments());
		}
	}

	@Test
	public void validateListMetadata_EmptyDate() {
		try {
			this.germplasmListValidator
				.validateListMetadata(new GermplasmListMetadataRequest(), RandomStringUtils.randomAlphabetic(20), null);
			fail("Should have failed");
		} catch (final ApiRequestValidationException e) {
			MatcherAssert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("param.null"));
			Assert.assertEquals(new String[] {"date"}, e.getErrors().get(0).getArguments());
		}
	}

	@Test
	public void validateListMetadata_EmptyDescription() {
		try {
			final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
			request.setDate(new Date());
			this.germplasmListValidator.validateListMetadata(request, RandomStringUtils.randomAlphabetic(20), null);
			fail("Should have failed");
		} catch (final ApiRequestValidationException e) {
			MatcherAssert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("param.null"));
			Assert.assertEquals(new String[] {"description"}, e.getErrors().get(0).getArguments());
		}
	}

	@Test
	public void validateListMetadata_DescriptionExceedsMaxLength() {
		try {
			final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
			request.setDate(new Date());
			request.setDescription(RandomStringUtils.randomAlphabetic(260));
			this.germplasmListValidator.validateListMetadata(request, RandomStringUtils.randomAlphabetic(20), null);
			fail("Should have failed");
		} catch (final ApiRequestValidationException e) {
			MatcherAssert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("text.field.max.length"));
			Assert.assertEquals(new String[] {"description", "255"}, e.getErrors().get(0).getArguments());
		}
	}

	@Test
	public void validateListMetadata_NotesExceedsMaxLength() {
		try {
			final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
			request.setDate(new Date());
			request.setDescription(RandomStringUtils.randomAlphabetic(200));
			request.setNotes(RandomStringUtils.randomAlphabetic(65536));
			this.germplasmListValidator.validateListMetadata(request, RandomStringUtils.randomAlphabetic(20), null);
			fail("Should have failed");
		} catch (final ApiRequestValidationException e) {
			MatcherAssert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("text.field.max.length"));
			Assert.assertEquals(new String[] {"notes", "65535"}, e.getErrors().get(0).getArguments());
		}
	}

	@Test
	public void validateListMetadata_EmptyListType() {
		try {
			final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
			request.setDate(new Date());
			request.setDescription(RandomStringUtils.randomAlphabetic(200));
			this.germplasmListValidator.validateListMetadata(request, RandomStringUtils.randomAlphabetic(20), null);
			fail("Should have failed");
		} catch (final ApiRequestValidationException e) {
			MatcherAssert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("param.null"));
			Assert.assertEquals(new String[] {"type"}, e.getErrors().get(0).getArguments());
		}
	}

	@Test
	public void validateListMetadata_InvalidListType() {
		try {
			final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
			request.setDate(new Date());
			request.setDescription(RandomStringUtils.randomAlphabetic(200));
			request.setType(RandomStringUtils.random(12));

			this.germplasmListValidator.validateListMetadata(request, RandomStringUtils.randomAlphabetic(20), null);
			fail("Should have failed");
		} catch (final ApiValidationException e) {
			Assert.assertEquals("error.germplasmlist.save.type.not.exists", e.getErrorCode());
		}
	}

	@Test
	public void validateListMetadata_EmptyName() {
		try {
			final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
			request.setDate(new Date());
			request.setDescription(RandomStringUtils.randomAlphabetic(200));
			request.setType(GERMPLASM_LIST_TYPE);
			this.germplasmListValidator.validateListMetadata(request, RandomStringUtils.randomAlphabetic(20), null);
			fail("Should have failed");
		} catch (final ApiRequestValidationException e) {
			MatcherAssert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("param.null"));
			Assert.assertEquals(new String[] {"name"}, e.getErrors().get(0).getArguments());
		}
	}

	@Test
	public void validateListMetadata_NameExceedsLength() {
		try {
			final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
			request.setDate(new Date());
			request.setDescription(RandomStringUtils.randomAlphabetic(200));
			request.setType(GERMPLASM_LIST_TYPE);
			request.setName(RandomStringUtils.randomAlphabetic(55));
			this.germplasmListValidator.validateListMetadata(request, RandomStringUtils.randomAlphabetic(20), null);
			fail("Should have failed");
		} catch (final ApiRequestValidationException e) {
			MatcherAssert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("text.field.max.length"));
			Assert.assertEquals(new String[] {"name", "50"}, e.getErrors().get(0).getArguments());
		}
	}

	@Test
	public void validateListMetadata_NameEqualsCropLists() {
		try {
			final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
			request.setDate(new Date());
			request.setDescription(RandomStringUtils.randomAlphabetic(200));
			request.setType(GERMPLASM_LIST_TYPE);
			request.setName(AppConstants.CROP_LISTS.getString());

			this.germplasmListValidator.validateListMetadata(request, RandomStringUtils.randomAlphabetic(20), null);
			fail("Should have failed");
		} catch (final ApiValidationException e) {
			Assert.assertEquals("error.list.name.invalid", e.getErrorCode());
		}
	}

	@Test
	public void validateListMetadata_NameEqualsProgramLists() {
		try {
			final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
			request.setDate(new Date());
			request.setDescription(RandomStringUtils.randomAlphabetic(200));
			request.setType(GERMPLASM_LIST_TYPE);
			request.setName(AppConstants.PROGRAM_LISTS.getString());

			this.germplasmListValidator.validateListMetadata(request, RandomStringUtils.randomAlphabetic(20), null);
			fail("Should have failed");
		} catch (final ApiValidationException e) {
			Assert.assertEquals("error.list.name.invalid", e.getErrorCode());
		}
	}

	@Test
	public void validateListMetadata_ListCreation_NameAlreadyExists() {
		try {
			final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
			request.setDate(new Date());
			request.setDescription(RandomStringUtils.randomAlphabetic(200));
			request.setType(GERMPLASM_LIST_TYPE);
			final String listName = RandomStringUtils.randomAlphabetic(45);
			request.setName(listName);

			final String currentProgram = RandomStringUtils.randomAlphabetic(20);
			Mockito.doReturn(Collections.singletonList(new GermplasmList(1))).when(this.germplasmListManager).getGermplasmListByName(listName, currentProgram, 0, 1,
				Operation.EQUAL);
			this.germplasmListValidator.validateListMetadata(request, currentProgram, null);
			fail("Should have failed");
		} catch (final ApiValidationException e) {
			Assert.assertEquals("error.list.name.exists", e.getErrorCode());
		}
	}

	@Test
	public void validateListMetadata_ListCreation_Successful() {
		final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
		request.setDate(new Date());
		request.setDescription(RandomStringUtils.randomAlphabetic(200));
		request.setType(GERMPLASM_LIST_TYPE);
		final String listName = RandomStringUtils.randomAlphabetic(45);
		request.setName(listName);

		final String currentProgram = RandomStringUtils.randomAlphabetic(20);
		Mockito.doReturn(Collections.emptyList()).when(this.germplasmListManager).getGermplasmListByName(listName, currentProgram, 0, 1,
			Operation.EQUAL);
		this.germplasmListValidator.validateListMetadata(request, currentProgram, null);
	}

	@Test
	public void validateListMetadata_ExistingList_NameTakenByAnotherList() {
		try {
			final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
			request.setDate(new Date());
			request.setDescription(RandomStringUtils.randomAlphabetic(200));
			request.setType(GERMPLASM_LIST_TYPE);
			final String listName = RandomStringUtils.randomAlphabetic(45);
			request.setName(listName);

			final String currentProgram = RandomStringUtils.randomAlphabetic(20);
			final Integer existingListId = 2;
			Mockito.doReturn(Collections.singletonList(new GermplasmList(1))).when(this.germplasmListManager)
				.getGermplasmListByName(listName, currentProgram, 0, 1,
					Operation.EQUAL);
			this.germplasmListValidator.validateListMetadata(request, currentProgram, existingListId);
			fail("Should have failed");
		} catch (final ApiValidationException e) {
			Assert.assertEquals("error.list.name.exists", e.getErrorCode());
		}
	}

	@Test
	public void validateListMetadata_ExistingList_RetainSameName() {
		final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
		request.setDate(new Date());
		request.setDescription(RandomStringUtils.randomAlphabetic(200));
		request.setType(GERMPLASM_LIST_TYPE);
		final String listName = RandomStringUtils.randomAlphabetic(45);
		request.setName(listName);

		final String currentProgram = RandomStringUtils.randomAlphabetic(20);
		final Integer existingListId = 2;
		Mockito.doReturn(Collections.singletonList(new GermplasmList(existingListId))).when(this.germplasmListManager)
			.getGermplasmListByName(listName, currentProgram, 0, 1,
				Operation.EQUAL);
		this.germplasmListValidator.validateListMetadata(request, currentProgram, existingListId);
	}


	@Test
	public void validateListMetadata_ExistingList_Successful() {
		final GermplasmListMetadataRequest request = new GermplasmListMetadataRequest();
		request.setDate(new Date());
		request.setDescription(RandomStringUtils.randomAlphabetic(200));
		request.setType(GERMPLASM_LIST_TYPE);
		final String listName = RandomStringUtils.randomAlphabetic(45);
		request.setName(listName);

		final String currentProgram = RandomStringUtils.randomAlphabetic(20);
		final Integer existingListId = 2;
		Mockito.doReturn(Collections.emptyList()).when(this.germplasmListManager)
			.getGermplasmListByName(listName, currentProgram, 0, 1,
				Operation.EQUAL);
		this.germplasmListValidator.validateListMetadata(request, currentProgram, existingListId);
	}
}