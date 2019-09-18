package org.ibp.api.rest.inventory_new;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.inventory_new.LotDto;
import org.generationcp.middleware.domain.inventory_new.LotsSearchDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Lot Resource")
@RestController
public class LotResource {

	@ApiOperation(value = "It will retrieve all the observation units", notes = "It will retrieve all the observation units including observations and props values in a format that will be used by the Observations table.")
	@RequestMapping(value = "/crops/{cropname}/lots/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<LotDto> getLots(@PathVariable final String cropname, //
		@RequestBody final LotsSearchDto searchDTO) {
		return null;
	}

}
