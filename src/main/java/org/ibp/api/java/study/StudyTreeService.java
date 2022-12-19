package org.ibp.api.java.study;

public interface StudyTreeService {

	Integer createStudyTreeFolder(final String cropName, final String programUUID, final Integer parentId, final String name);

}
