package org.ibp.api.conversion;

import org.generationcp.middleware.pojos.GermplasmListData;
import org.ibp.api.domain.study.StudyGermplasm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GermplasmListDataConverter implements Converter<StudyGermplasm, GermplasmListData>{

	@Override
	public GermplasmListData convert(StudyGermplasm source) {
//		List<GermplasmListData> germplasmListData = new ArrayList<>();
		
//		StudyGermplasm germ;
//		for (int i = 0; i < source.getGermplasms().size(); i++) {
//			germ = source.getGermplasms().get(i);
			GermplasmListData target = new GermplasmListData(
					null,
					null, // owner list
					source.getGermplasmListEntrySummary().getGid(), //gid
					Integer.valueOf(source.getEntryNo()), //entry id
					source.getGermplasmListEntrySummary().getEntryCode(), //entry code (???)
					source.getGermplasmListEntrySummary().getSeedSource(), //source
					source.getGermplasmListEntrySummary().getDesignation(), //desig
					"0", //group name
					0, //status
					0); //local record id locrecid
			
//			germplasmListData.add(entry);
			
//		}
	
		
		return target;
	}

}
