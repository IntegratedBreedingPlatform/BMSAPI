package org.ibp.api.brapi.v2.observationunits;

import org.generationcp.middleware.domain.search_request.brapi.v1.PhenotypeSearchRequestDTO;
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
		mapper.addMappings(new PropertyMap<PhenotypeSearchRequestDTO, ObservationUnitSearchRequestDTO>() {

			@Override
			protected void configure() {

				this.map().setGermplasmDbIds(this.source.getGermplasmDbIds());
				this.map().setLocationDbIds(this.source.getLocationDbIds());
				this.map().setObservationLevel(this.source.getObservationLevel());
				this.map().setObservationTimeStampRangeStart(this.source.getObservationTimeStampRangeStart());
				this.map().setObservationTimeStampRangeEnd(this.source.getObservationTimeStampRangeEnd());
				this.map().setObservationVariableDbIds(this.source.getObservationVariableDbIds());
				this.map().setProgramDbIds(this.source.getProgramDbIds());
				this.map().setStudyDbIds(this.source.getStudyDbIds());
				this.map().setTrialDbIds(this.source.getTrialDbIds());
				this.map().setSeasonDbIds(this.source.getSeasonDbIds());
				this.map().setIncludeObservations(true); //default to true on v1
			}
		});
	}
}
