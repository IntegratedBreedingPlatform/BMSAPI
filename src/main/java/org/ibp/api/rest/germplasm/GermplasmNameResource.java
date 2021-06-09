package org.ibp.api.rest.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.germplasm.GermplasmCodeNameBatchRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.service.api.GermplasmGroupNamingResult;
import org.ibp.api.java.impl.middleware.germplasm.GermplasmNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "Germplasm name Services")
@Controller
public class GermplasmNameResource {

	@Autowired
	private GermplasmNameService germplasmNameService;

	@ApiOperation(value = "Create name for a specified germplasm")
	@PreAuthorize("hasAnyAuthority('ADMIN','GERMPLASM', 'MANAGE_GERMPLASM', 'EDIT_GERMPLASM', 'MODIFY_NAMES')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/names", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Integer> createGermplasmName(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@PathVariable final Integer gid, @RequestBody final GermplasmNameRequestDto germplasmNameRequestDto) {
		return new ResponseEntity<>(this.germplasmNameService.createName(programUUID, germplasmNameRequestDto, gid), HttpStatus.OK);
	}

	@ApiOperation(value = "Create code name (CODE1, CODE2, CODE2) for specified list of germplasm")
	@PreAuthorize("hasAnyAuthority('ADMIN','GERMPLASM', 'MANAGE_GERMPLASM', 'EDIT_GERMPLASM', 'MODIFY_NAMES')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/coding", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<GermplasmGroupNamingResult>> createGermplasmCodeNames(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto) {
		return new ResponseEntity<>(this.germplasmNameService.createCodeNames(programUUID, germplasmCodeNameBatchRequestDto), HttpStatus.OK);
	}

	@ApiOperation(value = "Get next code name sequence based on the specified name settings")
	@PreAuthorize("hasAnyAuthority('ADMIN','GERMPLASM', 'MANAGE_GERMPLASM', 'EDIT_GERMPLASM', 'MODIFY_NAMES')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/coding/sequence", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> getNextSequence(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmNameSetting germplasmNameSetting) {
		return new ResponseEntity<>(this.germplasmNameService.getNextNameInSequence(germplasmNameSetting), HttpStatus.OK);
	}

	@ApiOperation(value = "Update name for a specified Germplasm")
	@PreAuthorize("hasAnyAuthority('ADMIN','GERMPLASM', 'MANAGE_GERMPLASM', 'EDIT_GERMPLASM', 'MODIFY_NAMES')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/names/{nameId}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Void> updateGermplasmName(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@PathVariable final Integer gid, @PathVariable final Integer nameId,
		@RequestBody final GermplasmNameRequestDto germplasmNameRequestDto) {
		this.germplasmNameService.updateName(programUUID, germplasmNameRequestDto, gid, nameId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Delete name for a specified Germplasm")
	@PreAuthorize("hasAnyAuthority('ADMIN','GERMPLASM', 'MANAGE_GERMPLASM', 'EDIT_GERMPLASM', 'MODIFY_NAMES')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/names/{nameId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteGermplasmName(@PathVariable final String cropName, @RequestParam(required = false) final String programUUID,
		@PathVariable final Integer gid, @PathVariable final Integer nameId) {
		this.germplasmNameService.deleteName(gid, nameId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
