package org.ibp.api.brapi.v2.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

@Api(value = "BrAPI Germplasm Services")
@Controller
public class BreedingMethodResourceBrapi {

	public ResponseEntity<EntityListResponse<BreedingMethod>> getAllBreedingMethods(@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION)
	@RequestParam(value = "page", required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION)
		@RequestParam(value = "pageSize", required = false) final Integer pageSize) {

	}
}
