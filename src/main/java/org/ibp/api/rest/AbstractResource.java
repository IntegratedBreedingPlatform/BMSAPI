
package org.ibp.api.rest;

import org.ibp.api.java.impl.middleware.ontology.validator.RequestIdValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractResource {
	
	@Autowired
	protected TermDeletableValidator termDeletableValidator;

	@Autowired
	protected RequestIdValidator requestIdValidator;

	@Autowired
	protected TermValidator termValidator;


}
