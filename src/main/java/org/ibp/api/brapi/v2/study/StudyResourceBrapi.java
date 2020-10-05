package org.ibp.api.brapi.v2.study;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.api.brapi.v1.location.LocationDetailsDto;
import org.generationcp.middleware.service.api.location.LocationFilters;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.location.Location;
import org.ibp.api.brapi.v1.location.LocationMapper;
import org.ibp.api.brapi.v1.study.StudyDetails;
import org.ibp.api.brapi.v1.study.StudyDetailsData;
import org.ibp.api.brapi.v1.study.StudyMapper;
import org.ibp.api.java.study.StudyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "BrAPI v2 Study Services")
@Controller(value = "StudyResourceBrapiV2")
public class StudyResourceBrapi {

	@Autowired
	private StudyService studyService;

	@Autowired
	private LocationDataManager locationDataManager;


	@ApiOperation(value = "Get the details for a specific Study", notes = "Get the details for a specific Study")
	@RequestMapping(value = "/{crop}/brapi/v2/studies/{studyDbId}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<StudyDetails> getStudyDetails(@PathVariable final String crop, @PathVariable final Integer studyDbId) {

		final StudyDetailsDto mwStudyDetails = this.studyService.getStudyDetailsByGeolocation(studyDbId);

		if (mwStudyDetails != null) {
			final StudyDetails studyDetails = new StudyDetails();
			final Metadata metadata = new Metadata();
			final Pagination pagination = new Pagination().withPageNumber(1).withPageSize(1).withTotalCount(1L).withTotalPages(1);
			metadata.setPagination(pagination);
			metadata.setStatus(Collections.singletonList(new HashMap<>()));
			studyDetails.setMetadata(metadata);
			final ModelMapper studyMapper = StudyMapper.getInstance();
			final StudyDetailsData result = studyMapper.map(mwStudyDetails, StudyDetailsData.class);
			if (mwStudyDetails.getMetadata().getLocationId() != null) {
				final Map<LocationFilters, Object> filters = new EnumMap<>(LocationFilters.class);
				filters.put(LocationFilters.LOCATION_ID, String.valueOf(mwStudyDetails.getMetadata().getLocationId()));
				final List<LocationDetailsDto> locations = this.locationDataManager.getLocationsByFilter(0, 1, filters);
				if (!locations.isEmpty()) {
					final ModelMapper locationMapper = LocationMapper.getInstance();
					final Location location = locationMapper.map(locations.get(0), Location.class);
					result.setLocation(location);
				}
			}
			result.setCommonCropName(crop);
			studyDetails.setResult(result);

			return ResponseEntity.ok(studyDetails);
		} else {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
	}
}
