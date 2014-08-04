package org.generationcp.bms.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.generationcp.bms.domain.OntologyUsage;
import org.generationcp.bms.util.IdentifierMadness;
import org.generationcp.bms.util.VariableCategory;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariableSummary;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/ontology/usage")
public class OntologyUsageResource {
	
    private static final Logger LOG = LoggerFactory.getLogger(OntologyUsageResource.class);

    @Autowired
    private OntologyService ontologyService;
     
    @Autowired
    private FieldbookService fieldbookMiddlewareService;
      
    /**
     * Gets the usage stats for standard variables 
     *
     * @return the ontology usage details
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonGenerationException 
     */
	@RequestMapping(value = "/variables", method = RequestMethod.GET)
    public String getUsageBySettingsMode(@RequestParam(required=true) Integer groupId, @RequestParam(required=false) Boolean flat,
    		@RequestParam(required=false) Integer maxResults) {
    	try {
    		
    		if(flat == null) flat = true;
    		
    		// Fetch the list of filtered Standard Variable References and extract ids (this would be better if 
    		// filtered SVs were full objects)
    		List<StandardVariableReference> stdVars = filterStandardVariablesForSetting(groupId, new HashSet<Integer>());
    		if(maxResults == null) maxResults = stdVars.size();
    		LOG.info("Filtering for " + groupId + " : results : " + stdVars.size());
    		
    		List<Integer> ids = new ArrayList<Integer>();
    		for (StandardVariableReference standardVariableReference : stdVars) {
				ids.add(standardVariableReference.getId());
			}
    		// create a Map - - we will select from this list to return, as the include method and scale information
    		Map<Integer, StandardVariableSummary> svMap = new HashMap<Integer, StandardVariableSummary>();
    		// fetch filtered Standard Variables 
    		List<StandardVariableSummary> standardVariables = ontologyService.getStandardVariableSummaries(ids);
    		for (StandardVariableSummary standardVariable : standardVariables) {
				svMap.put(Integer.valueOf(standardVariable.getId()), standardVariable);
			}
    		
    		// Collecting stats in a TreeMap to sort on experimental usage
    		Map<Long, List<String>> usageMap = new TreeMap<Long, List<String>>(Collections.reverseOrder());
    		List<OntologyUsage> usageList = new ArrayList<OntologyUsage>();
    		
    		List<TraitClassReference> tree = ontologyService.getAllTraitGroupsHierarchy(true);
    		for (TraitClassReference root : tree) {
    			for (TraitClassReference traitClassReference : root.getTraitClassChildren()) {
    				usageList = processTreeTraitClasses(svMap, stdVars, usageList, traitClassReference);
				}
			}
    		
	    	ObjectMapper om = new ObjectMapper();
	    	if(flat) {
				for (OntologyUsage usage : usageList) {
					if (usageMap.get(Long.valueOf(usage.getExperimentCount())) == null) {
						usageMap.put(Long.valueOf(usage.getExperimentCount()), new ArrayList<String>());
					}
					usageMap.get(Long.valueOf(usage.getExperimentCount())).add(usage.getFlatView());
				}
	    		return om.writeValueAsString(usageMap.values());
	    	}
	    	else {
	    		// FIXME : aim to avoid warning suppression    		
	    		Collections.sort(usageList, new Comparator<OntologyUsage>() {

					@Override
					public int compare(OntologyUsage o1, OntologyUsage o2) {
						return o2.getExperimentCount().compareTo(o1.getExperimentCount());
					}
				});
	    		if(maxResults < usageList.size()) {
	    			return om.writeValueAsString(usageList.subList(0, maxResults));
	    		}
	    		return om.writeValueAsString(usageList);
	    	}
			
		} catch (JsonGenerationException e) {
			LOG.error("Error generating JSON for property trees " + e.getMessage());
		} catch (JsonMappingException e) {
			LOG.error("Error mapping JSON for property trees " + e.getMessage());
		} catch (IOException e) {
			LOG.error("Error writing JSON for property trees " + e.getMessage());
		} catch (MiddlewareQueryException e) {
			LOG.error("Error querying Ontology Manager for full Ontology Tree " + e.getMessage());
		}
    	return "[]";
    }
    
    
	public List<StandardVariableReference> filterStandardVariablesForSetting(int mode, Set<Integer> selectedIds)
			throws MiddlewareQueryException {
		
		List<StandardVariableReference> result = new ArrayList<StandardVariableReference>();
		
		List<Integer> storedInIds = getStoredInIdsByMode(mode, true);
		List<Integer> propertyIds = getPropertyIdsByMode(mode);

        List<StandardVariableReference> dbList = fieldbookMiddlewareService.filterStandardVariablesByMode(storedInIds, propertyIds,
                mode == VariableCategory.TRAITS.getId() || mode == VariableCategory.NURSERY_CONDITIONS.getId() ? true : false);

        if (dbList != null && !dbList.isEmpty()) {
            for (StandardVariableReference ref : dbList) {
                if (!selectedIds.contains(ref.getId())) {
                    if (mode == VariableCategory.STUDY.getId()) {
                        if (IdentifierMadness.FILTER_NURSERY_FIELDS.contains(ref.getId())
                                || ref.getId().intValue() == TermId.DATASET_NAME.getId()
                                || ref.getId().intValue() == TermId.DATASET_TITLE.getId()
                                || ref.getId().intValue() == TermId.DATASET_TYPE.getId()
                                || IdentifierMadness.HIDE_ID_VARIABLES.contains((ref.getId())))
                            continue;

                    } else if (mode == VariableCategory.PLOT.getId()) {
                        if (IdentifierMadness.HIDE_PLOT_FIELDS.contains(ref.getId())) {
                            continue;
                        }
                    } else if (mode == VariableCategory.SELECTION_VARIATES.getId()) {
                        if (IdentifierMadness.HIDE_ID_VARIABLES.contains(ref.getId())) {
                            continue;
                        }
                    }
                    result.add(ref);
                }
            }
        }
        Collections.sort(result);
        return result;
    }
	
