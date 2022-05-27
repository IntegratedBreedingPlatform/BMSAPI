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
@RequestMapping("/crops")
public class CropGenotypingParameterResource {

	@Autowired
	private CropGenotypingParameterService cropGenotypingParameterService;

	@ApiOperation(value = "Get crop genotyping parameter", notes = "")
	@RequestMapping(value = "/{cropName}/crop-genotyping-parameters", method = RequestMethod.GET)
	public ResponseEntity<CropGenotypingParameterDTO> getCropGenotypingParameter(@PathVariable final String cropName) {
		return new ResponseEntity<>(this.cropGenotypingParameterService.getCropGenotypingParameter(cropName), HttpStatus.OK);
	}

	@ApiOperation(value = "Update crop genotyping parameter", notes = "")
	@RequestMapping(value = "/{cropName}/crop-genotyping-parameters", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT')")
	public ResponseEntity<Void> updateCropTypeDetails(@PathVariable final String cropName,
		@RequestBody final CropGenotypingParameterDTO cropGenotypingParameterDTO) {
		this.cropGenotypingParameterService.updateCropGenotypingParameter(cropName, cropGenotypingParameterDTO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Create crop genotyping parameter", notes = "")
	@RequestMapping(value = "/{cropName}/crop-genotyping-parameters", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT')")
	public ResponseEntity<Void> createCropTypeDetails(@PathVariable final String cropName,
		@RequestBody final CropGenotypingParameterDTO cropGenotypingParameterDTO) {
		this.cropGenotypingParameterService.createCropGenotypingParameter(cropName, cropGenotypingParameterDTO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Generate token", notes = "Get the token using the credentials in Crop Genotype Parameter configuration")
	@RequestMapping(value = "/{cropName}/crop-genotyping-parameters/token", method = RequestMethod.GET)
	public ResponseEntity<String> getToken(@PathVariable final String cropName) {
		return new ResponseEntity<>(this.cropGenotypingParameterService.getToken(cropName), HttpStatus.OK);
	}

}
