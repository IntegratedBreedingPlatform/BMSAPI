package org.ibp.api.rest.crossplan;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty

public class CrossPlanPreview {

    private String femaleParent;
    private String maleParent;
    private String femaleCross;
    private String maleCross;
    private String germplasmOrigin;


    public String getFemaleParent() {
        return femaleParent;
    }

    public void setFemaleParent(String femaleParent) {
        this.femaleParent = femaleParent;
    }

    public String getMaleParent() {
        return maleParent;
    }

    public void setMaleParent(String maleParent) {
        this.maleParent = maleParent;
    }

    public String getFemaleCross() {
        return femaleCross;
    }

    public void setFemaleCross(String femaleCross) {
        this.femaleCross = femaleCross;
    }

    public String getMaleCross() {
        return maleCross;
    }

    public void setMaleCross(String maleCross) {
        this.maleCross = maleCross;
    }

    public String getGermplasmOrigin() {
        return germplasmOrigin;
    }

    public void setGermplasmOrigin(String germplasmOrigin) {
        this.germplasmOrigin = germplasmOrigin;
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
