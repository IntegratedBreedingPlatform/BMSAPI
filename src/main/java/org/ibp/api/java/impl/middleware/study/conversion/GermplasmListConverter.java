package org.ibp.api.java.impl.middleware.study.conversion;

import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.domain.study.StudyWorkbook;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.DateUtils;

@Component
public class GermplasmListConverter implements Converter<StudyWorkbook, GermplasmList>{

	@Override
	public GermplasmList convert(StudyWorkbook source) {
		
		String listType = "LST";
		String listName = source.getName()+"-"+listType;
		String listDescription = source.getObjective();
		Long creationDate = Long.valueOf( source.getStartDate() ); // validate this field is not null
		Integer userId = 51; //jarojas
		Integer status = 1;
		
		GermplasmList germplasmList = new GermplasmList(null, 
				listName, creationDate, listType, userId, listDescription, null, status);
		
		return germplasmList;
	}

}
