package org.ibp.api.brapi.v1.trial;

import org.generationcp.middleware.domain.dms.StudySummary;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class TrialMapper {
	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private TrialMapper() {

	}

	static {
		TrialMapper.addTrialSummaryMapper(TrialMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return TrialMapper.applicationWideModelMapper;
	}


	private static void addTrialSummaryMapper(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<StudySummary, TrialSummary>() {

			@Override
			protected void configure() {
				map(source.getLocationId(), destination.getLocationDbId());
				map(source.getStudyDbid(), destination.getTrialDbId());
				map(source.getName(), destination.getTrialName());
				map(source.getProgramDbId(), destination.getProgramDbId());
				map(source.getProgramName(), destination.getProgramName());
				map(source.getStartDate(), destination.getStartDate());
				map(source.getEndDate(), destination.getEndDate());
				map(source.isActive(), destination.isActive());
				map(source.getOptionalInfo(), destination.getAdditionalInfo());

			}

		});
	}
}
