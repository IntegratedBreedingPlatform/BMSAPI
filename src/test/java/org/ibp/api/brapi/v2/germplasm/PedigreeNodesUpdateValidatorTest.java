package org.ibp.api.brapi.v2.germplasm;

import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeReferenceDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.ParentType;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmMatchRequestDto;
import org.generationcp.middleware.pojos.MethodType;
import org.ibp.api.java.germplasm.GermplasmService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PedigreeNodesUpdateValidatorTest {

	private final Random random = new Random();

	@Mock
	private BindingResult errors;

	@Mock
	private GermplasmService mockGermplasmService;

	@Mock
	private BreedingMethodService mockBreedingMethodService;

	@Mock
	private org.generationcp.middleware.api.germplasm.GermplasmService mockGermplasmMiddlewareService;

	@InjectMocks
	private PedigreeNodesUpdateValidator pedigreeNodesUpdateValidatorUnderTest;

	@Test
	public void testPrunePedigreeNodesForUpdate_OK() {
		final int breedingMethodDbId = this.random.nextInt();
		final int femaleParentDbId = this.random.nextInt();
		final int maleParentDbId = this.random.nextInt();
		final PedigreeNodeDTO pedigreeNodeDTO = this.createPedigreeNodeGenerative(breedingMethodDbId, femaleParentDbId, maleParentDbId);

		final Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap = new HashMap<>();
		pedigreeNodeDTOMap.put(pedigreeNodeDTO.getGermplasmDbId(), pedigreeNodeDTO);

		final GermplasmDto germplasmDto = new GermplasmDto();
		germplasmDto.setGermplasmUUID(pedigreeNodeDTO.getGermplasmDbId());
		germplasmDto.setBreedingMethodId(Integer.valueOf(pedigreeNodeDTO.getBreedingMethodDbId()));
		final GermplasmDto femaleGermplasmDto = new GermplasmDto();
		femaleGermplasmDto.setGermplasmUUID(String.valueOf(femaleParentDbId));
		final GermplasmDto maleGermplasmDto = new GermplasmDto();
		maleGermplasmDto.setGermplasmUUID(String.valueOf(maleParentDbId));

		final List<GermplasmDto> germplasmDtos = Arrays.asList(germplasmDto, femaleGermplasmDto, maleGermplasmDto);
		when(this.mockGermplasmService.findGermplasmMatches(any(GermplasmMatchRequestDto.class), eq(null))).thenReturn(
			germplasmDtos);
		when(this.mockGermplasmMiddlewareService.countGermplasmDerivativeProgeny(anySet())).thenReturn(new HashMap<>());

		final BreedingMethodDTO breedingMethodDTO = new BreedingMethodDTO();
		breedingMethodDTO.setMid(breedingMethodDbId);
		breedingMethodDTO.setType(MethodType.GENERATIVE.getCode());
		final List<BreedingMethodDTO> breedingMethodDTOS = Arrays.asList(breedingMethodDTO);
		when(this.mockBreedingMethodService.searchBreedingMethods(any(BreedingMethodSearchRequest.class), eq(null),
			any())).thenReturn(breedingMethodDTOS);

		final BindingResult result = this.pedigreeNodesUpdateValidatorUnderTest.prunePedigreeNodesForUpdate(pedigreeNodeDTOMap);

		assertFalse(pedigreeNodeDTOMap.isEmpty());
		assertFalse(result.hasErrors());
	}

	@Test
	public void testPrunePedigreeNodesForUpdate_WithError() {
		final int breedingMethodDbId = this.random.nextInt();
		final int femaleParentDbId = this.random.nextInt();
		final int maleParentDbId = this.random.nextInt();
		// Create pedigree node without parents
		final PedigreeNodeDTO pedigreeNodeDTO = this.createPedigreeNodeGenerative(breedingMethodDbId, null, null);

		final Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap = new HashMap<>();
		pedigreeNodeDTOMap.put(pedigreeNodeDTO.getGermplasmDbId(), pedigreeNodeDTO);

		when(this.mockGermplasmService.findGermplasmMatches(any(GermplasmMatchRequestDto.class), eq(null))).thenReturn(
			new ArrayList<>());

		final BreedingMethodDTO breedingMethodDTO = new BreedingMethodDTO();
		breedingMethodDTO.setMid(breedingMethodDbId);
		breedingMethodDTO.setType(MethodType.GENERATIVE.getCode());
		final List<BreedingMethodDTO> breedingMethodDTOS = Arrays.asList(breedingMethodDTO);
		when(this.mockBreedingMethodService.searchBreedingMethods(any(BreedingMethodSearchRequest.class), eq(null),
			any())).thenReturn(breedingMethodDTOS);

		final BindingResult result = this.pedigreeNodesUpdateValidatorUnderTest.prunePedigreeNodesForUpdate(pedigreeNodeDTOMap);

		assertTrue(pedigreeNodeDTOMap.isEmpty());
		assertTrue(result.hasErrors());
	}

	@Test
	public void testValidateEmptyMap() {
		assertFalse(this.pedigreeNodesUpdateValidatorUnderTest.validateEmptyMap(this.errors, null));
		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.empty.map", "");
	}

	@Test
	public void testValidateGermplasmDbId_MissingKey() {
		final int breedingMethodDbId = this.random.nextInt();
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();
		final PedigreeNodeDTO pedigreeNodeDTO = this.createPedigreeNodeGenerative(breedingMethodDbId, null, null);

		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateGermplasmDbId(null, pedigreeNodeDTO, germplasmMapByUUIDs, this.errors, 1));

		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.missing.key", new String[] {"1"}, "");
	}

	@Test
	public void testValidateGermplasmDbId_MissingGermplasmDbId() {
		final int breedingMethodDbId = this.random.nextInt();
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();
		final PedigreeNodeDTO pedigreeNodeDTO = this.createPedigreeNodeGenerative(breedingMethodDbId, null, null);
		pedigreeNodeDTO.setGermplasmDbId(null);

		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateGermplasmDbId(String.valueOf(this.random.nextInt()), pedigreeNodeDTO,
				germplasmMapByUUIDs, this.errors, 1));

		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.missing.germplasmdbid", new String[] {"1"}, "");
	}

	@Test
	public void testValidateGermplasmDbId_GermplasmKeyAndGermplasmDbIdMismatch() {
		final int breedingMethodDbId = this.random.nextInt();
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();
		final PedigreeNodeDTO pedigreeNodeDTO = this.createPedigreeNodeGenerative(breedingMethodDbId, null, null);

		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateGermplasmDbId(String.valueOf(this.random.nextInt()), pedigreeNodeDTO,
				germplasmMapByUUIDs, this.errors, 1));

		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.key.germplasmdbid.mismatch", new String[] {"1"}, "");
	}

	@Test
	public void testValidateGermplasmDbId_InvalidGermplasmDbId() {
		final int breedingMethodDbId = this.random.nextInt();
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();
		final PedigreeNodeDTO pedigreeNodeDTO = this.createPedigreeNodeGenerative(breedingMethodDbId, null, null);

		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateGermplasmDbId(pedigreeNodeDTO.getGermplasmDbId(), pedigreeNodeDTO,
				germplasmMapByUUIDs, this.errors, 1));

		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.invalid.germplasmdbid", new String[] {"1", pedigreeNodeDTO.getGermplasmDbId()}, "");
	}

	@Test
	public void testValidateBreedingMethod() {
		final int breedingMethodDbId = this.random.nextInt();
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds = new HashMap<>();
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();
		final PedigreeNodeDTO pedigreeNodeDTO = this.createPedigreeNodeGenerative(breedingMethodDbId, null, null);

		pedigreeNodeDTO.setBreedingMethodDbId(null);
		assertFalse(this.pedigreeNodesUpdateValidatorUnderTest.validateBreedingMethod(pedigreeNodeDTO, breedingMethodDTOMapByIds,
			germplasmMapByUUIDs,
			this.errors, 1));

		Mockito.verify(this.errors).reject("pedigree.nodes.update.missing.breedingmethoddbid", new String[] {"1"}, "");

		pedigreeNodeDTO.setBreedingMethodDbId(String.valueOf(breedingMethodDbId));
		assertFalse(this.pedigreeNodesUpdateValidatorUnderTest.validateBreedingMethod(pedigreeNodeDTO, breedingMethodDTOMapByIds,
			germplasmMapByUUIDs,
			this.errors, 1));
		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.invalid.breedingmethoddbid", new String[] {"1", pedigreeNodeDTO.getBreedingMethodDbId()}, "");

		pedigreeNodeDTO.setBreedingMethodDbId(String.valueOf(breedingMethodDbId));

		final int existingMethodDbId = this.random.nextInt();
		final BreedingMethodDTO breedingMethodOfGermplasm = new BreedingMethodDTO();
		breedingMethodOfGermplasm.setMid(breedingMethodDbId);
		final BreedingMethodDTO breedingMethodDTO = new BreedingMethodDTO();
		breedingMethodDTO.setMid(existingMethodDbId);
		breedingMethodDTOMapByIds.put(String.valueOf(breedingMethodOfGermplasm.getMid()), breedingMethodOfGermplasm);
		breedingMethodDTOMapByIds.put(String.valueOf(breedingMethodDTO.getMid()), breedingMethodDTO);
		final GermplasmDto existingGermplasm = new GermplasmDto();
		existingGermplasm.setBreedingMethodId(existingMethodDbId);
		germplasmMapByUUIDs.put(pedigreeNodeDTO.getGermplasmDbId(), existingGermplasm);

		assertFalse(this.pedigreeNodesUpdateValidatorUnderTest.validateBreedingMethod(pedigreeNodeDTO, breedingMethodDTOMapByIds,
			germplasmMapByUUIDs,
			this.errors, 1));
		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.breedingmethoddbid.do.not.match.the.existing.germplasm.breeding.method",
				new String[] {"1", pedigreeNodeDTO.getBreedingMethodDbId()}, "");

	}

	@Test
	public void testValidateParents_MissingParents() {
		final PedigreeNodeDTO pedigreeNodeDTO = this.createPedigreeNodeGenerative(1, null, null);
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds = new HashMap<>();
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();

		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));

		Mockito.verify(this.errors).reject("pedigree.nodes.update.missing.parents", new String[] {"1"}, "");

	}

	@Test
	public void testValidateParents_InvalidParentType() {
		PedigreeNodeDTO pedigreeNodeDTO;
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds = new HashMap<>();
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();

		pedigreeNodeDTO = this.createPedigreeNodeGenerative(1, this.random.nextInt(), this.random.nextInt());
		pedigreeNodeDTO.getParents().get(0).setParentType("Invalid Parent Type");
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors, Mockito.times(1)).reject("pedigree.nodes.invalid.parent.type", new String[] {"1"}, "");

		pedigreeNodeDTO = this.createPedigreeNodeGenerative(1, this.random.nextInt(), this.random.nextInt());
		pedigreeNodeDTO.getParents().get(1).setParentType("Invalid Parent Type");
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors, Mockito.times(2)).reject("pedigree.nodes.invalid.parent.type", new String[] {"1"}, "");

	}

	@Test
	public void testValidateParents_Generative_BothMaleAndFemaleParentsMustBeSpecified() {
		final int breedingMethodId = this.random.nextInt();
		PedigreeNodeDTO pedigreeNodeDTO;
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds =
			this.createBreedingMethodMap(breedingMethodId, MethodType.GENERATIVE);
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();

		// Male parent/s not specified
		pedigreeNodeDTO = this.createPedigreeNodeGenerative(breedingMethodId, this.random.nextInt(), null);
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors, Mockito.times(1))
			.reject("pedigree.nodes.update.both.female.and.male.parents.must.be.specified", new String[] {"1"}, "");

		// Female parent not specified
		pedigreeNodeDTO = this.createPedigreeNodeGenerative(breedingMethodId, null, this.random.nextInt());
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors, Mockito.times(2))
			.reject("pedigree.nodes.update.both.female.and.male.parents.must.be.specified", new String[] {"1"}, "");

	}

	@Test
	public void testValidateParents_Generative_OnlyOneFemaleParentCanBeSpecified() {
		final int breedingMethodId = this.random.nextInt();
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds =
			this.createBreedingMethodMap(breedingMethodId, MethodType.GENERATIVE);
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();

		final PedigreeNodeDTO pedigreeNodeDTO =
			this.createPedigreeNodeGenerative(breedingMethodId, this.random.nextInt(), this.random.nextInt());
		// Add another female parent
		pedigreeNodeDTO.getParents()
			.add(new PedigreeNodeReferenceDTO(String.valueOf(this.random.nextInt()), "germplasmName", ParentType.FEMALE.name()));
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors).reject("pedigree.nodes.update.only.one.female.parent.can.be.specified", new String[] {"1"}, "");

	}

	@Test
	public void testValidateParents_Generative_InvalidGermplasmDbIds() {
		final int breedingMethodId = this.random.nextInt();
		final int femaleParentDbId = this.random.nextInt();
		final int maleParentDbId = this.random.nextInt();
		final PedigreeNodeDTO pedigreeNodeDTO = this.createPedigreeNodeGenerative(breedingMethodId, femaleParentDbId, maleParentDbId);
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds =
			this.createBreedingMethodMap(breedingMethodId, MethodType.GENERATIVE);
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();

		// Invalid Female germplasmDbId
		germplasmMapByUUIDs.put(String.valueOf(maleParentDbId), new GermplasmDto());
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.female.parent.invalid.germplasmdbid", new String[] {"1", String.valueOf(femaleParentDbId)}, "");

		// Invalid Male germplasmDbId
		germplasmMapByUUIDs.clear();
		germplasmMapByUUIDs.put(String.valueOf(femaleParentDbId), new GermplasmDto());
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.male.parent.invalid.germplasmdbid", new String[] {"1", String.valueOf(maleParentDbId)}, "");

		// Null Female germplasmDbId
		pedigreeNodeDTO.getParents().get(0).setGermplasmDbId(null);
		germplasmMapByUUIDs.clear();
		germplasmMapByUUIDs.put(String.valueOf(maleParentDbId), new GermplasmDto());
		assertTrue(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verifyNoMoreInteractions(this.errors);

		// Null Male germplasmDbId
		pedigreeNodeDTO.getParents().get(1).setGermplasmDbId(null);
		germplasmMapByUUIDs.clear();
		germplasmMapByUUIDs.put(String.valueOf(femaleParentDbId), new GermplasmDto());
		assertTrue(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verifyNoMoreInteractions(this.errors);

	}

	@Test
	public void testValidateParents_Generative_MaleParentDbIdIsEqualToTheGermplasmToBeUpdated() {
		final int breedingMethodId = this.random.nextInt();
		final int femaleParentDbId = this.random.nextInt();
		final int maleParentDbId = this.random.nextInt();
		final PedigreeNodeDTO pedigreeNodeDTO = this.createPedigreeNodeGenerative(breedingMethodId, femaleParentDbId, maleParentDbId);
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds =
			this.createBreedingMethodMap(breedingMethodId, MethodType.GENERATIVE);
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();

		// Male Parent has the same germplasmDbId as the germplasm to be updated.
		pedigreeNodeDTO.getParents().get(1).setGermplasmDbId(pedigreeNodeDTO.getGermplasmDbId());
		germplasmMapByUUIDs.put(pedigreeNodeDTO.getGermplasmDbId(), new GermplasmDto());
		germplasmMapByUUIDs.put(String.valueOf(maleParentDbId), new GermplasmDto());
		germplasmMapByUUIDs.put(String.valueOf(femaleParentDbId), new GermplasmDto());
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.male.progenitors.can.not.be.equals.to.germplasmdbid",
				new String[] {"1", String.valueOf(pedigreeNodeDTO.getGermplasmDbId())}, "");

	}

	@Test
	public void testValidateParents_Derivative_BothGroupAndImmediateSourceParentsMustBeSpecified() {
		final int breedingMethodId = this.random.nextInt();
		PedigreeNodeDTO pedigreeNodeDTO;
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds =
			this.createBreedingMethodMap(breedingMethodId, MethodType.DERIVATIVE);
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();

		// Group Source parent/s not specified
		pedigreeNodeDTO = this.createPedigreeNodeDerivative(breedingMethodId, this.random.nextInt(), null);
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors, Mockito.times(1))
			.reject("pedigree.nodes.update.both.group.source.and.immediate.source.parents.must.be.specified", new String[] {"1"}, "");

		// Immediate Source parent not specified
		pedigreeNodeDTO = this.createPedigreeNodeDerivative(breedingMethodId, null, this.random.nextInt());
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors, Mockito.times(2))
			.reject("pedigree.nodes.update.both.group.source.and.immediate.source.parents.must.be.specified", new String[] {"1"}, "");

	}

	@Test
	public void testValidateParents_Derivative_OnlyOneGroupAndImmediateSourceParentCanBeSpecified() {
		final int breedingMethodId = this.random.nextInt();
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds =
			this.createBreedingMethodMap(breedingMethodId, MethodType.DERIVATIVE);
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();

		PedigreeNodeDTO pedigreeNodeDTO =
			this.createPedigreeNodeDerivative(breedingMethodId, this.random.nextInt(), this.random.nextInt());
		// Add another group source parent
		pedigreeNodeDTO.getParents()
			.add(new PedigreeNodeReferenceDTO(String.valueOf(this.random.nextInt()), "germplasmName", ParentType.POPULATION.name()));
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors).reject("pedigree.nodes.update.only.one.population.parent.can.be.specified", new String[] {"1"}, "");

		pedigreeNodeDTO =
			this.createPedigreeNodeDerivative(breedingMethodId, this.random.nextInt(), this.random.nextInt());
		// Add another immediate source parent
		pedigreeNodeDTO.getParents()
			.add(new PedigreeNodeReferenceDTO(String.valueOf(this.random.nextInt()), "germplasmName", ParentType.SELF.name()));
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors).reject("pedigree.nodes.update.only.one.self.parent.can.be.specified", new String[] {"1"}, "");

	}

	@Test
	public void testValidateParents_Derivative_InvalidGermplasmDbIds() {
		final int breedingMethodId = this.random.nextInt();
		final int groupSourceParentDbId = this.random.nextInt();
		final int immediateSourceParentDbId = this.random.nextInt();
		final PedigreeNodeDTO pedigreeNodeDTO =
			this.createPedigreeNodeDerivative(breedingMethodId, groupSourceParentDbId, immediateSourceParentDbId);
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds =
			this.createBreedingMethodMap(breedingMethodId, MethodType.DERIVATIVE);
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();

		// Invalid Group Source germplasmDbId
		germplasmMapByUUIDs.put(String.valueOf(immediateSourceParentDbId), new GermplasmDto());
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.population.parent.invalid.germplasmdbid",
				new String[] {"1", String.valueOf(groupSourceParentDbId)},
				"");

		// Invalid Immediate Source germplasmDbId
		germplasmMapByUUIDs.clear();
		germplasmMapByUUIDs.put(String.valueOf(groupSourceParentDbId), new GermplasmDto());
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.self.parent.invalid.germplasmdbid",
				new String[] {"1", String.valueOf(immediateSourceParentDbId)},
				"");

		// Null Group Source germplasmDbId
		pedigreeNodeDTO.getParents().get(0).setGermplasmDbId(null);
		germplasmMapByUUIDs.clear();
		germplasmMapByUUIDs.put(String.valueOf(immediateSourceParentDbId), new GermplasmDto());
		assertTrue(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verifyNoMoreInteractions(this.errors);

		// Null Immediate Source germplasmDbId
		pedigreeNodeDTO.getParents().get(1).setGermplasmDbId(null);
		germplasmMapByUUIDs.clear();
		germplasmMapByUUIDs.put(String.valueOf(groupSourceParentDbId), new GermplasmDto());
		assertTrue(
			this.pedigreeNodesUpdateValidatorUnderTest.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs,
				this.errors, 1));
		Mockito.verifyNoMoreInteractions(this.errors);

	}

	@Test
	public void testValidateGermplasmHasExistingDerivativeProgeny_PedigreeNodeIsDerivative() {
		final int breedingMethodId = this.random.nextInt();
		final int groupSourceParentDbId = this.random.nextInt();
		final int immediateSourceParentDbId = this.random.nextInt();
		final int gid = this.random.nextInt();
		final PedigreeNodeDTO pedigreeNodeDTO =
			this.createPedigreeNodeDerivative(breedingMethodId, groupSourceParentDbId, immediateSourceParentDbId);
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds =
			this.createBreedingMethodMap(breedingMethodId, MethodType.DERIVATIVE);
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();
		final GermplasmDto germplasmDto = new GermplasmDto();
		germplasmDto.setGid(gid);
		germplasmDto.setBreedingMethodId(breedingMethodId);
		germplasmMapByUUIDs.put(String.valueOf(pedigreeNodeDTO.getGermplasmDbId()), germplasmDto);

		final Map<Integer, Integer> germplasmDerivativeProgenyCount = new HashMap<>();

		// Germplasm is derivative but has no existing derivative progeny
		germplasmDerivativeProgenyCount.put(gid, 0);
		assertTrue(
			this.pedigreeNodesUpdateValidatorUnderTest.validateGermplasmHasExistingDerivativeProgeny(pedigreeNodeDTO, germplasmMapByUUIDs,
				breedingMethodDTOMapByIds, germplasmDerivativeProgenyCount, this.errors, 1));
		Mockito.verifyNoMoreInteractions(this.errors);

		// Germplasm is derivative and it has existing derivative progeny
		germplasmDerivativeProgenyCount.put(gid, 1);
		assertFalse(
			this.pedigreeNodesUpdateValidatorUnderTest.validateGermplasmHasExistingDerivativeProgeny(pedigreeNodeDTO, germplasmMapByUUIDs,
				breedingMethodDTOMapByIds, germplasmDerivativeProgenyCount, this.errors, 1));
		Mockito.verify(this.errors)
			.reject("pedigree.nodes.update.germplasm.with.derivative.progeny.cannot.be.updated",
				new String[] {"1"}, "");

	}

	@Test
	public void testValidateGermplasmHasExistingDerivativeProgeny_PedigreeNodeIsGenerative() {
		final int breedingMethodId = this.random.nextInt();
		final int femaleParentDbId = this.random.nextInt();
		final int maleParentDbId = this.random.nextInt();
		final int gid = this.random.nextInt();
		final PedigreeNodeDTO pedigreeNodeDTO =
			this.createPedigreeNodeGenerative(breedingMethodId, femaleParentDbId, maleParentDbId);
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapIds =
			this.createBreedingMethodMap(breedingMethodId, MethodType.GENERATIVE);
		final Map<String, GermplasmDto> germplasmMapByUUIDs = new HashMap<>();
		final GermplasmDto germplasmDto = new GermplasmDto();
		germplasmDto.setGid(gid);
		germplasmDto.setBreedingMethodId(breedingMethodId);
		germplasmMapByUUIDs.put(String.valueOf(pedigreeNodeDTO.getGermplasmDbId()), germplasmDto);

		final Map<Integer, Integer> germplasmDerivativeProgenyCount = new HashMap<>();

		// If germplasm is generative, it sould not check if germplasm has existing derivative progeny
		germplasmDerivativeProgenyCount.put(gid, 0);
		assertTrue(
			this.pedigreeNodesUpdateValidatorUnderTest.validateGermplasmHasExistingDerivativeProgeny(pedigreeNodeDTO, germplasmMapByUUIDs,
				breedingMethodDTOMapIds, germplasmDerivativeProgenyCount, this.errors, 1));
		Mockito.verifyNoMoreInteractions(this.errors);

	}

	private PedigreeNodeDTO createPedigreeNodeGenerative(final Integer breedingMethodId, final Integer parent1, final Integer... parent2) {
		final PedigreeNodeDTO pedigreeNodeDTO = this.createPedigreeNode();
		pedigreeNodeDTO.setBreedingMethodDbId(String.valueOf(breedingMethodId));
		final List<PedigreeNodeReferenceDTO> parents = new LinkedList<>();
		if (parent1 != null) {
			final PedigreeNodeReferenceDTO parentReference1 =
				new PedigreeNodeReferenceDTO(String.valueOf(parent1), "germplasmName", ParentType.FEMALE.name());
			parents.add(parentReference1);
		}
		if (parent2 != null) {
			for (final Integer maleParent : parent2) {
				if (maleParent != null) {
					final PedigreeNodeReferenceDTO maleParentReference =
						new PedigreeNodeReferenceDTO(String.valueOf(maleParent), "germplasmName", ParentType.MALE.name());
					parents.add(maleParentReference);
				}
			}
		}
		pedigreeNodeDTO.setParents(parents);
		return pedigreeNodeDTO;
	}

	private PedigreeNodeDTO createPedigreeNodeDerivative(final Integer breedingMethodId, final Integer parent1, final Integer parent2) {
		final PedigreeNodeDTO pedigreeNodeDTO = this.createPedigreeNode();
		final List<PedigreeNodeReferenceDTO> parents = new LinkedList<>();
		pedigreeNodeDTO.setBreedingMethodDbId(String.valueOf(breedingMethodId));
		if (parent1 != null) {
			final PedigreeNodeReferenceDTO parentReference1 =
				new PedigreeNodeReferenceDTO(String.valueOf(parent1), "germplasmName", ParentType.POPULATION.name());
			parents.add(parentReference1);
		}
		if (parent2 != null) {
			final PedigreeNodeReferenceDTO parentReference2 =
				new PedigreeNodeReferenceDTO(String.valueOf(parent2), "germplasmName", ParentType.SELF.name());
			parents.add(parentReference2);
		}
		pedigreeNodeDTO.setParents(parents);
		return pedigreeNodeDTO;
	}

	private PedigreeNodeDTO createPedigreeNode() {
		final PedigreeNodeDTO pedigreeNodeDTO = new PedigreeNodeDTO();
		pedigreeNodeDTO.setAdditionalInfo(new HashMap<>());
		pedigreeNodeDTO.setBreedingMethodDbId("breedingMethodDbId");
		pedigreeNodeDTO.setBreedingMethodName("breedingMethodName");
		pedigreeNodeDTO.setCrossingProjectDbId("crossingProjectDbId");
		pedigreeNodeDTO.setCrossingYear(2020);
		pedigreeNodeDTO.setDefaultDisplayName("defaultDisplayName");
		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID("referenceID");
		externalReferenceDTO.setReferenceSource("referenceSource");
		externalReferenceDTO.setEntityId("entityId");
		pedigreeNodeDTO.setExternalReferences(Arrays.asList(externalReferenceDTO));
		pedigreeNodeDTO.setFamilyCode("familyCode");
		pedigreeNodeDTO.setGermplasmDbId(String.valueOf(this.random.nextInt()));
		pedigreeNodeDTO.setGermplasmName("germplasmName");
		pedigreeNodeDTO.setGermplasmPUI("germplasmPUI");
		return pedigreeNodeDTO;
	}

	private Map<String, BreedingMethodDTO> createBreedingMethodMap(final Integer breedingMethodId, final MethodType methodType) {
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds = new HashMap<>();
		final BreedingMethodDTO breedingMethodDTO = new BreedingMethodDTO();
		breedingMethodDTO.setMid(breedingMethodId);
		breedingMethodDTO.setType(methodType.getCode());
		breedingMethodDTOMapByIds.put(String.valueOf(breedingMethodId), breedingMethodDTO);
		return breedingMethodDTOMapByIds;
	}

}
