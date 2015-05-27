package org.ibp.api.java.impl.middleware.germplasm;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.Operation;
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
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		germplasmServiceImpl = new GermplasmServiceImpl();
		germplasmServiceImpl.setGermplasmDataManager(germplasmDataManager);
		germplasmServiceImpl.setPedigreeService(pedigreeService);
		germplasmServiceImpl.setLocationDataManger(locationDataManger);
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
		Mockito.when(germplasmDataManager.searchForGermplasm("CML", Operation.LIKE, false, false)).thenReturn(middlewareSearchResults);
		String gpPedigree = "CML1/CML2";
		Mockito.when(pedigreeService.getCrossExpansion(Mockito.anyInt(), Mockito.any(CrossExpansionProperties.class))).thenReturn(gpPedigree);
		
		Name gpName = new Name();
		gpName.setGermplasmId(gp.getGid());
		gpName.setNval("CML1");
		List<Name> gpNames = Lists.newArrayList(gpName);
		Mockito.when(germplasmDataManager.getNamesByGID(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(GermplasmNameType.class))).thenReturn(gpNames);

		Method gpMethod = new Method();
		gpMethod.setMname("Backcross");
		Mockito.when(germplasmDataManager.getMethodByID(Mockito.anyInt())).thenReturn(gpMethod);
		
		Location gpLocation = new Location();
		gpLocation.setLname("Mexico");
		Mockito.when(locationDataManger.getLocationByID(Mockito.anyInt())).thenReturn(gpLocation);
		
		List<GermplasmSummary> germplasmSummaries = germplasmServiceImpl.searchGermplasm("CML");
		Assert.assertTrue(!germplasmSummaries.isEmpty());
		
		Assert.assertEquals(gp.getGid().toString(), germplasmSummaries.get(0).getGermplasmId());
		Assert.assertEquals(gp.getGpid1().toString(), germplasmSummaries.get(0).getParent1Id());
		Assert.assertEquals(gp.getGpid2().toString(), germplasmSummaries.get(0).getParent2Id());
		Assert.assertEquals(gpPedigree, germplasmSummaries.get(0).getPedigreeString());
		Assert.assertEquals(gpMethod.getMname(), germplasmSummaries.get(0).getBreedingMethod());
		Assert.assertEquals(gpLocation.getLname(), germplasmSummaries.get(0).getLocation());
		Assert.assertEquals(gpName.getNval(), germplasmSummaries.get(0).getNames().get(0));
	}

}
