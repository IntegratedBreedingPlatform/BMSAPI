
package org.ibp.api.java.impl.middleware.study.conversion;

import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.domain.study.StudyWorkbook;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GermplasmListConverter implements Converter<StudyWorkbook, GermplasmList> {

	@Override
	public GermplasmList convert(final StudyWorkbook source) {

		final String listType = "LST";
		final String listName = source.getName() + "-" + listType;
		final String listDescription = source.getObjective();
		final Long creationDate = Long.valueOf(source.getStartDate()); // validate this field is not null
		final Integer userId = 51; // jarojas
		final Integer status = 1;

		final GermplasmList germplasmList =
				new GermplasmList(null, listName, creationDate, listType, userId, listDescription, null, status);

		return germplasmList;
	}

}
