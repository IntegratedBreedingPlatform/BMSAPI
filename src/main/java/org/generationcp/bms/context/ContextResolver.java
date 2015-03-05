package org.generationcp.bms.context;

public interface ContextResolver {
    String resolveDatabaseFromUrl() throws ContextResolutionException;
}
