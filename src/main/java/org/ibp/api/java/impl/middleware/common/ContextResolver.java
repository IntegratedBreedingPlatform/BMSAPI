
package org.ibp.api.java.impl.middleware.common;

public interface ContextResolver {

	String resolveDatabaseFromUrl() throws ContextResolutionException;

	String resolveCropNameFromUrl() throws ContextResolutionException;

	String resolveCropNameFromUrl(boolean doRequireCrop, boolean includeBrAPI) throws ContextResolutionException;

}
