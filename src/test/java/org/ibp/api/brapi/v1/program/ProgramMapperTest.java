package org.ibp.api.brapi.v1.program;

import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class ProgramMapperTest {

	final static String PROGRAM_UUID = "92c47f83-4427-44c9-982f-b611b8917a2d";

	@Test
	public void programDetailsMapperTest() {
		final ModelMapper mapper = ProgramMapper.getInstance();
		ProgramDetailsDto programDetailsDto = new ProgramDetailsDto(PROGRAM_UUID, "Wheat", null, null, null, null, null, null);
		Program program = mapper.map(programDetailsDto, Program.class);

		assertThat(program.getProgramDbId(), equalTo(programDetailsDto.getProgramDbId()));
		assertThat(program.getName(), equalTo(programDetailsDto.getName()));
		assertThat(program.getAbbreviation(), equalTo(programDetailsDto.getAbbreviation()));
		assertThat(program.getLeadPerson(), equalTo(programDetailsDto.getLeadPerson()));
		assertThat(program.getObjective(), equalTo(programDetailsDto.getObjective()));

	}
}
