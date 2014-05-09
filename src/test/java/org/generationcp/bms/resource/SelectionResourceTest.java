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
	private List<GermplasmScoreCard> mockObs;
	
	@Before
	public void setUpMocks() {
		
		simpleDao = Mockito.mock(SimpleDao.class);		
		mockObs = new ArrayList<GermplasmScoreCard>();
		
		// Germplasm 1
		GermplasmScoreCard gsc1 = new GermplasmScoreCard(1, "GP-1");
		gsc1.addObservation(new Observation(new ObservationKey(21735, 1, 5765), "22"));
		gsc1.addObservation(new Observation(new ObservationKey(21726, 1, 5765), "22"));
		gsc1.addObservation(new Observation(new ObservationKey(20826, 1, 5765), "3"));
		// Germplasm 2
		GermplasmScoreCard gsc2 = new GermplasmScoreCard(2, "GP-2");
		gsc2.addObservation(new Observation(new ObservationKey(21735, 2, 5765), "24"));
		gsc2.addObservation(new Observation(new ObservationKey(21726, 2, 5765), "2"));
		gsc2.addObservation(new Observation(new ObservationKey(20826, 2, 5765), "7"));
		// Germplasm 3
		GermplasmScoreCard gsc3 = new GermplasmScoreCard(3, "GP-3");
		gsc3.addObservation(new Observation(new ObservationKey(21735, 3, 5765), "28"));
		gsc3.addObservation(new Observation(new ObservationKey(20826, 3, 5765), "10"));
		gsc3.addObservation(new Observation(new ObservationKey(21726, 3, 5765), "56"));
		// Germplasm 4
		GermplasmScoreCard gsc4 = new GermplasmScoreCard(4, "GP-4");
		gsc4.addObservation(new Observation(new ObservationKey(21735, 4, 5765), "29"));
		gsc4.addObservation(new Observation(new ObservationKey(21726, 4, 5765), "109"));
		gsc4.addObservation(new Observation(new ObservationKey(20826, 4, 5765), "7"));
		// Germplasm 5
		GermplasmScoreCard gsc5 = new GermplasmScoreCard(5, "GP-5");
		gsc5.addObservation(new Observation(new ObservationKey(21726, 5, 5765), "125"));
		gsc5.addObservation(new Observation(new ObservationKey(21735, 5, 5765), "30"));
		gsc5.addObservation(new Observation(new ObservationKey(20826, 5, 5765), "7"));
		// Germplasm 6
		GermplasmScoreCard gsc6 = new GermplasmScoreCard(6, "GP-6");
		gsc6.addObservation(new Observation(new ObservationKey(21735, 6, 5765), "33"));
		gsc6.addObservation(new Observation(new ObservationKey(21726, 6, 5765), "33"));
		gsc6.addObservation(new Observation(new ObservationKey(20826, 6, 5765), "7"));
		
		mockObs.add(gsc1);
		mockObs.add(gsc2);
		mockObs.add(gsc3);
		mockObs.add(gsc4);
		mockObs.add(gsc5);
		mockObs.add(gsc6);
		
	}
	
	@Test
	public void testTopPerformerSelection() {
		
		SelectionResource selector = new SelectionResource(simpleDao);
		
		// set up the selection scenario
		// let's start with a Trial Environment - a trial we have just completed
		Integer trialEnvironmentId = new Integer(5765);
		
		// traits we are interested in selecting for
		ArrayList<TraitInfo> interestingTraits = new ArrayList<TraitInfo>();
		interestingTraits.add(new TraitInfo(21735)); // amylose
		interestingTraits.add(new TraitInfo(21726)); // plant height
		interestingTraits.add(new TraitInfo(20826)); // grain density
		
		String traits = new String("27135,21726,20826");
		
		// selection intensity : the percentage of plants we like to keep - generally a percentage
		int selectionIntensity = 50;
		
		Mockito.when(simpleDao.getTraitObservationsForTrial(Mockito.anyInt(), Mockito.anyList())).thenReturn(mockObs);
		
		//select
		List<GermplasmScoreCard> topPerformers = selector.selectTopPerformersForTrialInstance(trialEnvironmentId, selectionIntensity, traits);
		
		assertTrue(topPerformers.size() > 0);
		assertTrue(topPerformers.size() == 3);
		
		Debug.println(topPerformers.toString());
		
	}

}
