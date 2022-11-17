package org.ibp.api.rest.crop;

import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterDTO;
import org.ibp.api.java.crop.CropGenotypingParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crops/{cropName}")
public class CropGenotypingParameterResource {

	@Autowired
	private CropGenotypingParameterService cropGenotypingParameterService;

	private static final String GENOTYPING_SERVER = "gigwa";

	@ApiOperation(value = "Get crop genotyping parameter", notes = "")
	@RequestMapping(value = "/crop-genotyping-parameters", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<CropGenotypingParameterDTO> getCropGenotypingParameter(@PathVariable final String cropName) {
		CropGenotypingParameterDTO parameter = this.cropGenotypingParameterService.getCropGenotypingParameter(GENOTYPING_SERVER);
		return new ResponseEntity<>(parameter, HttpStatus.OK);
	}

	@ApiOperation(value = "Generate token", notes = "Get the token using the credentials in Crop Genotype Parameter configuration")
	@RequestMapping(value = "/crop-genotyping-parameters/token", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<String> getToken(@PathVariable final String cropName) {
		return new ResponseEntity<>(this.cropGenotypingParameterService.getToken(GENOTYPING_SERVER), HttpStatus.OK);
	}

}
