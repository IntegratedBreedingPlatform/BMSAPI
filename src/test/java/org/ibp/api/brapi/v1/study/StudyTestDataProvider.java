package org.ibp.api.brapi.v1.study;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.service.api.location.LocationDetailsDto;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyMetadata;
import org.generationcp.middleware.service.api.user.UserDto;

import com.google.common.collect.Lists;

public class StudyTestDataProvider {

	public static UserDto getUserDto() {
		UserDto user = new UserDto();
		user.setUserId(1);
		user.setEmail("a@a.com");
		user.setFirstName("admin");
		user.setLastName("admin");
		user.setRole(new Role(1,"ADMIN"));
		return user;
	}

	public static StudyMetadata getStudyMetadata() {
		final List<String> seasons = Lists.newArrayList("WET");
		final StudyMetadata metadata =
				new StudyMetadata().setActive(Boolean.TRUE).setEndDate("20161010").setStartDate("20161010").setLocationId(2)
						.setNurseryOrTrialId(5).setSeasons(seasons).setStudyType(StudyType.T.getName()).setTrialName("TN").setTrialDbId(2)
					.setStudyName("SN")
						.setStudyDbId(5);
		return metadata;
	}

	public static Map<String, String> getStudyProperties() {
		Map<String, String> properties = new HashMap<>();
		properties.put("prop1", "val1");
		return properties;
	}

	public static StudyDetailsDto getStudyDetailsDto() {
		StudyDetailsDto studyDetailsDto = new StudyDetailsDto();
		studyDetailsDto.setMetadata(StudyTestDataProvider.getStudyMetadata());
		studyDetailsDto.setContacts(Lists.newArrayList(StudyTestDataProvider.getUserDto()));
		studyDetailsDto.setAdditionalInfo(StudyTestDataProvider.getStudyProperties());
		return studyDetailsDto;
	}

	public static LocationDetailsDto getLocationDetailsDto() {
		LocationDetailsDto locationDetailsDto =
				new LocationDetailsDto(2, "Breeding Location", "New Zealand", "NZL", "NZL", "NZ", 156.2, 58.6, 5.2);
		return locationDetailsDto;
	}

	public static List<LocationDetailsDto> getListLocationDetailsDto() {
		List<LocationDetailsDto> locationDetailsDtoList = new ArrayList<>();
		locationDetailsDtoList.add(StudyTestDataProvider.getLocationDetailsDto());
		return locationDetailsDtoList;
	}

}
