
package org.ibp.api.java.impl.middleware.study.conversion;

import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.domain.study.StudyImportDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GermplasmListConverter implements Converter<StudyImportDTO, GermplasmList> {

	@Override
	public GermplasmList convert(final StudyImportDTO source) {

		final String listName = source.getName() + " Germplasm List";
		final String listDescription = "List prepared for study " + source.getName();
		final Long creationDate = Long.valueOf(source.getStartDate());
		final Integer status = 1;

		final GermplasmList germplasmList =
				new GermplasmList(null, listName, creationDate, GermplasmListType.LST.name(), source.getUserId(), listDescription, null,
						status);

		return germplasmList;
	}

}
