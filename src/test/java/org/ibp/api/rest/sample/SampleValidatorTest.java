package org.ibp.api.rest.sample;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SampleValidatorTest {

	@Mock
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	@InjectMocks
	private SampleValidator validator;

	@Test
	public void validateSamplesForImportPlateErrors() {

		final Integer listId = 1;
		final List<SampleDTO> samples = new ArrayList<>();

		// Sample with plate and well id exceeded 255
		final SampleDTO sampleDTO = new SampleDTO();
		sampleDTO.setSampleBusinessKey("BusinessKey1");
		sampleDTO.setPlateId(RandomStringUtils.randomAlphabetic(300));
		sampleDTO.setWell(RandomStringUtils.randomAlphabetic(300));

		// Business key duplicate
		final SampleDTO sampleDTO2 = new SampleDTO();
		sampleDTO2.setSampleBusinessKey("BusinessKey1");
		sampleDTO2.setPlateId(RandomStringUtils.randomAlphabetic(300));
		sampleDTO2.setWell(RandomStringUtils.randomAlphabetic(300));

		// No Business key
		final SampleDTO sampleDTO3 = new SampleDTO();
		sampleDTO3.setSampleBusinessKey("");
		sampleDTO3.setPlateId(RandomStringUtils.randomAlphabetic(10));
		sampleDTO3.setWell(RandomStringUtils.randomAlphabetic(10));

		samples.add(sampleDTO);
		samples.add(sampleDTO2);
		samples.add(sampleDTO3);

		// Some SampleIds do not exist in DB
		Mockito.when(this.sampleListServiceMW.countSamplesByUIDs(Mockito.<String>anySet(), Mockito.anyInt())).thenReturn(1l);


		try {
			this.validator.validateSamplesForImportPlate(listId, samples);
			Assert.fail("Expcected to throw ApiRequestValidationException");
		} catch (final ApiRequestValidationException e) {
			final List<String> codes = new ArrayList<>();
			for (final ObjectError error : e.getErrors()) {
				codes.add(error.getCode());
			}
			Assert.assertTrue(codes.contains("sample.record.not.include.sample.id.in.file"));
			Assert.assertTrue(codes.contains("sample.id.repeat.in.file"));
			Assert.assertTrue(codes.contains("sample.plate.id.exceed.length"));
			Assert.assertTrue(codes.contains("sample.well.exceed.length"));
			Assert.assertTrue(codes.contains("sample.sample.ids.not.present.in.file"));
		}

	}

	@Test
	public void validateSamplesForImportPlateSuccess() {

		final Integer listId = 1;
		final List<SampleDTO> samples = new ArrayList<>();

		// Sample with plate and well id exceeded 255
		final SampleDTO sampleDTO = new SampleDTO();
		sampleDTO.setSampleBusinessKey("BusinessKey1");
		sampleDTO.setPlateId(RandomStringUtils.randomAlphabetic(10));
		sampleDTO.setWell(RandomStringUtils.randomAlphabetic(10));

		// Business key duplicate
		final SampleDTO sampleDTO2 = new SampleDTO();
		sampleDTO2.setSampleBusinessKey("BusinessKey2");
		sampleDTO2.setPlateId(RandomStringUtils.randomAlphabetic(10));
		sampleDTO2.setWell(RandomStringUtils.randomAlphabetic(10));

		samples.add(sampleDTO);
		samples.add(sampleDTO2);

		Mockito.when(this.sampleListServiceMW.countSamplesByUIDs(Mockito.<String>anySet(), Mockito.anyInt())).thenReturn(Long.valueOf(samples.size()));

		try {
			this.validator.validateSamplesForImportPlate(listId, samples);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Expcected to NOT throw ApiRequestValidationException");
		}

	}


}
