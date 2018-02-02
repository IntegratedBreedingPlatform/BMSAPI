
package org.ibp.api.java.study;

import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchDTO;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchRequestDTO;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyFilters;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyDetails;
import org.ibp.api.domain.study.StudyFolder;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudyImportDTO;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.domain.study.StudySummary;

import java.util.List;
import java.util.Map;

public interface StudyService {

	List<StudySummary> search(final String programUniqueId, String cropname, String principalInvestigator, String location, String season);

	int countTotalObservationUnits(final int studyIdentifier, final int instanceId);

	List<Observation> getObservations(final Integer studyId, final int instanceId, final int pageNumber, final int pageSize,
			final String sortBy, final String sortOrder);

	Observation getSingleObservation(Integer studyId, Integer obeservationId);

	Observation updateObservation(final Integer studyIdentifier, Observation observation);

	List<Observation> updateObservations(final Integer studyIdentifier, List<Observation> observation);

	List<StudyGermplasm> getStudyGermplasmList(final Integer studyIdentifer);

	StudyDetails getStudyDetails(String studyId);

	TrialObservationTable getTrialObservationTable(final int studyIdentifier);

	/**
	 *
	 * @param studyIdentifier id for the study (Nursery / Trial)
	 * @param instanceDbId id for a Trial instance of a Trial (Nursery has 1 instance). If present studyIdentifier will not be used
	 * @return
	 */
	TrialObservationTable getTrialObservationTable(int studyIdentifier, Integer instanceDbId);

	Map<Integer, FieldMap> getFieldMap(final String studyIdentifier);

	Integer importStudy(final StudyImportDTO studyImportDTO, final String programUUID, final String cropPrefix);

	List<StudyFolder> getAllStudyFolders();

	String getProgramUUID(Integer studyIdentifier);

	List<StudyInstance> getStudyInstances(int studyId);

	StudyDetailsDto getStudyDetailsDto (final Integer studyId);

	Long countStudies(final Map<StudyFilters, String> filters);

	List<PhenotypeSearchDTO> searchPhenotypes(Integer pageSize, Integer pageNumber, PhenotypeSearchRequestDTO requestDTO);

	long countPhenotypes(PhenotypeSearchRequestDTO requestDTO);

	List<org.generationcp.middleware.domain.dms.StudySummary> getStudies(final Map<StudyFilters, String> filters, Integer pageSize, Integer pageNumber);

	Boolean isSampled (final Integer studyId);
}
