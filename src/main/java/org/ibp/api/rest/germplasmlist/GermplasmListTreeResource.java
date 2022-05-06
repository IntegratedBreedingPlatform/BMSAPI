package org.ibp.api.rest.germplasmlist;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.ibp.api.java.germplasm.GermplasmListDataService;
import org.ibp.api.java.germplasm.GermplasmListTemplateExportService;
import org.ibp.api.java.germplasm.GermplasmListTreeService;
import org.ibp.api.rest.common.UserTreeState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "Germplasm List Tree Services")
@Controller
public class GermplasmListTreeResource {

	private static final String MANAGE_GERMPLASM_LISTS_PERMISSIONS = "'LISTS', 'MANAGE_GERMPLASM_LISTS'";
	private static final String MANAGE_GERMPLASM_PERMISSIONS = "'GERMPLASM', 'MANAGE_GERMPLASM'";

	@Autowired
	public GermplasmListTreeService germplasmListTreeService;

	@Autowired
	public GermplasmListDataService germplasmListDataService;

	@Autowired
	public GermplasmListTemplateExportService germplasmListTemplateExportService;

	@ApiOperation(value = "Get germplasm lists given a tree parent node folder", notes = "Get germplasm lists given a tree parent node folder")
	@RequestMapping(value = "/crops/{crop}/germplasm-lists/tree", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<TreeNode>> getGermplasmListByParentFolderId(
		@ApiParam(value = "The crop type", required = true) @PathVariable final String crop,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@ApiParam(value = "The id of the parent folder") @RequestParam(required = false) final String parentFolderId,
		@ApiParam(value = "Only folders") @RequestParam(required = true) final Boolean onlyFolders) {
		final List<TreeNode> children =
			this.germplasmListTreeService.getGermplasmListChildrenNodes(crop, programUUID, parentFolderId, onlyFolders);
		return new ResponseEntity<>(children, HttpStatus.OK);
	}

	@ApiOperation(value = "Create germplasm list folder", notes = "Create sample list folder.")
	// TODO add specific permission to create list from germplasm manager? IBP-5387
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_PERMISSIONS + ", 'IMPORT_GERMPLASM', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ")")
	@RequestMapping(value = "/crops/{crop}/germplasm-list-folders", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity createGermplasmListFolder(
		@PathVariable final String crop,
		@RequestParam(required = false) final String programUUID,
		@RequestParam final String folderName,
		@RequestParam final String parentId) {

		final Integer folderId = this.germplasmListTreeService.createGermplasmListFolder(crop, programUUID, folderName, parentId);
		return new ResponseEntity<>(folderId, HttpStatus.OK);
	}

	@ApiOperation(value = "Update germplasm list folder", notes = "Update germplasm list folder.")
	// TODO add specific permission to create list from germplasm manager? IBP-5387
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_PERMISSIONS + ", 'IMPORT_GERMPLASM', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ")")
	@RequestMapping(value = "/crops/{crop}/germplasm-list-folders/{folderId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateGermplasmListFolderName(
		@PathVariable final String crop,
		@PathVariable final String folderId,
		@RequestParam(required = false) final String programUUID,
		@RequestParam final String newFolderName) {

		final Integer updatedFolderId = this.germplasmListTreeService
			.updateGermplasmListFolderName(crop, programUUID, newFolderName, folderId);
		return new ResponseEntity<>(updatedFolderId, HttpStatus.OK);
	}

	@ApiOperation(value = "Move germplasm list folder.", notes = "Move germplasm list folder.")
	// TODO add specific permission to create list from germplasm manager? IBP-5387
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_PERMISSIONS + ", 'IMPORT_GERMPLASM', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ")")
	@RequestMapping(value = "/crops/{crop}/germplasm-list-folders/{folderId}/move", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<TreeNode> moveGermplasmList(
		@PathVariable final String crop,
		@PathVariable final String folderId,
		@RequestParam(required = false) final String programUUID,
		@RequestParam final String newParentId) {

		final TreeNode movedNode = this.germplasmListTreeService.moveGermplasmListNode(crop, programUUID, folderId, newParentId);
		return new ResponseEntity<>(movedNode, HttpStatus.OK);
	}

	@ApiOperation(value = "Delete germplasm list folder", notes = "Delete germplasm list folder.")
	// TODO add specific permission to create list from germplasm manager? IBP-5387
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_PERMISSIONS + ", 'IMPORT_GERMPLASM', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ")")
	@RequestMapping(value = "/crops/{crop}/germplasm-list-folders/{folderId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteGermplasmListFolder(
		@PathVariable final String crop,
		@PathVariable final String folderId,
		@RequestParam(required = false) final String programUUID) {
		this.germplasmListTreeService.deleteGermplasmListFolder(crop, programUUID, folderId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get tree of expanded germplasm list folders last used by user", notes = "Get tree of expanded germplasm list folders last used by user")
	@RequestMapping(value = "/crops/{crop}/germplasm-lists/tree-state", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<TreeNode>> getUserTreeState(
		@ApiParam(value = "The crop type", required = true) @PathVariable final String crop,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@ApiParam(value = "The User ID") @RequestParam(required = true) final String userId) {
		return new ResponseEntity<>(this.germplasmListTreeService.getUserTreeState(crop, programUUID, userId), HttpStatus.OK);
	}

	@ApiOperation(value = "Save hierarchy of germplasm list folders last used by user", notes = "Save hierarchy of germplasm list folders last used by user")
	@RequestMapping(value = "/crops/{crop}/germplasm-lists/tree-state", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> saveUserTreeState(
		@ApiParam(value = "The crop type", required = true) @PathVariable final String crop,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@RequestBody final UserTreeState treeState) {
		this.germplasmListTreeService.saveGermplasmListTreeState(crop, programUUID, treeState);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
