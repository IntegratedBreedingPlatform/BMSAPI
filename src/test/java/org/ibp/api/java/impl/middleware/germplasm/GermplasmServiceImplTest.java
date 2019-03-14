
package org.ibp.api.java.impl.middleware.germplasm;

import java.util.List;

import org.generationcp.middleware.dao.germplasm.GermplasmSearchRequestDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
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
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class GermplasmServiceImplTest {

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
	public void testSearchGermplasm() throws MiddlewareQueryException {

		Germplasm gp = new Germplasm();
		gp.setGid(3);
		gp.setGpid1(1);
		gp.setGpid2(2);
		gp.setMethodId(1);
		gp.setLocationId(1);

		List<Germplasm> middlewareSearchResults = Lists.newArrayList(gp);
		Mockito.doReturn(middlewareSearchResults).when(germplasmDataManager).searchForGermplasm(Mockito.any(GermplasmSearchParameter.class));

		String gpPedigree = "CML1/CML2";
		Mockito.doReturn(gpPedigree).when(pedigreeService).getCrossExpansion(gp.getGid(), this.crossExpansionProperties);

		Name gpName = new Name();
		gpName.setGermplasmId(gp.getGid());
		gpName.setNval("CML1");
		List<Name> gpNames = Lists.newArrayList(gpName);
		Mockito.doReturn(gpNames).when(germplasmDataManager).getNamesByGID(Matchers.anyInt(),ArgumentMatchers.isNull(Integer.class),
				ArgumentMatchers.isNull(GermplasmNameType.class));

		Method gpMethod = new Method();
		gpMethod.setMname("Backcross");
		Mockito.doReturn(gpMethod).when(germplasmDataManager).getMethodByID(Matchers.anyInt());

		Location gpLocation = new Location();
		gpLocation.setLname("Mexico");
		Mockito.doReturn(gpLocation).when(locationDataManger).getLocationByID(Matchers.anyInt());

		List<GermplasmSummary> germplasmSummaries = this.germplasmServiceImpl.searchGermplasm("CML", 1, 20);
		Assert.assertTrue(!germplasmSummaries.isEmpty());

		Assert.assertEquals(gp.getGid().toString(), germplasmSummaries.get(0).getGermplasmId());
		Assert.assertEquals(gp.getGpid1().toString(), germplasmSummaries.get(0).getParent1Id());
		Assert.assertEquals(gp.getGpid2().toString(), germplasmSummaries.get(0).getParent2Id());
		Assert.assertEquals(gpPedigree, germplasmSummaries.get(0).getPedigreeString());
		Assert.assertEquals(gpMethod.getMname(), germplasmSummaries.get(0).getBreedingMethod());
		Assert.assertEquals(gpLocation.getLname(), germplasmSummaries.get(0).getLocation());
		Assert.assertEquals(gpName.getNval(), germplasmSummaries.get(0).getNames().get(0).getName());
	}

	@Test
	public void testsearchGermplasmDTO () {

		final GermplasmSearchRequestDTO germplasmSearchRequestDTO = Mockito.mock(GermplasmSearchRequestDTO.class);

		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId("1");
		germplasmDTO.setGermplasmName("CB1");
		germplasmDTO.setGermplasmSeedSource("AF07A-412-201");
		List<GermplasmDTO> germplasmDTOList = Lists.newArrayList(germplasmDTO);

		Mockito.when(germplasmDataManager.searchGermplasmDTO(germplasmSearchRequestDTO)).thenReturn(germplasmDTOList);
		Mockito.when(pedigreeService.getCrossExpansion(Integer.parseInt(germplasmDTO.getGermplasmDbId()), this.crossExpansionProperties)).thenReturn("CB1");

		this.germplasmServiceImpl.searchGermplasmDTO(germplasmSearchRequestDTO);
		Assert.assertEquals("CB1", germplasmDTOList.get(0).getPedigree());
	}


}
