package org.ibp.api.rest.crossplan;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ibp.api.java.crossplan.CrossPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "Cross Plan Design Services")
@RestController
public class CrossPlanDesignResource {

    @Autowired
    private CrossPlanService crossPlanService;

    @ApiOperation(value = "Generate a cross plan design", notes = "Generate a cross plan design")
    @RequestMapping(value = "/crops/{cropName}/crossPlan/design/generation", method = RequestMethod.POST)
    @ResponseBody
    public List<CrossPlanPreview> crossPlanDesign(
            @PathVariable final String cropName,
            @RequestBody final CrossPlanDesignInput crossPlanDesignInput){
        return crossPlanService.generateDesign(crossPlanDesignInput);
    }
}
