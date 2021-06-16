package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.coding.CodingRuleExecutionContext;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.generationcp.commons.service.GermplasmNamingService;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.domain.germplasm.GermplasmCodeNameBatchRequestDto;
import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.pojos.naming.NamingConfiguration;
import org.generationcp.middleware.service.api.GermplasmCodingResult;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.service.api.NamingConfigurationService;
import org.generationcp.middleware.util.Util;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.germplasm.GermplasmCodeGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service to generate Code Names (aka. Group Names)
 */
@Transactional
public class GermplasmCodeGenerationServiceImpl implements GermplasmCodeGenerationService {

	private static final String GERMPLASM_NOT_PART_OF_MANAGEMENT_GROUP =
		"Germplasm (gid: %s) is not part of a management group. Can not assign group name.";
	protected static final String CODING_RULE_SEQUENCE = "coding";

	@Autowired
	private RulesService rulesService;

	@Autowired
	private RuleFactory ruleFactory;

	@Autowired
	private GermplasmGroupingService germplasmGroupingService;

	@Autowired
	private GermplasmNamingService germplasmNamingService;

	@Autowired
	private GermplasmNameTypeService germplasmNameTypeService;

	@Autowired
	private NamingConfigurationService namingConfigurationService;

	@Autowired
	private GermplasmService germplasmService;

	public GermplasmCodeGenerationServiceImpl() {
		// do nothing
	}

	@Override
	public List<GermplasmCodingResult> createCodeNames(final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto) {

		final Optional<GermplasmNameTypeDTO> germplasmNameTypeDTO =
			this.germplasmNameTypeService.getNameTypeByCode(germplasmCodeNameBatchRequestDto.getNameType());

		if (germplasmNameTypeDTO.isPresent()) {
			try {
				// For manual code naming
				if (germplasmCodeNameBatchRequestDto.getGermplasmCodeNameSetting() != null) {
					return this.applyGroupNamesForManualNaming(new HashSet<>(germplasmCodeNameBatchRequestDto.getGids()),
						germplasmCodeNameBatchRequestDto.getGermplasmCodeNameSetting(), germplasmNameTypeDTO.get());
				} else {
					// For automatic code naming
					return this.applyGroupNamesForAutomaticNaming(new HashSet<>(germplasmCodeNameBatchRequestDto.getGids()),
						germplasmNameTypeDTO.get());
				}
			} catch (final Exception e) {
				throw new ApiRuntimeException("An error has occurred when trying generate code names", e);
			}
		}
		return Collections.emptyList();
	}

	protected List<GermplasmCodingResult> applyGroupNamesForAutomaticNaming(final Set<Integer> gidsToProcess,
		final GermplasmNameTypeDTO nameType) throws RuleException {

		final NamingConfiguration namingConfiguration = this.namingConfigurationService.getNamingConfigurationByName(nameType.getName());

		final List<String> executionOrder = Arrays.asList(this.ruleFactory.getRuleSequenceForNamespace(CODING_RULE_SEQUENCE));
		final CodingRuleExecutionContext codingRuleExecutionContext = new CodingRuleExecutionContext(executionOrder, namingConfiguration);
		final List<GermplasmCodingResult> assignCodesResultsList = new ArrayList<>();

		for (final Integer gid : gidsToProcess) {
			final String generatedCodeName = (String) this.rulesService.runRules(codingRuleExecutionContext);
			assignCodesResultsList.add(this.applyGroupName(gid, nameType, generatedCodeName));
			codingRuleExecutionContext.reset();
		}

		return assignCodesResultsList;
	}

	protected List<GermplasmCodingResult> applyGroupNamesForManualNaming(final Set<Integer> gids, final GermplasmNameSetting setting,
		final GermplasmNameTypeDTO nameType) throws InvalidGermplasmNameSettingException {
		final List<GermplasmCodingResult> assignCodesResultsList = new ArrayList<>();
		final boolean startNumberSpecified = setting.getStartNumber() != null;
		Integer startNumber = setting.getStartNumber();

		// Call this method to check first if the name settings are valid.
		this.germplasmNamingService.getNextNameInSequence(setting);

		for (final Integer gid : gids) {
			// Increment start number of succeeding germplasm processed based on initial start # specified, if any
			if (startNumberSpecified) {
				setting.setStartNumber(startNumber++);
			}
			final String nameWithSequence = this.germplasmNamingService.generateNextNameAndIncrementSequence(setting);
			final GermplasmCodingResult result = this.applyGroupName(gid, nameType, nameWithSequence);
			assignCodesResultsList.add(result);
		}
		return assignCodesResultsList;
	}

	protected GermplasmCodingResult applyGroupName(final Integer gid,
		final GermplasmNameTypeDTO nameType, final String generatedCodeName) {

		final GermplasmCodingResult result = new GermplasmCodingResult();
		result.setGid(gid);

		final Germplasm germplasm = this.germplasmService.getGermplasmByGID(gid);

		if (germplasm.getMgid() == null || germplasm.getMgid() == 0) {
			result.addMessage(String.format(GERMPLASM_NOT_PART_OF_MANAGEMENT_GROUP, germplasm.getGid()));
			return result;
		}

		final List<Germplasm> groupMembers =
			this.germplasmGroupingService.getDescendantGroupMembers(germplasm.getGid(), germplasm.getMgid());
		groupMembers.add(0, germplasm);

		for (final Germplasm member : groupMembers) {
			this.addName(member, generatedCodeName, nameType, result);
		}

		return result;

	}

	private void addName(final Germplasm germplasm, final String groupName, final GermplasmNameTypeDTO nameType,
		final GermplasmCodingResult result) {

		final List<Name> currentNames = germplasm.getNames();

		Name existingNameOfGivenType = null;
		if (!currentNames.isEmpty() && nameType != null) {
			for (final Name name : currentNames) {
				if (nameType.getId().equals(name.getTypeId())) {
					existingNameOfGivenType = name;
					break;
				}
			}
		}

		if (existingNameOfGivenType == null) {
			// Make the current preferred name as non-preferred by setting nstat = 0
			final Name currentPreferredName = germplasm.findPreferredName();
			if (currentPreferredName != null) {
				// nstat = 0 means it is not a preferred name.
				currentPreferredName.setNstat(0);
			}

			final Name name = new Name();
			name.setGermplasm(germplasm);
			name.setTypeId(nameType.getId());
			name.setNval(groupName);
			// nstat = 1 means it is preferred name.
			name.setNstat(1);
			// Hard coded to zero for now.
			name.setLocationId(0);
			name.setNdate(Util.getCurrentDateAsIntegerValue());
			name.setReferenceId(0);

			germplasm.getNames().add(name);
			this.germplasmService.saveGermplasm(germplasm);
			result.addMessage(
				String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", germplasm.getGid(),
					groupName, nameType.getCode()));
		} else {
			result.addMessage(String.format("Germplasm (gid: %s) already has existing name %s of type %s. Supplied name %s was not added.",
				germplasm.getGid(), existingNameOfGivenType.getNval(), nameType.getCode(), groupName));
		}
	}

}
