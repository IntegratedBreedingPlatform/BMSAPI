package org.ibp.api.java.impl.middleware.ontology;

import java.util.ArrayList;

import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.OntologyVariable;
import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.ibp.api.domain.ontology.*;
import org.ibp.api.domain.ontology.MethodSummary;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

public class OntologyMapper {

	private static volatile ModelMapper SINGLETON = new ModelMapper();

	/**
	 * Custom Mapping for Middleware Method Class to MethodSummary Definition to
	 * Description Mapping
	 */
	private static PropertyMap<Method, MethodSummary> methodMap = new PropertyMap<Method, MethodSummary>() {
		@Override
		protected void configure() {
			this.map().setId(String.valueOf(this.source.getId()));
			this.map().setName(this.source.getName());
			this.map().setDescription(this.source.getDefinition());
		}
	};

	/**
	 * Custom Mapping for Middleware Method Class to MethodResponse
	 */
	private static PropertyMap<Method, MethodResponse> methodResponseMap = new PropertyMap<Method, MethodResponse>() {
		@Override
		protected void configure() {
			this.map().setId(String.valueOf(this.source.getId()));
			this.map().setName(this.source.getName());
			this.map().setDescription(this.source.getDefinition());
			this.map().setEditableFields(new ArrayList<String>());
			this.map().setDeletable(false);
		}
	};

	/**
	 * Custom mapping for RequestBody PropertyRequestBase to PropertyRequest
	 */
	private static PropertyMap<PropertyRequestBase, PropertyRequest> propertyBaseToRequest = new PropertyMap<PropertyRequestBase, PropertyRequest>() {
		@Override
		protected void configure() {
			this.map().setId(null);
			this.map().setName(this.source.getName());
			this.map().setDescription(this.source.getDescription());
			this.map().setCropOntologyId(this.source.getCropOntologyId());
			this.map().setClasses(this.source.getClasses());
		}
	};

	/**
	 * Custom Mapping for Middleware Property Class to PropertySummary
	 */
	private static PropertyMap<Property, PropertySummary> propertyMap = new PropertyMap<Property, PropertySummary>() {

		@Override
		protected void configure() {
			this.map().setId(String.valueOf(this.source.getId()));
			this.map().setName(this.source.getName());
			this.map().setDescription(this.source.getDefinition());
			this.map().setCropOntologyId(this.source.getCropOntologyId());
			this.map().setClasses(this.source.getClasses());
		}
	};

	/**
	 * Custom Mapping for Middleware Property Class to PropertyResponse
	 */
	private static PropertyMap<Property, PropertyResponse> propertyResponseMap = new PropertyMap<Property, PropertyResponse>() {
		@Override
		protected void configure() {
			this.map().setId(String.valueOf(this.source.getId()));
			this.map().setName(this.source.getName());
			this.map().setDescription(this.source.getDefinition());
			this.map().setCropOntologyId(this.source.getCropOntologyId());
			this.map().setClasses(this.source.getClasses());
			this.map().setEditableFields(new ArrayList<String>());
			this.map().setDeletable(false);
		}
	};

	/**
	 * Custom Mapping for Middleware Scale Class to ScaleSummary
	 */
	private static PropertyMap<Scale, ScaleSummary> scaleMap = new PropertyMap<Scale, ScaleSummary>() {
		@Override
		protected void configure() {
			this.map().setId(this.source.getId());
			this.map().setName(this.source.getName());
			this.map().setDescription(this.source.getDefinition());
			this.map().setMinValue(this.source.getMinValue());
			this.map().setMaxValue(this.source.getMaxValue());
			this.map().setCategories(this.source.getCategories());
		}
	};

	/**
	 * Custom Mapping for Middleware Scale Class to ScaleResponse
	 */
	private static PropertyMap<Scale, ScaleResponse> scaleResponseMap = new PropertyMap<Scale, ScaleResponse>() {
		@Override
		protected void configure() {
			this.map().setId(this.source.getId());
			this.map().setName(this.source.getName());
			this.map().setDescription(this.source.getDefinition());
			this.map().setMinValue(this.source.getMinValue());
			this.map().setMaxValue(this.source.getMaxValue());
			this.map().setCategories(this.source.getCategories());
			this.map().setEditableFields(new ArrayList<String>());
			this.map().setDeletable(false);
		}
	};

	/**
	 * Custom Mapping for Middleware OntologyVariableSummary Class to
	 * VariableSummary
	 */
	private static PropertyMap<OntologyVariableSummary, VariableSummary> variableMap = new PropertyMap<OntologyVariableSummary, VariableSummary>() {
		@Override
		protected void configure() {
			this.map().setId(this.source.getId());
			this.map().setName(this.source.getName());
			this.map().setDescription(this.source.getDescription());
			this.map().setFavourite(this.source.getIsFavorite());
			this.map().setAlias(this.source.getAlias());
			this.map().setVariableTypes(this.source.getVariableTypes());
			this.map().setCreatedDate(this.source.getDateCreated());
			this.map().setModifiedData(this.source.getDateLastModified());
			this.map().setObservations(this.source.getObservations());
			this.map().setExpectedMin(this.source.getMinValue());
			this.map().setExpectedMax(this.source.getMaxValue());
		}
	};

