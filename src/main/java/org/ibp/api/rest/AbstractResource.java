
package org.ibp.api.rest;

import org.ibp.api.java.impl.middleware.ontology.validator.MiddlewareIdFormatValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractResource {

	// TODO Remove all these validators once all resource classeses have been updated to move validators inside Middleware service impls.
	@Autowired
	protected TermDeletableValidator termDeletableValidator;

	@Autowired
	protected MiddlewareIdFormatValidator requestIdValidator;

	@Autowired
	protected TermValidator termValidator;

}
