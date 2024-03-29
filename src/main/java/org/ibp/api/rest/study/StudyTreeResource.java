package org.ibp.api.rest.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.ibp.api.java.study.StudyTreeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "Study Tree Services")
@Controller
public class StudyTreeResource {

	@Resource
	private StudyTreeService studyTreeService;

	@ApiOperation(value = "Create a study folder")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/crops/{crop}/programs/{programUUID}/study-folders", method = RequestMethod.POST)
	public ResponseEntity<Integer> createStudyFolder(
		@PathVariable final String crop,
		@PathVariable final String programUUID,
		@RequestParam final String folderName,
		@RequestParam final Integer parentId) {

		final Integer createdFolderId = this.studyTreeService.createStudyTreeFolder(crop, programUUID, parentId, folderName);
		return new ResponseEntity<>(createdFolderId, HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update the given study folder")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/crops/{crop}/programs/{programUUID}/study-folders/{folderId}", method = RequestMethod.PUT)
	public ResponseEntity<Integer> updateStudyFolder(
		@PathVariable final String crop,
		@PathVariable final String programUUID,
		@PathVariable final Integer folderId,
		@RequestParam final String newFolderName) {

		final Integer updatedFolderId = this.studyTreeService.updateStudyTreeFolder(crop, programUUID, folderId, newFolderName);
		return new ResponseEntity<>(updatedFolderId, HttpStatus.CREATED);
	}

	@ApiOperation(value = "Delete the given study folder")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/crops/{crop}/programs/{programUUID}/study-folders/{folderId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> deleteStudyFolder(
		@PathVariable final String crop,
		@PathVariable final String programUUID,
		@PathVariable final Integer folderId) {

		this.studyTreeService.deleteStudyFolder(crop, programUUID, folderId);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@ApiOperation(value = "Move study node")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
	@RequestMapping(value = "/crops/{crop}/programs/{programUUID}/study-folders/{nodeId}/move", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<TreeNode> moveStudyNode(
		@PathVariable final String crop,
		@PathVariable final Integer nodeId,
		@PathVariable final String programUUID,
		@RequestParam final Integer newParentId) {

		final TreeNode movedNode = this.studyTreeService.moveStudyNode(crop, programUUID, nodeId, newParentId);
		return new ResponseEntity<>(movedNode, HttpStatus.OK);
	}

	@ApiOperation(value = "Get the study tree")
	@RequestMapping(value = "/crops/{cropName}/studies/tree", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<TreeNode>> getStudyTree(final @PathVariable String cropName,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@ApiParam(value = "The id of the parent folder") @RequestParam(required = false) final String parentFolderId) {

		final List<TreeNode> studyTree = this.studyTreeService.getStudyTree(parentFolderId, programUUID);
		return new ResponseEntity<>(studyTree, HttpStatus.OK);
	}

}