	/**
	 * Custom Mapping for Middleware OntologyVariable Class to VariableResponse
	 */
	private static PropertyMap<OntologyVariable, VariableResponse> variableResponseMap = new PropertyMap<OntologyVariable, VariableResponse>() {
		@Override
		protected void configure() {
			this.map().setName(this.source.getName());
			this.map().setDescription(this.source.getDefinition());
			this.map().setFavourite(this.source.getIsFavorite());
			this.map().setAlias(this.source.getAlias());
			this.map().setVariableTypes(this.source.getVariableTypes());
			this.map().setCreatedDate(this.source.getDateCreated());
			this.map().setModifiedData(this.source.getDateLastModified());
			this.map().setObservations(this.source.getObservations());
			this.map().setExpectedMin(this.source.getMinValue());
			this.map().setExpectedMax(this.source.getMaxValue());
			this.map().setEditableFields(new ArrayList<String>());
			this.map().setDeletable(false);
		}
	};

	private OntologyMapper() {
	}

	/**
	 * Eager Initialization of ModelMapper Instance Used when Simple Class to
	 * Class Mapping is Required without Custom Mapping
	 * 
	 * @return ModelMapper Instance
	 */
	public static ModelMapper getInstance() {
		return OntologyMapper.SINGLETON;
	}

	/**
	 * Customise Mapped method 'methodMap' is Initialize in Mapper and Returned
	 * 
	 * @return ModelMapper Instance
	 */
	public static ModelMapper methodMapper() {
		ModelMapper mapper = new ModelMapper();
		mapper.addMappings(OntologyMapper.methodMap);
		mapper.addMappings(OntologyMapper.methodResponseMap);
		return mapper;
	}

	/**
	 * Customise Mapped property 'propertyBaseToRequest' is Initialize in Mapper and Returned
	 * @return ModelMapper Instance
	 */
	public static ModelMapper propertyBaseToRequestMapper(){
		ModelMapper mapper = new ModelMapper();
		mapper.addMappings(OntologyMapper.propertyBaseToRequest);
		return mapper;
	}

	/**
	 * Customise Mapped property 'propertyMap' is Initialize in Mapper and
	 * Returned
	 * 
	 * @return ModelMapper Instance
	 */
	public static ModelMapper propertyMapper() {
		ModelMapper mapper = new ModelMapper();
		mapper.addMappings(OntologyMapper.propertyMap);
		mapper.addMappings(OntologyMapper.propertyResponseMap);
		return mapper;
	}

	/**
	 * Customise Mapped property 'scaleMap' is Initialize in Mapper and Returned
	 * 
	 * @return ModelMapper Instance
	 */
	public static ModelMapper scaleMapper() {
		ModelMapper scaleMapper = new ModelMapper();
		scaleMapper.addMappings(OntologyMapper.scaleMap);
		scaleMapper.addMappings(OntologyMapper.scaleResponseMap);
		scaleMapper.createTypeMap(DataType.class, IdName.class).setConverter(
				new Converter<DataType, IdName>() {
					@Override
					public IdName convert(MappingContext<DataType, IdName> mappingContext) {
						return new IdName();
					}
				});
		return scaleMapper;
	}

	/**
	 * Customise Mapped property 'variableMap' is Initialize in Mapper and
	 * Returned
	 * 
	 * @return ModelMapper Instance
	 */
	public static ModelMapper variableMapper() {
		ModelMapper variableMapper = new ModelMapper();
		variableMapper.addMappings(OntologyMapper.variableMap);
		variableMapper.createTypeMap(TermSummary.class, IdName.class).setConverter(
				new Converter<TermSummary, IdName>() {
					@Override
					public IdName convert(MappingContext<TermSummary, IdName> mappingContext) {
						return new IdName();
					}
				});
		return variableMapper;
	}

	/**
	 * Customise Mapped property 'variableResponseMap' is Initialize in Mapper
	 * and Returned
	 * 
	 * @return ModelMapper Instance
	 */
	public static ModelMapper variableResponseMapper() {
		ModelMapper variableMapper = new ModelMapper();
		variableMapper.addMappings(OntologyMapper.methodMap);
		variableMapper.addMappings(OntologyMapper.propertyMap);
		variableMapper.addMappings(OntologyMapper.scaleMap);
		variableMapper.addMappings(OntologyMapper.variableResponseMap);
		return variableMapper;
	}
}
