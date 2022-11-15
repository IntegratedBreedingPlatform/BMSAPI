package org.ibp.api.java.impl.middleware.germplasm;

import com.beust.jcommander.internal.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.ruleengine.RuleException;
import org.generationcp.middleware.ruleengine.RuleFactory;
import org.generationcp.middleware.ruleengine.coding.CodingRuleExecutionContext;
import org.generationcp.middleware.ruleengine.service.RulesService;
import org.generationcp.middleware.ruleengine.newnaming.service.GermplasmNamingService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.pojos.naming.NamingConfiguration;
import org.generationcp.middleware.service.api.GermplasmCodingResult;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.service.api.NamingConfigurationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmCodeGenerationServiceImplTest {

	private static final int NAMING_CONFIG_STARTING_SEQUENCE = 11;
	private static final String PREFIX = "ABH";
	private static final String SUFFIX = "CDE";
	private static final Integer NEXT_NUMBER = 3;
	private static final String KEY = "[SEQUENCE]";

	@Mock
	private RulesService rulesService;

	@Mock
	private RuleFactory ruleFactory;

	@Mock
	private GermplasmGroupingService germplasmGroupingService;

	@Mock
	private GermplasmNamingService germplasmNamingService;

	@Mock
	private NamingConfigurationService namingConfigurationService;

	@Mock
	private GermplasmNameService germplasmNameService;

	@Mock
	private GermplasmService germplasmService;

	@InjectMocks
	private final GermplasmCodeGenerationServiceImpl germplasmCodeGenerationService = new GermplasmCodeGenerationServiceImpl();

	private GermplasmNameSetting germplasmNameSetting;
	private NamingConfiguration namingConfiguration;
	private CodingRuleExecutionContext codingRuleExecutionContext;
	private GermplasmNameTypeDTO codeNameType;

	@Before
	public void setUp() throws InvalidGermplasmNameSettingException {
		this.germplasmNameSetting = this.createGermplasmNameSetting();
		this.namingConfiguration = this.createNamingConfiguration();
		this.setupCodeNameType();

		final String nextNameInSequence = this.getExpectedName(NEXT_NUMBER);
		Mockito.doReturn(this.namingConfiguration).when(this.namingConfigurationService).getNamingConfigurationByName(Mockito.any());
		Mockito.doReturn(nextNameInSequence).when(this.germplasmNamingService)
			.generateNextNameAndIncrementSequence(this.germplasmNameSetting);
		Mockito.doReturn(nextNameInSequence).when(this.germplasmNamingService).getNextNameInSequence(this.germplasmNameSetting);
		final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasenames("messages_en");
		this.germplasmCodeGenerationService.setMessageSource(messageSource);
	}

	@Test
	public void testApplyGroupNamesManualNaming() throws InvalidGermplasmNameSettingException {
		final Set<Integer> gids = new HashSet<>(Arrays.asList(1001, 1002, 1003, 1004));
		final Map<Integer, GermplasmDto> germplasmMap = new HashMap<>();
		final List<GermplasmNameDto> germplasmNames = new ArrayList<>();
		int startNumber = NEXT_NUMBER;
		for (final Integer gid : gids) {
			final GermplasmDto germplasmDto = new GermplasmDto();
			germplasmDto.setGid(gid);
			germplasmDto.setGroupId(gid);
			// Setup existing preferred name
			germplasmNames.add(this.createGermplasmNameDto(gid, "CODE2"));
			germplasmMap.put(gid, germplasmDto);
			Mockito.when(this.germplasmService.getGermplasmDtoById(gid)).thenReturn(germplasmDto);
		}

		Mockito.when(this.germplasmNameService.getGermplasmNamesByGids(Mockito.anyList())).thenReturn(germplasmNames);
		Mockito.when(this.germplasmNamingService.generateNextNameAndIncrementSequence(this.germplasmNameSetting))
			.thenReturn(this.getExpectedName(startNumber), this.getExpectedName(startNumber + 1), this.getExpectedName(startNumber + 2),
				this.getExpectedName(startNumber + 3), this.getExpectedName(startNumber + 4), this.getExpectedName(startNumber + 5));

		Mockito.when(this.germplasmNameService.createName(Mockito.any(), Mockito.any())).thenReturn(1);
		final List<GermplasmCodingResult> resultsList =
			this.germplasmCodeGenerationService.applyGroupNamesForManualNaming(gids, this.germplasmNameSetting, this.codeNameType);
		Assert.assertEquals("Expected service to return with " + gids.size() + " naming results, one per germplasm.", gids.size(),
			resultsList.size());

		for (final GermplasmCodingResult germplasmCodingResult : resultsList) {
			final GermplasmDto germplasm = germplasmMap.get(germplasmCodingResult.getGid());
			final String expectedCodedName = PREFIX + " 000000" + (startNumber++) + " " + SUFFIX;

			final ArgumentCaptor<GermplasmNameRequestDto> germplasmNameRequestDtoCaptor =
				ArgumentCaptor.forClass(GermplasmNameRequestDto.class);
			Mockito.verify(this.germplasmNameService)
				.createName(germplasmNameRequestDtoCaptor.capture(), Mockito.eq(germplasmCodingResult.getGid()));

			Assert.assertEquals(
				"Expected germplasm " + germplasmCodingResult.getGid() + " to have a coded name assigned as preferred name.",
				expectedCodedName, germplasmNameRequestDtoCaptor.getValue().getName());
			Assert.assertEquals("Expected germplasm " + germplasmCodingResult.getGid() + " to have a coded name with coded name type.",
				this.codeNameType.getCode(),
				germplasmNameRequestDtoCaptor.getValue().getNameTypeCode());

			Assert.assertEquals(1, germplasmCodingResult.getMessages().size());
			Assert.assertEquals(
				String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", germplasm.getGid(),
					expectedCodedName, this.codeNameType.getCode()), germplasmCodingResult.getMessages().get(0));
		}

	}

	@Test
	public void testApplyGroupNamesAutomaticNaming() throws RuleException {
		final Set<Integer> gids = new HashSet<>(Arrays.asList(1001, 1002, 1003, 1004));
		final Map<Integer, GermplasmDto> germplasmMap = new HashMap<>();
		final List<GermplasmNameDto> germplasmNames = new ArrayList<>();
		Integer startNumber = NAMING_CONFIG_STARTING_SEQUENCE;
		for (final Integer gid : gids) {
			final GermplasmDto germplasmDto = new GermplasmDto();
			germplasmDto.setGid(gid);
			germplasmDto.setGroupId(gid);

			// Setup existing preferred name
			germplasmNames.add(this.createGermplasmNameDto(gid, "CODE2"));
			germplasmMap.put(gid, germplasmDto);

			Mockito.when(this.germplasmService.getGermplasmDtoById(gid)).thenReturn(germplasmDto);
		}
		Mockito.when(this.germplasmNameService.getGermplasmNamesByGids(Mockito.anyList())).thenReturn(germplasmNames);
		Mockito.when(this.ruleFactory.getRuleSequenceForNamespace(GermplasmCodeGenerationServiceImpl.CODING_RULE_SEQUENCE))
			.thenReturn(new String[] {});
		Mockito.when(this.rulesService.runRules(Mockito.any(CodingRuleExecutionContext.class)))
			.thenReturn(PREFIX + NAMING_CONFIG_STARTING_SEQUENCE + SUFFIX, PREFIX + (NAMING_CONFIG_STARTING_SEQUENCE + 1) + SUFFIX,
				PREFIX + (NAMING_CONFIG_STARTING_SEQUENCE + 2) + SUFFIX, PREFIX + (NAMING_CONFIG_STARTING_SEQUENCE + 3) + SUFFIX,
				PREFIX + (NAMING_CONFIG_STARTING_SEQUENCE + 4) + SUFFIX, PREFIX + (NAMING_CONFIG_STARTING_SEQUENCE + 5) + SUFFIX);

		Mockito.when(this.germplasmNameService.createName(Mockito.any(), Mockito.any())).thenReturn(1);
		final List<GermplasmCodingResult> resultsList =
			this.germplasmCodeGenerationService.applyGroupNamesForAutomaticNaming(gids, this.codeNameType);
		Assert.assertEquals("Expected service to return with " + gids.size() + " naming results, one per germplasm.", gids.size(),
			resultsList.size());

		for (final GermplasmCodingResult germplasmCodingResult : resultsList) {
			final GermplasmDto germplasmDto = germplasmMap.get(germplasmCodingResult.getGid());
			final String expectedCodedName = PREFIX + (startNumber++) + SUFFIX;

			final ArgumentCaptor<GermplasmNameRequestDto> germplasmNameRequestDtoCaptor =
				ArgumentCaptor.forClass(GermplasmNameRequestDto.class);
			Mockito.verify(this.germplasmNameService)
				.createName(germplasmNameRequestDtoCaptor.capture(), Mockito.eq(germplasmCodingResult.getGid()));
			Assert.assertEquals(
				"Expected germplasm " + germplasmCodingResult.getGid() + " to have a coded name assigned as preferred name.",
				expectedCodedName,
				germplasmNameRequestDtoCaptor.getValue().getName());
			Assert.assertEquals("Expected germplasm " + germplasmCodingResult.getGid() + " to have a coded name with coded name type.",
				this.codeNameType.getCode(),
				germplasmNameRequestDtoCaptor.getValue().getNameTypeCode());
			Assert.assertEquals(1, germplasmCodingResult.getMessages().size());
			Assert.assertEquals(
				String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", germplasmDto.getGid(),
					expectedCodedName, this.codeNameType.getCode()), germplasmCodingResult.getMessages().get(0));
		}

	}

	@Test
	public void testApplyGroupName_GermplasmIsNotFixed() {
		final GermplasmDto germplasmDto1 = new GermplasmDto();
		germplasmDto1.setGid(1);

		Mockito.when(this.germplasmService.getGermplasmDtoById(germplasmDto1.getGid())).thenReturn(germplasmDto1);

		final GermplasmCodingResult result =
			this.germplasmCodeGenerationService
				.applyGroupName(germplasmDto1.getGid(), this.codeNameType, RandomStringUtils.randomAlphabetic(10));
		Assert.assertEquals("Expected service to return with one validation message regarding germplasm not being fixed.", 1,
			result.getMessages().size());
		Assert.assertTrue("Expected service to return with validation regarding germplasm not being fixed.",
			result.getMessages().contains("Germplasm (gid: 1) is not part of a management group. Can not assign group name."));
	}

	@Test
	public void testApplyGroupName_GermplasmIsFixedAndHasGroupMembers() {

		final List<GermplasmNameDto> germplasmNames = new ArrayList<>();
		final Integer mgid = 1;

		final GermplasmDto g1 = new GermplasmDto();
		g1.setGid(1);
		g1.setGroupId(mgid);

		// Setup existing preferred name
		germplasmNames.add(this.createGermplasmNameDto(g1.getGid(), "CODE2"));

		Mockito.when(this.germplasmService.getGermplasmDtoById(g1.getGid())).thenReturn(g1);

		final GermplasmDto g2 = new GermplasmDto();
		g2.setGid(2);
		g2.setGroupId(mgid);

		final GermplasmDto g3 = new GermplasmDto();
		g3.setGid(3);
		g3.setGroupId(mgid);

		Mockito.when(this.germplasmNameService.getGermplasmNamesByGids(Mockito.anyList())).thenReturn(germplasmNames);
		Mockito.when(this.germplasmGroupingService.getDescendantGroupMembersGids(g1.getGid(), mgid)).thenReturn(
			Lists.newArrayList(g2.getGid(), g3.getGid()));
		Mockito.when(this.germplasmNameService.createName(Mockito.any(), Mockito.any())).thenReturn(1);

		final String expectedCodedName = PREFIX + " 000000" + NEXT_NUMBER + " " + SUFFIX;

		final GermplasmCodingResult result =
			this.germplasmCodeGenerationService.applyGroupName(g1.getGid(), this.codeNameType, expectedCodedName);

		final ArgumentCaptor<GermplasmNameRequestDto> germplasmNameRequestDtoCaptor =
			ArgumentCaptor.forClass(GermplasmNameRequestDto.class);
		Mockito.verify(this.germplasmNameService).createName(germplasmNameRequestDtoCaptor.capture(), Mockito.eq(g1.getGid()));

		Assert.assertEquals("Expected service to return with 3 messages, one per group member.", 3, result.getMessages().size());

		Assert.assertEquals("Expected germplasm g1 to have a coded name assigned as preferred name.", expectedCodedName,
			germplasmNameRequestDtoCaptor.getValue().getName());
		Assert.assertEquals("Expected germplasm g1 to have a coded name with coded name type.", this.codeNameType.getCode(),
			germplasmNameRequestDtoCaptor.getValue().getNameTypeCode());
		Assert.assertTrue(result.getMessages()
			.contains(String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", g1.getGid(),
				expectedCodedName, this.codeNameType.getCode())));

		Mockito.verify(this.germplasmNameService).createName(germplasmNameRequestDtoCaptor.capture(), Mockito.eq(g2.getGid()));

		Assert.assertEquals("Expected germplasm g2 to have a coded name assigned.", expectedCodedName,
			germplasmNameRequestDtoCaptor.getValue().getName());
		Assert.assertEquals("Expected germplasm g2 to have a coded name with coded name type.", this.codeNameType.getCode(),
			germplasmNameRequestDtoCaptor.getValue().getNameTypeCode());
		Assert.assertTrue(result.getMessages()
			.contains(String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", g2.getGid(),
				expectedCodedName, this.codeNameType.getCode())));

		Mockito.verify(this.germplasmNameService).createName(germplasmNameRequestDtoCaptor.capture(), Mockito.eq(g3.getGid()));

		Assert.assertEquals("Expected germplasm g3 to have a coded name assigned.", expectedCodedName,
			germplasmNameRequestDtoCaptor.getValue().getName());
		Assert.assertEquals("Expected germplasm g3 to have a coded name with coded name type.", this.codeNameType.getCode(),
			germplasmNameRequestDtoCaptor.getValue().getNameTypeCode());
		Assert.assertTrue(result.getMessages()
			.contains(String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", g3.getGid(),
				expectedCodedName, this.codeNameType.getCode())));
	}

	@Test
	public void testApplyGroupName_GermplasmIsFixedAndHasGroupMembersWithExistingCodedNames() {
		final Integer mgid = 1;

		final GermplasmDto g1 = new GermplasmDto();
		g1.setGid(1);
		g1.setGroupId(mgid);

		Mockito.when(this.germplasmService.getGermplasmDtoById(g1.getGid())).thenReturn(g1);

		final GermplasmDto g2 = new GermplasmDto();
		g2.setGid(2);
		g2.setGroupId(mgid);

		final GermplasmDto g3 = new GermplasmDto();
		g3.setGid(3);
		g3.setGroupId(mgid);

		// Lets setup the third member with existing coded name.
		final List<GermplasmNameDto> germplasmNames = new ArrayList<>();
		germplasmNames.add(this.createGermplasmNameDto(g3.getGid(), "CODE1"));

		Mockito.when(this.germplasmNameService.getGermplasmNamesByGids(Mockito.anyList())).thenReturn(germplasmNames);
		Mockito.when(this.germplasmGroupingService.getDescendantGroupMembersGids(g1.getGid(), mgid))
			.thenReturn(Lists.newArrayList(g2.getGid(), g3.getGid()));
		Mockito.when(this.germplasmNameService.createName(Mockito.any(), Mockito.any())).thenReturn(1);

		final String expectedCodedName = PREFIX + " 000000" + NEXT_NUMBER + " " + SUFFIX;

		final GermplasmCodingResult result =
			this.germplasmCodeGenerationService.applyGroupName(g1.getGid(), this.codeNameType, expectedCodedName);
		Assert.assertEquals("Expected service to return with 3 messages, one per group member.", 3, result.getMessages().size());

		final ArgumentCaptor<GermplasmNameRequestDto> germplasmNameRequestDtoCaptor =
			ArgumentCaptor.forClass(GermplasmNameRequestDto.class);
		Mockito.verify(this.germplasmNameService).createName(germplasmNameRequestDtoCaptor.capture(), Mockito.eq(g1.getGid()));

		Assert.assertEquals("Expected germplasm g1 to have a coded name assigned as preferred name.", expectedCodedName,
			germplasmNameRequestDtoCaptor.getValue().getName());
		Assert.assertEquals("Expected germplasm g1 to have a coded name with coded name type.", this.codeNameType.getCode(),
			germplasmNameRequestDtoCaptor.getValue().getNameTypeCode());
		Assert.assertTrue(result.getMessages()
			.contains(String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", g1.getGid(),
				expectedCodedName, this.codeNameType.getCode())));

		Mockito.verify(this.germplasmNameService).createName(germplasmNameRequestDtoCaptor.capture(), Mockito.eq(g2.getGid()));

		Assert.assertEquals("Expected germplasm g2 to have a coded name assigned.", expectedCodedName,
			germplasmNameRequestDtoCaptor.getValue().getName());
		Assert.assertEquals("Expected germplasm g2 to have a coded name with coded name type.", this.codeNameType.getCode(),
			germplasmNameRequestDtoCaptor.getValue().getNameTypeCode());
		Assert.assertTrue(result.getMessages()
			.contains(String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", g2.getGid(),
				expectedCodedName, this.codeNameType.getCode())));
		Mockito.verify(this.germplasmNameService, Mockito.times(0))
			.createName(germplasmNameRequestDtoCaptor.capture(), Mockito.eq(g3.getGid()));

		Assert.assertTrue(
			"Expected service to return with validation regarding germplasm g3 not assigned given name because it already has one with same type.",
			result.getMessages().contains(
				"Germplasm (gid: 3) already has existing name Name G-3 of type CODE1. Supplied name "
					+ expectedCodedName + " was not added."));
	}

	private GermplasmNameDto createGermplasmNameDto(final Integer gid, final String nameTypeCode) {
		final GermplasmNameDto germplasmNameDto = new GermplasmNameDto();
		germplasmNameDto.setGid(gid);
		germplasmNameDto.setName("Name G-" + gid);
		germplasmNameDto.setPreferred(true);
		germplasmNameDto.setNameTypeCode(nameTypeCode);
		return germplasmNameDto;
	}

	private NamingConfiguration createNamingConfiguration() {
		final NamingConfiguration namingConfiguration = new NamingConfiguration();
		namingConfiguration.setPrefix(GermplasmCodeGenerationServiceImplTest.PREFIX);
		namingConfiguration.setSuffix(GermplasmCodeGenerationServiceImplTest.SUFFIX);
		namingConfiguration.setCount(GermplasmCodeGenerationServiceImplTest.KEY);
		return namingConfiguration;
	}

	private void setupCodeNameType() {
		this.codeNameType = new GermplasmNameTypeDTO();
		this.codeNameType.setId(41);
		this.codeNameType.setCode("CODE1");
	}

	private GermplasmNameSetting createGermplasmNameSetting() {
		final GermplasmNameSetting setting = new GermplasmNameSetting();

		setting.setPrefix(GermplasmCodeGenerationServiceImplTest.PREFIX);
		setting.setSuffix(GermplasmCodeGenerationServiceImplTest.SUFFIX);
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);
		setting.setNumOfDigits(7);

		return setting;
	}

	private String getExpectedName(final Integer number) {
		return GermplasmCodeGenerationServiceImplTest.PREFIX + " 000000" + number + " " + GermplasmCodeGenerationServiceImplTest.SUFFIX;
	}
}
