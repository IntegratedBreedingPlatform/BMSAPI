
package org.ibp.api.java.impl.middleware.location;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.ibp.api.domain.location.Location;
import org.ibp.api.domain.location.LocationType;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.location.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl implements LocationService {

	@Autowired
	private LocationDataManager locationDataManager;

	@Override
	public Set<LocationType> getAllLocationTypes() {
		Set<LocationType> locationTypes = new LinkedHashSet<>();
		try {
			List<UserDefinedField> locationTypeUdflds =
					this.locationDataManager.getUserDefinedFieldByFieldTableNameAndType(UDTableType.LOCATION_LTYPE.getTable(), UDTableType.LOCATION_LTYPE.getType());

			for (UserDefinedField udfld : locationTypeUdflds) {
				locationTypes.add(new LocationType(udfld.getFldno().toString(), udfld.getFcode(), udfld.getFname()));
			}
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
		return locationTypes;
	}

	@Override
	public List<Location> getLocationsByType(String locationTypeId, int pageNumber, int pageSize) {
		List<Location> locationsByType = new ArrayList<>();
		try {
			Integer locTypeId = Integer.valueOf(locationTypeId);
			int start = pageSize * (pageNumber - 1);
			int numOfRows = pageSize;

			List<org.generationcp.middleware.pojos.Location> mwLocationsByType = 
					this.locationDataManager.getLocationsByType(locTypeId, start, numOfRows);

			UserDefinedField locationTypeUdfld = 
					this.locationDataManager.getUserDefinedFieldByID(locTypeId);

			LocationType locationType = 
					new LocationType(locationTypeUdfld.getFldno().toString(), locationTypeUdfld.getFcode(), locationTypeUdfld.getFname());

			for (org.generationcp.middleware.pojos.Location mwLoc : mwLocationsByType) {
				Location location = new Location();
				location.setId(mwLoc.getLocid().toString());
				location.setName(mwLoc.getLname());
				location.setAbbreviation(mwLoc.getLabbr());
				if (mwLoc.getGeoref() != null) {
					location.setAltitude(mwLoc.getGeoref().getAlt());
					location.setLatitude(mwLoc.getGeoref().getLat());
					location.setLongitude(mwLoc.getGeoref().getLon());
				}
				location.setLocationType(locationType);
				locationsByType.add(location);
			}
		} catch (NumberFormatException | MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
		return locationsByType;
	}

	@Override
	public long countLocationByType(String locationTypeId) {
		try {
			return this.locationDataManager.countLocationsByType(Integer.valueOf(locationTypeId));
		} catch (NumberFormatException | MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

}
