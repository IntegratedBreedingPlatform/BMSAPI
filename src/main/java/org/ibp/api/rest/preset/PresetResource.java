package org.ibp.api.rest.preset;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.java.preset.PresetService;
import org.ibp.api.rest.preset.domain.PresetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Preset Services")
@RestController
public class PresetResource {

	@Autowired
	private PresetService presetService;

	@RequestMapping(value = "/crops/{cropname}/presets", method = RequestMethod.POST)
	@ApiOperation(value = "Create a new Preset",
		notes = "Create a new preset.")
	@ResponseBody
	public ResponseEntity<Void> createPreset(
		@PathVariable
			String cropname,
		@RequestBody
			PresetDTO presetDTO) {
		presetService.savePreset(presetDTO);
		return new ResponseEntity<>( HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropname}/presets", method = RequestMethod.GET)
	@ApiOperation(value = "Get presets",
			notes = "Get presets.")
	@ResponseBody
	@JsonView(PresetDTO.View.Qualified.class)
	public ResponseEntity<List<PresetDTO>> getPresets(
			@PathVariable
			String cropname,
			@RequestParam
			String programUUID,
			@RequestParam
			Integer toolId,
			@RequestParam
			String toolSection) {
		final List<PresetDTO> presetDTOs = presetService.getPresets(programUUID, toolId, toolSection);
		return new ResponseEntity<>(presetDTOs, HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropname}/presets/{presetId}", method = RequestMethod.DELETE)
	@ApiOperation(value = "Delete preset",
		notes = "Delete preset.")
	@ResponseBody
	public ResponseEntity<Void> deletePreset(
		@PathVariable
			String cropname,
		@RequestParam
			Integer presetId) {
		presetService.deletePreset(presetId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
