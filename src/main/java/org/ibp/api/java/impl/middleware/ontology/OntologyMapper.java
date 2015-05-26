package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.TermRelationship;
import org.generationcp.middleware.domain.oms.VariableType;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.ibp.api.domain.ontology.*;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MappingContext;

import java.util.ArrayList;

public class OntologyMapper {

	private static ModelMapper SINGLETON = null;

	/**
	 * We do not want public constructor of this class as all methods are static
	 */
	private OntologyMapper() {

	}

	/**
	 * Eager Initialization of ModelMapper Instance Used when Simple Class to
	 * Class Mapping is Required without Custom Mapping
	 *
	 * @return ModelMapper Instance
	 */
	public static ModelMapper getInstance() {

		if (SINGLETON == null) {
			// Thread Safe. Might be costly operation in some case
			synchronized (OntologyMapper.class) {
				if (SINGLETON == null) {
					SINGLETON = new ModelMapper();
					applyMapperConfiguration(SINGLETON);
					addMethodMappers(SINGLETON);
					addPropertyMappers(SINGLETON);
					addScaleMappers(SINGLETON);
					addVariableMappers(SINGLETON);
					addTermRelationShipMapper(SINGLETON);
					addVariableTypeMapper(SINGLETON);
					addDataTypeMapper(SINGLETON);
				}
			}
		}

		return SINGLETON;
	}

	private static void applyMapperConfiguration(ModelMapper mapper) {
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
	}

