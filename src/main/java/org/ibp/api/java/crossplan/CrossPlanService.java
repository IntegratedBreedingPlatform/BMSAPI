package org.ibp.api.java.crossplan;

import org.ibp.api.rest.crossplan.CrossPlanDesignInput;
import org.ibp.api.rest.crossplan.CrossPlanPreview;

import java.util.List;

public interface CrossPlanService {

    List<CrossPlanPreview> generateDesign(CrossPlanDesignInput crossPlanDesignInput);

    void saveCrossPlan();
}
