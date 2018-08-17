package org.ibp.api.brapi.v1.germplasm;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(value = "BrAPI Germplasm Services")
@Controller
public class GermplasmResourceBrapi {

	@ApiOperation(value = "Search germplasms", notes = "Search germplasms")
	@RequestMapping(value = "/{crop}/brapi/v1/germplasm-search", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Germplasms> searchGermplasms(
			@PathVariable
			final String crop,
			@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
			@RequestParam(value = "page",
					required = false)
			final Integer currentPage,
			@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
			@RequestParam(value = "pageSize",
					required = false)
			final Integer pageSize,
			@ApiParam(value = "Permanent unique identifier", required = false)
			@RequestParam(value = "germplasmPUI",
					required = false)
			final String germplasmPUI,
			@ApiParam(value = "Internal database identifier", required = false)
			@RequestParam(value = "germplasmDbId",
					required = false)
			final String germplasmDbId,
			@ApiParam(value = "Name of the germplasm", required = false)
			@RequestParam(value = "germplasmName",
					required = false)
			final String germplasmName,
			@ApiParam(value = "The common crop name. This value is discarded, crop needs to be included as part of the URL", required = false)
			@RequestParam(value = "commonCropName",
					required = false)
			final String commonCropName) {
		return null;
	}
}
