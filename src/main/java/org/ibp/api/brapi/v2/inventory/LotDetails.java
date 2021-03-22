package org.ibp.api.brapi.v2.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LotDetails {

    private Double amount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date lastUpdated;

    private String germplasmDbId;

    private Integer locationDbId;

    private String programDbId;

    private String seedLotDescription;

    private String seedLotName;

    private String sourceCollection;

    private String storageLocation;

    private String units;

    private String seedLotDbId;

    private Map<String, Object> additionalInfo = new HashMap<>();

    public Map<String, Object> getAdditionalInfo() {
        return this.additionalInfo;
    }

    public void setAdditionalInfo(final Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Double getAmount() {
        return this.amount;
    }

    public void setAmount(final Double amount) {
        this.amount = amount;
    }

    public Date getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate(final Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated(final Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getGermplasmDbId() {
        return this.germplasmDbId;
    }

    public void setGermplasmDbId(final String germplasmDbId) {
        this.germplasmDbId = germplasmDbId;
    }

    public Integer getLocationDbId() {
        return this.locationDbId;
    }

    public void setLocationDbId(final Integer locationDbId) {
        this.locationDbId = locationDbId;
    }

    public String getProgramDbId() {
        return this.programDbId;
    }

    public void setProgramDbId(final String programDbId) {
        this.programDbId = programDbId;
    }

    public String getSeedLotDescription() {
        return this.seedLotDescription;
    }

    public void setSeedLotDescription(final String seedLotDescription) {
        this.seedLotDescription = seedLotDescription;
    }

    public String getSeedLotName() {
        return this.seedLotName;
    }

    public void setSeedLotName(final String seedLotName) {
        this.seedLotName = seedLotName;
    }

    public String getSourceCollection() {
        return this.sourceCollection;
    }

    public void setSourceCollection(final String sourceCollection) {
        this.sourceCollection = sourceCollection;
    }

    public String getStorageLocation() {
        return this.storageLocation;
    }

    public void setStorageLocation(final String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public String getUnits() {
        return this.units;
    }

    public void setUnits(final String units) {
        this.units = units;
    }

    public String getSeedLotDbId() {
        return this.seedLotDbId;
    }

    public void setSeedLotDbId(final String seedLotDbId) {
        this.seedLotDbId = seedLotDbId;
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
    public boolean equals(final Object o) {
        return Pojomatic.equals(this, o);
    }
}
