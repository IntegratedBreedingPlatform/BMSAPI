package org.ibp.api.rest.keysequenceregister;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ibp.api.domain.keysequenceregister.KeySequenceRegisterDeleteResponse;
import org.ibp.api.java.keysequenceregister.KeySequenceRegisterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "Key Sequence Register Services")
@Controller
public class KeySequenceRegisterResource {

	@Resource
	private KeySequenceRegisterService keySequenceRegisterService;

	@ApiOperation(value = "Delete Key Prefix Cache")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'GERMPLASM', 'MANAGE_GERMPLASM')")
	@RequestMapping(value = "/crops/{cropName}/key-sequences", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<KeySequenceRegisterDeleteResponse> deleteGermplasm(@PathVariable final String cropName,
		@RequestParam final List<Integer> gids,
		@RequestParam final List<String> prefixes) {

		return new ResponseEntity<>(this.keySequenceRegisterService.deleteKeySequence(gids, prefixes), HttpStatus.OK);
	}

}
