package org.ibp.api.brapi.v1.study;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"studyDbId", "observationVariableDbId", "observationVariableName", "data"})
public class StudyDetailDto {

    private Long studyDbId;

    private List<Long> observationVariableDbId;

    private List<String> observationVariableName;

    private List<List<String>> data;

    /**
     *
     * @return The study db id
     */
    public Long getStudyDbId() {
        return studyDbId;
    }

    /**
     *
     * @param studyDbId The study db id
     * @return this
     */
    public StudyDetailDto setStudyDbId(final Long studyDbId) {
        this.studyDbId = studyDbId;
        return this;
    }

    /**
     *
     * @return the observation variables id
     */
    public List<Long> getObservationVariableDbId() {
        return observationVariableDbId;
    }

    /**
     *
     * @param observationVariableDbId
     * @return this
     */
    public StudyDetailDto setObservationVariableDbId(final List<Long> observationVariableDbId) {
        this.observationVariableDbId = observationVariableDbId;
        return this;
    }

    /**
     *
     * @return The observation variable names
     */
    public List<String> getObservationVariableName() {
        return observationVariableName;
    }

    /**
     *
     * @param observationVariableName
     * @return this
     */
    public StudyDetailDto setObservationVariableName(final List<String> observationVariableName) {
        this.observationVariableName = observationVariableName;
        return this;
    }

    /**
     *
     * @return List of lists of the measurement values for every trait in the observation variables ids
     */
    public List<List<String>> getData() {
        return data;
    }

    /**
     *
     * @param data
     * @return this
     */
    public StudyDetailDto setData(final List<List<String>> data) {
        this.data = data;
        return this;
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
