
package org.ibp.api.java.study;

import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.api.germplasm.GermplasmStudyDto;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.brapi.v2.trial.TrialImportResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyService {

	String getProgramUUID(Integer studyIdentifier);

	Boolean isSampled(Integer studyId);

	List<StudyTypeDto> getStudyTypes();

	StudyReference getStudyReference(Integer studyId);

	void updateStudy(Study study);

	List<TreeNode> getStudyTree(String parentKey, String programUUID);

	Integer getEnvironmentDatasetId(Integer studyId);

	List<GermplasmStudyDto> getGermplasmStudies(Integer gid);

	void deleteStudy(Integer studyId);

}
