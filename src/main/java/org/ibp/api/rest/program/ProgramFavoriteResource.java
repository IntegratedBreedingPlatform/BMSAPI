package org.ibp.api.rest.program;

import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.program.ProgramFavoriteDTO;
import org.generationcp.middleware.api.program.ProgramFavoriteRequestDto;
import org.generationcp.middleware.api.program.ProgramFavoriteService;
import org.ibp.api.java.impl.middleware.program.validator.ProgramFavoriteValidator;
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
import java.util.Set;

@RestController
public class ProgramFavoriteResource {

	@Autowired
	private ProgramFavoriteService programFavoriteService;

	@Autowired
	private ProgramFavoriteValidator programFavoriteValidator;

	@ApiOperation(value = "Add Favorites", notes = "Add Favorites")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/favorites", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_PROGRAMS')")
	@ResponseBody
	public ResponseEntity<List<ProgramFavoriteDTO>> addFavorite(@PathVariable final String cropName,
		@PathVariable final String programUUID, @RequestBody final ProgramFavoriteRequestDto programFavoriteRequestDtos) {
		this.programFavoriteValidator.validateAddFavorites(programUUID, programFavoriteRequestDtos);
		final List<ProgramFavoriteDTO> programFavoriteDTOS = this.programFavoriteService
				.addProgramFavorites(programUUID, programFavoriteRequestDtos.getFavoriteType(),
						programFavoriteRequestDtos.getEntityIds());
		return new ResponseEntity<>(programFavoriteDTOS, HttpStatus.OK);
	}

	@ApiOperation(value = "Delete Favorites", notes = "Delete Favorites")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/favorites", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_PROGRAMS')")
	public ResponseEntity<Void> deleteFavorites(@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@RequestParam(value = "programFavoriteIds", required = true) final Set<Integer> programFavoriteIds) {
		this.programFavoriteValidator.validateDeleteFavorites(programUUID, programFavoriteIds);
		this.programFavoriteService.deleteProgramFavorites(programUUID, programFavoriteIds);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
