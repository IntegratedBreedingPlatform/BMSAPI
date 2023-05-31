package org.ibp.api.rest.dataset;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class EnvironmentVariableValuesPutRequestInput {

    private List<List<String>> data;

    public List<List<String>> getData() {
        return this.data;
    }

    public void setData(final List<List<String>> data) {
        this.data = data;
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
