package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.ruleengine.RuleException;
import org.generationcp.middleware.ruleengine.RuleFactory;
import org.generationcp.middleware.ruleengine.coding.CodingRuleExecutionContext;
import org.generationcp.middleware.ruleengine.service.RulesService;
import org.generationcp.middleware.ruleengine.newnaming.service.GermplasmNamingService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.domain.germplasm.GermplasmCodeNameBatchRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.pojos.naming.NamingConfiguration;
import org.generationcp.middleware.service.api.GermplasmCodingResult;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.service.api.NamingConfigurationService;
import org.generationcp.middleware.util.Util;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.germplasm.GermplasmCodeGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service to generate Code Names (aka. Group Names)
 */
@Transactional
public class GermplasmCodeGenerationServiceImpl implements GermplasmCodeGenerationService {

	protected static final String CODING_RULE_SEQUENCE = "coding";
	protected static final String GERMPLASM_CODE_NAME_GERMPLASM_NOT_PART_OF_MANAGEMENT_GROUP =
		"germplasm.code.name.germplasm.not.part.of.management.group";
	protected static final String GERMPLASM_CODE_NAME_GERMPLASM_HAS_EXISTING_NAME_FOR_NAME_TYPE =
		"germplasm.code.name.germplasm.has.existing.name.for.name.type";
	protected static final String GERMPLASM_CODE_NAME_SUCCESSFULLY_ASSIGNED_NAME = "germplasm.code.name.successfully.assigned.name";

	@Autowired
	private ResourceBundleMessageSource messageSource;

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
	private GermplasmNameService germplasmNameService;

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
					return this.applyGroupNamesForManualNaming(new LinkedHashSet<>(germplasmCodeNameBatchRequestDto.getGids()),
						germplasmCodeNameBatchRequestDto.getGermplasmCodeNameSetting(), germplasmNameTypeDTO.get());
				} else {
					// For automatic code naming
					return this.applyGroupNamesForAutomaticNaming(new LinkedHashSet<>(germplasmCodeNameBatchRequestDto.getGids()),
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
		final Optional<Integer> startNumberOptional = Optional.ofNullable(setting.getStartNumber());
		// Call this method to check first if the name settings are valid.
		this.germplasmNamingService.getNextNameInSequence(setting);

		for (final Integer gid : gids) {
			// Increment start number of succeeding germplasm processed based on initial start # specified, if any
			if (startNumberOptional.isPresent()) {
				int startNumber = setting.getStartNumber();
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

		final GermplasmDto germplasm = this.germplasmService.getGermplasmDtoById(gid);

		if (germplasm.getGroupId() == null || germplasm.getGroupId() == 0) {
			result.addMessage(this.messageSource
				.getMessage(GERMPLASM_CODE_NAME_GERMPLASM_NOT_PART_OF_MANAGEMENT_GROUP, new String[] {String.valueOf(germplasm.getGid())},
					LocaleContextHolder.getLocale()));
			return result;
		}

		final List<Integer> groupMembersGids =
			this.germplasmGroupingService.getDescendantGroupMembersGids(germplasm.getGid(), germplasm.getGroupId());

		groupMembersGids.add(germplasm.getGid());

		final Map<Integer, List<GermplasmNameDto>> germplasmNameMapByGids =
			this.germplasmNameService.getGermplasmNamesByGids(groupMembersGids).stream()
				.collect(Collectors.groupingBy(GermplasmNameDto::getGid));

		for (final Integer groupMemberGid : groupMembersGids) {
			final Optional<GermplasmNameDto> existingGermplasmNameDto =
				this.getGermplasmNameDtoByType(germplasmNameMapByGids.get(groupMemberGid), nameType.getCode());
			if (!existingGermplasmNameDto.isPresent()) {
				this.addName(groupMemberGid, generatedCodeName, nameType, result);
			} else {
				result.addMessage(this.messageSource
					.getMessage(GERMPLASM_CODE_NAME_GERMPLASM_HAS_EXISTING_NAME_FOR_NAME_TYPE,
						new String[] {
							String.valueOf(groupMemberGid), existingGermplasmNameDto.get().getName(), nameType.getCode(),
							generatedCodeName},
						LocaleContextHolder.getLocale()));

			}
		}

		return result;

	}

	private Optional<GermplasmNameDto> getGermplasmNameDtoByType(final List<GermplasmNameDto> germplasmNameDtoList,
		final String nameTypeCode) {
		if (!CollectionUtils.isEmpty(germplasmNameDtoList)) {
			return germplasmNameDtoList.stream().filter(germplasmNameDto -> germplasmNameDto.getNameTypeCode().equals(nameTypeCode))
				.findAny();
		}
		return Optional.empty();
	}

	private void addName(final Integer gid, final String groupName, final GermplasmNameTypeDTO nameType,
		final GermplasmCodingResult result) {
		final GermplasmNameRequestDto germplasmNameRequestDto = new GermplasmNameRequestDto();
		germplasmNameRequestDto.setName(groupName);
		germplasmNameRequestDto.setNameTypeCode(nameType.getCode());
		germplasmNameRequestDto.setPreferredName(true);
		germplasmNameRequestDto.setLocationId(0);
		germplasmNameRequestDto.setDate(String.valueOf(Util.getCurrentDateAsIntegerValue()));
		this.germplasmNameService.createName(germplasmNameRequestDto, gid);
		result.addMessage(this.messageSource
			.getMessage(GERMPLASM_CODE_NAME_SUCCESSFULLY_ASSIGNED_NAME,
				new String[] {
					String.valueOf(gid),
					groupName, nameType.getCode()},
				LocaleContextHolder.getLocale()));
	}

	public void setMessageSource(final ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