	/**
	 * Get ModelMapper instance and add Method related mapping to it
	 */
	private static void addMethodMappers(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<Method, MethodSummary>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().getMetadata().setDateCreated(this.source.getDateCreated());
				this.map().getMetadata().setDateLastModified(this.source.getDateLastModified());
			}
		});

		mapper.addMappings(new PropertyMap<Method, MethodDetails>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().getMetadata().setDateCreated(this.source.getDateCreated());
				this.map().getMetadata().setDateLastModified(this.source.getDateLastModified());
				this.map().getMetadata().setEditableFields(new ArrayList<String>());
				this.map().getMetadata().setDeletable(false);
			}
		});
	}

	/**
	 * Get ModelMapper instance and add Property related mapping to it
	 */
	public static void addPropertyMappers(ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<Property, PropertySummary>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setCropOntologyId(this.source.getCropOntologyId());
				this.map().setClasses(this.source.getClasses());
				this.map().getMetadata().setDateCreated(this.source.getDateCreated());
				this.map().getMetadata().setDateLastModified(this.source.getDateLastModified());
			}
		});

		mapper.addMappings(new PropertyMap<Property, PropertyDetails>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setCropOntologyId(this.source.getCropOntologyId());
				this.map().setClasses(this.source.getClasses());
				this.map().getMetadata().setEditableFields(new ArrayList<String>());
				this.map().getMetadata().setDeletable(false);
				this.map().getMetadata().setDateCreated(this.source.getDateCreated());
				this.map().getMetadata().setDateLastModified(this.source.getDateLastModified());
			}
		});
	}

	/**
	 *  Get ModelMapper instance and add Scale related mapping to it
	 */
	public static void addScaleMappers(ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<Scale, ScaleSummary>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setMinValue(CommonUtil.tryParseSafe(this.source.getMinValue()));
				this.map().setMaxValue(CommonUtil.tryParseSafe(this.source.getMaxValue()));
				this.map().setCategories(this.source.getCategories());
				this.map().getMetadata().setDateCreated(this.source.getDateCreated());
				this.map().getMetadata().setDateLastModified(this.source.getDateLastModified());
			}
		});

		mapper.addMappings(new PropertyMap<Scale, ScaleDetails>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setMinValue(CommonUtil.tryParseSafe(this.source.getMinValue()));
				this.map().setMaxValue(CommonUtil.tryParseSafe(this.source.getMaxValue()));
				this.map().setCategories(this.source.getCategories());
				this.map().getMetadata().setEditableFields(new ArrayList<String>());
				this.map().getMetadata().setDeletable(false);
				this.map().getMetadata().setDateCreated(this.source.getDateCreated());
				this.map().getMetadata().setDateLastModified(this.source.getDateLastModified());
			}
		});
	}

	/**
	 * Get ModelMapper instance and add Property related mapping to it
	 */
	public static void addVariableMappers(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<OntologyVariableSummary, VariableSummary>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDescription());
				this.map().getMethodSummary().setDescription(this.source.getMethodSummary().getDefinition());
				this.map().getPropertySummary().setDescription(this.source.getPropertySummary().getDefinition());
				this.map().setFavourite(this.source.getIsFavorite());
				this.map().setAlias(this.source.getAlias());
				this.map().setExpectedMin(this.source.getMinValue());
				this.map().setExpectedMax(this.source.getMaxValue());
				this.map().getMetadata().setDateCreated(this.source.getDateCreated());
				this.map().getMetadata().setDateLastModified(this.source.getDateLastModified());
			}
		});

		mapper.addMappings(new PropertyMap<Variable, VariableDetails>() {
			@Override
			protected void configure() {
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setFavourite(this.source.getIsFavorite());
				this.map().setAlias(this.source.getAlias());
				this.map().setMethodSummary(this.source.getMethod());
				this.map().setPropertySummary(this.source.getProperty());
				this.map().setObservations(this.source.getObservations());
				this.map().setStudies(this.source.getStudies());
				this.map().setExpectedMin(this.source.getMinValue());
				this.map().setExpectedMax(this.source.getMaxValue());
				this.map().getMetadata().setDateCreated(this.source.getDateCreated());
				this.map().getMetadata().setDateLastModified(this.source.getDateLastModified());
				this.map().getMetadata().setEditableFields(new ArrayList<String>());
				this.map().getMetadata().setDeletable(false);
			}
		});
	}

	/**
	 * This will map middleware TermRelationship(SubjectTerm) to bmsapi TermSummary
	 * @param mapper ModelMapper instance
	 */
	public static void addTermRelationShipMapper(ModelMapper mapper){

		mapper.addMappings(new PropertyMap<TermRelationship, TermSummary>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getSubjectTerm().getId()));
				this.map().setName(this.source.getSubjectTerm().getName());
				this.map().setDescription(this.source.getSubjectTerm().getDefinition());
			}
		});
	}

	public static void addVariableTypeMapper(ModelMapper mapper){
		// Note: This will type map middleware VariableType which is enum to BMSAPI VariableType (Both are collections element)
		mapper.createTypeMap(VariableType.class, org.ibp.api.domain.ontology.VariableType.class).setConverter(
				new Converter<VariableType, org.ibp.api.domain.ontology.VariableType>() {
					@Override
					public org.ibp.api.domain.ontology.VariableType convert(MappingContext<VariableType, org.ibp.api.domain.ontology.VariableType> context) {
						if (context.getSource() == null) {
							return null;
						}
						VariableType variableEnum = context.getSource();

						org.ibp.api.domain.ontology.VariableType variableType = new org.ibp.api.domain.ontology.VariableType();
						variableType.setId(variableEnum.getId());
						variableType.setName(variableEnum.getName());
						variableType.setDescription(variableEnum.getDescription());
						return variableType;
					}
				});
	}

	public static void addDataTypeMapper(ModelMapper mapper){
		// Note: This will type map middleware DataType which is enum to BMSAPI DataType (Both are collections element)
		mapper.createTypeMap(DataType.class, org.ibp.api.domain.ontology.DataType.class).setConverter(
				new Converter<DataType, org.ibp.api.domain.ontology.DataType>() {
					@Override
					public org.ibp.api.domain.ontology.DataType convert(MappingContext<DataType, org.ibp.api.domain.ontology.DataType> context) {
						if (context.getSource() == null) {
							return null;
						}
						DataType dataTypeEnum = context.getSource();

						org.ibp.api.domain.ontology.DataType dataType = new org.ibp.api.domain.ontology.DataType();
						dataType.setId(dataTypeEnum.getId());
						dataType.setName(dataTypeEnum.getName());
						return dataType;
					}
				});
	}

}
