package org.ibp.builders;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.oms.Term;

public class StandardVariableBuilder {

	private final StandardVariable standardVariable;

	public StandardVariableBuilder() {
		this.standardVariable = new StandardVariable();
	}

	public StandardVariableBuilder id(int id) {
		this.standardVariable.setId(id);
		return this;
	}

	public StandardVariableBuilder name(String name) {
		this.standardVariable.setName(name);
		return this;
	}

	public StandardVariableBuilder description(String description) {
		this.standardVariable.setDescription(description);
		return this;
	}

	public StandardVariableBuilder cropOntologyId(String cropOntologyId) {
		this.standardVariable.setCropOntologyId(cropOntologyId);
		return this;
	}

	public StandardVariableBuilder setScale(int id, String name, String description) {
		this.standardVariable.setScale(new Term(id, name, description));
		return this;
	}

	public StandardVariableBuilder setProperty(int id, String name, String description) {
		this.standardVariable.setProperty(new Term(id, name, description));
		return this;
	}

	public StandardVariableBuilder setMethod(int id, String name, String description) {
		this.standardVariable.setMethod(new Term(id, name, description));
		return this;
	}

	public StandardVariableBuilder setDataType(int id, String name, String description) {
		this.standardVariable.setDataType(new Term(id, name, description));
		return this;
	}

	public StandardVariableBuilder setStoredIn(int id, String name, String description) {
		this.standardVariable.setStoredIn(new Term(id, name, description));
		return this;
	}

	public StandardVariableBuilder setIsA(int id, String name, String description) {
		this.standardVariable.setIsA(new Term(id, name, description));
		return this;
	}

	public StandardVariableBuilder setVariableConstraints(Double minValue, Double maxValue) {
		this.standardVariable.setConstraints(new VariableConstraints(minValue, maxValue));
		return this;
	}

	public StandardVariable build() {
		return this.standardVariable;
	}
}
