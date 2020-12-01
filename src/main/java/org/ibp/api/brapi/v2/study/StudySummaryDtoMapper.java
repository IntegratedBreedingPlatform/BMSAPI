package org.ibp.api.brapi.v2.study;

import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.ibp.api.brapi.v1.study.ContactConverter;
import org.ibp.api.brapi.v1.study.EnvironmentParameterConverter;
import org.ibp.api.brapi.v1.study.ExperimentalDesignConverter;
import org.ibp.api.brapi.v1.study.StudySummaryDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.util.ArrayList;
import java.util.List;

public class StudySummaryDtoMapper {
	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private StudySummaryDtoMapper() {

	}

	static {
		StudySummaryDtoMapper.addStudySummaryDtoMapping(StudySummaryDtoMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return StudySummaryDtoMapper.applicationWideModelMapper;
	}

	private static class DatasetSummaryConverter implements Converter<List<DatasetDTO>, List<DatasetSummary>> {

		@Override
		public List<DatasetSummary> convert(final MappingContext<List<DatasetDTO>, List<DatasetSummary>> context) {
			final List<DatasetSummary> datasetSummaries = new ArrayList<>();
			for (final DatasetDTO dataset : context.getSource()) {
				datasetSummaries.add(new DatasetSummary(dataset.getDatasetId(), dataset.getName(),dataset.getDatasetTypeId()));
			}
			return context.getMappingEngine().map(context.create(datasetSummaries, context.getDestinationType()));
		}

	}

	private static void addStudySummaryDtoMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<StudyDetailsDto, StudySummaryDto>() {

			@Override protected void configure() {
				this.map().setAdditionalInfo(this.source.getAdditionalInfo());
				this.map().setSeasons(this.source.getMetadata().getSeasons());
				this.map().setActive(String.valueOf(this.source.getMetadata().getActive()));
				this.map().setStartDate(this.source.getMetadata().getStartDate());
				this.map().setStudyDbId(this.source.getMetadata().getStudyDbId());
				this.map().setStudyName(this.source.getMetadata().getStudyName());
				this.map().setStudyType(this.source.getMetadata().getStudyType());
				this.map().setTrialName(this.source.getMetadata().getTrialName());
				this.map().setStudyDescription(this.source.getMetadata().getStudyDescription());
				this.map().setLastUpdate(this.source.getMetadata().getLastUpdate());
				this.map().setLocationDbId(String.valueOf(this.source.getMetadata().getLocationId()));
				this.map().setLocationName(String.valueOf(this.source.getMetadata().getLocationName()));
				this.map().setStudyCode(this.source.getMetadata().getStudyCode());
				this.map().setStudyPUI(this.source.getMetadata().getStudyPUI());
				this.map().setTrialDbid(this.source.getMetadata().getNurseryOrTrialId());
				this.using(new ExperimentalDesignConverter()).map(this.source.getMetadata().getExperimentalDesign()).setExperimentalDesign(null);
				this.using(new ContactConverter()).map(this.source.getContacts()).setContacts(null);
				this.using(new EnvironmentParameterConverter()).map(this.source.getEnvironmentParameters()).setEnvironmentParameters(null);
				this.using(new DatasetSummaryConverter()).map(this.source.getDatasets()).setObservationLevels(null);
			}
		});
	}




}
