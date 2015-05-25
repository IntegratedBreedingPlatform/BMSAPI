package org.ibp.api.java.location;

import java.util.Set;

import org.ibp.api.domain.location.LocationType;


public interface LocationService {

	Set<LocationType> getAllLocationTypes();
}
