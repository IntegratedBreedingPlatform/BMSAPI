package org.ibp.api.brapi.v1.study;

import org.generationcp.middleware.data.initializer.GermplasmListDataTestDataInitializer;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.ibp.api.java.impl.middleware.study.StudyEntryPropertiesMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StudyEntryPropertiesMapperTest {
	private static final List<Integer> DESCRIPTOR_IDS = Arrays.asList(TermId.ENTRY_TYPE.getId(), TermId.SEED_SOURCE.getId(),
		TermId.GROUPGID.getId(), TermId.CROSS.getId(), TermId.GERMPLASM_SOURCE.getId());

	@Test
	public void testMapWithGermplasmAsSource() {
		final Germplasm source = GermplasmTestDataInitializer.createGermplasm(1);
		final int entryTypeId = SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId();
		final String cross = "CROSS";
		final Map<Integer, StudyEntryPropertyData> studyEntryPropertyDataMap =
			StudyEntryPropertiesMapper.map(source, DESCRIPTOR_IDS, entryTypeId, cross);

		Assert.assertEquals(Integer.toString(entryTypeId), studyEntryPropertyDataMap.get(TermId.ENTRY_TYPE.getId()).getValue());
		Assert.assertTrue(studyEntryPropertyDataMap.get(TermId.SEED_SOURCE.getId()).getValue().isEmpty());
		Assert.assertEquals(source.getMgid().toString(), studyEntryPropertyDataMap.get(TermId.GROUPGID.getId()).getValue());
		Assert.assertEquals(cross, studyEntryPropertyDataMap.get(TermId.CROSS.getId()).getValue());
		Assert.assertTrue(studyEntryPropertyDataMap.get(TermId.GERMPLASM_SOURCE.getId()).getValue().isEmpty());
	}

	@Test
	public void testMapWithGermplasmListDataAsSource() {
		final GermplasmListData source = GermplasmListDataTestDataInitializer.createGermplasmListData(new GermplasmList(), 1, 1);
		final Germplasm germplasmSource = GermplasmTestDataInitializer.createGermplasm(1);
		source.setGermplasm(germplasmSource);
		final String entryTypeId = String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
		final Map<Integer, StudyEntryPropertyData> studyEntryPropertyDataMap =
			StudyEntryPropertiesMapper.map(source, DESCRIPTOR_IDS);

		Assert.assertEquals(entryTypeId, studyEntryPropertyDataMap.get(TermId.ENTRY_TYPE.getId()).getValue());
		Assert.assertEquals(source.getSeedSource(), studyEntryPropertyDataMap.get(TermId.SEED_SOURCE.getId()).getValue());
		Assert.assertEquals(germplasmSource.getMgid().toString(), studyEntryPropertyDataMap.get(TermId.GROUPGID.getId()).getValue());
		Assert.assertEquals(source.getGroupName(), studyEntryPropertyDataMap.get(TermId.CROSS.getId()).getValue());
		Assert.assertEquals(source.getSeedSource(), studyEntryPropertyDataMap.get(TermId.GERMPLASM_SOURCE.getId()).getValue());
	}

}
