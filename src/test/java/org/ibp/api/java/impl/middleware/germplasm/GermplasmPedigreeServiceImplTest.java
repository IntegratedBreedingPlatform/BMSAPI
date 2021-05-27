package org.ibp.api.java.impl.middleware.germplasm;

import org.ibp.api.exception.ApiRequestValidationException;
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

import java.util.Collections;

public class GermplasmPedigreeServiceImplTest {

	private static final Integer GID = 999;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private org.generationcp.middleware.api.germplasm.pedigree.GermplasmPedigreeService middlewareGermplasmPedigreeService;

	@InjectMocks
	private GermplasmPedigreeServiceImpl germplasmPedigreeService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetGermplasmPedigreeTree_WhereGIDIsInvalid() {
		final int level = 1;
		final boolean includeDerivativeLines = true;
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(Collections.singletonList(GID)));
			this.germplasmPedigreeService.getGermplasmPedigreeTree(GID, level, includeDerivativeLines);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
				ArgumentMatchers.eq(Collections.singletonList(GID)));
			Mockito.verify(this.middlewareGermplasmPedigreeService, Mockito.never())
				.getGermplasmPedigreeTree(GID, level, includeDerivativeLines);
		}
	}

	@Test
	public void testGetGermplasmPedigreeTree_Success() {
		final int level = 1;
		final boolean includeDerivativeLines = true;
		this.germplasmPedigreeService.getGermplasmPedigreeTree(GID, level, includeDerivativeLines);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(GID)));
		Mockito.verify(this.middlewareGermplasmPedigreeService).getGermplasmPedigreeTree(GID, level, includeDerivativeLines);
	}

	@Test
	public void testGetGenerationHistory_WhereGIDIsInvalid() {
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(Collections.singletonList(GID)));
			this.germplasmPedigreeService.getGenerationHistory(GID);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
				ArgumentMatchers.eq(Collections.singletonList(GID)));
			Mockito.verify(this.middlewareGermplasmPedigreeService, Mockito.never()).getGenerationHistory(GID);
		}
	}

	@Test
	public void testGetGenerationHistory_Success() {
		this.germplasmPedigreeService.getGenerationHistory(GID);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(GID)));
		Mockito.verify(this.middlewareGermplasmPedigreeService).getGenerationHistory(GID);
	}

	@Test
	public void testGetManagementNeighbors_WhereGIDIsInvalid() {
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(Collections.singletonList(GID)));
			this.germplasmPedigreeService.getManagementNeighbors(GID);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
				ArgumentMatchers.eq(Collections.singletonList(GID)));
			Mockito.verify(this.middlewareGermplasmPedigreeService, Mockito.never()).getManagementNeighbors(GID);
		}
	}

	@Test
	public void testGetManagementNeighbors_Success() {
		this.germplasmPedigreeService.getManagementNeighbors(GID);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(GID)));
		Mockito.verify(this.middlewareGermplasmPedigreeService).getManagementNeighbors(GID);
	}

	@Test
	public void testGetGroupRelatives_WhereGIDIsInvalid() {
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(Collections.singletonList(GID)));
			this.germplasmPedigreeService.getGroupRelatives(GID);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
				ArgumentMatchers.eq(Collections.singletonList(GID)));
			Mockito.verify(this.middlewareGermplasmPedigreeService, Mockito.never()).getGroupRelatives(GID);
		}
	}

	@Test
	public void testGetGroupRelatives_Success() {
		this.germplasmPedigreeService.getGroupRelatives(GID);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(GID)));
		Mockito.verify(this.middlewareGermplasmPedigreeService).getGroupRelatives(GID);
	}

	@Test
	public void testGetGermplasmMaintenanceNeighborhood_WhereGIDIsInvalid() {
		final int numberOfStepsBackwards = 2;
		final int numberOfStepsForward = 2;
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(Collections.singletonList(GID)));
			this.germplasmPedigreeService.getGermplasmMaintenanceNeighborhood(GID, numberOfStepsBackwards, numberOfStepsForward);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
				ArgumentMatchers.eq(Collections.singletonList(GID)));
			Mockito.verify(this.middlewareGermplasmPedigreeService, Mockito.never())
				.getGermplasmMaintenanceNeighborhood(GID, numberOfStepsBackwards, numberOfStepsForward);
		}
	}

	@Test
	public void testGetGermplasmMaintenanceNeighborhood_Success() {
		final int numberOfStepsBackwards = 2;
		final int numberOfStepsForward = 2;
		this.germplasmPedigreeService.getGermplasmMaintenanceNeighborhood(GID, numberOfStepsBackwards, numberOfStepsForward);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(GID)));
		Mockito.verify(this.middlewareGermplasmPedigreeService)
			.getGermplasmMaintenanceNeighborhood(GID, numberOfStepsBackwards, numberOfStepsForward);
	}

	@Test
	public void testGetGermplasmDerivativeNeighborhood_WhereGIDIsInvalid() {
		final int numberOfStepsBackwards = 2;
		final int numberOfStepsForward = 2;
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(Collections.singletonList(GID)));
			this.germplasmPedigreeService.getGermplasmDerivativeNeighborhood(GID, numberOfStepsBackwards, numberOfStepsForward);
			Assert.fail("should throw an exception");
		} catch (final ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
				ArgumentMatchers.eq(Collections.singletonList(GID)));
			Mockito.verify(this.middlewareGermplasmPedigreeService, Mockito.never())
				.getGermplasmDerivativeNeighborhood(GID, numberOfStepsBackwards, numberOfStepsForward);
		}
	}

	@Test
	public void testGetGermplasmDerivativeNeighborhood_Success() {
		final int numberOfStepsBackwards = 2;
		final int numberOfStepsForward = 2;
		this.germplasmPedigreeService.getGermplasmDerivativeNeighborhood(GID, numberOfStepsBackwards, numberOfStepsForward);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(GID)));
		Mockito.verify(this.middlewareGermplasmPedigreeService)
			.getGermplasmDerivativeNeighborhood(GID, numberOfStepsBackwards, numberOfStepsForward);
	}
}
