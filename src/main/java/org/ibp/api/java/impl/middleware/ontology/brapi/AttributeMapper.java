package org.ibp.api.java.impl.middleware.ontology.brapi;

import org.generationcp.middleware.api.brapi.v2.attribute.AttributeDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class AttributeMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private AttributeMapper() {

	}

	static {
		AttributeMapper.addVariableDTOMapping(AttributeMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return AttributeMapper.applicationWideModelMapper;
	}

	private static void addVariableDTOMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<VariableDTO, AttributeDTO>() {
			@Override
			protected void configure() {
				this.map().setAdditionalInfo(this.source.getAdditionalInfo());
				this.map().setAttributeCategory(this.source.getTrait().getTraitClass());
				this.map().setAttributeDescription(this.source.getDefinition());
				this.map().setAttributeDbId(this.source.getObservationVariableDbId());
				this.map().setAttributeName(this.source.getObservationVariableName());
				this.map().setContextOfUse(this.source.getContextOfUse());
				this.map().setDefaultValue(this.source.getDefaultValue());
				this.map().setDocumentationURL(this.source.getDocumentationURL());
				this.map().setExternalReferences(this.source.getExternalReferences());
				this.map().setGrowthStage(this.source.getGrowthStage());
				this.map().setInstitution(this.source.getInstitution());
				this.map().setLanguage(this.source.getLanguage());
				this.map().setMethod(this.source.getMethod());
				this.map().setOntologyReference(this.source.getOntologyReference());
				this.map().setScale(this.source.getScale());
				this.map().setScientist(this.source.getScientist());
				this.map().setStatus(this.source.getStatus());
				this.map().setSubmissionTimestamp(this.source.getSubmissionTimestamp());
				this.map().setSynonyms(this.source.getSynonyms());
				this.map().setTrait(this.source.getTrait());
			}
		});
	}

}
