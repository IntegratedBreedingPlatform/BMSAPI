package org.ibp.api.brapi.v1.trial;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.dao.dms.InstanceMetadata;
import org.generationcp.middleware.domain.dms.TrialSummary;
import org.generationcp.middleware.service.api.user.ContactDto;
import org.generationcp.middleware.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrialSummaryTestDataProvider {

	private static Map<String, String> getOptionalInfo() {
		final Map<String, String> additionalInfo = new HashMap<>();
		additionalInfo.put("K", "V");
		return additionalInfo;
	}

	private static List<InstanceMetadata> getInstanceMatadatas() {
		final List<InstanceMetadata> instanceMetadatas = new ArrayList<>();
		final InstanceMetadata instanceMetadata = new InstanceMetadata();
		instanceMetadata.setLocationName("LOC");
		instanceMetadata.setTrialName("TR");
		instanceMetadata.setInstanceDbId(2);
		instanceMetadata.setInstanceNumber("1");
		instanceMetadata.setLocationAbbreviation("ABBR");
		instanceMetadatas.add(instanceMetadata);
		return instanceMetadatas;
	}

	public static List<ContactDto> getContacts() {
		final List<ContactDto> contacts = new ArrayList<>();
		contacts.add(new ContactDto(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(10),
			RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(10),
			RandomStringUtils.randomAlphabetic(10)));
		return contacts;
	}

	public static TrialSummary getTrialSummary() {
		final TrialSummary trialSummary = new TrialSummary();
		trialSummary.setLocationId("1");
		trialSummary.setActive(Boolean.TRUE);
		trialSummary.setEndDate(Util.tryParseDate("20170404"));
		trialSummary.setProgramDbId("64646");
		trialSummary.setProgramName("PROGRAM1");
		trialSummary.setStartDate(Util.tryParseDate("20160404"));
		trialSummary.setTrialDbId(2);
		trialSummary.setName("STUDY1");
		trialSummary.setAdditionalInfo(getOptionalInfo());
		trialSummary.setInstanceMetaData(getInstanceMatadatas());
		trialSummary.setContacts(getContacts());
		return trialSummary;
	}

}
