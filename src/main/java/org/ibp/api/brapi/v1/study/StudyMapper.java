package org.ibp.api.brapi.v1.study;

import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

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
				this.using(new ExperimentalDesignConverter()).map(this.source.getMetadata()).setExperimentalDesign(null);
				this.using(new ContactConverter()).map(this.source.getContacts()).setContacts(null);
				this.using(new EnvironmentParameterConverter()).map(this.source.getEnvironmentParameters()).setEnvironmentParameters(null);
			}
		});
	}

}
