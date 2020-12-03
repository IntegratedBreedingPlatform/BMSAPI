package org.ibp.api.brapi.v2.study;

import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.service.api.study.ObservationLevel;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.brapi.v1.study.Contact;
import org.ibp.api.brapi.v1.study.EnvironmentParameterConverter;
import org.ibp.api.brapi.v1.study.ExperimentalDesignConverter;
import org.ibp.api.brapi.v1.study.StudySummaryDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private static class DatasetSummaryConverter implements Converter<List<DatasetDTO>, List<ObservationLevel>> {

		@Override
		public List<ObservationLevel> convert(final MappingContext<List<DatasetDTO>, List<ObservationLevel>> context) {
			final List<ObservationLevel> datasetSummaries = new ArrayList<>();
			for (final DatasetDTO dataset : context.getSource()) {
				datasetSummaries.add(new ObservationLevel(dataset.getDatasetTypeId(), dataset.getName()));
			}
			return context.getMappingEngine().map(context.create(datasetSummaries, context.getDestinationType()));
		}

	}

	public static class ContactConverter implements Converter<List<UserDto>, List<Contact>> {

		@Override
		public List<Contact> convert(final MappingContext<List<UserDto>, List<Contact>> context) {
			final List<Contact> contacts = new ArrayList<>();
			for (final UserDto userDto : context.getSource()) {
				contacts.add(new Contact(userDto.getUserId(), userDto.getEmail(), userDto.getFirstName(),"Creator", ""));
			}
			return context.getMappingEngine().map(context.create(contacts, context.getDestinationType()));
		}

	}

	public static class LastUpdateConverter implements Converter<String, Map<String, String>> {

		@Override
		public Map<String, String> convert(final MappingContext<String, Map<String, String>> context) {
			final Map<String, String> lastUpdate = new HashMap<>();
			lastUpdate.put("timeStamp", context.getSource());
			lastUpdate.put("version", "1.0");
			return context.getMappingEngine().map(context.create(lastUpdate, context.getDestinationType()));
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
				this.using(new LastUpdateConverter()).map(this.source.getMetadata().getLastUpdate()).setLastUpdate(null);
				this.map().setLocationDbId(String.valueOf(this.source.getMetadata().getLocationId()));
				this.map().setLocationName(String.valueOf(this.source.getMetadata().getLocationName()));
				this.map().setStudyCode(this.source.getMetadata().getStudyCode());
				this.map().setStudyPUI(this.source.getMetadata().getStudyPUI());
				this.map().setTrialDbid(this.source.getMetadata().getNurseryOrTrialId());
				this.using(new ExperimentalDesignConverter()).map(this.source.getMetadata()).setExperimentalDesign(null);
				this.using(new ContactConverter()).map(this.source.getContacts()).setContacts(null);
				this.using(new EnvironmentParameterConverter()).map(this.source.getEnvironmentParameters()).setEnvironmentParameters(null);
				this.using(new DatasetSummaryConverter()).map(this.source.getDatasets()).setObservationLevels(null);
			}
		});
	}




}
