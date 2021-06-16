package org.ibp.api.java.impl.middleware.germplasm;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.coding.CodingRuleExecutionContext;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.generationcp.commons.service.GermplasmNamingService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.dao.NamingConfigurationDAO;
import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.pojos.naming.NamingConfiguration;
import org.generationcp.middleware.service.api.GermplasmCodingResult;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

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
	private NamingConfigurationDAO namingConfigurationDAO;

	@Mock
	private GermplasmGroupingService germplasmGroupingService;

	@Mock
	private GermplasmNamingService germplasmNamingService;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@InjectMocks
	private final GermplasmCodeGenerationServiceImpl germplasmCodeGenerationService = new GermplasmCodeGenerationServiceImpl();

	private GermplasmNameSetting germplasmNameSetting;
	private NamingConfiguration namingConfiguration;
	private CodingRuleExecutionContext codingRuleExecutionContext;
	private GermplasmNameTypeDTO codeNameType;

	@Before
	public void setUp() throws InvalidGermplasmNameSettingException {
		MockitoAnnotations.initMocks(this);
		this.germplasmNameSetting = this.createGermplasmNameSetting();
		this.namingConfiguration = this.createNamingConfiguration();
		this.setupCodeNameType();

		final String nextNameInSequence = this.getExpectedName(NEXT_NUMBER);
		Mockito.doReturn(nextNameInSequence).when(this.germplasmNamingService)
			.generateNextNameAndIncrementSequence(this.germplasmNameSetting);
		Mockito.doReturn(nextNameInSequence).when(this.germplasmNamingService).getNextNameInSequence(this.germplasmNameSetting);
	}

	@Test
	public void testApplyGroupNamesManualNaming() throws InvalidGermplasmNameSettingException {
		final Set<Integer> gids = new HashSet<>(Arrays.asList(1001, 1002, 1003, 1004));
		final Map<Integer, Germplasm> germplasmMap = new HashMap<>();
		final Map<Integer, Name> oldPreferredNames = new HashMap<>();
		Integer startNumber = NEXT_NUMBER;
		for (final Integer gid : gids) {
			final Germplasm germplasm = new Germplasm();
			germplasm.setGid(gid);
			germplasm.setMgid(gid);

			// Setup existing preferred name
			final Name g1Name = new Name();
			g1Name.setNval("Name G-" + gid);
			g1Name.setNstat(1);
			germplasm.getNames().add(g1Name);
			germplasmMap.put(gid, germplasm);
			oldPreferredNames.put(gid, g1Name);

			Mockito.when(this.germplasmDataManager.getGermplasmByGID(gid)).thenReturn(germplasm);
		}
		Mockito.when(this.germplasmNamingService.generateNextNameAndIncrementSequence(this.germplasmNameSetting))
			.thenReturn(this.getExpectedName(startNumber), this.getExpectedName(startNumber + 1), this.getExpectedName(startNumber + 2),
				this.getExpectedName(startNumber + 3), this.getExpectedName(startNumber + 4), this.getExpectedName(startNumber + 5));

		final List<GermplasmCodingResult> resultsList =
			this.germplasmCodeGenerationService.applyGroupNamesForManualNaming(gids, this.germplasmNameSetting, this.codeNameType);
		Assert.assertEquals("Expected service to return with " + gids.size() + " naming results, one per germplasm.", gids.size(),
			resultsList.size());

		for (final GermplasmCodingResult germplasmCodingResult : resultsList) {
			final Germplasm germplasm = germplasmMap.get(germplasmCodingResult.getGid());
			final String expectedCodedName = PREFIX + " 000000" + (startNumber++) + " " + SUFFIX;
			Assert.assertEquals(
				"Expected germplasm " + germplasmCodingResult.getGid() + " to have a coded name assigned as preferred name.",
				expectedCodedName,
				germplasm.findPreferredName().getNval());
			Assert.assertEquals("Expected germplasm " + germplasmCodingResult.getGid() + " to have a coded name with coded name type.",
				this.codeNameType.getId(),
				germplasm.findPreferredName().getTypeId());
			Assert.assertEquals(
				"Expected existing preferred name of germplasm " + germplasmCodingResult.getGid() + " to be set as non-preferred.",
				new Integer(0),
				oldPreferredNames.get(germplasmCodingResult.getGid()).getNstat());

			Assert.assertEquals(1, germplasmCodingResult.getMessages().size());
			Assert.assertEquals(
				String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", germplasm.getGid(),
					expectedCodedName, this.codeNameType.getCode()), germplasmCodingResult.getMessages().get(0));
		}

	}

	@Test
	public void testApplyGroupNamesAutomaticNaming() throws RuleException {
		final Set<Integer> gids = new HashSet<>(Arrays.asList(1001, 1002, 1003, 1004));
		final Map<Integer, Germplasm> germplasmMap = new HashMap<>();
		final Map<Integer, Name> oldPreferredNames = new HashMap<>();
		Integer startNumber = NAMING_CONFIG_STARTING_SEQUENCE;
		for (final Integer gid : gids) {
			final Germplasm germplasm = new Germplasm();
			germplasm.setGid(gid);
			germplasm.setMgid(gid);

			// Setup existing preferred name
			final Name g1Name = new Name();
			g1Name.setNval("Name G-" + gid);
			g1Name.setNstat(1);
			germplasm.getNames().add(g1Name);
			germplasmMap.put(gid, germplasm);
			oldPreferredNames.put(gid, g1Name);

			Mockito.when(this.germplasmDataManager.getGermplasmByGID(gid)).thenReturn(germplasm);
		}
		Mockito.when(this.ruleFactory.getRuleSequenceForNamespace(GermplasmCodeGenerationServiceImpl.CODING_RULE_SEQUENCE))
			.thenReturn(new String[] {});
		Mockito.when(this.rulesService.runRules(Mockito.any(CodingRuleExecutionContext.class)))
			.thenReturn(PREFIX + NAMING_CONFIG_STARTING_SEQUENCE + SUFFIX, PREFIX + (NAMING_CONFIG_STARTING_SEQUENCE + 1) + SUFFIX,
				PREFIX + (NAMING_CONFIG_STARTING_SEQUENCE + 2) + SUFFIX, PREFIX + (NAMING_CONFIG_STARTING_SEQUENCE + 3) + SUFFIX,
				PREFIX + (NAMING_CONFIG_STARTING_SEQUENCE + 4) + SUFFIX, PREFIX + (NAMING_CONFIG_STARTING_SEQUENCE + 5) + SUFFIX);

		final List<GermplasmCodingResult> resultsList =
			this.germplasmCodeGenerationService.applyGroupNamesForAutomaticNaming(gids, this.codeNameType);
		Assert.assertEquals("Expected service to return with " + gids.size() + " naming results, one per germplasm.", gids.size(),
			resultsList.size());

		for (final GermplasmCodingResult germplasmCodingResult : resultsList) {
			final Germplasm germplasm = germplasmMap.get(germplasmCodingResult.getGid());
			final String expectedCodedName = PREFIX + (startNumber++) + SUFFIX;
			Assert.assertEquals(
				"Expected germplasm " + germplasmCodingResult.getGid() + " to have a coded name assigned as preferred name.",
				expectedCodedName,
				germplasm.findPreferredName().getNval());
			Assert.assertEquals("Expected germplasm " + germplasmCodingResult.getGid() + " to have a coded name with coded name type.",
				this.codeNameType.getId(),
				germplasm.findPreferredName().getTypeId());
			Assert.assertEquals(
				"Expected existing preferred name of germplasm " + germplasmCodingResult.getGid() + " to be set as non-preferred.",
				new Integer(0),
				oldPreferredNames.get(germplasmCodingResult.getGid()).getNstat());

			Assert.assertEquals(1, germplasmCodingResult.getMessages().size());
			Assert.assertEquals(
				String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", germplasm.getGid(),
					expectedCodedName, this.codeNameType.getCode()), germplasmCodingResult.getMessages().get(0));
		}

	}

	@Test
	public void testApplyGroupName_GermplasmIsNotFixed() {
		final Germplasm g1 = new Germplasm();
		g1.setGid(1);

		Mockito.when(this.germplasmDataManager.getGermplasmByGID(g1.getGid())).thenReturn(g1);

		final GermplasmCodingResult result =
			this.germplasmCodeGenerationService.applyGroupName(g1.getGid(), this.codeNameType, RandomStringUtils.randomAlphabetic(10));
		Assert.assertEquals("Expected service to return with one validation message regarding germplasm not being fixed.", 1,
			result.getMessages().size());
		Assert.assertTrue("Expected service to return with validation regarding germplasm not being fixed.",
			result.getMessages().contains("Germplasm (gid: 1) is not part of a management group. Can not assign group name."));
	}

	@Test
	public void testApplyGroupName_GermplasmIsFixedAndHasGroupMembers() {
		final Integer mgid = 1;

		final Germplasm g1 = new Germplasm();
		g1.setGid(1);
		g1.setMgid(mgid);

		// Setup existing preferred name
		final Name g1Name = new Name();
		g1Name.setNval("g1Name");
		g1Name.setNstat(1);
		g1.getNames().add(g1Name);

		Mockito.when(this.germplasmDataManager.getGermplasmByGID(g1.getGid())).thenReturn(g1);

		final Germplasm g2 = new Germplasm();
		g2.setGid(2);
		g2.setMgid(mgid);

		final Germplasm g3 = new Germplasm();
		g3.setGid(3);
		g3.setMgid(mgid);

		Mockito.when(this.germplasmGroupingService.getDescendantGroupMembers(g1.getGid(), mgid)).thenReturn(Lists.newArrayList(g2, g3));

		final String expectedCodedName = PREFIX + " 000000" + NEXT_NUMBER + " " + SUFFIX;

		final GermplasmCodingResult result =
			this.germplasmCodeGenerationService.applyGroupName(g1.getGid(), this.codeNameType, expectedCodedName);
		Assert.assertEquals("Expected service to return with 3 messages, one per group member.", 3, result.getMessages().size());

		Assert.assertEquals("Expected germplasm g1 to have a coded name assigned as preferred name.", expectedCodedName,
			g1.findPreferredName().getNval());
		Assert.assertEquals("Expected germplasm g1 to have a coded name with coded name type.", this.codeNameType.getId(),
			g1.findPreferredName().getTypeId());
		Assert.assertEquals("Expected existing preferred name of germplasm g1 to be set as non-preferred.", new Integer(0),
			g1Name.getNstat());
		Assert.assertEquals(String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", g1.getGid(),
			expectedCodedName, this.codeNameType.getCode()), result.getMessages().get(0));

		Assert.assertEquals("Expected germplasm g2 to have a coded name assigned.", expectedCodedName, g2.findPreferredName().getNval());
		Assert.assertEquals("Expected germplasm g2 to have a coded name with coded name type.", this.codeNameType.getId(),
			g2.findPreferredName().getTypeId());
		Assert.assertEquals(String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", g2.getGid(),
			expectedCodedName, this.codeNameType.getCode()), result.getMessages().get(1));

		Assert.assertEquals("Expected germplasm g3 to have a coded name assigned.", expectedCodedName, g3.findPreferredName().getNval());
		Assert.assertEquals("Expected germplasm g3 to have a coded name with coded name type.", this.codeNameType.getId(),
			g3.findPreferredName().getTypeId());
		Assert.assertEquals(String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", g3.getGid(),
			expectedCodedName, this.codeNameType.getCode()), result.getMessages().get(2));
	}

	@Test
	public void testApplyGroupName_GermplasmIsFixedAndHasGroupMembersWithExistingCodedNames() {
		final Integer mgid = 1;

		final Germplasm g1 = new Germplasm();
		g1.setGid(1);
		g1.setMgid(mgid);

		Mockito.when(this.germplasmDataManager.getGermplasmByGID(g1.getGid())).thenReturn(g1);

		final Germplasm g2 = new Germplasm();
		g2.setGid(2);
		g2.setMgid(mgid);

		final Germplasm g3 = new Germplasm();
		g3.setGid(3);
		g3.setMgid(mgid);

		// Lets setup the third member with existing coded name.
		final Name g3CodedName = new Name();
		// same name type
		g3CodedName.setTypeId(this.codeNameType.getId());
		// but different name
		final String existingCodedNameOfG3 = "ExistingCodedNameOfG3";
		g3CodedName.setNval(existingCodedNameOfG3);
		g3CodedName.setNstat(1);
		g3.getNames().add(g3CodedName);

		Mockito.when(this.germplasmGroupingService.getDescendantGroupMembers(g1.getGid(), mgid)).thenReturn(Lists.newArrayList(g2, g3));

		final String expectedCodedName = PREFIX + " 000000" + NEXT_NUMBER + " " + SUFFIX;

		final GermplasmCodingResult result =
			this.germplasmCodeGenerationService.applyGroupName(g1.getGid(), this.codeNameType, expectedCodedName);
		Assert.assertEquals("Expected service to return with 3 messages, one per group member.", 3, result.getMessages().size());

		Assert.assertEquals("Expected germplasm g1 to have a coded name assigned as preferred name.", expectedCodedName,
			g1.findPreferredName().getNval());
		Assert.assertEquals("Expected germplasm g1 to have a coded name with coded name type.", this.codeNameType.getId(),
			g1.findPreferredName().getTypeId());
		Assert.assertEquals(String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", g1.getGid(),
			expectedCodedName, this.codeNameType.getCode()), result.getMessages().get(0));

		Assert.assertEquals("Expected germplasm g2 to have a coded name assigned.", expectedCodedName, g2.findPreferredName().getNval());
		Assert.assertEquals("Expected germplasm g2 to have a coded name with coded name type.", this.codeNameType.getId(),
			g2.findPreferredName().getTypeId());
		Assert.assertEquals(String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", g2.getGid(),
			expectedCodedName, this.codeNameType.getCode()), result.getMessages().get(1));

		Assert.assertEquals("Expected existing coded name of g3 to be retained.", existingCodedNameOfG3, g3.findPreferredName().getNval());
		Assert.assertTrue(
			"Expected service to return with validation regarding germplasm g3 not assigned given name because it already has one with same type.",
			result.getMessages().contains(
				"Germplasm (gid: 3) already has existing name ExistingCodedNameOfG3 of type CODE1. Supplied name "
					+ expectedCodedName + " was not added."));
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
