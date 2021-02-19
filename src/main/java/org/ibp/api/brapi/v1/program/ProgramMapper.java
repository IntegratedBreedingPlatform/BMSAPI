package org.ibp.api.brapi.v1.program;

import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class ProgramMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private ProgramMapper() {

	}

	static {
		ProgramMapper.addProgramDetailsDataMapping(ProgramMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return ProgramMapper.applicationWideModelMapper;
	}

	private static void addProgramDetailsDataMapping(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<ProgramDetailsDto, Program>() {

			@Override
			protected void configure() {
				this.map().setProgramDbId(this.source.getProgramDbId());
				this.map().setName(this.source.getName());
				this.map().setAbbreviation(this.source.getAbbreviation());
				this.map().setObjective(this.source.getObjective());
				this.map().setLeadPerson(this.source.getLeadPerson());

				// New Fields for BrapiV2
				this.map().setProgramName(this.source.getName());
				this.map().setLeadPersonDbId(this.source.getLeadPersonDbId());
				this.map().setLeadPersonName(this.source.getLeadPerson());
				this.map().setCommonCropName(this.source.getCropName());
			}

		});
	}
}
