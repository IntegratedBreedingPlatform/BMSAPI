package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.hamcrest.MatcherAssert;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
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

	@InjectMocks
	private GermplasmListValidator germplasmListValidator;

	@Mock
	private GermplasmListService germplasmListService;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
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
		} catch (final Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			MatcherAssert.assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("list.folder.name.exists"));
		}

		Mockito.verify(this.germplasmListService).getGermplasmListByParentAndName(folderName, parentId, PROGRAM_UUID);
		Mockito.verifyNoMoreInteractions(this.germplasmListService);
	}

}
