package org.ibp.api.rest.sample;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SampleMapperTest {

	private static final SimpleDateFormat DATE_FORMAT = DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT_3);

	@Test
	public void testSampleMapper() {
		ModelMapper mapper = SampleMapper.getInstance();
		SampleDTO sampleDTO =
			new SampleDTO(randomAlphanumeric(6), randomAlphanumeric(6), randomAlphanumeric(6), new Date(), randomAlphanumeric(6),
				new Random().nextInt(), randomAlphanumeric(6));
		org.ibp.api.rest.sample.SampleDTO sampleDTOApi = mapper.map(sampleDTO, org.ibp.api.rest.sample.SampleDTO.class);

		assertThat(sampleDTOApi.getPlantBusinessKey(), equalTo(sampleDTO.getPlantBusinessKey()));
		assertThat(sampleDTOApi.getPlantNumber(), equalTo(sampleDTO.getPlantNumber()));
		assertThat(sampleDTOApi.getSampleBusinessKey(), equalTo(sampleDTO.getSampleBusinessKey()));
		assertThat(sampleDTOApi.getSampleList(), equalTo(sampleDTO.getSampleList()));
		assertThat(sampleDTOApi.getSampleName(), equalTo(sampleDTO.getSampleName()));
		assertThat(sampleDTOApi.getSamplingDate(), equalTo(DATE_FORMAT.format(sampleDTO.getSamplingDate())));
		assertThat(sampleDTOApi.getTakenBy(), equalTo(sampleDTO.getTakenBy()));
	}

}
