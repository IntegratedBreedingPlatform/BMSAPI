package org.ibp.api.rest.crossplan;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.ibp.api.java.crossplan.CrossPlanTreeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "Cross Plan Tree Services")
@Controller
public class CrossPlanTreeResource {

    @Resource
    private CrossPlanTreeService crossPlanTreeService;

    @ApiOperation(value = "Create a cross plan folder")
    @ResponseBody
    @RequestMapping(value = "/crops/{crop}/programs/{programUUID}/cross-plan-folders", method = RequestMethod.POST)
    public ResponseEntity<Integer> createStudyFolder(
            @PathVariable final String crop,
            @PathVariable final String programUUID,
            @RequestParam final String folderName,
            @RequestParam final Integer parentId) {

        final Integer createdFolderId = this.crossPlanTreeService.createCrossPlanTreeFolder(crop, programUUID, parentId, folderName);
        return new ResponseEntity<>(createdFolderId, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update the given cross plan folder")
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
    @RequestMapping(value = "/crops/{crop}/programs/{programUUID}/cross-plan-folders/{folderId}", method = RequestMethod.PUT)
    public ResponseEntity<Integer> updateStudyFolder(
            @PathVariable final String crop,
            @PathVariable final String programUUID,
            @PathVariable final Integer folderId,
            @RequestParam final String newFolderName) {

        final Integer updatedFolderId = this.crossPlanTreeService.updateCrossPlanTreeFolder(crop, programUUID, folderId, newFolderName);
        return new ResponseEntity<>(updatedFolderId, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete the given cross plan folder")
    @ResponseBody
    @RequestMapping(value = "/crops/{crop}/programs/{programUUID}/cross-plan-folders/{folderId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteStudyFolder(
            @PathVariable final String crop,
            @PathVariable final String programUUID,
            @PathVariable final Integer folderId) {

        this.crossPlanTreeService.deleteCrossPlanFolder(crop, programUUID, folderId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Move cross plan node")
    @RequestMapping(value = "/crops/{crop}/programs/{programUUID}/cross-plan-folders/{nodeId}/move", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<TreeNode> moveStudyNode(
            @PathVariable final String crop,
            @PathVariable final Integer nodeId,
            @PathVariable final String programUUID,
            @RequestParam final Integer newParentId) {

        final TreeNode movedNode = this.crossPlanTreeService.moveCrossPlanNode(crop, programUUID, nodeId, newParentId);
        return new ResponseEntity<>(movedNode, HttpStatus.OK);
    }

    @ApiOperation(value = "Get the cross-plan tree")
    @RequestMapping(value = "/crops/{cropName}/cross-plan/tree", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<TreeNode>> getStudyTree(final @PathVariable String cropName,
                                                       @ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
                                                       @ApiParam(value = "The id of the parent folder") @RequestParam(required = false) final String parentFolderId) {

        final List<TreeNode> studyTree = this.crossPlanTreeService.geCrossPlanTree(parentFolderId, programUUID);
        return new ResponseEntity<>(studyTree, HttpStatus.OK);
    }
}
