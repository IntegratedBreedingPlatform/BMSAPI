package org.ibp.api.brapi.v2.observation;

import io.swagger.annotations.ApiModel;

import java.util.Map;

@SuppressWarnings("ALL")
public class ObservationUnitPatchRequestDTO {

	private ObservationUnitPosition observationUnitPosition = new ObservationUnitPosition();

	@ApiModel("ObservationUnitPatchRequestDTOPosition")
	public static class ObservationUnitPosition {

		private Map<String, Object> geoCoordinates;

		public Map<String, Object> getGeoCoordinates() {
			return geoCoordinates;
		}

		public void setGeoCoordinates(final Map<String, Object> geoCoordinates) {
			this.geoCoordinates = geoCoordinates;
		}
	}

	public ObservationUnitPosition getObservationUnitPosition() {
		return observationUnitPosition;
	}

	public void setObservationUnitPosition(
		final ObservationUnitPosition observationUnitPosition) {
		this.observationUnitPosition = observationUnitPosition;
	}
}
