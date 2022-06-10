package org.ibp.api.java.impl.middleware.program.validator;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.program.ProgramFavoriteRequestDto;
import org.generationcp.middleware.api.program.ProgramFavoriteService;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.ProgramLocationDefault;
import org.generationcp.middleware.pojos.dms.ProgramFavorite;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProgramFavoriteValidator {
	@Autowired
	private LocationDataManager locationDataManager;

	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private ProgramFavoriteService programFavoriteService;

	@Autowired
	private LocationService locationService;

	public void validateAddFavorites(final String programUUID, final ProgramFavoriteRequestDto programFavoriteRequestDtos) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), ProgramFavoriteRequestDto.class.getName());

		if (CollectionUtils.isEmpty(programFavoriteRequestDtos.getEntityIds())) {
			errors.reject("program.favorite.entity.list.id.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final Set<Integer> entityIds = new HashSet<>(programFavoriteRequestDtos.getEntityIds());
		final ProgramFavorite.FavoriteType favoriteType = programFavoriteRequestDtos.getFavoriteType();
		final List<ProgramFavorite> favorites = this.programFavoriteService.getProgramFavorites(programUUID, favoriteType, entityIds);

		if (!favorites.isEmpty()) {
			final List<Integer> favoritesIds = favorites.stream().map(ProgramFavorite::getEntityId).collect(Collectors.toList());
			errors.reject("program.favorite.already.exists", new String[] {
				String.join(",", Arrays.toString(favoritesIds.toArray())), favoriteType.name()}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());

		}

		switch (favoriteType) {
			case LOCATION:
				this.validateLocationId(errors, entityIds);
				break;
			case METHODS:
				this.validateMethodId(errors, entityIds);
				break;
			case VARIABLES:
				this.validateVariableId(errors, programUUID, entityIds);
				break;
			default:
				errors.reject("program.favorite.type.not.identified", new String[] {favoriteType.name()}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

	public void validateDeleteFavorites(final String programUUID, final Set<Integer> entityIds) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		if (CollectionUtils.isEmpty(entityIds)) {
			errors.reject("program.favorite.entity.list.id.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final List<ProgramFavorite> favoriteLocations =
			this.programFavoriteService.getProgramFavorites(programUUID, ProgramFavorite.FavoriteType.LOCATION, null);
		final List<Integer> programFavoriteLocationIds =
			favoriteLocations.stream().filter(pf ->entityIds.contains(pf.getProgramFavoriteId()))
				.map(ProgramFavorite::getEntityId).collect(Collectors.toList());

		if(CollectionUtils.isNotEmpty(programFavoriteLocationIds)) {
			final ProgramLocationDefault programLocationDefault = this.locationService.getProgramLocationDefault(programUUID);
			final List<Integer> locationDefaults =
				Arrays.asList(programLocationDefault.getBreedingLocationId(), programLocationDefault.getStorageLocationId());
			final List<Integer> invalidLocationIds = locationDefaults.stream()
				.filter(programFavoriteLocationIds::contains).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(invalidLocationIds)) {
				errors.reject("program.favorite.location.used.as.default", new String[] {
					String.join(",", Arrays.toString(invalidLocationIds.toArray()))}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
	}


	private void validateVariableId(final BindingResult errors, final String programUUID, final Set<Integer> variableIds) {
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.setProgramUuid(programUUID);
		variableFilter.getVariableIds().addAll(variableIds);
		final List<Variable> variables = this.ontologyVariableDataManager.getWithFilter(variableFilter);

		final List<Integer> variableFiltered = variables.stream().map(Variable::getId).collect(Collectors.toList());

		variableIds.removeAll(variableFiltered);
		if(!variableIds.isEmpty()){
			errors.reject("program.favorite.variable.not.identified", new String[] {String.join(",", Arrays.toString(variableIds.toArray()))}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

	private void validateMethodId(final BindingResult errors, final Set<Integer> methodsIs) {
		final BreedingMethodSearchRequest methodSearchRequest = new BreedingMethodSearchRequest();
		methodSearchRequest.setMethodIds(methodsIs.stream().collect(Collectors.toList()));

		final List<Integer> breedingMethodIds = this.breedingMethodService.searchBreedingMethods(methodSearchRequest, null, null) //
			.stream().map(BreedingMethodDTO::getMid).collect(Collectors.toList());

		methodsIs.removeAll(breedingMethodIds);
		if (!methodsIs.isEmpty()) {
			errors.reject("program.favorite.breeding.methods.not.identified", new String[] {String.join(",", Arrays.toString(methodsIs.toArray()))}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private void validateLocationId(final BindingResult errors, final Set<Integer> locationsIds) {
		final List<Location> locations = this.locationDataManager.getLocationsByIDs(Lists.newArrayList(locationsIds));
		final List<Integer> locIds = locations.stream().map(Location::getLocid).collect(Collectors.toList());

		locationsIds.removeAll(locIds);
		if (!locationsIds.isEmpty()) {
			errors.reject("program.favorite.location.not.identified", new String[] {String.join(",", Arrays.toString(locationsIds.toArray()))}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

}
