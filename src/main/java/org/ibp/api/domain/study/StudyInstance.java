
package org.ibp.api.domain.study;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class StudyInstance {

	private int instanceDbId;
	private int experimentId;
	private String locationName;
	private String locationAbbreviation;
	private String customLocationAbbreviation;
	private int instanceNumber;
	private boolean hasFieldmap;
	private Boolean hasGeoJSON;
	/** has X/Y coordinates */
	private Boolean hasFieldLayout;
	private Boolean hasInventory;
	private Boolean hasExperimentalDesign;
	private Boolean hasMeasurements;
	private Boolean canBeDeleted;

	public StudyInstance() {

	}

	public StudyInstance(final int instanceDbId, final int experimentId, final String locationName, final String locationAbbreviation,
		final int instanceNumber, final String customLocationAbbreviation, final boolean hasFieldmap) {
		this.instanceDbId = instanceDbId;
		this.experimentId = experimentId;
		this.locationName = locationName;
		this.locationAbbreviation = locationAbbreviation;
		this.instanceNumber = instanceNumber;
		this.customLocationAbbreviation = customLocationAbbreviation;
		this.hasFieldmap = hasFieldmap;
	}

	public int getInstanceDbId() {
		return this.instanceDbId;
	}

	public void setInstanceDbId(final int instanceDbId) {
		this.instanceDbId = instanceDbId;
	}

	public String getLocationName() {
		return this.locationName;
	}

	public void setLocationName(final String locationName) {
		this.locationName = locationName;
	}

	public String getLocationAbbreviation() {
		return this.locationAbbreviation;
	}

	public void setLocationAbbreviation(final String locationAbbreviation) {
		this.locationAbbreviation = locationAbbreviation;
	}

	public int getInstanceNumber() {
		return this.instanceNumber;
	}

	public void setInstanceNumber(final int instanceNumber) {
		this.instanceNumber = instanceNumber;
	}

	public String getCustomLocationAbbreviation() {
		return customLocationAbbreviation;
	}

	public void setCustomLocationAbbreviation(final String customLocationAbbreviation) {
		this.customLocationAbbreviation = customLocationAbbreviation;
	}

	public boolean getHasFieldmap() {
		return hasFieldmap;
	}

	public void setHasFieldmap(boolean hasFieldmap) {
		this.hasFieldmap = hasFieldmap;
	}

	public Boolean getHasGeoJSON() {
		return this.hasGeoJSON;
	}

	public void setHasGeoJSON(final Boolean hasGeoJSON) {
		this.hasGeoJSON = hasGeoJSON;
	}

	public Boolean getHasFieldLayout() {
		return this.hasFieldLayout;
	}

	public void setHasFieldLayout(final Boolean hasFieldLayout) {
		this.hasFieldLayout = hasFieldLayout;
	}

	public Boolean getHasInventory() {
		return this.hasInventory;
	}

	public void setHasInventory(final Boolean hasInventory) {
		this.hasInventory = hasInventory;
	}

	public Boolean isHasExperimentalDesign() {
		return hasExperimentalDesign;
	}

	public void setHasExperimentalDesign(final Boolean hasExperimentalDesign) {
		this.hasExperimentalDesign = hasExperimentalDesign;
	}

	public Boolean isHasMeasurements() {
		return hasMeasurements;
	}

	public void setHasMeasurements(final Boolean hasMeasurements) {
		this.hasMeasurements = hasMeasurements;
	}

	public Boolean getCanBeDeleted() {
		return canBeDeleted;
	}

	public void setCanBeDeleted(final Boolean canBeDeleted) {
		this.canBeDeleted = canBeDeleted;
	}

	public int getExperimentId() {
		return this.experimentId;
	}

	public void setExperimentId(final int experimentId) {
		this.experimentId = experimentId;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof StudyInstance)) {
			return false;
		}
		final StudyInstance castOther = (StudyInstance) other;
		return new EqualsBuilder().append(this.instanceDbId, castOther.instanceDbId).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.instanceDbId).toHashCode();
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
