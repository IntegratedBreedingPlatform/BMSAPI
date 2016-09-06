
package org.ibp.api.ibpworkbench.service;


import org.ibp.api.ibpworkbench.exceptions.BreedingViewImportException;

import java.io.File;
import java.util.Map;

public interface BreedingViewImportService {

	void importMeansData(File file, int studyId) throws BreedingViewImportException;

	void importMeansData(File file, int studyId, Map<String, String> localNameToAliasMap) throws BreedingViewImportException;

	void importSummaryStatsData(File file, int studyId) throws BreedingViewImportException;

	void importSummaryStatsData(File file, int studyId, Map<String, String> localNameToAliasMap) throws BreedingViewImportException;

	void importOutlierData(File file, int studyId) throws BreedingViewImportException;

	void importOutlierData(File file, int studyId, Map<String, String> localNameToAliasMap) throws BreedingViewImportException;

}
