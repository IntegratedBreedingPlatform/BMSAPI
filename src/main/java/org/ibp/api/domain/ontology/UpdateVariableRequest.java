package org.ibp.api.domain.ontology;

public class UpdateVariableRequest extends AddVariableRequest {

    private String alias;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return "UpdateVariableDetail{" +
                "alias='" + alias + '\'' +
                "} " + super.toString();
    }
}
