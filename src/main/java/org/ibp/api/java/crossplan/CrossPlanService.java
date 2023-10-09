package org.ibp.api.java.crossplan;

import org.generationcp.middleware.api.crossplan.CrossPlanSearchRequest;
import org.generationcp.middleware.api.crossplan.CrossPlanSearchResponse;
import org.ibp.api.rest.crossplan.CrossPlanDesignInput;
import org.ibp.api.rest.crossplan.CrossPlanPreview;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CrossPlanService {

    List<CrossPlanPreview> generateDesign(CrossPlanDesignInput crossPlanDesignInput);

    void saveCrossPlan();

    Long countSearchCrossPlans(String programUUID, CrossPlanSearchRequest crossPlanSearchRequest);

    List<CrossPlanSearchResponse> searchCrossPlans(String programUUID, CrossPlanSearchRequest crossPlanSearchRequest, Pageable pageable);
}
