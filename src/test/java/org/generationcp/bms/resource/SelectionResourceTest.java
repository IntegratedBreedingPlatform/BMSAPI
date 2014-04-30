package org.generationcp.bms.resource;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.bms.dao.SimpleDao;
import org.generationcp.bms.domain.GermplasmScoreCard;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.ObservationKey;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.util.Debug;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SelectionResourceTest {
	
	private SimpleDao simpleDao;
	private List<Observation> mockObs;
	
	@Before
	public void setUpMocks() {
		simpleDao = Mockito.mock(SimpleDao.class);
		mockObs = new ArrayList<>();
		// trait amylose content
		mockObs.add(new Observation(new ObservationKey(21735, 1, 5765), "22"));
		mockObs.add(new Observation(new ObservationKey(21735, 2, 5765), "24"));
		mockObs.add(new Observation(new ObservationKey(21735, 3, 5765), "28"));
		mockObs.add(new Observation(new ObservationKey(21735, 4, 5765), "29"));
		mockObs.add(new Observation(new ObservationKey(21735, 5, 5765), "30"));
		mockObs.add(new Observation(new ObservationKey(21735, 6, 5765), "33"));
		//trait plant height
		mockObs.add(new Observation(new ObservationKey(21726, 1, 5765), "22"));
		mockObs.add(new Observation(new ObservationKey(21726, 2, 5765), "2"));
		mockObs.add(new Observation(new ObservationKey(21726, 3, 5765), "56"));
		mockObs.add(new Observation(new ObservationKey(21726, 4, 5765), "109"));
		mockObs.add(new Observation(new ObservationKey(21726, 5, 5765), "125"));
		mockObs.add(new Observation(new ObservationKey(21726, 6, 5765), "33"));
		// trait grain length
		mockObs.add(new Observation(new ObservationKey(20826, 1, 5765), "3"));
		mockObs.add(new Observation(new ObservationKey(20826, 2, 5765), "7"));
		mockObs.add(new Observation(new ObservationKey(20826, 3, 5765), "10"));
		mockObs.add(new Observation(new ObservationKey(20826, 4, 5765), "7"));
		mockObs.add(new Observation(new ObservationKey(20826, 5, 5765), "7"));
		mockObs.add(new Observation(new ObservationKey(20826, 6, 5765), "7"));
	}
	
	@Test
	public void testTopPerformerSelection() {
		
		SelectionResource selector = new SelectionResource(simpleDao);
		
		// set up the selection scenario
		// let's start with a Trial Environment - a trial we have just completed
		Integer trialEnvironmentId = new Integer(5765);
		
		// traits we are interested in selecting for
		ArrayList<TraitInfo> interestingTraits = new ArrayList<TraitInfo>();
		interestingTraits.add(new TraitInfo(21735, "AMYLOSE")); // amylose
		interestingTraits.add(new TraitInfo(21726, "PLTHGT")); // plant height
		interestingTraits.add(new TraitInfo(20826, "GRNLTH")); // grain density
		
		// selection intensity : the percentage of plants we like to keep - generally a percentage
		int selectionIntensity = 50;
		
		Mockito.when(simpleDao.getTraitObservationsForTrial(trialEnvironmentId)).thenReturn(mockObs);
		
		//select
		List<GermplasmScoreCard> topPerformers = selector.selectTopPerformers(trialEnvironmentId, selectionIntensity, interestingTraits);
		
		assertTrue(topPerformers.size() > 0);
		assertTrue(topPerformers.size() == 3);
		
		Debug.println(topPerformers.toString());
		
	}

}
