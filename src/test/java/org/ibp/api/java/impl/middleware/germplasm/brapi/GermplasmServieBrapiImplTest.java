package org.ibp.api.java.impl.middleware.germplasm.brapi;

import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.api.brapi.GermplasmServiceBrapi;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.brapi.v2.germplasm.GermplasmImportRequestValidator;
import org.ibp.api.brapi.v2.germplasm.GermplasmImportResponse;
import org.ibp.api.brapi.v2.germplasm.GermplasmUpdateRequestValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
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

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.nullValue;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmServieBrapiImplTest {

	private static final int PAGE_SIZE = 10;
	private static final int PAGE = 1;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Mock
	private GermplasmServiceBrapi middlewareGermplasmServiceBrapi;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private GermplasmImportRequestValidator germplasmImportValidator;

	@Mock
	private GermplasmUpdateRequestValidator germplasmUpdateRequestValidator;

	@InjectMocks
	private GermplasmServiceBrapiImpl germplasmServiceBrapi;

	@Test
	public void testCreateGermplasm_AllCreated(){
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

		Mockito.doReturn(germplasmDTOList).when(this.middlewareGermplasmServiceBrapi).createGermplasm(cropName, germplasmList);

		final GermplasmImportResponse importResponse = this.germplasmServiceBrapi.createGermplasm(cropName, germplasmList);
		Mockito.verify(this.middlewareGermplasmServiceBrapi).createGermplasm(cropName, germplasmList);
		final int size = germplasmList.size();
		Assert.assertThat(importResponse.getCreatedSize(), is(size));
		Assert.assertThat(importResponse.getImportListSize(), is(size));
		Assert.assertThat(importResponse.getEntityList(), iterableWithSize(germplasmDTOList.size()));
		Assert.assertThat(importResponse.getErrors(), nullValue());
	}

	@Test
	public void testCreateGermplasm_InvalidNotCreated(){
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

		Mockito.doReturn(germplasmDTOList).when(this.middlewareGermplasmServiceBrapi).createGermplasm(cropName, germplasmList);

		final GermplasmImportResponse importResponse = this.germplasmServiceBrapi.createGermplasm(cropName, germplasmList);
		Mockito.verify(this.middlewareGermplasmServiceBrapi).createGermplasm(cropName, germplasmList);
		final int size = germplasmList.size();
		Assert.assertThat(importResponse.getCreatedSize(), is(germplasmDTOList.size()));
		Assert.assertThat(importResponse.getImportListSize(), is(size));
		Assert.assertThat(importResponse.getEntityList(), iterableWithSize(germplasmDTOList.size()));
		Assert.assertThat(importResponse.getErrors(), is(Lists.newArrayList(error)));
	}

	@Test
	public void testUpdateGermplasm(){
		final String germplasmDbId = RandomStringUtils.randomAlphabetic(20);
		final String breedingMethodDbId = "13";

		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(germplasmDbId);
		germplasmDTO.setGid("1");
		germplasmDTO.setGermplasmName("CB1");
		germplasmDTO.setGermplasmSeedSource("AF07A-412-201");
		final GermplasmUpdateRequest updateRequest = new GermplasmUpdateRequest();
		updateRequest.setBreedingMethodDbId(breedingMethodDbId);
		Mockito.doReturn(germplasmDTO).when(this.middlewareGermplasmServiceBrapi)
			.updateGermplasm(germplasmDbId, updateRequest);

		final GermplasmDTO germplasm = this.germplasmServiceBrapi.updateGermplasm(germplasmDbId, updateRequest);
		Mockito.verify(this.germplasmValidator).validateGermplasmUUID(ArgumentMatchers.any(), ArgumentMatchers.eq(germplasmDbId));
		Mockito.verify(this.germplasmUpdateRequestValidator).validate(updateRequest);
		Mockito.verify(this.middlewareGermplasmServiceBrapi).updateGermplasm(germplasmDbId, updateRequest);
	}

	@Test
	public void testSearchGermplasmDTO() {

		final GermplasmSearchRequestDto germplasmSearchRequestDTO = new GermplasmSearchRequestDto();

		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setGid("1");
		germplasmDTO.setGermplasmName("CB1");
		germplasmDTO.setGermplasmSeedSource("AF07A-412-201");
		final List<GermplasmDTO> germplasmDTOList = Lists.newArrayList(germplasmDTO);

		Mockito.when(this.middlewareGermplasmServiceBrapi.searchGermplasmDTO(germplasmSearchRequestDTO, new PageRequest(PAGE, PAGE_SIZE))).thenReturn(germplasmDTOList);
		final int gid = Integer.parseInt(germplasmDTO.getGid());
		Mockito.when(this.pedigreeService.getCrossExpansions(Collections.singleton(gid), null, this.crossExpansionProperties))
			.thenReturn(Collections.singletonMap(gid, "CB1"));

		this.germplasmServiceBrapi.searchGermplasmDTO(germplasmSearchRequestDTO, new PageRequest(PAGE, PAGE_SIZE));
		Assert.assertEquals("CB1", germplasmDTOList.get(0).getPedigree());

		Mockito.verify(this.middlewareGermplasmServiceBrapi, Mockito.times(1)).searchGermplasmDTO(germplasmSearchRequestDTO, new PageRequest(PAGE, PAGE_SIZE));
	}

}
