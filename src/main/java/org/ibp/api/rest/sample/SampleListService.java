package org.ibp.api.rest.sample;

import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.pojos.SampleList;
import org.springframework.data.domain.Pageable;

import java.util.List;
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
	 * @param programUUID
	 * @throws Exception
	 */
	Map<String, Object> moveSampleListFolder(final Integer folderId, final Integer newParentId, final boolean isCropList,
			final String programUUID);

	/**
	 * Delete a folder
	 * Folder ID must exist and it can not contain any child
	 *
	 * @param folderId
	 * @throws Exception
	 */
	void deleteSampleListFolder(final Integer folderId);

	/**
	 * Search for SampleLists with names that contain the search term
	 * @param searchString
	 * @param exactMatch
	 * @return
	 */
	List<SampleList> search(final String searchString, final boolean exactMatch, final String programUUID, final Pageable pageable);

	List<SampleDetailsDTO> getSampleDetailsDTOs(Integer listId);

	void importSamplePlateInformation(final List<SampleDTO> sampleDTOs, final Integer listId);

	List<TreeNode> getSampleListChildrenNodes(final String crop, final String programUUID, final String parentId, final Boolean folderOnly);

	void deleteSamples(Integer sampleListId, List<Integer> sampleIds);

}
