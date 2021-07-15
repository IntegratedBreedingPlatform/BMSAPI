package org.ibp.api.domain.sample;

import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class SampleObservationMapper {

    private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

    static {
        SampleObservationMapper.addSampleObservationMapper(SampleObservationMapper.applicationWideModelMapper);
    }

    private SampleObservationMapper() {
    }

    public static ModelMapper getInstance() {
        return SampleObservationMapper.applicationWideModelMapper;
    }

    private static void addSampleObservationMapper(final ModelMapper mapper) {
        mapper.addMappings(new PropertyMap<SampleDetailsDTO, SampleObservationDto>() {

            @Override
            protected void configure() {
                this.map().setStudyDbId(this.source.getStudyDbId().toString());
                this.map().setObservationUnitDbId(this.source.getObsUnitId());
                this.map().setSampleDbId(this.source.getSampleBusinessKey());
                this.map().setTakenBy(this.source.getTakenBy());
                this.map().setSampleType(this.source.getSampleType());
                this.map().setTissueType(this.source.getTissueType());
                this.map().setNotes(this.source.getNotes());
                this.map().setGermplasmDbId(this.source.getGermplasmUUID());
                this.map().setSampleTimestamp(this.source.getSampleDate());
                this.map().setPlateDbId(this.source.getPlateId());
                this.map().setPlateIndex(this.source.getSampleNumber());
                this.map().setPlotDbId(this.source.getPlotNo().toString());
            }
        });
    }
}

