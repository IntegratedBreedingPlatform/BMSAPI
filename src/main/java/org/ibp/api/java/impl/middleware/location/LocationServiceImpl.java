
package org.ibp.api.java.impl.middleware.location;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.UserDefinedField;
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

}
