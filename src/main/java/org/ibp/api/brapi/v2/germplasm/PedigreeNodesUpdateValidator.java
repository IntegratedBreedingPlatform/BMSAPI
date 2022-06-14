package org.ibp.api.brapi.v2.germplasm;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class PedigreeNodesUpdateValidator {

	private static final String GENERATIVE = MethodType.GENERATIVE.getCode();

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private org.generationcp.middleware.api.germplasm.GermplasmService germplasmMiddlewareService;

	public BindingResult prunePedigreeNodesForUpdate(final Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), PedigreeNodeDTO.class.getName());

		if (!this.validateEmptyMap(errors, pedigreeNodeDTOMap)) {
			return errors;
		}

		final Map<String, GermplasmDto> germplasmMapByUUIDs = this.getGermplasmMapByUUIDs(pedigreeNodeDTOMap);
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds = this.getBreedingMethodMapByIds(pedigreeNodeDTOMap);

		final List<PedigreeNodeDTO> pedigreeNodeDTOList = new ArrayList<>(pedigreeNodeDTOMap.values());
		final Map<PedigreeNodeDTO, Integer> indexMap = IntStream.range(0, pedigreeNodeDTOMap.size()).boxed()
			.collect(Collectors.toMap(pedigreeNodeDTOList::get, i -> i));

		pedigreeNodeDTOMap.entrySet().removeIf(entry -> {

			final Integer index = indexMap.get(entry.getValue()) + 1;

			final String germplasmDbIdMapKey = entry.getKey();
			final PedigreeNodeDTO pedigreeNodeDTO = entry.getValue();
			return !this.validateGermplasmDbId(germplasmDbIdMapKey, pedigreeNodeDTO, germplasmMapByUUIDs, errors, index)
				|| !this.validateBreedingMethod(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs, errors, index)
				|| !this.validateParents(pedigreeNodeDTO, breedingMethodDTOMapByIds, germplasmMapByUUIDs, errors, index);
		});

		return errors;

	}

	private Map<String, BreedingMethodDTO> getBreedingMethodMapByIds(final Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap) {
		final List<Integer> breedingMethodsDbIds =
			pedigreeNodeDTOMap.values().stream().filter(pedigreeNodeDTO ->
					StringUtils.isNotBlank(pedigreeNodeDTO.getBreedingMethodDbId()))
				.map(pedigreeNodeDTO -> StringUtils.isNumeric(pedigreeNodeDTO.getBreedingMethodDbId()) ?
					Integer.valueOf(pedigreeNodeDTO.getBreedingMethodDbId()) : null).collect(Collectors.toList());

		final BreedingMethodSearchRequest breedingMethodSearchRequest = new BreedingMethodSearchRequest();
		breedingMethodSearchRequest.setMethodIds(breedingMethodsDbIds);
		return this.breedingMethodService.searchBreedingMethods(breedingMethodSearchRequest, null, null)
			.stream().collect(Collectors.toMap(breedingMethodDTO -> String.valueOf(breedingMethodDTO.getMid()), Function.identity()));
	}

	private Map<String, GermplasmDto> getGermplasmMapByUUIDs(final Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap) {

		// Extract the germplasmDbIds of the germplasm to be updated
		final Set<String> germplasmUUIDs =
			pedigreeNodeDTOMap.values().stream().map(dto -> StringUtils.isNotEmpty(dto.getGermplasmDbId()) ? dto.getGermplasmDbId() : null)
				.filter(
					Objects::nonNull).collect(Collectors.toSet());
		// Extract the germplasmDbIds of the parents
		final Set<String>
			progenitorGermplasmUUIDs =
			pedigreeNodeDTOMap.values().stream().map(PedigreeNodeDTO::getParents).filter(Objects::nonNull).collect(Collectors.toList())
				.stream().flatMap(Collection::stream).map(PedigreeNodeReferenceDTO::getGermplasmDbId).filter(Objects::nonNull).collect(
					Collectors.toSet());

		final List<String> allUUIDs = new ArrayList<>();
		allUUIDs.addAll(germplasmUUIDs);
		allUUIDs.addAll(progenitorGermplasmUUIDs);

		final GermplasmMatchRequestDto germplasmMatchRequestDto = new GermplasmMatchRequestDto();
		germplasmMatchRequestDto.setGermplasmUUIDs(allUUIDs);
		return this.germplasmService.findGermplasmMatches(germplasmMatchRequestDto, null).stream().collect(
			Collectors.toMap(GermplasmDto::getGermplasmUUID, Function.identity()));
	}

	protected boolean validateEmptyMap(final BindingResult errors, final Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap) {
		if (pedigreeNodeDTOMap == null || pedigreeNodeDTOMap.isEmpty()) {
			errors.reject("pedigree.nodes.update.empty.map", "");
			return false;
		}
		return true;
	}

	protected boolean validateGermplasmDbId(final String mapKey, final PedigreeNodeDTO pedigreeNodeDTO,
		final Map<String, GermplasmDto> germplasmMapByUUIDs, final BindingResult errors, final int index) {

		if (StringUtils.isBlank(mapKey)) {
			errors.reject("pedigree.nodes.update.missing.key", new String[] {String.valueOf(index)}, "");
			return false;
		}

		if (StringUtils.isBlank(pedigreeNodeDTO.getGermplasmDbId())) {
			errors.reject("pedigree.nodes.update.missing.germplasmdbid", new String[] {String.valueOf(index)}, "");
			return false;
		}

		if (!mapKey.equals(pedigreeNodeDTO.getGermplasmDbId())) {
			errors.reject("pedigree.nodes.update.key.germplasmdbid.mismatch", new String[] {String.valueOf(index)}, "");
			return false;
		}

		if (!germplasmMapByUUIDs.containsKey(pedigreeNodeDTO.getGermplasmDbId())) {
			errors.reject("pedigree.nodes.update.invalid.germplasmdbid",
				new String[] {String.valueOf(index), pedigreeNodeDTO.getGermplasmDbId()}, "");
			return false;
		}
		return true;
	}

	protected boolean validateBreedingMethod(final PedigreeNodeDTO pedigreeNodeDTO,
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds, final Map<String, GermplasmDto> germplasmMapByUUIDs,
		final BindingResult errors, final int index) {
		if (StringUtils.isBlank(pedigreeNodeDTO.getBreedingMethodDbId())) {
			errors.reject("pedigree.nodes.update.missing.breedingmethoddbid", new String[] {String.valueOf(index)}, "");
			return false;
		}
		if (!breedingMethodDTOMapByIds.containsKey(pedigreeNodeDTO.getBreedingMethodDbId())) {
			errors.reject("pedigree.nodes.update.invalid.breedingmethoddbid",
				new String[] {String.valueOf(index), pedigreeNodeDTO.getBreedingMethodDbId()}, "");
			return false;
		}
		if (!pedigreeNodeDTO.getBreedingMethodDbId()
			.equals(germplasmMapByUUIDs.get(pedigreeNodeDTO.getGermplasmDbId()).getBreedingMethodId().toString())) {
			errors.reject("pedigree.nodes.update.breedingmethoddbid.do.not.match.the.existing.germplasm.breeding.method",
				new String[] {String.valueOf(index), pedigreeNodeDTO.getBreedingMethodDbId()}, "");
			return false;
		}
		return true;
	}

	protected boolean validateParents(final PedigreeNodeDTO pedigreeNodeDTO,
		final Map<String, BreedingMethodDTO> breedingMethodDTOMapByIds, final Map<String, GermplasmDto> germplasmMapByUUIDs,
		final BindingResult errors, final int index) {
		if (CollectionUtils.isEmpty(pedigreeNodeDTO.getParents())) {
			errors.reject("pedigree.nodes.update.missing.parents", new String[] {String.valueOf(index)}, "");
			return false;
		}

		if (pedigreeNodeDTO.getParents().stream().anyMatch(p -> ParentType.fromString(p.getParentType()) == null)) {
			errors.reject("pedigree.nodes.invalid.parent.type", new String[] {String.valueOf(index)}, "");
			return false;
		}

		final BreedingMethodDTO breedingMethodDTO = breedingMethodDTOMapByIds.get(pedigreeNodeDTO.getBreedingMethodDbId());

		if (GENERATIVE.equals(breedingMethodDTO.getType())) {
			return this.validateGenerativeParents(pedigreeNodeDTO, germplasmMapByUUIDs, errors, index);
		} else {
			return this.validateDerivativeParents(pedigreeNodeDTO, germplasmMapByUUIDs, errors, index);
		}
	}

	private boolean validateDerivativeParents(final PedigreeNodeDTO pedigreeNodeDTO, final Map<String, GermplasmDto> germplasmMapByUUIDs,
		final BindingResult errors, final int index) {
		final List<PedigreeNodeReferenceDTO> populationParent =
			pedigreeNodeDTO.getParents().stream().filter(dto -> ParentType.POPULATION.name().equals(dto.getParentType()))
				.collect(Collectors.toList());
		final List<PedigreeNodeReferenceDTO> selfParent =
			pedigreeNodeDTO.getParents().stream().filter(dto -> ParentType.SELF.name().equals(dto.getParentType()))
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(populationParent) || CollectionUtils.isEmpty(selfParent)) {
			errors.reject("pedigree.nodes.update.both.group.source.and.immediate.source.parents.must.be.specified",
				new String[] {String.valueOf(index)}, "");
			return false;
		}
		if (populationParent.size() > 1) {
			errors.reject("pedigree.nodes.update.only.one.population.parent.can.be.specified", new String[] {String.valueOf(index)}, "");
			return false;
		}
		if (selfParent.size() > 1) {
			errors.reject("pedigree.nodes.update.only.one.self.parent.can.be.specified", new String[] {String.valueOf(index)}, "");
			return false;
		}
		if (!germplasmMapByUUIDs.containsKey(populationParent.get(0).getGermplasmDbId())) {
			errors.reject("pedigree.nodes.update.population.parent.invalid.germplasmdbid",
				new String[] {String.valueOf(index), populationParent.get(0).getGermplasmDbId()}, "");
			return false;
		}
		if (!germplasmMapByUUIDs.containsKey(selfParent.get(0).getGermplasmDbId())) {
			errors.reject("pedigree.nodes.update.self.parent.invalid.germplasmdbid",
				new String[] {String.valueOf(index), selfParent.get(0).getGermplasmDbId()}, "");
			return false;
		}
		return true;
	}

	private boolean validateGenerativeParents(final PedigreeNodeDTO pedigreeNodeDTO, final Map<String, GermplasmDto> germplasmMapByUUIDs,
		final BindingResult errors, final int index) {
		final List<PedigreeNodeReferenceDTO> femaleParent =
			pedigreeNodeDTO.getParents().stream().filter(dto -> ParentType.FEMALE.name().equals(dto.getParentType()))
				.collect(Collectors.toList());
		final List<PedigreeNodeReferenceDTO> maleParents =
			pedigreeNodeDTO.getParents().stream().filter(dto -> ParentType.MALE.name().equals(dto.getParentType()))
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(femaleParent) || CollectionUtils.isEmpty(maleParents)) {
			errors.reject("pedigree.nodes.update.both.female.and.male.parents.must.be.specified", new String[] {String.valueOf(index)}, "");
			return false;
		}
		if (femaleParent.size() > 1) {
			errors.reject("pedigree.nodes.update.only.one.female.parent.can.be.specified", new String[] {String.valueOf(index)}, "");
			return false;
		}
		if (!germplasmMapByUUIDs.containsKey(femaleParent.get(0).getGermplasmDbId())) {
			errors.reject("pedigree.nodes.update.female.parent.invalid.germplasmdbid",
				new String[] {String.valueOf(index), femaleParent.get(0).getGermplasmDbId()}, "");
			return false;
		}
		boolean isMaleParentInvalidGermplasmDbId = false;
		boolean isMaleParentGermplasmDbIdIsSameAsTheGermplasm = false;
		for (final PedigreeNodeReferenceDTO maleParent : maleParents) {
			if (!germplasmMapByUUIDs.containsKey(maleParent.getGermplasmDbId())) {
				errors.reject("pedigree.nodes.update.male.parent.invalid.germplasmdbid",
					new String[] {String.valueOf(index), maleParent.getGermplasmDbId()}, "");
				isMaleParentInvalidGermplasmDbId = true;
			}
			if (maleParent.getGermplasmDbId().equals(pedigreeNodeDTO.getGermplasmDbId())) {
				errors.reject("pedigree.nodes.update.male.progenitors.can.not.be.equals.to.germplasmdbid",
					new String[] {String.valueOf(index), maleParent.getGermplasmDbId()}, "");
				isMaleParentGermplasmDbIdIsSameAsTheGermplasm = true;
			}
		}
		if (isMaleParentInvalidGermplasmDbId) {
			return false;
		}
		if (isMaleParentGermplasmDbIdIsSameAsTheGermplasm) {
			return false;
		}
		return true;
	}

}
