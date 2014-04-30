package org.generationcp.bms.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.bms.dao.SimpleDao;
import org.generationcp.bms.domain.GermplasmScoreCard;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.ObservationKey;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/selection")
public class SelectionResource {	
	
	private final SimpleDao simpleDao;
	
	@Autowired
	public SelectionResource(SimpleDao simpleDao){
		this.simpleDao = simpleDao;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(HttpServletRequest request) throws MiddlewareQueryException {
		return "Please provide parameters for selection in format host:port/selection/top/{trialId}/{selectionIntensity}";
	}
	
	@RequestMapping(value="/top/{trialId}/{selectionIntensity}", method = RequestMethod.GET)
	public List<GermplasmScoreCard> selectTopPerformers(@PathVariable Integer trialId, @PathVariable int selectionIntensity, ArrayList<TraitInfo> traits) {
		
		// FIXME : Using Mock Data just to get the service running
		List<Observation> observations = getMockData();
		// FIXME : wire this up to the view
		// List<Observation> observations = simpleDao.getTraitObservationsForTrial(trialId);
		
		// sort the Germplasm Observations into lists of Observatons for a single Germplasm
		Map<Integer, GermplasmScoreCard> scoreMap = new HashMap<Integer, GermplasmScoreCard>();
		for (Observation observation : observations) {
			if (!scoreMap.containsKey(observation.getId().getGermplasmId())) {
				scoreMap.put(observation.getId().getGermplasmId(), new GermplasmScoreCard(observation.getId().getGermplasmId(), "Germplasm IR 123-" + observation.getId().getGermplasmId()));
			}
			scoreMap.get(observation.getId().getGermplasmId()).incrementScore(Integer.parseInt(observation.getValue()));
		}
		
		List<GermplasmScoreCard> scores = new ArrayList<GermplasmScoreCard>(scoreMap.values());
		Collections.sort(scores);
		
		//select the top designated percentage of the sorted collection
		int selectionThreshold = (int) (scores.size()*selectionIntensity/100);
		return scores.subList(0, selectionThreshold);
		
	}

	private List<Observation> getMockData() {
		List<Observation> mockObs = new ArrayList<Observation>();
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
		
		return mockObs;
	}
	
	

}
