package org.ibp.builders;

import java.util.Date;

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Method;

public class MethodBuilder {

	private final Term term;

	public MethodBuilder() {
		this.term = new Term();
	}

	public Method build(int id, String name, String description) {
		this.term.setId(id);
		this.term.setName(name);
		this.term.setDefinition(description);
		return new Method(this.term);
	}
	
	public Method build(int id, String name, String description, Date dateCreated, Date dateLastModified) {
		Method method = build(id, name, description);
		method.setDateCreated(dateCreated);
		method.setDateLastModified(dateLastModified);
		return method;
	}
}
