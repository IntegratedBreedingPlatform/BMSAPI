package org.ibp.api.java.impl.middleware.crossplan;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.CollectionTransformationUtil;
import org.generationcp.middleware.api.crossplan.CrossPlanSearchRequest;
import org.generationcp.middleware.api.crossplan.CrossPlanSearchResponse;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.java.crossplan.CrossPlanService;
import org.ibp.api.rest.crossplan.CrossPlanDesignInput;
import org.ibp.api.rest.crossplan.CrossPlanPreview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CrossPlanServiceImpl implements CrossPlanService {

    private static final String OPENING_SQUARE_BRACKET = "[";
    private static final String CLOSING_SQUARE_BRACKET = "]";
    private static final String SEPARATOR = ", ";

    @Autowired
    private GermplasmDataManager germplasmDataManager;

    @Autowired
    private PedigreeService pedigreeService;

    @Autowired

    private org.generationcp.middleware.service.api.crossplan.CrossPlanService crossPlanServiceMiddleware;
    @Resource
    private CrossExpansionProperties crossExpansionProperties;

    @Override
    public List<CrossPlanPreview> generateDesign(final CrossPlanDesignInput crossPlanDesignInput) {
        List<CrossPlanPreview> crossPreviewList = null;

        switch (crossPlanDesignInput.getCrossingMethod()) {

            case CROSS_EACH_SELECTED_FEMALE_WITH_EACH_SELECTED_MALE:
                break;

            case CROSS_MATCHED_PAIRS_OF_SELECTED_FEMALE_AND_MALE_LINES_IN_TOP_TO_BOTTOM_ORDER:
                break;

            case CROSS_EACH_FEMALE_WITH_AN_UNKNOWN_MALE_PARENT:
                crossPreviewList = this.makeCrossesWithUnknownMaleParent(crossPlanDesignInput);
                break;

            case CROSS_EACH_FEMALE_WITH_ALL_MALE_PARENTS:
                break;

            default: break;
        }
        return crossPreviewList;
    }

    private List<CrossPlanPreview> makeCrossesWithUnknownMaleParent(CrossPlanDesignInput crossPlanDesignInput){
        final ImmutableMap<Integer, Germplasm> germplasmWithPreferredName = this.getGermplasmWithPreferredNameForBothParents(crossPlanDesignInput.getFemaleList(), new ArrayList());
        final Map<Integer, String> parentsPedigreeString = pedigreeService.getCrossExpansions(germplasmWithPreferredName.keySet(), null, crossExpansionProperties);
        final List<CrossPlanPreview> crossPreviewList = new ArrayList<>();

        crossPlanDesignInput.getFemaleList().forEach((femaleGid) -> {
                this.makeCrossPreview(crossPreviewList, crossPlanDesignInput.isExcludeSelfs(), femaleGid, 0, germplasmWithPreferredName, parentsPedigreeString);
        });

        return crossPreviewList;
    }

    private void makeCrossPreview(final List<CrossPlanPreview> crossPreviewList, final boolean excludeSelf, final Integer femaleGid, final Integer maleGid, final Map<Integer, Germplasm> preferredNamesMap,
                                  final Map<Integer, String> parentPedigreeStringMap){
        // 1-Validate if Already exist the cross.
        final List<Integer> maleGids = Collections.singletonList(maleGid);
        if(crossValidation(femaleGid, maleGid, crossPreviewList, excludeSelf)){
            final String femalePreferredName = getGermplasmPreferredName(preferredNamesMap.get(femaleGid));
            final String femaleParentPedigreeString = parentPedigreeStringMap.get(femaleGid);

            final boolean hasUnknownMaleParent = maleGid.equals(0);
            final String maleParentPedigreeString = hasUnknownMaleParent? Name.UNKNOWN : this.generateMalePedigreeString(maleGids, parentPedigreeStringMap);

            CrossPlanPreview crossPlanPreview = new CrossPlanPreview();
            crossPlanPreview.setFemaleParent(femalePreferredName);
            crossPlanPreview.setMaleParent(getMaleParent(maleGids, preferredNamesMap, hasUnknownMaleParent));
            crossPlanPreview.setFemaleCross(femaleParentPedigreeString);
            crossPlanPreview.setMaleCross(maleParentPedigreeString);
            crossPlanPreview.setGermplasmOrigin("");

            // 3-Add Cross to Preview
            crossPreviewList.add(crossPlanPreview);
        }

    }

    private String getMaleParent(List<Integer> maleGids, Map<Integer, Germplasm> preferredNamesMap, boolean hasUnknownMaleParent) {
        final List<String> maleParentNames = new ArrayList<>();
        final String maleParentName;

        if (hasUnknownMaleParent) {
            maleParentName = Name.UNKNOWN;
        } else {
            maleGids.forEach((maleParentGid) -> {
                final String maleParentPreferredName = this.getGermplasmPreferredName(preferredNamesMap.get(maleParentGid));
                maleParentNames.add(maleParentPreferredName);
            });

            if (maleParentNames.size() > 1) {
                maleParentName = OPENING_SQUARE_BRACKET + String.join(SEPARATOR, maleParentNames) + CLOSING_SQUARE_BRACKET;
            } else {
                maleParentName = maleParentNames.get(0);
            }
        }
        return maleParentName;
    }

    private boolean crossValidation (final Integer femaleGid, final Integer maleGid, final List<CrossPlanPreview> crossPreviewList, final boolean excludeSelf) {
        return crossPreviewList.isEmpty() || crossPreviewList.stream().noneMatch((crossPlanPreview) -> crossPlanPreview.getFemaleParent().equals(femaleGid.toString())) &&
                (!excludeSelf || !this.hasSameParent(femaleGid, maleGid));
    }

    private String getGermplasmPreferredName(final Germplasm germplasm) {
        if(germplasm != null && germplasm.getPreferredName() != null && StringUtils.isNotBlank(germplasm.getPreferredName().getNval())) {
            return germplasm.getPreferredName().getNval();
        }
        return "Unknown";
    }

    boolean hasSameParent(final Integer femaleParent, final Integer maleParent) {
        return femaleParent.equals(maleParent);
    }

    private String generateSeedSource(){ //Germplasm Origin
        // The calculation is made using the workbook if it isn't defined return a double quote.
        return "";
    }
    private String generateMalePedigreeString(final List<Integer> maleParents, final Map<Integer,String> parentPedigreeStringMap){
        final List<String> maleParentsPedigree = maleParents.stream().map(parentPedigreeStringMap::get).collect(Collectors.toList());

        if (maleParents.size() > 1) {
            return OPENING_SQUARE_BRACKET + StringUtils.join(maleParentsPedigree, SEPARATOR) + CLOSING_SQUARE_BRACKET;
        }
        return maleParentsPedigree.get(0);

    }
    ImmutableMap<Integer, Germplasm> getGermplasmWithPreferredNameForBothParents(final List<Integer> femaleParents, final List<Integer> maleParents) {
        final Set<Integer> germplasmListEntries = getAllGidsFromParents(femaleParents, maleParents);
        final List<Germplasm> germplasmWithAllNamesAndAncestry = germplasmDataManager.getGermplasmWithAllNamesAndAncestry(germplasmListEntries, 0);
        return CollectionTransformationUtil.getGermplasmMap(germplasmWithAllNamesAndAncestry);
    }

    private Set<Integer> getAllGidsFromParents(final List<Integer> femaleParents, final List<Integer> maleParents) {
        return new ImmutableSet.Builder<Integer>().addAll(femaleParents).addAll(maleParents).build();
    }

    @Override
    public void saveCrossPlan() {

    }

    @Override
    public Long countSearchCrossPlans(String programUUID, CrossPlanSearchRequest crossPlanSearchRequest) {
        return this.crossPlanServiceMiddleware.countSearchCrossPlans(programUUID,crossPlanSearchRequest);
    }

    @Override
    public List<CrossPlanSearchResponse> searchCrossPlans(String programUUID, CrossPlanSearchRequest crossPlanSearchRequest, Pageable pageable) {
        return this.crossPlanServiceMiddleware.searchCrossPlans(programUUID, crossPlanSearchRequest, pageable);
    }
}
