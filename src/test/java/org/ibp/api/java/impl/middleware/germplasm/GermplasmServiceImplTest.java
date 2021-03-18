
package org.ibp.api.java.impl.middleware.germplasm;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.brapi.v2.germplasm.GermplasmImportRequestValidator;
import org.ibp.api.brapi.v2.germplasm.GermplasmImportResponse;
import org.ibp.api.brapi.v2.germplasm.GermplasmUpdateRequestValidator;
import org.ibp.api.domain.germplasm.GermplasmDeleteResponse;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmDeleteValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmServiceImplTest {

	private static final int PAGE_SIZE = 10;
	private static final int PAGE = 1;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmService middlewareGermplasmService;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Mock
	private GermplasmDeleteValidator germplasmDeleteValidator;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private SecurityService securityService;

	@Mock
	private GermplasmImportRequestValidator germplasmImportValidator;

	@Mock
	private GermplasmUpdateRequestValidator germplasmUpdateRequestValidator;

	@InjectMocks
	private GermplasmServiceImpl germplasmServiceImpl;


	@Test
	public void testSearchGermplasmDTO() {

		final GermplasmSearchRequestDto germplasmSearchRequestDTO = new GermplasmSearchRequestDto();

		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setGid("1");
		germplasmDTO.setGermplasmName("CB1");
		germplasmDTO.setGermplasmSeedSource("AF07A-412-201");
		final List<GermplasmDTO> germplasmDTOList = Lists.newArrayList(germplasmDTO);

		Mockito.when(this.middlewareGermplasmService.searchFilteredGermplasm(germplasmSearchRequestDTO, new PageRequest(PAGE, PAGE_SIZE))).thenReturn(germplasmDTOList);
		final int gid = Integer.parseInt(germplasmDTO.getGid());
		Mockito.when(this.pedigreeService.getCrossExpansions(Collections.singleton(gid), null, this.crossExpansionProperties))
			.thenReturn(Collections.singletonMap(gid, "CB1"));

		this.germplasmServiceImpl.searchGermplasmDTO(germplasmSearchRequestDTO, new PageRequest(PAGE, PAGE_SIZE));
		Assert.assertEquals("CB1", germplasmDTOList.get(0).getPedigree());

		Mockito.verify(this.middlewareGermplasmService, Mockito.times(1)).searchFilteredGermplasm(germplasmSearchRequestDTO, new PageRequest(PAGE, PAGE_SIZE));
	}

	@Test
	public void shouldFilterGermplasmNameTypes() {

		final Set<String> codes = new HashSet() {{
			this.add("LNAME");
		}};

		final GermplasmNameTypeDTO nameTypeDTO = new GermplasmNameTypeDTO();
		nameTypeDTO.setCode("LNAME");
		nameTypeDTO.setId(new Random().nextInt());
		nameTypeDTO.setName("LINE NAME");

		final Set<String> types = Collections.singleton(UDTableType.NAMES_NAME.getType());
		Mockito.when(this.middlewareGermplasmService.filterGermplasmNameTypes(codes)).thenReturn(Arrays.asList(nameTypeDTO));

		final List<GermplasmNameTypeDTO> germplasmListTypes = this.germplasmServiceImpl.filterGermplasmNameTypes(codes);
		assertNotNull(germplasmListTypes);
		assertThat(germplasmListTypes, hasSize(1));
		final GermplasmNameTypeDTO actualGermplasmListTypeDTO = germplasmListTypes.get(0);
		assertThat(actualGermplasmListTypeDTO.getCode(), is(nameTypeDTO.getCode()));
		assertThat(actualGermplasmListTypeDTO.getId(), is(nameTypeDTO.getId()));
		assertThat(actualGermplasmListTypeDTO.getName(), is(nameTypeDTO.getName()));

		Mockito.verify(this.middlewareGermplasmService).filterGermplasmNameTypes(codes);
		Mockito.verifyNoMoreInteractions(this.middlewareGermplasmService);
	}

	@Test
	public void testDeleteGermplasm_WithValidGids() {

		final List<Integer> gids = Lists.newArrayList(1, 2, 3);
		Mockito.when(this.germplasmDeleteValidator.checkInvalidGidsForDeletion(gids)).thenReturn(Sets.newHashSet());
		final GermplasmDeleteResponse response = this.germplasmServiceImpl.deleteGermplasm(gids);

		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(), ArgumentMatchers.anyList());
		Mockito.verify(this.middlewareGermplasmService).deleteGermplasm(gids);
		Assert.assertThat(response.getDeletedGermplasm(), iterableWithSize(3));
		Assert.assertThat(response.getGermplasmWithErrors(), iterableWithSize(0));
	}

	@Test
	public void testDeleteGermplasm_WithInvalidGermplasmForDeletion() {

		final List<Integer> gids = Lists.newArrayList(1, 2, 3);
		Mockito.when(this.germplasmDeleteValidator.checkInvalidGidsForDeletion(gids)).thenReturn(new HashSet<>(gids));
		final GermplasmDeleteResponse response = this.germplasmServiceImpl.deleteGermplasm(gids);

		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(), ArgumentMatchers.anyList());
		Mockito.verify(this.middlewareGermplasmService, Mockito.times(0)).deleteGermplasm(ArgumentMatchers.anyList());
		Assert.assertThat(response.getDeletedGermplasm(), iterableWithSize(0));
		Assert.assertThat(response.getGermplasmWithErrors(), iterableWithSize(3));
	}

	@Test
	public void testCreateGermplasm_AllCreated(){
		final WorkbenchUser user = new WorkbenchUser(1);
		Mockito.doReturn(user).when(this.securityService).getCurrentlyLoggedInUser();
		final BindingResult result = Mockito.mock(BindingResult.class);
		Mockito.doReturn(false).when(result).hasErrors();
		Mockito.doReturn(result).when(this.germplasmImportValidator).pruneGermplasmInvalidForImport(ArgumentMatchers.anyList());

		final GermplasmDTO germplasmDTO1 = new GermplasmDTO();
		germplasmDTO1.setGermplasmDbId(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO1.setGid("1");
		germplasmDTO1.setGermplasmName("CB1");
		germplasmDTO1.setGermplasmSeedSource("AF07A-412-201");
		final GermplasmDTO germplasmDTO2 = new GermplasmDTO();
		germplasmDTO2.setGermplasmDbId(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO2.setGid("2");
		germplasmDTO2.setGermplasmName("CB2");
		germplasmDTO2.setGermplasmSeedSource("AF07A-412-202");
		final List<GermplasmDTO> germplasmDTOList = Lists.newArrayList(germplasmDTO1, germplasmDTO2);
		final GermplasmImportRequest importRequest1 = new GermplasmImportRequest();
		importRequest1.setBreedingMethodDbId("13");
		importRequest1.setDefaultDisplayName(germplasmDTOList.get(0).getGermplasmName());
		importRequest1.setSeedSource(germplasmDTOList.get(0).getSeedSource());
		final GermplasmImportRequest importRequest2 = new GermplasmImportRequest();
		importRequest2.setBreedingMethodDbId("13");
		importRequest2.setDefaultDisplayName(germplasmDTOList.get(1).getGermplasmName());
		importRequest2.setSeedSource(germplasmDTOList.get(1).getSeedSource());
		final List<GermplasmImportRequest> germplasmList = Lists.newArrayList(importRequest1, importRequest2);
		final String cropName = "maize";

		Mockito.doReturn(germplasmDTOList).when(this.middlewareGermplasmService).createGermplasm(user.getUserid(), cropName, germplasmList);

		final GermplasmImportResponse importResponse = this.germplasmServiceImpl.createGermplasm(cropName, germplasmList);
		Mockito.verify(this.middlewareGermplasmService).createGermplasm(user.getUserid(), cropName, germplasmList);
		final int size = germplasmList.size();
		Assert.assertThat(importResponse.getStatus(), is(size + " out of " + size + " germplasm created successfully."));
		Assert.assertThat(importResponse.getGermplasmList(), iterableWithSize(germplasmDTOList.size()));
		Assert.assertThat(importResponse.getErrors(), nullValue());
	}

	@Test
	public void testCreateGermplasm_InvalidNotCreated(){
		final WorkbenchUser user = new WorkbenchUser(1);
		Mockito.doReturn(user).when(this.securityService).getCurrentlyLoggedInUser();
		final BindingResult result = Mockito.mock(BindingResult.class);
		final ObjectError error = Mockito.mock(ObjectError.class);
		Mockito.doReturn(true).when(result).hasErrors();
		Mockito.doReturn(Lists.newArrayList(error)).when(result).getAllErrors();
		Mockito.doReturn(result).when(this.germplasmImportValidator).pruneGermplasmInvalidForImport(ArgumentMatchers.anyList());

		final GermplasmDTO germplasmDTO1 = new GermplasmDTO();
		germplasmDTO1.setGermplasmDbId(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO1.setGid("1");
		germplasmDTO1.setGermplasmName("CB1");
		germplasmDTO1.setGermplasmSeedSource("AF07A-412-201");
		final List<GermplasmDTO> germplasmDTOList = Lists.newArrayList(germplasmDTO1);
		final GermplasmImportRequest importRequest1 = new GermplasmImportRequest();
		importRequest1.setBreedingMethodDbId("13");
		importRequest1.setDefaultDisplayName(germplasmDTOList.get(0).getGermplasmName());
		importRequest1.setSeedSource(germplasmDTOList.get(0).getSeedSource());
		final GermplasmImportRequest importRequest2 = new GermplasmImportRequest();
		importRequest2.setBreedingMethodDbId("13");
		importRequest2.setDefaultDisplayName("CB2");
		importRequest2.setSeedSource("BC07A-412-201");
		final List<GermplasmImportRequest> germplasmList = Lists.newArrayList(importRequest1, importRequest2);
		final String cropName = "maize";

		Mockito.doReturn(germplasmDTOList).when(this.middlewareGermplasmService).createGermplasm(user.getUserid(), cropName, germplasmList);

		final GermplasmImportResponse importResponse = this.germplasmServiceImpl.createGermplasm(cropName, germplasmList);
		Mockito.verify(this.middlewareGermplasmService).createGermplasm(user.getUserid(), cropName, germplasmList);
		final int size = germplasmList.size();
		Assert.assertThat(importResponse.getStatus(), is(germplasmDTOList.size() + " out of " + size + " germplasm created successfully."));
		Assert.assertThat(importResponse.getGermplasmList(), iterableWithSize(germplasmDTOList.size()));
		Assert.assertThat(importResponse.getErrors(), is(Lists.newArrayList(error)));
	}

	@Test
	public void testUpdateGermplasm(){
		final WorkbenchUser user = new WorkbenchUser(1);
		Mockito.doReturn(user).when(this.securityService).getCurrentlyLoggedInUser();

		final String germplasmDbId = RandomStringUtils.randomAlphabetic(20);
		final String breedingMethodDbId = "13";

		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(germplasmDbId);
		germplasmDTO.setGid("1");
		germplasmDTO.setGermplasmName("CB1");
		germplasmDTO.setGermplasmSeedSource("AF07A-412-201");
		final GermplasmUpdateRequest updateRequest = new GermplasmUpdateRequest();
		updateRequest.setBreedingMethodDbId(breedingMethodDbId);
		Mockito.doReturn(germplasmDTO).when(this.middlewareGermplasmService)
			.updateGermplasm(user.getUserid(), germplasmDbId, updateRequest);

		final GermplasmDTO germplasm = this.germplasmServiceImpl.updateGermplasm(germplasmDbId, updateRequest);
		Mockito.verify(this.germplasmValidator).validateGermplasmUUID(ArgumentMatchers.any(), ArgumentMatchers.eq(germplasmDbId));
		Mockito.verify(this.germplasmUpdateRequestValidator).validate(updateRequest);
		Mockito.verify(this.middlewareGermplasmService).updateGermplasm(user.getUserid(), germplasmDbId, updateRequest);
	}



}
