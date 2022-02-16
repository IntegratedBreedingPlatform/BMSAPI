package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.germplasmlist.GermplasmListBasicInfoDTO;
import org.generationcp.middleware.pojos.GermplasmList;

import java.util.Optional;

class GermplasmListHelper {

	static void assignFolderDependentProperties(final GermplasmListBasicInfoDTO request, final String currentProgram, final Optional<GermplasmList> parentFolderOptional) {

		final String parentFolderId = request.getParentFolderId();
		final String programUUID = calculateProgramUUID(currentProgram, parentFolderOptional, parentFolderId);
		request.setProgramUUID(programUUID);

		request.setStatus((StringUtils.isEmpty(request.getProgramUUID())) ? GermplasmList.Status.LOCKED_LIST.getCode() :
			GermplasmList.Status.LIST.getCode());

		if (GermplasmListTreeServiceImpl.CROP_LISTS.equals(parentFolderId) || GermplasmListTreeServiceImpl.PROGRAM_LISTS.equals(parentFolderId)) {
			request.setParentFolderId(null);
		}
	}

	static String calculateProgramUUID (final String currentProgramUUID, final Optional<GermplasmList> parentFolder, final String parentId) {
		if (GermplasmListTreeServiceImpl.CROP_LISTS.equals(parentId) || (parentFolder.isPresent() && StringUtils.isEmpty(parentFolder.get()
			.getProgramUUID()))) {
			return null;
		} else {
			return currentProgramUUID;
		}
	}

}
