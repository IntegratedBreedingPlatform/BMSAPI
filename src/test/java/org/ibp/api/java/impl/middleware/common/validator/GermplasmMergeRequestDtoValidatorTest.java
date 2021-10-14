package org.ibp.api.java.impl.middleware.common.validator;

import com.beust.jcommander.internal.Lists;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.germplasm.GermplasmMergeRequestDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
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

	@Mock
	private GermplasmValidator germplasmValidator;

	@InjectMocks
	private GermplasmMergeRequestDtoValidator germplasmMergeRequestDtoValidator;

	@Before
	public void init() {
		this.germplasmMergeRequestDtoValidator.setMaximumGermplasmToMerge(100);
	}

	@Test
	public void testValidate() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = this.getGermplasmMergeRequestDto();
		this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(), ArgumentMatchers.eq(Lists.newArrayList(2, 3, 1)));
		Mockito.verify(this.germplasmServiceMiddleware).getGidsOfGermplasmWithDescendants(Lists.newArrayList(2, 3));
		Mockito.verify(this.germplasmServiceMiddleware).getCodeFixedGidsByGidList(Lists.newArrayList(2, 3));
		Mockito.verify(this.germplasmServiceMiddleware).getGermplasmUsedInLockedList(Lists.newArrayList(2, 3));
		Mockito.verify(this.germplasmServiceMiddleware).getGermplasmUsedInLockedStudies(Lists.newArrayList(2, 3));
	}

	@Test
	public void testValidate_NullRequest() {
		try {
			this.germplasmMergeRequestDtoValidator.validate(null);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.request.null"));
			Mockito.verifyZeroInteractions(this.germplasmValidator);
			Mockito.verifyZeroInteractions(this.germplasmServiceMiddleware);
		}
	}

	@Test
	public void testValidate_NullTargetGermplasm() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.target.germplasm.null"));
			Mockito.verifyZeroInteractions(this.germplasmValidator);
			Mockito.verifyZeroInteractions(this.germplasmServiceMiddleware);
		}
	}

	@Test
	public void testValidate_NullMergeOptions() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(100);
		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.merge.options.null"));
			Mockito.verifyZeroInteractions(this.germplasmValidator);
			Mockito.verifyZeroInteractions(this.germplasmServiceMiddleware);
		}
	}

	@Test
	public void testValidate_EmptyNonSelectedGermplasm() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		germplasmMergeRequestDto.setMergeOptions(new GermplasmMergeRequestDto.MergeOptions());
		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.import.list.null"));
			Mockito.verifyZeroInteractions(this.germplasmValidator);
			Mockito.verifyZeroInteractions(this.germplasmServiceMiddleware);
		}
	}

	@Test
	public void testValidate_NonSelectedGermplasmHasNullGid() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = this.getGermplasmMergeRequestDto();
		germplasmMergeRequestDto.getNonSelectedGermplasm().add(new GermplasmMergeRequestDto.NonSelectedGermplasm(null, false, false));
		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.non.selected.null.gid"));
			Mockito.verifyZeroInteractions(this.germplasmValidator);
			Mockito.verifyZeroInteractions(this.germplasmServiceMiddleware);
		}
	}

	@Test
	public void testValidate_NonSelectedGermplasmHasNullMergeLotsAttribute() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = this.getGermplasmMergeRequestDto();
		germplasmMergeRequestDto.getNonSelectedGermplasm().add(new GermplasmMergeRequestDto.NonSelectedGermplasm(4, null, false));
		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.non.selected.null.migrate.lots"));
			Mockito.verifyZeroInteractions(this.germplasmValidator);
			Mockito.verifyZeroInteractions(this.germplasmServiceMiddleware);
		}
	}

	@Test
	public void testValidate_TargetGermplasmIdInNonSelected() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = this.getGermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(3);
		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.target.gid.in.non.selected.germplasm"));
			Mockito.verifyZeroInteractions(this.germplasmValidator);
			Mockito.verifyZeroInteractions(this.germplasmServiceMiddleware);
		}
	}

	@Test
	public void testValidate_MaxNumberOfGermplasmExceeded() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = this.getGermplasmMergeRequestDto();
		this.germplasmMergeRequestDtoValidator.setMaximumGermplasmToMerge(1);

		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.maximum.number.exceeeded"));
			Mockito.verifyZeroInteractions(this.germplasmValidator);
			Mockito.verifyZeroInteractions(this.germplasmServiceMiddleware);
		}

	}

	@Test
	public void testValidate_NonSelectedGermplasmHasDescendants() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = this.getGermplasmMergeRequestDto();
		when(this.germplasmServiceMiddleware.getGidsOfGermplasmWithDescendants(Mockito.anyList())).thenReturn(
			new HashSet<>(Arrays.asList(2, 3)));

		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.cannot.merge.germplasm.with.progeny"));
		}
	}

	@Test
	public void testValidate_AnyOfNonSelectedGermplasmIsGrouped() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = this.getGermplasmMergeRequestDto();
		when(this.germplasmServiceMiddleware.getCodeFixedGidsByGidList(Mockito.anyList())).thenReturn(new HashSet<>(Arrays.asList(2, 3)));

		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.cannot.merge.germplasm.already.grouped"));
		}
	}

	@Test
	public void testValidate_NoGermplasmToMergeWhenAllNonSelectedOmitted() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		germplasmMergeRequestDto.setMergeOptions(new GermplasmMergeRequestDto.MergeOptions());
		germplasmMergeRequestDto.setNonSelectedGermplasm(
			Lists.newArrayList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, false, true),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(3, false, true)));

		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.no.germplasm.to.merge"));
			Mockito.verifyZeroInteractions(this.germplasmValidator);
			Mockito.verifyZeroInteractions(this.germplasmServiceMiddleware);
		}

	}

	@Test
	public void testValidate_AnyOfNonSelectedGermplasmIsUsedInLockedList() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = this.getGermplasmMergeRequestDto();
		when(this.germplasmServiceMiddleware.getGermplasmUsedInLockedList(Mockito.anyList())).thenReturn(
			new HashSet<>(Arrays.asList(2, 3)));

		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.cannot.merge.germplasm.is.in.locked.list"));
		}
	}

	@Test
	public void testValidate_AnyOfNonSelectedGermplasmIsUsedInLockedStudy() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = this.getGermplasmMergeRequestDto();
		when(this.germplasmServiceMiddleware.getGermplasmUsedInLockedStudies(Mockito.anyList())).thenReturn(
			new HashSet<>(Arrays.asList(2, 3)));

		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.cannot.merge.germplasm.is.in.locked.study"));
		}
	}

	private GermplasmMergeRequestDto getGermplasmMergeRequestDto() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		germplasmMergeRequestDto.setNonSelectedGermplasm(
			Lists.newArrayList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, false, false),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(3, false, false)));
		germplasmMergeRequestDto.setMergeOptions(new GermplasmMergeRequestDto.MergeOptions());
		return germplasmMergeRequestDto;
	}

}
