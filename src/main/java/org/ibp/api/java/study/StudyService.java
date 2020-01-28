
package org.ibp.api.java.study;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.study.StudyTypeDto;
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
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.domain.study.StudySummary;

import java.util.List;
import java.util.Map;

public interface StudyService {

	List<StudySummary> search(final String programUniqueId, String cropname, String principalInvestigator, String location, String season);

	List<Observation> getObservations(final Integer studyId, final int instanceId, final int pageNumber, final int pageSize,
			final String sortBy, final String sortOrder);

	Observation getSingleObservation(Integer studyId, Integer obeservationId);

	Observation updateObservation(final Integer studyIdentifier, Observation observation);

	List<Observation> updateObservations(final Integer studyIdentifier, List<Observation> observation);

	List<StudyGermplasm> getStudyGermplasmList(final Integer studyIdentifer);

	TrialObservationTable getTrialObservationTable(final int studyIdentifier);

	/**
	 *
	 * @param studyIdentifier id for the study (Nursery / Trial)
	 * @param instanceDbId id for a Trial instance of a Trial (Nursery has 1 instance). If present studyIdentifier will not be used
	 * @return
	 */
	TrialObservationTable getTrialObservationTable(int studyIdentifier, Integer instanceDbId);

	Map<Integer, FieldMap> getFieldMap(final String studyIdentifier);

	String getProgramUUID(Integer studyIdentifier);

	StudyDetailsDto getStudyDetailsForGeolocation (final Integer geolocationId);

	Long countStudies(final Map<StudyFilters, String> filters);

	List<PhenotypeSearchDTO> searchPhenotypes(final Integer pageSize, final Integer pageNumber, final PhenotypeSearchRequestDTO requestDTO);

	long countPhenotypes(final PhenotypeSearchRequestDTO requestDTO);

	List<org.generationcp.middleware.domain.dms.StudySummary> getStudies(final Map<StudyFilters, String> filters, Integer pageSize, Integer pageNumber);

	Boolean isSampled (final Integer studyId);

	List<StudyTypeDto> getStudyTypes();
	
	StudyReference getStudyReference(final Integer studyId);

	void updateStudy (final Study study);

}
