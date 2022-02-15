package org.ibp.api.brapi.v1.study;

import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.java.impl.middleware.study.StudyEntryMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudyEntryMapperTest {

	@Test
	public void testMap() {
		final Integer gid = 21;
		final List<Germplasm> sourceList = Collections.singletonList(GermplasmTestDataInitializer.createGermplasm(gid));
		final Map<Integer, String> gidDesignationMap = new HashMap<>();
		gidDesignationMap.put(gid, "Designation");
		final Integer startingEntryNumber = 5;
		final List<Integer> germplasmDescriptorIds = new ArrayList<>();
		final Integer entryTypeId = SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId();
		final Map<Integer, String> gidCrossMap = new HashMap<>();
		final StudyEntryDto entry =
			StudyEntryMapper.map(sourceList, gidDesignationMap, startingEntryNumber, germplasmDescriptorIds, entryTypeId, gidCrossMap).get(0);

		Assert.assertEquals(gid, entry.getGid());
		Assert.assertEquals(startingEntryNumber, entry.getEntryNumber());
		Assert.assertEquals(startingEntryNumber, entry.getEntryId());
		// TODO: assert entry code from properties
//		Assert.assertEquals(startingEntryNumber.toString(), entry.getEntryCode());
		Assert.assertEquals(gidDesignationMap.get(gid), entry.getDesignation());
		Assert.assertNotNull(entry.getProperties());
	}

}
