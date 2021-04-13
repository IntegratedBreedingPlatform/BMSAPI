package org.ibp.api.rest.preset;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ibp.api.java.preset.PresetService;
import org.ibp.api.rest.preset.domain.PresetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "Preset Services")
@PreAuthorize("hasAnyAuthority("
	+ "'ADMIN',"
	+ "'STUDIES',"
	+ "'MANAGE_STUDIES',"
	+ "'CROP_MANAGEMENT',"
	+ "'MANAGE_INVENTORY',"
	+ "'MANAGE_LOTS',"
	+ "'LOT_LABEL_PRINTING'"
	+ ")")
@RestController
public class PresetResource {

	@Autowired
	private PresetService presetService;

	@RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/presets", method = RequestMethod.PUT)
	@ApiOperation(value = "Create a new Preset",
		notes = "Create a new preset.")
	@ResponseBody
	public ResponseEntity<PresetDTO> createPreset(
		@PathVariable final String cropname, @PathVariable final String programUUID,
		@RequestBody
			PresetDTO presetDTO) {
		presetDTO = this.presetService.savePreset(cropname, presetDTO);
		return new ResponseEntity<>(presetDTO, HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/presets/{presetId}", method = RequestMethod.PUT)
	@ApiOperation(value = "update a existing Preset",
		notes = "update a existing Preset.")
	@ResponseBody
	public ResponseEntity<Void> updatePreset(
		@PathVariable final String cropname, @PathVariable final String programUUID, @PathVariable final Integer presetId,
		@RequestBody final PresetDTO presetDTO) {
		this.presetService.updatePreset(cropname, presetId, presetDTO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/presets", method = RequestMethod.GET)
	@ApiOperation(value = "Get presets",
		notes = "Get presets.")
	@ResponseBody
	@JsonView(PresetDTO.View.Qualified.class)
	public ResponseEntity<List<PresetDTO>> getPresets(
		@PathVariable
			final String cropname,
		@PathVariable
			final String programUUID,
		@RequestParam final
		Integer toolId,
		@RequestParam final
		String toolSection) {
		final List<PresetDTO> presetDTOs = this.presetService.getPresets(programUUID, toolId, toolSection);
		return new ResponseEntity<>(presetDTOs, HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/presets/{presetId}", method = RequestMethod.DELETE)
	@ApiOperation(value = "Delete preset",
		notes = "Delete preset.")
	@ResponseBody
	public ResponseEntity<Void> deletePreset(
		@PathVariable
			final String cropname,
		@PathVariable
			final String programUUID,
		@PathVariable final
		Integer presetId) {
		this.presetService.deletePreset(cropname, presetId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
