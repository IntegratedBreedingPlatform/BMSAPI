package org.ibp.api.domain.sample;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.generationcp.middleware.util.serializer.TimestampPropertySerializer;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.io.Serializable;
import java.util.Date;

@AutoProperty
public class SampleObservationDto implements Serializable {

    private static final long serialVersionUID = 2340381705850740790L;

    private Integer studyDbId;
    private String observationUnitDbId;
    private String plantDbId;
    private String sampleDbId;
    private String takenBy;
    private String sampleType;
    private String tissueType;
    private String notes;
    private String germplasmDbId;
    private String plateDbId;
    private Integer plateIndex;
    private String plotDbId;
    @JsonSerialize(using = TimestampPropertySerializer.class)
    private Date sampleTimestamp;

    public SampleObservationDto() {

    }

    public SampleObservationDto(final Integer studyDbId, final String obsUnitId, final String plantId, final String sampleDbId) {
        this.studyDbId = studyDbId;
        this.observationUnitDbId = obsUnitId;
        this.plantDbId = plantId;
        this.sampleDbId = sampleDbId;
    }

    public Integer getStudyDbId() {
        return this.studyDbId;
    }

    public void setStudyDbId(final Integer studyDbId) {
        this.studyDbId = studyDbId;
    }

    public String getObservationUnitDbId() {
        return this.observationUnitDbId;
    }

    public void setObservationUnitDbId(final String observationUnitDbId) {
        this.observationUnitDbId = observationUnitDbId;
    }

    public String getPlantDbId() {
        return this.plantDbId;
    }

    public void setPlantDbId(final String plantDbId) {
        this.plantDbId = plantDbId;
    }

    public String getSampleDbId() {
        return this.sampleDbId;
    }

    public void setSampleDbId(final String sampleDbId) {
        this.sampleDbId = sampleDbId;
    }

    public String getTakenBy() {
        return this.takenBy;
    }

    public void setTakenBy(final String takenBy) {
        this.takenBy = takenBy;
    }

    public String getSampleType() {
        return this.sampleType;
    }

    public void setSampleType(final String sampleType) {
        this.sampleType = sampleType;
    }

    public String getTissueType() {
        return this.tissueType;
    }

    public void setTissueType(final String tissueType) {
        this.tissueType = tissueType;
    }

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public String getGermplasmDbId() {
        return this.germplasmDbId;
    }

    public void setGermplasmDbId(final String germplasmDbId) {
        this.germplasmDbId = germplasmDbId;
    }

    public String getPlateDbId() {
        return plateDbId;
    }

    public void setPlateDbId(String plateDbId) {
        this.plateDbId = plateDbId;
    }

    public Integer getPlateIndex() {
        return plateIndex;
    }

    public void setPlateIndex(Integer plateIndex) {
        this.plateIndex = plateIndex;
    }

    public String getPlotDbId() {
        return plotDbId;
    }

    public void setPlotDbId(String plotDbId) {
        this.plotDbId = plotDbId;
    }

    public Date getSampleTimestamp() {
        return sampleTimestamp;
    }

    public void setSampleTimestamp(Date sampleTimestamp) {
        this.sampleTimestamp = sampleTimestamp;
    }

    @Override
    public int hashCode() {
        return Pojomatic.hashCode(this);
    }

    @Override
    public String toString() {
        return Pojomatic.toString(this);
    }

    @Override
    public boolean equals(Object o) {
        return Pojomatic.equals(this, o);
    }
}