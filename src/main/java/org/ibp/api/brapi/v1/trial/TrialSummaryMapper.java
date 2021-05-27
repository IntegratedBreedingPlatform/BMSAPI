package org.ibp.api.brapi.v1.trial;

import org.generationcp.middleware.dao.dms.InstanceMetadata;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.service.api.user.ContactDto;
import org.ibp.api.brapi.v1.study.Contact;
import org.ibp.api.brapi.v1.study.StudySummaryDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.util.ArrayList;
import java.util.List;

public class TrialSummaryMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private TrialSummaryMapper() {

	}

	static {
		TrialSummaryMapper.addTrialSummaryMapper(TrialSummaryMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return TrialSummaryMapper.applicationWideModelMapper;
	}

	private static class ContactConverter implements Converter<List<ContactDto>, List<Contact>> {

		@Override
		public List<Contact> convert(final MappingContext<List<ContactDto>, List<Contact>> context) {
			final List<Contact> contacts = new ArrayList<>();
			for (final ContactDto contactDto : context.getSource()) {
				contacts.add(new Contact(contactDto.getContactDbId(), contactDto.getEmail(), contactDto.getName(),
					contactDto.getType(), "", ""));
			}
			return context.getMappingEngine().map(context.create(contacts, context.getDestinationType()));
		}

	}


	private static class StudySummaryConverter implements Converter<List<InstanceMetadata>, List<StudySummaryDto>> {

		@Override
		public List<StudySummaryDto> convert(final MappingContext<List<InstanceMetadata>, List<StudySummaryDto>> context) {

			final List<StudySummaryDto> studySummaries = new ArrayList<>();

			for (final InstanceMetadata instance : context.getSource()) {
				final StudySummaryDto studyMetadata = new StudySummaryDto();
				studyMetadata.setStudyDbId(instance.getInstanceDbId());
				studyMetadata.setStudyName(instance.getTrialName() + " Environment Number " + instance.getInstanceNumber());
				studyMetadata.setLocationName(
					instance.getLocationName() != null ? instance.getLocationName() : instance.getLocationAbbreviation());
				studyMetadata.setLocationDbId(String.valueOf(instance.getLocationDbId()));
				studySummaries.add(studyMetadata);
			}
			return context.getMappingEngine().map(context.create(studySummaries, context.getDestinationType()));

		}

	}

	private static void addTrialSummaryMapper(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<StudySummary, TrialSummary>() {

			@Override
			protected void configure() {
				this.map(this.source.getLocationId(), this.destination.getLocationDbId());
				this.map(this.source.getTrialDbId(), this.destination.getTrialDbId());
				this.map(this.source.getName(), this.destination.getTrialName());
				this.map(this.source.getDescription(), this.destination.getTrialDescription());
				this.map(this.source.getObservationUnitId(), this.destination.getTrialPUI());
				this.map(this.source.getProgramDbId(), this.destination.getProgramDbId());
				this.map(this.source.getProgramName(), this.destination.getProgramName());
				this.map(this.source.getStartDate(), this.destination.getStartDate());
				this.map(this.source.getEndDate(), this.destination.getEndDate());
				this.map(this.source.isActive(), this.destination.isActive());
				this.map(this.source.getAdditionalInfo(), this.destination.getAdditionalInfo());
				this.using(new StudySummaryConverter()).map(this.source.getInstanceMetaData()).setStudies(null);
				this.using(new ContactConverter()).map(this.source.getContacts()).setContacts(null);
			}

		});
	}
}
