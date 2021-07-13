package org.ibp.api.java.impl.middleware.observationunits;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationLevelRelationship;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportRequestDto;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitPosition;
import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ObservationUnitImportRequestValidator {

	private static final int MAX_REFERENCE_ID_LENGTH = 2000;
	private static final int MAX_REFERENCE_SOURCE_LENGTH = 255;
	public static final String PLOT = "PLOT";
	private static final List<String> OBSERVATION_LEVEL_NAMES = Arrays.asList("PLOT", "BLOCK", "REP");

	@Autowired
	private StudyInstanceService studyInstanceService;

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private OntologyService ontologyService;

	protected BindingResult errors;

	public BindingResult pruneObservationUnitsInvalidForImport(
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos) {
		BaseValidator.checkNotEmpty(observationUnitImportRequestDtos, "observation.unit.import.request.null");
		this.errors = new MapBindingResult(new HashMap<>(), StudyImportRequestDTO.class.getName());

		final List<String> studyDbIds = observationUnitImportRequestDtos.stream().filter(obs -> StringUtils.isNotEmpty(obs.getStudyDbId()))
			.map(ObservationUnitImportRequestDto::getStudyDbId).collect(Collectors.toList());

		final List<String> germplasmDbIds =
			observationUnitImportRequestDtos.stream().filter(obs -> StringUtils.isNotEmpty(obs.getGermplasmDbId()))
				.map(ObservationUnitImportRequestDto::getGermplasmDbId).collect(Collectors.toList());
		final GermplasmSearchRequestDto germplasmSearchRequestDto = new GermplasmSearchRequestDto();
		germplasmSearchRequestDto.setGermplasmDbIds(germplasmDbIds);
		final Map<String, GermplasmDTO> germplasmDTOMap = this.germplasmService.searchFilteredGermplasm(germplasmSearchRequestDto, null)
			.stream().collect(Collectors.toMap(GermplasmDTO::getGermplasmDbId, Function.identity()));

		final StudySearchFilter studySearchFilter = new StudySearchFilter();
		studySearchFilter.setStudyDbIds(studyDbIds);
		final Map<String, StudyInstanceDto> studyInstancesMap =
			this.studyInstanceService.getStudyInstances(studySearchFilter, null).stream()
				.collect(Collectors.toMap(StudyInstanceDto::getStudyDbId, Function.identity()));

		final Integer index = 1;
		final Iterator<ObservationUnitImportRequestDto> iterator = observationUnitImportRequestDtos.iterator();

		final List<String> entryTypes =
			this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), null).getEnumerations()
				.stream().map(e -> e.getDescription().toUpperCase()).collect(Collectors.toList());

		final Map<String, List<String>> entryTypesMap = new HashMap<>();

		while (iterator.hasNext()) {
			final ObservationUnitImportRequestDto dto = iterator.next();
			if (StringUtils.isEmpty(dto.getProgramDbId())) {
				this.errors.reject("observation.unit.import.programDbId.null", new String[] {index.toString()}, "");
				iterator.remove();
				continue;
			}

			if (StringUtils.isEmpty(dto.getTrialDbId())) {
				this.errors.reject("observation.unit.import.trialDbId.null", new String[] {index.toString()}, "");
				iterator.remove();
				continue;
			}

			if (StringUtils.isEmpty(dto.getStudyDbId())) {
				this.errors.reject("observation.unit.import.studyDbId.null", new String[] {index.toString()}, "");
				iterator.remove();
				continue;
			}

			if (!studyInstancesMap.containsKey(dto.getStudyDbId())
				|| !studyInstancesMap.get(dto.getStudyDbId()).getProgramDbId().equalsIgnoreCase(dto.getProgramDbId())
				|| !studyInstancesMap.get(dto.getStudyDbId()).getTrialDbId().equals(dto.getTrialDbId())) {
				this.errors.reject("observation.unit.import.no.study", new String[] {index.toString()}, "");
				iterator.remove();
				continue;
			}

			if (StringUtils.isEmpty(dto.getGermplasmDbId())) {
				this.errors.reject("observation.unit.import.germplasmDbId.null", new String[] {index.toString()}, "");
				iterator.remove();
				continue;
			}

			if (!germplasmDTOMap.containsKey(dto.getGermplasmDbId())) {
				this.errors.reject("observation.unit.import.germplasmDbId.invalid", new String[] {index.toString()}, "");
				iterator.remove();
				continue;
			}

			final ObservationUnitPosition position = dto.getObservationUnitPosition();

			if (position == null || StringUtils.isEmpty(position.getEntryType())) {
				this.errors.reject("observation.unit.import.entry.type.required", new String[] {index.toString()}, "");
				iterator.remove();
				continue;
			}

			if (!entryTypes.contains(position.getEntryType().toUpperCase())) {
				if(!entryTypesMap.containsKey(dto.getProgramDbId())) {
					entryTypesMap.put(dto.getProgramDbId(),
						this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), dto.getProgramDbId()).getEnumerations()
						.stream().map(e -> e.getDescription().toUpperCase()).collect(Collectors.toList()));
				}
				if(!entryTypesMap.get(dto.getProgramDbId()).contains(position.getEntryType().toUpperCase())) {
					this.errors.reject("observation.unit.import.entry.type.invalid", new String[] {index.toString()}, "");
					iterator.remove();
					continue;
				}
			}

			if(!isObservationLevelRelationshipNamesValid(position.getObservationLevelRelationships(), index)) {
				iterator.remove();
				continue;
			}

			if ((StringUtils.isEmpty(position.getPositionCoordinateX()) && StringUtils.isNotEmpty(position.getPositionCoordinateY()))
				|| (StringUtils.isEmpty(position.getPositionCoordinateY()) && StringUtils.isNotEmpty(position.getPositionCoordinateX()))) {
				this.errors.reject("observation.unit.import.position.invalid", new String[] {index.toString()}, "");
				iterator.remove();
				continue;
			}

			if (this.isAnyExternalReferenceInvalid(dto, index)) {
				iterator.remove();
			}
		}

		return this.errors;
	}

	private boolean isObservationLevelRelationshipNamesValid(final List<ObservationLevelRelationship> observationLevelRelationships, final Integer index) {
		boolean hasPlot = false;
		if(!CollectionUtils.isEmpty(observationLevelRelationships)) {
			for(final ObservationLevelRelationship relationship: observationLevelRelationships) {
				if(relationship.getLevelName().equalsIgnoreCase(PLOT)) {
					hasPlot = true;
				}
				if(!OBSERVATION_LEVEL_NAMES.contains(relationship.getLevelName().toUpperCase())) {
					this.errors.reject("observation.unit.import.invalid.observation.level.name", new String[] {index.toString()}, "");
					return false;
				}
			}
			if(hasPlot) {
				return true;
			}
		}
		this.errors.reject("observation.unit.import.no.plot", new String[] {index.toString()}, "");
		return false;
	}

	private boolean isAnyExternalReferenceInvalid(final ObservationUnitImportRequestDto dto, final Integer index) {
		if (dto.getExternalReferences() != null) {
			return dto.getExternalReferences().stream().anyMatch(r -> {
				if (r == null || StringUtils.isEmpty(r.getReferenceID()) || StringUtils.isEmpty(r.getReferenceSource())) {
					this.errors.reject("observation.unit.import.reference.null", new String[] {index.toString(), "externalReference"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceID()) && r.getReferenceID().length() > MAX_REFERENCE_ID_LENGTH) {
					this.errors
						.reject("observation.unit.import.reference.id.exceeded.length", new String[] {index.toString(), "referenceID"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceSource()) && r.getReferenceSource().length() > MAX_REFERENCE_SOURCE_LENGTH) {
					this.errors.reject("observation.unit.import.reference.source.exceeded.length",
						new String[] {index.toString(), "referenceSource"}, "");
					return true;
				}
				return false;
			});
		}
		return false;
	}

}
