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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date lastUpdated;

    private Integer germplasmDbId;

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
        return additionalInfo;
    }

    public void setAdditionalInfo(final Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(final Double amount) {
        this.amount = amount;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(final Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Integer getGermplasmDbId() {
        return germplasmDbId;
    }

    public void setGermplasmDbId(final Integer germplasmDbId) {
        this.germplasmDbId = germplasmDbId;
    }

    public Integer getLocationDbId() {
        return locationDbId;
    }

    public void setLocationDbId(final Integer locationDbId) {
        this.locationDbId = locationDbId;
    }

    public String getProgramDbId() {
        return programDbId;
    }

    public void setProgramDbId(final String programDbId) {
        this.programDbId = programDbId;
    }

    public String getSeedLotDescription() {
        return seedLotDescription;
    }

    public void setSeedLotDescription(final String seedLotDescription) {
        this.seedLotDescription = seedLotDescription;
    }

    public String getSeedLotName() {
        return seedLotName;
    }

    public void setSeedLotName(final String seedLotName) {
        this.seedLotName = seedLotName;
    }

    public String getSourceCollection() {
        return sourceCollection;
    }

    public void setSourceCollection(final String sourceCollection) {
        this.sourceCollection = sourceCollection;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(final String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(final String units) {
        this.units = units;
    }

    public String getSeedLotDbId() {
        return seedLotDbId;
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
    public boolean equals(Object o) {
        return Pojomatic.equals(this, o);
    }
}
