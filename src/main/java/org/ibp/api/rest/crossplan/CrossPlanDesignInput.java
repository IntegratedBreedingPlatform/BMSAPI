package org.ibp.api.rest.crossplan;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class CrossPlanDesignInput {

    private List<Integer> femaleList;

    private List<Integer> maleList;

    private boolean makeReciprocalCrosses;

    private boolean excludeSelfs;

    private CrossingMethod crossingMethod;

    public List<Integer> getFemaleList() {
        return femaleList;
    }

    public void setFemaleList(List<Integer> femaleList) {
        this.femaleList = femaleList;
    }

    public List<Integer> getMaleList() {
        return maleList;
    }

    public void setMaleList(List<Integer> maleList) {
        this.maleList = maleList;
    }

    public boolean isMakeReciprocalCrosses() {
        return makeReciprocalCrosses;
    }

    public void setMakeReciprocalCrosses(boolean makeReciprocalCrosses) {
        this.makeReciprocalCrosses = makeReciprocalCrosses;
    }

    public boolean isExcludeSelfs() {
        return excludeSelfs;
    }

    public void setExcludeSelfs(boolean excludeSelfs) {
        this.excludeSelfs = excludeSelfs;
    }

    public CrossingMethod getCrossingMethod() {
        return crossingMethod;
    }

    public void setCrossingMethod(CrossingMethod crossingMethod) {
        this.crossingMethod = crossingMethod;
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
