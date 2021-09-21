package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.germplasm.GermplasmMergeRequestDto;
import org.generationcp.middleware.pojos.Germplasm;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmMergeRequestDtoValidatorTest {

	@Mock
	private GermplasmService germplasmServiceMiddleware;

	@InjectMocks
	private GermplasmMergeRequestDtoValidator germplasmMergeRequestDtoValidator;

	@Before
	public void init() {
		this.germplasmMergeRequestDtoValidator.setMaximumGermplasmToMerge(100);
	}

	@Test
	public void testValidate() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		germplasmMergeRequestDto.setNonSelectedGermplasm(
			Arrays.asList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, false, false, false),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(3, false, false, false)));
		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
		} catch (final Exception e) {
			fail("Method should not throw an error");
		}
	}

	@Test
	public void testValidate_MaxNumberOfGermplasmExceeded() {

		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		germplasmMergeRequestDto.setNonSelectedGermplasm(
			Arrays.asList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, false, false, false),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(3, false, false, false)));

		this.germplasmMergeRequestDtoValidator.setMaximumGermplasmToMerge(2);

		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.maximum.number.exceeeded"));
		}

	}

	@Test
	public void testValidate_NonSelectedGermplasmHasDescendants() {

		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		germplasmMergeRequestDto.setNonSelectedGermplasm(
			Arrays.asList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, false, false, false),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(3, false, false, false)));

		when(this.germplasmServiceMiddleware.getGidsOfGermplasmWithDescendants(Mockito.anyList())).thenReturn(
			new HashSet<>(Arrays.asList(2, 3)));

		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.cannot.merge.germplasm.with.progeny"));
		}
	}

	@Test
	public void testValidate_AnyOfNonSelectedGermplasmIsGrouped() {

		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		germplasmMergeRequestDto.setNonSelectedGermplasm(
			Arrays.asList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, false, false, false),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(3, false, false, false)));

		final Germplasm germplasm = new Germplasm();
		// Mark germplasm as grouped
		germplasm.setMgid(1);
		when(this.germplasmServiceMiddleware.getGermplasmByGIDs(Mockito.anyList())).thenReturn(Arrays.asList(germplasm));

		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.cannot.merge.germplasm.already.grouped"));
		}
	}

	@Test
	public void testValidate_NoGermplasmToMerge() {

		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		germplasmMergeRequestDto.setNonSelectedGermplasm(
			Arrays.asList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, false, false, true),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(3, false, false, true)));

		final Germplasm germplasm = new Germplasm();
		when(this.germplasmServiceMiddleware.getGermplasmByGIDs(Mockito.anyList())).thenReturn(Arrays.asList(germplasm));

		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final Exception e) {
			assertThat(e, instanceOf(ApiRequestValidationException.class));
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.no.germplasm.to.merge"));
		}

	}

}
