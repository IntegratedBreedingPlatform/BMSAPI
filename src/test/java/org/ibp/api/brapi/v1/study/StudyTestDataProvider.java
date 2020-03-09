package org.ibp.api.brapi.v1.study;

import com.google.common.collect.Lists;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.service.api.location.LocationDetailsDto;
import org.generationcp.middleware.service.api.study.SeasonDto;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyDto;
import org.generationcp.middleware.service.api.study.StudyMetadata;
import org.generationcp.middleware.service.api.user.RoleDto;
import org.generationcp.middleware.service.api.user.UserDto;
import org.generationcp.middleware.service.api.user.UserRoleDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StudyTestDataProvider {

	public static UserDto getUserDto() {
		UserDto user = new UserDto();
		user.setUserId(1);
		user.setEmail("a@a.com");
		user.setFirstName("admin");
		user.setLastName("admin");
		final UserRoleDto userRoleDto = new UserRoleDto(1,
			new RoleDto(1, "Admin", "", "instance", true, true, true), null,
			null, null);
		final List<UserRoleDto> userRoleDtos = new ArrayList<>();
		userRoleDtos.add(userRoleDto);
		user.setUserRoles(userRoleDtos);
		return user;
	}

	public static StudyMetadata getStudyMetadata() {
		final List<String> seasons = Lists.newArrayList("WET");

		final Date startDate = new Date("2016-01-01");
		final Date endDate = new Date("2017-01-01");

		final StudyMetadata metadata =
			new StudyMetadata().setActive(Boolean.TRUE).setEndDate(endDate).setStartDate(startDate).setLocationId(2)
				.setNurseryOrTrialId(5).setSeasons(seasons).setStudyType(StudyTypeDto.TRIAL_NAME).setTrialName("TN").setTrialDbId(2)
				.setStudyName("SN")
				.setStudyDbId(5);
		return metadata;
	}

	public static Map<String, String> getStudyProperties() {
		Map<String, String> properties = new HashMap<>();
		properties.put("prop1", "val1");
		return properties;
	}

	public static List<MeasurementVariable> getEnvironmentParameters() {
		final List<MeasurementVariable> environmentParameters = new ArrayList<>();
		environmentParameters
			.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.BLOCK_ID.getId(), TermId.BLOCK_ID.name(), "1"));
		return environmentParameters;
	}

	public static StudyDetailsDto getStudyDetailsDto() {
		StudyDetailsDto studyDetailsDto = new StudyDetailsDto();
		studyDetailsDto.setMetadata(StudyTestDataProvider.getStudyMetadata());
		studyDetailsDto.setContacts(Lists.newArrayList(StudyTestDataProvider.getUserDto()));
		studyDetailsDto.setAdditionalInfo(StudyTestDataProvider.getStudyProperties());
		studyDetailsDto.setEnvironmentParameters(StudyTestDataProvider.getEnvironmentParameters());
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

	public static StudyDto getStudyDto() {
		final StudyDto studyDto = new StudyDto();
		studyDto.setProgramName("Maize Program");
		studyDto.setProgramDbId(UUID.randomUUID().toString());
		studyDto.setLocationName("Afghanistan");
		studyDto.setLocationDbId("1");
		studyDto.setActive("true");
		studyDto.setCommonCropName("maize");
		studyDto.setStartDate(new Date());
		studyDto.setEndDate(new Date());

		final SeasonDto seasonDto = new SeasonDto();
		seasonDto.setSeasonDbId(String.valueOf(TermId.SEASON_DRY.getId()));
		seasonDto.setSeason("Dry Season");
		studyDto.setSeasons(Arrays.asList(seasonDto));

		studyDto.setStudyDbId("1");
		studyDto.setStudyName("Study1");
		studyDto.setTrialDbId("2");
		studyDto.setStudyTypeDbId("6");
		studyDto.setStudyTypeName("Trial");
		studyDto.setStudyDbId("6");
		studyDto.setTrialName("Trial1");
		return studyDto;
	}

	public static List<StudyDto> getListStudyDto() {
		final List<StudyDto> studyDtoList = new ArrayList<>();
		studyDtoList.add(StudyTestDataProvider.getStudyDto());
		return studyDtoList;
	}

}
