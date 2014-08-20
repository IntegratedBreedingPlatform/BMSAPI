package org.generationcp.bms.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.generationcp.bms.dao.SimpleDao;
import org.generationcp.bms.domain.GermplasmScoreCard;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/selection")
public class SelectionResource {	
	
	private final SimpleDao simpleDao;
	
	@Autowired
	public SelectionResource(SimpleDao simpleDao){
		this.simpleDao = simpleDao;
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		return "redirect:/api-docs/default/selection-resource";
	}
	
	@RequestMapping(value="/top/trial/instance/{trialId}/{selectionIntensity}/{traits}", method = RequestMethod.GET)
	@ResponseBody
	public List<GermplasmScoreCard> selectTopPerformersForTrialInstance(@PathVariable Integer trialId, @PathVariable int selectionIntensity, @PathVariable String traits) {
		
		List<TraitInfo> traitList = new ArrayList<TraitInfo>();
		String[] traitsIds = traits.split(",");
		for (int i = 0; i < traitsIds.length; i++) {
			traitList.add(new TraitInfo(Integer.valueOf(traitsIds[i]).intValue()));
		}
		
		List<GermplasmScoreCard> scoreCards = simpleDao.getTraitObservationsForTrial(trialId, traitList);
		Collections.sort(scoreCards);
		
		//select the top designated percentage of the sorted collection
		int selectionThreshold = (int) (scoreCards.size()*selectionIntensity/100);
		return scoreCards.subList(0, selectionThreshold);
		
	}
	
	@RequestMapping(value="/trial/{studyId}/top", method = RequestMethod.GET)
	@ResponseBody
	public List<GermplasmScoreCard> selectTopPerformersForTrial(@PathVariable Integer studyId, @RequestParam int si, @RequestParam String traits) {
		
		List<TraitInfo> traitList = new ArrayList<TraitInfo>();
		String[] traitsIds = traits.split(",");
		for (int i = 0; i < traitsIds.length; i++) {
			traitList.add(new TraitInfo(Integer.valueOf(traitsIds[i]).intValue()));
		}
		
		List<GermplasmScoreCard> scoreCards = simpleDao.getTraitObservationsForStudy(studyId, traitList);
		Collections.sort(scoreCards);
		
		//select the top designated percentage of the sorted collection
		int selectionThreshold = (int) (scoreCards.size()*si/100);
		return scoreCards.subList(0, selectionThreshold);
		
	}

}
