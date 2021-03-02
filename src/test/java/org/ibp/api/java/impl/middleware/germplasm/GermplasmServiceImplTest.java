
package org.ibp.api.java.impl.middleware.germplasm;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.germplasm.GermplasmDeleteResponse;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmDeleteValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
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

	@Captor
	private ArgumentCaptor<Set<String>> setArgumentCaptor;

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

		final UserDefinedField userDefinedField = new UserDefinedField();
		userDefinedField.setFcode("LNAME");
		userDefinedField.setFldno(new Random().nextInt());
		userDefinedField.setFname("LINE NAME");

		final Set<String> types = Collections.singleton(UDTableType.NAMES_NAME.getType());
		Mockito.when(this.germplasmDataManager.getUserDefinedFieldByTableTypeAndCodes(UDTableType.NAMES_NAME.getTable(), types, codes))
			.thenReturn(Arrays.asList(userDefinedField));

		final List<GermplasmNameTypeDTO> germplasmListTypes = this.germplasmServiceImpl.filterGermplasmNameTypes(codes);
		assertNotNull(germplasmListTypes);
		assertThat(germplasmListTypes, hasSize(1));
		final GermplasmNameTypeDTO actualGermplasmListTypeDTO = germplasmListTypes.get(0);
		assertThat(actualGermplasmListTypeDTO.getCode(), is(userDefinedField.getFcode()));
		assertThat(actualGermplasmListTypeDTO.getId(), is(userDefinedField.getFldno()));
		assertThat(actualGermplasmListTypeDTO.getName(), is(userDefinedField.getFname()));

		Mockito.verify(this.germplasmDataManager).getUserDefinedFieldByTableTypeAndCodes(UDTableType.NAMES_NAME.getTable(), types, codes);
		Mockito.verifyNoMoreInteractions(this.germplasmDataManager);
	}

	@Test
	public void shouldFilterGermplasmAttributes() {
		final Set<String> codes = Collections.singleton("NOTE");

		final UserDefinedField userDefinedField = new UserDefinedField();
		userDefinedField.setFcode("NOTE");
		userDefinedField.setFldno(new Random().nextInt());
		userDefinedField.setFname("NOTES");

		Mockito.when(
			this.germplasmDataManager.getUserDefinedFieldByTableTypeAndCodes(ArgumentMatchers.eq(UDTableType.ATRIBUTS_ATTRIBUTE.getTable()),
				ArgumentMatchers.anySet(), ArgumentMatchers.eq(codes))).thenReturn(Arrays.asList(userDefinedField));

		final List<AttributeDTO> germplasmListTypes = this.germplasmServiceImpl.filterGermplasmAttributes(codes);
		assertNotNull(germplasmListTypes);
		assertThat(germplasmListTypes, hasSize(1));
		final AttributeDTO actualGermplasmListTypeDTO = germplasmListTypes.get(0);
		assertThat(actualGermplasmListTypeDTO.getCode(), is(userDefinedField.getFcode()));
		assertThat(actualGermplasmListTypeDTO.getId(), is(userDefinedField.getFldno()));
		assertThat(actualGermplasmListTypeDTO.getName(), is(userDefinedField.getFname()));

		Mockito.verify(this.germplasmDataManager)
			.getUserDefinedFieldByTableTypeAndCodes(ArgumentMatchers.eq(UDTableType.ATRIBUTS_ATTRIBUTE.getTable()),
				this.setArgumentCaptor.capture(), ArgumentMatchers.eq(codes));
		final Set<String> actualTypes = this.setArgumentCaptor.getValue();
		assertNotNull(actualTypes);
		assertThat(actualTypes, hasSize(2));
		assertThat(actualTypes, contains(UDTableType.ATRIBUTS_ATTRIBUTE.getType(), UDTableType.ATRIBUTS_PASSPORT.getType()));
		Mockito.verifyNoMoreInteractions(this.germplasmDataManager);
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

}
