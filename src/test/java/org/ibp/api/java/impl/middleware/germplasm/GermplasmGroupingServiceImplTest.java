package org.ibp.api.java.impl.middleware.germplasm;

import com.beust.jcommander.internal.Lists;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.ibp.api.domain.germplasm.GermplasmUngroupingResponse;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmGroupingRequest;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

public class GermplasmGroupingServiceImplTest {

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private GermplasmGroupingService middlewareGermplasmGroupingService;

	@InjectMocks
	private GermplasmGroupingServiceImpl germplasmGroupingService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testMarkFixed_WhereGIDIsInvalid() {
		final Integer gid = new Random().nextInt();
		final GermplasmGroupingRequest request = new GermplasmGroupingRequest();
		request.setGids(Collections.singletonList(gid));
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(Collections.singletonList(gid)));
			this.germplasmGroupingService.markFixed(request);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
				ArgumentMatchers.eq(Collections.singletonList(gid)));
			Mockito.verify(this.middlewareGermplasmGroupingService, Mockito.never())
				.markFixed(ArgumentMatchers.anyList(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.anyBoolean());
		}
	}

	@Test
	public void testMarkFixed_NoGidsToFix() {
		final GermplasmGroupingRequest request = new GermplasmGroupingRequest();
		try {
			this.germplasmGroupingService.markFixed(request);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("grouping.gids.null"));
		}
	}

	@Test
	public void testMarkFixed_NullGermplasmGroupingRequest() {
		try {
			this.germplasmGroupingService.markFixed(null);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("germplasm.group.request.null"));
		}
	}

	@Test
	public void testMarkFixed_Success() {
		final Random random = new Random();
		final GermplasmGroupingRequest request = new GermplasmGroupingRequest();
		final List<Integer> gids = Arrays.asList(random.nextInt(), random.nextInt(), random.nextInt());
		request.setGids(gids);
		request.setIncludeDescendants(random.nextBoolean());
		request.setPreserveExistingGroup(random.nextBoolean());
		this.germplasmGroupingService.markFixed(request);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(gids));
		Mockito.verify(this.middlewareGermplasmGroupingService)
			.markFixed(request.getGids(), request.isIncludeDescendants(), request.isPreserveExistingGroup());
	}

	@Test
	public void testUnfixLines_WhereGIDIsInvalid() {
		final Random random = new Random();
		final List<Integer> gids = Arrays.asList(random.nextInt(), random.nextInt(), random.nextInt());
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(gids));
			this.germplasmGroupingService.unfixLines(gids);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
				ArgumentMatchers.eq(gids));
			Mockito.verify(this.middlewareGermplasmGroupingService, Mockito.never())
				.unfixLines(gids);
		}
	}

	@Test
	public void testUnfixLines_NoGIDs() {
		try {
			this.germplasmGroupingService.unfixLines(Collections.emptyList());
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("grouping.gids.null"));
		}
	}

	@Test
	public void testUnfixLines_AllUnfixed() {
		final Random random = new Random();
		final List<Integer> gids = Lists.newArrayList(random.nextInt(), random.nextInt(), random.nextInt());

		Mockito.doReturn(gids).when(this.middlewareGermplasmGroupingService).unfixLines(gids);
		final GermplasmUngroupingResponse germplasmUngroupingResponse = this.germplasmGroupingService.unfixLines(gids);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(gids));
		Mockito.verify(this.middlewareGermplasmGroupingService).unfixLines(gids);
		Assert.assertEquals(gids, germplasmUngroupingResponse.getUnfixedGids());
		Assert.assertEquals(0, germplasmUngroupingResponse.getNumberOfGermplasmWithoutGroup().intValue());
	}

	@Test
	public void testUnfixLines_OnlySomeUnfixed() {
		final Random random = new Random();
		final List<Integer> gids = Lists.newArrayList(random.nextInt(), random.nextInt(), random.nextInt());

		final List<Integer> successfullyUnfixedGids = Arrays.asList(gids.get(0), gids.get(1));
		Mockito.doReturn(successfullyUnfixedGids).when(this.middlewareGermplasmGroupingService).unfixLines(gids);
		final GermplasmUngroupingResponse germplasmUngroupingResponse = this.germplasmGroupingService.unfixLines(gids);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(gids));
		Mockito.verify(this.middlewareGermplasmGroupingService).unfixLines(gids);
		Assert.assertEquals(successfullyUnfixedGids, germplasmUngroupingResponse.getUnfixedGids());
		Assert.assertEquals(gids.size() - successfullyUnfixedGids.size(), germplasmUngroupingResponse.getNumberOfGermplasmWithoutGroup().intValue());
	}

}
