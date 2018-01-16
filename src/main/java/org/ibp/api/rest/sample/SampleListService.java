package org.ibp.api.rest.sample;

import java.util.Map;

public interface SampleListService {

	Map<String, Object> createSampleList(final SampleListDto sampleListDto);

	/**
	 * Create a sample list folder
	 * Sample List folder name must be unique across the elements in the parent folder
	 *
	 * @param folderName
	 * @param parentId
	 * @param programUUID
	 * @return Folder ID
	 * @throws Exception
	 */
	Map<String, Object> createSampleListFolder(final String folderName, final Integer parentId, final String programUUID);

	/**
	 * Update sample list folder name
	 * New folder name should be unique across the elements in the parent folder
	 *
	 * @param folderId
	 * @param newFolderName
	 * @throws Exception
	 */
	Map<String, Object> updateSampleListFolderName(final Integer folderId, final String newFolderName);

	/**
	 * Move a folder to another folder
	 * FolderID must exist, newParentID must exist
	 * newParentID folder must not contain another sample list with the name that the one that needs to be moved
	 *
	 * @param folderId
	 * @param newParentId
	 * @param isCropList
	 * @throws Exception
	 */
	Map<String, Object> moveSampleListFolder(final Integer folderId, final Integer newParentId, final boolean isCropList);

	/**
	 * Delete a folder
	 * Folder ID must exist and it can not contain any child
	 *
	 * @param folderId
	 * @throws Exception
	 */
	void deleteSampleListFolder(final Integer folderId);
}
