package org.ibp.api.rest.attribute;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.api.attribute.AttributeService;
import org.generationcp.middleware.api.attribute.GermplasmAttributeDto;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Api(value = "Attribute Services")
@Controller
public class AttributeResource {

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private AttributeService attributeService;

	@Autowired
	private AttributeValidator attributeValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@ApiOperation(value = "Returns germplasm attributes filtered by a list of codes and attibute type",
		notes = "Returns germplasm attributes filtered by a list of codes and attibute type")
	@RequestMapping(value = "/crops/{cropName}/germplasm/attributes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<AttributeDTO>> getGermplasmAttributes(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final Set<String> codes,
		@RequestParam(required = false) final String type) {
		return new ResponseEntity<>(this.germplasmService.filterGermplasmAttributes(codes, type), HttpStatus.OK);
	}

	@ApiOperation(value = "Returns germplasm attributes filtered by gid and attribute type",
		notes = "Returns germplasm attributes by gid and attribute type")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/attributes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmAttributeDto>> getGermplasmAttributeDtos(@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@RequestParam(required = false) final String type) {
		BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		this.attributeValidator.validateAttributeType(errors, type);
		return new ResponseEntity<>(this.attributeService.getGermplasmAttributeDtos(gid, type), HttpStatus.OK);
	}
}
