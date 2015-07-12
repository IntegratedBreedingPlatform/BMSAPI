
package org.ibp.api.java.impl.middleware.common;

public interface ContextResolver {

	String resolveDatabaseFromUrl() throws ContextResolutionException;
}
