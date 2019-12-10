package org.ibp.api.brapi.v2.observation;

import org.generationcp.middleware.domain.search_request.brapi.v2.ObservationUnitsSearchRequestDto;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchRequestDTO;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

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
		mapper.addMappings(new PropertyMap<PhenotypeSearchRequestDTO, ObservationUnitsSearchRequestDto>() {

			@Override
			protected void configure() {

				this.map().setGermplasmDbIds(this.source.getGermplasmDbIds());
				this.map().setLocationDbIds(this.source.getLocationDbIds());
				this.map().setObservationLevel(this.source.getObservationLevel());
				this.map().setObservationUnitDbIds(this.source.getObservationUnitDbIds());
				this.map().setProgramDbIds(this.source.getProgramDbIds());
				this.map().setStudyDbIds(this.source.getStudyDbIds());
				this.map().setTrialDbIds(this.source.getTrialDbIds());
			}
		});
	}
}
