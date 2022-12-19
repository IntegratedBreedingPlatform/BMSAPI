package org.ibp.api.java.study;

public interface StudyTreeService {

	Integer createStudyTreeFolder(String cropName, String programUUID, Integer parentId, String folderName);

	Integer updateStudyTreeFolder(final String cropName, String programUUID, int parentId, String newFolderName);

}
