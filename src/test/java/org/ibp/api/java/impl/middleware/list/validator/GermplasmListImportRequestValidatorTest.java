package org.ibp.api.java.impl.middleware.list.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.list.GermplasmListImportRequestDTO;
import org.generationcp.middleware.api.germplasmlist.search.GermplasmListSearchRequest;
import org.generationcp.middleware.api.germplasmlist.search.GermplasmListSearchResponse;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.java.germplasm.GermplasmListService;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmListImportRequestValidatorTest {

	private static final Integer LIST_OWNER_ID = 1;

	@Mock
	private UserService userService;

	@Mock
	private GermplasmListService germplasmListService;

	@InjectMocks
	private GermplasmListImportRequestValidator validator;

	@Before
	public void setUp() {
		final WorkbenchUser user = new WorkbenchUser(LIST_OWNER_ID);
		Mockito.when(this.userService.getUsersByIds(Collections.singletonList(LIST_OWNER_ID))).thenReturn(Collections.singletonList(user));
	}

	@Test
	public void testPruneListsInvalidForImport() {
		final BindingResult result = this.validator.pruneListsInvalidForImport(this.createGermplasmListImportRequestDTOList());
		Assert.assertFalse(result.hasErrors());
		Mockito.verify(this.germplasmListService)
			.searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
				ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(Collections.singletonList(LIST_OWNER_ID));
	}

	@Test
	public void testPruneListsInvalidForImport_WhereListNameIsEmpty() {
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		list.get(0).setListName(null);
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.name.null", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService, Mockito.never())
			.searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
				ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(Collections.singletonList(LIST_OWNER_ID));
	}

	@Test
	public void testPruneListsInvalidForImport_WhereListNameExceedsMaxLength() {
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		list.get(0).setListName(RandomStringUtils.randomAlphanumeric(100));
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.name.exceed.length", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService, Mockito.never())
			.searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
				ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(Collections.singletonList(LIST_OWNER_ID));
	}

	@Test
	public void testPruneListsInvalidForImport_WhereListNameHasDuplicate() {
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		final GermplasmListImportRequestDTO importRequestDTO = new GermplasmListImportRequestDTO();
		importRequestDTO.setListName(list.get(0).getListName());
		list.add(importRequestDTO);
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.name.duplicate.import", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService).searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
				ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(Collections.singletonList(LIST_OWNER_ID));
	}

	@Test
	public void testPruneListsInvalidForImport_WhereListNameHasExistsInDatabase() {
		Mockito.when(this.germplasmListService.searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
			ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(Collections.singletonList(new GermplasmListSearchResponse()));
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.name.duplicate.not.unique", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService).searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
				ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(Collections.singletonList(LIST_OWNER_ID));
	}

	@Test
	public void testPruneListsInvalidForImport_WhereListDescriptionExceedMaxLength() {
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		list.get(0).setListDescription(RandomStringUtils.randomAlphabetic(2000));
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.description.exceed.length", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService).searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
				ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(Collections.singletonList(LIST_OWNER_ID));
	}

	@Test
	public void testPruneListsInvalidForImport_WhereDateCreatedIsEmpty() {
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		list.get(0).setDateCreated(null);
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.date.created.null", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService).searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
			ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(Collections.singletonList(LIST_OWNER_ID));
	}

	@Test
	public void testPruneListsInvalidForImport_WhereDateCreatedIsInvalid() {
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		list.get(0).setDateCreated(RandomStringUtils.randomAlphanumeric(20));
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.date.created.invalid.format", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService).searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
			ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(Collections.singletonList(LIST_OWNER_ID));
	}

	@Test
	public void testPruneListsInvalidForImport_WhereExternalReferenceHasMissingInfo() {
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		list.get(0).getExternalReferences().get(0).setReferenceID(null);
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.reference.null", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService).searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
			ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(Collections.singletonList(LIST_OWNER_ID));
	}

	@Test
	public void testPruneListsInvalidForImport_WhereExternalReferenceIdExceedsLength() {
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		list.get(0).getExternalReferences().get(0).setReferenceID(RandomStringUtils.randomAlphanumeric(3000));
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.reference.id.exceeded.length", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService).searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
			ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(Collections.singletonList(LIST_OWNER_ID));
	}

	@Test
	public void testPruneListsInvalidForImport_WhereExternalReferenceSourceExceedsLength() {
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		list.get(0).getExternalReferences().get(0).setReferenceSource(RandomStringUtils.randomAlphanumeric(3000));
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.reference.source.exceeded.length", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService).searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
			ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(Collections.singletonList(LIST_OWNER_ID));
	}

	@Test
	public void testPruneListsInvalidForImport_WhereOwnerPersonDbIdIsEmpty() {
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		list.get(0).setListOwnerPersonDbId(null);
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.owner.null", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService).searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
			ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService, Mockito.never()).getUsersByIds(Collections.singletonList(LIST_OWNER_ID));
	}

	@Test
	public void testPruneListsInvalidForImport_WhereOwnerPersonDbIdIsInvalid() {
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		list.get(0).setListOwnerPersonDbId("2");
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.owner.invalid", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService).searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
			ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(ArgumentMatchers.any());
	}

	@Test
	public void testPruneListsInvalidForImport_WhereListTypeIsInvalid() {
		final List<GermplasmListImportRequestDTO> list = this.createGermplasmListImportRequestDTOList();
		list.get(0).setListType(RandomStringUtils.randomAlphanumeric(5));
		final BindingResult result = this.validator.pruneListsInvalidForImport(list);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("list.import.invalid.list.type", result.getAllErrors().get(0).getCode());
		Mockito.verify(this.germplasmListService).searchGermplasmList(ArgumentMatchers.any(GermplasmListSearchRequest.class),
			ArgumentMatchers.isNull(), ArgumentMatchers.isNull());
		Mockito.verify(this.userService).getUsersByIds(ArgumentMatchers.any());
	}

	private List<GermplasmListImportRequestDTO> createGermplasmListImportRequestDTOList() {
		final List<GermplasmListImportRequestDTO> list = new ArrayList<>();
		final GermplasmListImportRequestDTO importRequestDTO = new GermplasmListImportRequestDTO();
		importRequestDTO.setListName(RandomStringUtils.randomAlphanumeric(10));
		importRequestDTO.setListDescription(RandomStringUtils.randomAlphanumeric(10));
		importRequestDTO.setListOwnerPersonDbId(LIST_OWNER_ID.toString());
		importRequestDTO.setDateCreated("2022-03-05");
		importRequestDTO.setListType(GermplasmListImportRequestValidator.ALLOWED_LIST_TYPE);
		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphanumeric(25));
		externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphanumeric(25));
		importRequestDTO.setExternalReferences(Collections.singletonList(externalReferenceDTO));
		list.add(importRequestDTO);
		return list;
	}

}
