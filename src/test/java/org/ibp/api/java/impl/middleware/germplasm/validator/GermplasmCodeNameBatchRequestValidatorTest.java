package org.ibp.api.java.impl.middleware.germplasm.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.germplasm.GermplasmCodeNameBatchRequestDto;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmCodeNameBatchRequestValidatorTest {

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private BindingResult errors;

	@InjectMocks
	private GermplasmCodeNameBatchRequestValidator germplasmCodeNameBatchRequestValidator;

	@Test
	public void testValidate_Success() {
		final List<Integer> gids = Arrays.asList(1);
		final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto = new GermplasmCodeNameBatchRequestDto();
		germplasmCodeNameBatchRequestDto.setGids(gids);
		germplasmCodeNameBatchRequestDto.setNameType(GermplasmCodeNameBatchRequestValidator.CODE_1);
		final GermplasmNameSetting germplasmNameSetting = new GermplasmNameSetting();
		germplasmNameSetting.setPrefix(RandomStringUtils.randomAlphabetic(10));
		germplasmNameSetting.setSuffix(RandomStringUtils.random(10));
		germplasmCodeNameBatchRequestDto.setGermplasmCodeNameSetting(germplasmNameSetting);

		try {
			this.germplasmCodeNameBatchRequestValidator.validate(germplasmCodeNameBatchRequestDto);
		} catch (final ApiRequestValidationException exception) {
			Assert.fail("Method should now throw an exception.");
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidate_InvalidGids() {
		final List<Integer> gids = Arrays.asList(1);
		final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto = new GermplasmCodeNameBatchRequestDto();
		germplasmCodeNameBatchRequestDto.setGids(gids);

		Mockito.doThrow(new ApiRequestValidationException(new ArrayList<>())).when(this.germplasmValidator)
			.validateGids(Mockito.any(), Mockito.anyList());

		this.germplasmCodeNameBatchRequestValidator.validate(germplasmCodeNameBatchRequestDto);
	}

	@Test
	public void testValidate_InvalidNameType() {
		final List<Integer> gids = Arrays.asList(1);
		final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto = new GermplasmCodeNameBatchRequestDto();
		germplasmCodeNameBatchRequestDto.setGids(gids);
		germplasmCodeNameBatchRequestDto.setNameType(RandomStringUtils.randomAlphabetic(10));

		try {
			this.germplasmCodeNameBatchRequestValidator.validate(germplasmCodeNameBatchRequestDto);
		} catch (final ApiRequestValidationException exception) {
			Assert.assertTrue(Arrays.stream(exception.getErrors().get(0).getCodes())
				.anyMatch(val -> val.equals("germplasm.code.name.invalid.name.type")));
		}
	}

	@Test
	public void testValidate_PrefixIsBlank() {
		final List<Integer> gids = Arrays.asList(1);
		final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto = new GermplasmCodeNameBatchRequestDto();
		germplasmCodeNameBatchRequestDto.setGids(gids);
		germplasmCodeNameBatchRequestDto.setNameType(GermplasmCodeNameBatchRequestValidator.CODE_1);
		final GermplasmNameSetting germplasmNameSetting = new GermplasmNameSetting();
		germplasmCodeNameBatchRequestDto.setGermplasmCodeNameSetting(germplasmNameSetting);
		try {
			this.germplasmCodeNameBatchRequestValidator.validate(germplasmCodeNameBatchRequestDto);
		} catch (final ApiRequestValidationException exception) {
			Assert.assertTrue(Arrays.stream(exception.getErrors().get(0).getCodes())
				.anyMatch(val -> val.equals("germplasm.code.name.prefix.required")));
		}
	}

	@Test
	public void testValidate_PrefixExceedsMaxLength() {
		final List<Integer> gids = Arrays.asList(1);
		final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto = new GermplasmCodeNameBatchRequestDto();
		germplasmCodeNameBatchRequestDto.setGids(gids);
		germplasmCodeNameBatchRequestDto.setNameType(GermplasmCodeNameBatchRequestValidator.CODE_1);
		final GermplasmNameSetting germplasmNameSetting = new GermplasmNameSetting();
		germplasmNameSetting.setPrefix(RandomStringUtils.random(50));
		germplasmCodeNameBatchRequestDto.setGermplasmCodeNameSetting(germplasmNameSetting);
		try {
			this.germplasmCodeNameBatchRequestValidator.validate(germplasmCodeNameBatchRequestDto);
		} catch (final ApiRequestValidationException exception) {
			Assert.assertTrue(Arrays.stream(exception.getErrors().get(0).getCodes())
				.anyMatch(val -> val.equals("germplasm.code.name.prefix.max.length.exceeded")));
		}
	}

	@Test
	public void testValidate_SuffixExceedsMaxLength() {
		final List<Integer> gids = Arrays.asList(1);
		final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto = new GermplasmCodeNameBatchRequestDto();
		germplasmCodeNameBatchRequestDto.setGids(gids);
		germplasmCodeNameBatchRequestDto.setNameType(GermplasmCodeNameBatchRequestValidator.CODE_1);
		final GermplasmNameSetting germplasmNameSetting = new GermplasmNameSetting();
		germplasmNameSetting.setPrefix(RandomStringUtils.randomAlphabetic(10));
		germplasmNameSetting.setSuffix(RandomStringUtils.random(50));
		germplasmCodeNameBatchRequestDto.setGermplasmCodeNameSetting(germplasmNameSetting);
		try {
			this.germplasmCodeNameBatchRequestValidator.validate(germplasmCodeNameBatchRequestDto);
		} catch (final ApiRequestValidationException exception) {
			Assert.assertTrue(Arrays.stream(exception.getErrors().get(0).getCodes())
				.anyMatch(val -> val.equals("germplasm.code.name.suffix.max.length.exceeded")));
		}
	}

}
