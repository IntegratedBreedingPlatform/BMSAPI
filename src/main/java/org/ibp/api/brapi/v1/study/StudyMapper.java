package org.ibp.api.brapi.v1.study;

import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.brapi.v1.location.Location;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.util.ArrayList;
import java.util.List;

public class StudyMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

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
						userDto.getRole(), ""));
			}
			return context.getMappingEngine().map(context.create(contacts, context.getDestinationType()));
		}

	}

	private static class LocationConverter implements Converter<Integer, Location> {

		@Override public Location convert(final MappingContext<Integer, Location> mappingContext) {
			Location location = new Location();
			location.setLocationDbId(mappingContext.getSource());
			return mappingContext.getMappingEngine().map(mappingContext.create(location, mappingContext.getDestinationType()));
		}
	}

	private static void addStudyDetailsDataMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<StudyDetailsDto, StudyDetailsData>() {

			@Override protected void configure() {
				this.map().setAdditionalInfo(this.source.getAdditionalInfo());
				this.map().setSeasons(this.source.getMetadata().getSeasons());
				this.map().setActive(this.source.getMetadata().getActive());
				this.map().setEndDate(this.source.getMetadata().getEndDate());
				this.map().setStartDate(this.source.getMetadata().getStartDate());
				this.map().setStudyDbId(this.source.getMetadata().getStudyDbId());
				this.map().setStudyName(this.source.getMetadata().getStudyName());
				this.map().setStudyType(this.source.getMetadata().getStudyType());
				this.map().setTrialName(this.source.getMetadata().getTrialName());
				this.map().setTrialDbId(this.source.getMetadata().getTrialDbId());
				this.using(new ContactConverter()).map(this.source.getContacts()).setContacts(null);
				this.using(new LocationConverter()).map(this.source.getMetadata().getLocationId()).setLocation(null);

			}
		});
	}

}
