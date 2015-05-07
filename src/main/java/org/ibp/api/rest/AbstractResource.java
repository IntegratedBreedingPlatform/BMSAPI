
package org.ibp.api.rest;

import org.ibp.api.java.impl.middleware.ontology.validator.MiddlewareIdFormatValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractResource {
	
	@Autowired
	protected TermDeletableValidator termDeletableValidator;

	//TODO Remove me once all resource classes have been updated to move me inside Middleware service impls.
	@Autowired
	protected MiddlewareIdFormatValidator requestIdValidator;

	@Autowired
	protected TermValidator termValidator;


}