	private List<Integer> getStoredInIdsByMode(int mode, boolean isNursery) {
		List<Integer> list = new ArrayList<Integer>();
		if (mode == VariableCategory.STUDY.getId()) {
			list.addAll(PhenotypicType.STUDY.getTypeStorages());
			if (isNursery) {
				list.addAll(PhenotypicType.TRIAL_ENVIRONMENT.getTypeStorages());
			}
		} else if (mode == VariableCategory.PLOT.getId()) {
			list.addAll(PhenotypicType.TRIAL_DESIGN.getTypeStorages());
			list.addAll(PhenotypicType.GERMPLASM.getTypeStorages());
		} else if (mode == VariableCategory.TRAITS.getId()
				|| mode == VariableCategory.SELECTION_VARIATES.getId()
				|| mode == VariableCategory.NURSERY_CONDITIONS.getId()) {
			list.addAll(PhenotypicType.VARIATE.getTypeStorages());
		} else if (mode == VariableCategory.TRIAL_ENVIRONMENT.getId()) {
			list.addAll(PhenotypicType.TRIAL_ENVIRONMENT.getTypeStorages());
		} else if (mode == VariableCategory.TREATMENT_FACTORS.getId()) {
			list.addAll(PhenotypicType.TRIAL_DESIGN.getTypeStorages());
		} else if (mode == VariableCategory.GERMPLASM.getId()) {
			list.addAll(PhenotypicType.GERMPLASM.getTypeStorages());
		}
		return list;
	}

	private List<Integer> getPropertyIdsByMode(int mode) {
		List<Integer> list = new ArrayList<Integer>();
		if (mode == VariableCategory.SELECTION_VARIATES.getId()
				|| mode == VariableCategory.TRAITS.getId()
				|| mode == VariableCategory.NURSERY_CONDITIONS.getId()) {
			list = IdentifierMadness.SELECTION_VARIATES_PROPERTIES;
		}
		return list;
	}

	/**
	 * Recursive kick-off spot for Ontological tree processing. Recursive node
	 * is the TraitClassNode. If Trait Classes and Properties for a TraitClass
	 * node are exhausted, then the algorithm will return with collected
	 * Properties.
	 * 
	 * @param classId
	 * 
	 * @param svMap
	 *            : standard variable map. Only return standard variables from
	 *            this map
	 * @param stdVars
	 *            : standard variables to process
	 * @param usageList
	 *            : collection units for results
	 * @param traitClassReference
	 *            : the class node that contains either further subClasses,
	 *            and/or Properties
	 * @return
	 * @throws MiddlewareQueryException
	 */
	private List<OntologyUsage> processTreeTraitClasses(
			Map<Integer, StandardVariableSummary> svMap, List<StandardVariableReference> stdVars,
			List<OntologyUsage> usageList, TraitClassReference traitClassReference)
			throws MiddlewareQueryException {

		LOG.info("Processing Trait Class : " + traitClassReference.getName());
		// We might encounter a trait class node - if so, process sub trait class nodes
		if (!traitClassReference.getTraitClassChildren().isEmpty()) {
			for (TraitClassReference subTraitClass : traitClassReference.getTraitClassChildren()) {
				usageList = processTreeTraitClasses(svMap, stdVars, usageList, subTraitClass);
			}
		}
		// and process properties of that trait class
		if (!traitClassReference.getProperties().isEmpty()) {
			usageList = processTreeProperties(svMap, stdVars, usageList, traitClassReference);
		}
		return usageList;

	}

	/**
	 * A method that takes a Trait Class parent in the tree, and processes the
	 * list of property nodes from the Trait Class parent in the tree and
	 * extracts the Standard Variables
	 * 
	 * @param svMap
	 * @param stdVars
	 * @param propertyTrees
	 * @param traitClassReference
	 * @return the Property Trees list that collects the new items
	 * @throws MiddlewareQueryException
	 * 
	 */
	private List<OntologyUsage> processTreeProperties(Map<Integer, StandardVariableSummary> svMap,
			List<StandardVariableReference> stdVars, List<OntologyUsage> usageList,
			TraitClassReference traitClassReference) throws MiddlewareQueryException {
		for (PropertyReference property : traitClassReference.getProperties()) {
			if (!property.getStandardVariables().isEmpty()) {
				LOG.info("Processing Property : " + property.getName());
				for (StandardVariableReference svRef : property.getStandardVariables()) {
					if (stdVars.contains(svRef)) {
						StandardVariableSummary sv = svMap.get(svRef.getId());
						long projectCount = ontologyService.countProjectsByVariable(svRef.getId());
						long experimentCount = ontologyService.countExperimentsByVariable(
								sv.getId(), sv.getStoredIn().getId());
						// if std variable is in the limited set, then add to
						// the result
						OntologyUsage ou = new OntologyUsage();
						ou.setStandardVariable(sv);
						ou.setProjectCount(Long.valueOf(projectCount));
						ou.setExperimentCount(Long.valueOf(experimentCount));
						usageList.add(ou);
					} else
						LOG.info("Missed : " + svRef.getId() + ":" + svRef.getName() + ":"
								+ svRef.getDescription());
				}
			}
		}
		return usageList;
	}

}
