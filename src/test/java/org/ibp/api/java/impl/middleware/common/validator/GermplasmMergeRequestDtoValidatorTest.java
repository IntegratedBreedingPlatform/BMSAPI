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
		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		germplasmMergeRequestDto.setNonSelectedGermplasm(
			Arrays.asList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, false, false, false),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(3, false, false, false)));
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
	public void testValidate_EmptyNonSelectedGermplasm() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
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
		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		germplasmMergeRequestDto.setNonSelectedGermplasm(
			Arrays.asList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, false, false, false),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(null, false, false, false)));
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
		germplasmMergeRequestDto.setNonSelectedGermplasm(
			Lists.newArrayList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, false, false, true),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(3, false, false, true)));

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

	@Test
	public void testValidate_InvalidMigrateAndCloseLotsSetting() {

		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		// Germplasm 2 has closeLots = true and migrateLots = true
		germplasmMergeRequestDto.setNonSelectedGermplasm(
			Lists.newArrayList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, true, true, false),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(3, false, false, false)));

		try {
			this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
			fail("Method should throw an error");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
				hasItem("germplasm.merge.invalid.lots.spec"));
		}
	}

	private GermplasmMergeRequestDto getGermplasmMergeRequestDto() {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		germplasmMergeRequestDto.setNonSelectedGermplasm(
			Lists.newArrayList(new GermplasmMergeRequestDto.NonSelectedGermplasm(2, true, false, false),
				new GermplasmMergeRequestDto.NonSelectedGermplasm(3, false, false, false)));
		return germplasmMergeRequestDto;
	}

}
