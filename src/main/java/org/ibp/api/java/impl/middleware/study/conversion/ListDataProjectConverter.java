package org.ibp.api.java.impl.middleware.study.conversion;

import org.generationcp.middleware.pojos.ListDataProject;
import org.ibp.api.domain.study.StudyGermplasm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ListDataProjectConverter implements Converter<StudyGermplasm, ListDataProject>{

	@Override
	public ListDataProject convert(StudyGermplasm source) {
//		List<ListDataProject> listDataProject = new ArrayList<>();
		ListDataProject target = new ListDataProject();;
//		StudyGermplasm germ;
		
//		for (int i = 0; i < source.getGermplasms().size(); i++) {
//			germ = source.getGermplasms().get(i);
			
//			target = new ListDataProject();
				target.setCheckType(0); //unknown?
				target.setGermplasmId(source.getGermplasmListEntrySummary().getGid());
				target.setDesignation(source.getGermplasmListEntrySummary().getDesignation());
				target.setEntryId(Integer.valueOf(source.getEntryNo()));
				target.setEntryCode(source.getGermplasmListEntrySummary().getEntryCode());
				target.setSeedSource(source.getGermplasmListEntrySummary().getSeedSource());
				target.setGroupName("0");
			
//			listDataProject.add(entry);
//		}
	
		
		return target;
	}

}
