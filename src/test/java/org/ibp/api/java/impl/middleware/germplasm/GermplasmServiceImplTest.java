
package org.ibp.api.java.impl.middleware.germplasm;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.search_request.GermplasmSearchRequestDto;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

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
	public void testSearchGermplasm() {

		final Germplasm gp = new Germplasm();
		gp.setGid(3);
		gp.setGpid1(1);
		gp.setGpid2(2);
		gp.setMethodId(1);
		gp.setLocationId(1);

		final List<Germplasm> middlewareSearchResults = Lists.newArrayList(gp);
		Mockito.doReturn(middlewareSearchResults).when(this.germplasmDataManager).searchForGermplasm(Mockito.any(GermplasmSearchParameter.class));

		final String gpPedigree = "CML1/CML2";
		final Integer gid = gp.getGid();
		Mockito.doReturn(gpPedigree).when(this.pedigreeService).getCrossExpansion(gid, this.crossExpansionProperties);

		final Name gpName = new Name();
		gpName.setGermplasmId(gid);
		gpName.setNval("CML1");
		final List<Name> gpNames = Lists.newArrayList(gpName);
			Mockito.doReturn(gpNames).when(this.germplasmDataManager).getNamesByGID(ArgumentMatchers.anyInt(),ArgumentMatchers.eq(null),
				ArgumentMatchers.eq(null));

		final Method gpMethod = new Method();
		gpMethod.setMname("Backcross");
		Mockito.doReturn(gpMethod).when(this.germplasmDataManager).getMethodByID(ArgumentMatchers.anyInt());

		final Location gpLocation = new Location();
		gpLocation.setLname("Mexico");
		Mockito.doReturn(gpLocation).when(this.locationDataManger).getLocationByID(ArgumentMatchers.anyInt());

		final List<GermplasmSummary> germplasmSummaries = this.germplasmServiceImpl.searchGermplasm("CML", 1, 20);
		Assert.assertTrue(!germplasmSummaries.isEmpty());

		Assert.assertEquals(gid.toString(), germplasmSummaries.get(0).getGermplasmId());
		Assert.assertEquals(gp.getGpid1().toString(), germplasmSummaries.get(0).getParent1Id());
		Assert.assertEquals(gp.getGpid2().toString(), germplasmSummaries.get(0).getParent2Id());
		Assert.assertEquals(gpPedigree, germplasmSummaries.get(0).getPedigreeString());
		Assert.assertEquals(gpMethod.getMname(), germplasmSummaries.get(0).getBreedingMethod());
		Assert.assertEquals(gpLocation.getLname(), germplasmSummaries.get(0).getLocation());
		Assert.assertEquals(gpName.getNval(), germplasmSummaries.get(0).getNames().get(0).getName());
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


}
