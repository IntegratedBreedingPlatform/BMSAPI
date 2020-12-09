package org.ibp.api.brapi.v1.study;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.service.api.study.EnvironmentParameter;
import org.generationcp.middleware.service.api.study.ExperimentalDesign;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.util.ArrayList;
import java.util.List;

public class StudyMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private StudyMapper() {
	}

	static {
		StudyMapper.addStudyDetailsDataMapping(StudyMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return StudyMapper.applicationWideModelMapper;
	}

	private static class ContactConverter implements Converter<List<UserDto>, List<Contact>> {

		@Override public List<Contact> convert(final MappingContext<List<UserDto>, List<Contact>> context) {
			final List<Contact> contacts = new ArrayList<>();
			for (final UserDto userDto : context.getSource()) {
				contacts.add(new Contact(userDto.getUserId(), userDto.getEmail(), userDto.getFirstName() + " " + userDto.getLastName(),
					"", ""));
			}
			return context.getMappingEngine().map(context.create(contacts, context.getDestinationType()));
		}

	}

	private static class EnvironmentParameterConverter implements Converter<List<MeasurementVariable>, List<EnvironmentParameter>> {

		@Override public List<EnvironmentParameter> convert(final MappingContext<List<MeasurementVariable>, List<EnvironmentParameter>> context) {
			final List<EnvironmentParameter> environmentParameters = new ArrayList<>();
			for (final MeasurementVariable variable : context.getSource()) {
				final EnvironmentParameter environmentParameter = new EnvironmentParameter();
				environmentParameter.setDescription(variable.getDescription());
				environmentParameter.setValue(variable.getValue());
				environmentParameter.setUnit(variable.getScale());
				environmentParameter.setParameterName(variable.getName());
				environmentParameters.add(environmentParameter);
			}
			return context.getMappingEngine().map(context.create(environmentParameters, context.getDestinationType()));
		}

	}

	private static class ExperimentalDesignConverted implements Converter<String, ExperimentalDesign> {

		@Override public ExperimentalDesign convert(final MappingContext<String, ExperimentalDesign> context) {
			final ExperimentalDesign experimentalDesign = new ExperimentalDesign();
			experimentalDesign.setDescription(context.getSource());
			return context.getMappingEngine().map(context.create(experimentalDesign, context.getDestinationType()));
		}

	}

	private static void addStudyDetailsDataMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<StudyDetailsDto, StudyDetailsData>() {

			@Override protected void configure() {
				this.map().setAdditionalInfo(this.source.getAdditionalInfo());
				this.map().setSeasons(this.source.getMetadata().getSeasons());
				this.map().setActive(String.valueOf(this.source.getMetadata().getActive()));
				this.map().setEndDate(this.source.getMetadata().getEndDate());
				this.map().setStartDate(this.source.getMetadata().getStartDate());
				this.map().setStudyDbId(String.valueOf(this.source.getMetadata().getStudyDbId()));
				this.map().setStudyName(this.source.getMetadata().getStudyName());
				this.map().setStudyType(this.source.getMetadata().getStudyType());
				this.map().setStudyTypeName(this.source.getMetadata().getStudyTypeName());
				this.map().setStudyTypeDbId(this.source.getMetadata().getStudyType());
				this.map().setTrialName(this.source.getMetadata().getTrialName());
				this.map().setTrialDbId(String.valueOf(this.source.getMetadata().getTrialDbId()));
				this.map().setStudyDescription(this.source.getMetadata().getStudyDescription());
				this.map().setLastUpdate(this.source.getMetadata().getLastUpdate());
				this.using(new ExperimentalDesignConverted()).map(this.source.getMetadata().getExperimentalDesign()).setExperimentalDesign(null);
				this.using(new ContactConverter()).map(this.source.getContacts()).setContacts(null);
				this.using(new EnvironmentParameterConverter()).map(this.source.getEnvironmentParameters()).setEnvironmentParameters(null);
			}
		});
	}

}
