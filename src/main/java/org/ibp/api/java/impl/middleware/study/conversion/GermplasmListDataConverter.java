
package org.ibp.api.java.impl.middleware.study.conversion;

import org.generationcp.middleware.pojos.GermplasmListData;
import org.ibp.api.domain.study.StudyGermplasm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GermplasmListDataConverter implements Converter<StudyGermplasm, GermplasmListData> {

	@Override
	public GermplasmListData convert(final StudyGermplasm source) {

		final GermplasmListData target = new GermplasmListData(null, null, // owner list
				source.getGermplasmListEntrySummary().getGid(), // gid
				source.getEntryNumber(), // entry id
				source.getGermplasmListEntrySummary().getEntryCode(), // entry code (???)
				source.getGermplasmListEntrySummary().getSeedSource(), // source
				source.getGermplasmListEntrySummary().getDesignation(), // desig
				source.getGermplasmListEntrySummary().getCross(), // group name
				0, // status
				0); // local record id locrecid

		return target;
	}

}
