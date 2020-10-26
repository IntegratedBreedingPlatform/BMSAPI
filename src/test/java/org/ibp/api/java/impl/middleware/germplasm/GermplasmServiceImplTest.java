
package org.ibp.api.java.impl.middleware.germplasm;

import com.google.common.collect.Lists;
import org.generationcp.middleware.api.germplasm.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hamcrest.Matchers;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class GermplasmServiceImplTest {

	private static final int PAGE_SIZE = 10;
	private static final int PAGE = 1;
	private GermplasmServiceImpl germplasmServiceImpl;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private LocationDataManager locationDataManger;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		this.germplasmServiceImpl = new GermplasmServiceImpl();
		this.germplasmServiceImpl.setGermplasmDataManager(this.germplasmDataManager);
		this.germplasmServiceImpl.setPedigreeService(this.pedigreeService);
		this.germplasmServiceImpl.setLocationDataManger(this.locationDataManger);
		this.germplasmServiceImpl.setCrossExpansionProperties(this.crossExpansionProperties);
	}

	@Test
	public void testSearchGermplasmDTO () {

		final GermplasmSearchRequestDto germplasmSearchRequestDTO = new GermplasmSearchRequestDto();

		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId("1");
		germplasmDTO.setGermplasmName("CB1");
		germplasmDTO.setGermplasmSeedSource("AF07A-412-201");
		final List<GermplasmDTO> germplasmDTOList = Lists.newArrayList(germplasmDTO);

		Mockito.when(this.germplasmDataManager.searchGermplasmDTO(germplasmSearchRequestDTO, PAGE, PAGE_SIZE)).thenReturn(germplasmDTOList);
		final int gid = Integer.parseInt(germplasmDTO.getGermplasmDbId());
		Mockito.when(this.pedigreeService.getCrossExpansions(Collections.singleton(gid), null, this.crossExpansionProperties))
			.thenReturn(Collections.singletonMap(gid, "CB1"));

		this.germplasmServiceImpl.searchGermplasmDTO(germplasmSearchRequestDTO, PAGE, PAGE_SIZE);
		Assert.assertEquals("CB1", germplasmDTOList.get(0).getPedigree());

		Mockito.verify(this.germplasmDataManager, Mockito.times(1)).searchGermplasmDTO(germplasmSearchRequestDTO, PAGE, PAGE_SIZE);
	}

	@Test
	public void shouldGermplasmNameTypesByCodes() {

		final Set<String> codes = new HashSet() {{
			add("LNAME");
		}};

		final UserDefinedField userDefinedField = new UserDefinedField();
		userDefinedField.setFcode("LNAME");
		userDefinedField.setFldno(new Random().nextInt());
		userDefinedField.setFname("LINE NAME");

		Mockito.when(this.germplasmDataManager.getUserDefinedFieldByTableTypeAndCodes("NAMES", "NAME", codes)).thenReturn(Arrays.asList(userDefinedField));

		final List<GermplasmNameTypeDTO> germplasmListTypes = this.germplasmServiceImpl.getGermplasmNameTypesByCodes(codes);
		assertNotNull(germplasmListTypes);
		assertThat(germplasmListTypes, hasSize(1));
		final GermplasmNameTypeDTO actualGermplasmListTypeDTO = germplasmListTypes.get(0);
		assertThat(actualGermplasmListTypeDTO.getCode(), is(userDefinedField.getFcode()));
		assertThat(actualGermplasmListTypeDTO.getId(), is(userDefinedField.getFldno()));
		assertThat(actualGermplasmListTypeDTO.getName(), is(userDefinedField.getFname()));

		Mockito.verify(this.germplasmDataManager).getUserDefinedFieldByTableTypeAndCodes("NAMES", "NAME", codes);
		Mockito.verifyNoMoreInteractions(this.germplasmDataManager);
	}


}
