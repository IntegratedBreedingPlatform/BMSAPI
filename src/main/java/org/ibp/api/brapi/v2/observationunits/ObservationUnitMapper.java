package org.ibp.api.brapi.v2.observationunits;

import org.generationcp.middleware.domain.search_request.brapi.v2.ObservationUnitsSearchRequestDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

@Deprecated
public class ObservationUnitMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	static {
		ObservationUnitMapper.addObservationMapper(ObservationUnitMapper.applicationWideModelMapper);
	}

	private ObservationUnitMapper() {
	}

	public static ModelMapper getInstance() {
		return ObservationUnitMapper.applicationWideModelMapper;
	}

	private static void addObservationMapper(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<ObservationUnitSearchRequestDTO, ObservationUnitsSearchRequestDto>() {

			@Override
			protected void configure() {

				this.map().setGermplasmDbIds(this.source.getGermplasmDbIds());
				this.map().setLocationDbIds(this.source.getLocationDbIds());
				this.map().setObservationLevels(this.source.getObservationLevels());
				this.map().setObservationUnitDbIds(this.source.getObservationUnitDbIds());
				this.map().setProgramDbIds(this.source.getProgramDbIds());
				this.map().setStudyDbIds(this.source.getStudyDbIds());
				this.map().setTrialDbIds(this.source.getTrialDbIds());
				this.map().setExternalReferenceIDs(this.source.getExternalReferenceIDs());
				this.map().setExternalReferenceSources(this.source.getExternalReferenceSources());
			}
		});
	}
}
