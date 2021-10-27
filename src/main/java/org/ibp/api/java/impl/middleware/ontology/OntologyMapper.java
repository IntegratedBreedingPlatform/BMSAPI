
package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.domain.oms.TermRelationship;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.ibp.api.domain.ontology.Category;
import org.ibp.api.domain.ontology.MethodDetails;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

public class OntologyMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	/**
	 * We do not want public constructor of this class as all methods are static
	 */
	private OntologyMapper() {

	}

	/**
	 * Configuring the application wide {@link ModelMapper} with ontology related configuration.
	 */
	static {
		OntologyMapper.addMethodMappers(OntologyMapper.applicationWideModelMapper);
		OntologyMapper.addPropertyMappers(OntologyMapper.applicationWideModelMapper);
		OntologyMapper.addScaleMappers(OntologyMapper.applicationWideModelMapper);
		OntologyMapper.addVariableMappers(OntologyMapper.applicationWideModelMapper);
		OntologyMapper.addTermRelationShipMapper(OntologyMapper.applicationWideModelMapper);
		OntologyMapper.addVariableTypeMapper(OntologyMapper.applicationWideModelMapper);
		OntologyMapper.addDataTypeMapper(OntologyMapper.applicationWideModelMapper);
		OntologyMapper.addTermSummaryMapper(OntologyMapper.applicationWideModelMapper);
		OntologyMapper.addCategoryMapper(OntologyMapper.applicationWideModelMapper);

	}

	/**
	 * Configuring the application wide {@link ModelMapper} with ontology related configuration.
	 *
	 * @return ModelMapper Instance
	 */
	public static ModelMapper getInstance() {
		return OntologyMapper.applicationWideModelMapper;
	}

	/**
	 * Get ModelMapper instance and add Method related mapping to it
	 */
	private static void addMethodMappers(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<Method, MethodDetails>() {

			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().getMetadata().setDateCreated(this.source.getDateCreated());
				this.map().getMetadata().setDateLastModified(this.source.getDateLastModified());
				this.map().getMetadata().setDeletable(false);
			}
		});
	}

	/**
	 * Get ModelMapper instance and add Property related mapping to it
	 */
	private static void addPropertyMappers(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<Property, PropertyDetails>() {

			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setCropOntologyId(this.source.getCropOntologyId());
				this.map().setClasses(this.source.getClasses());
				this.map().getMetadata().setDeletable(false);
				this.map().getMetadata().setDateCreated(this.source.getDateCreated());
				this.map().getMetadata().setDateLastModified(this.source.getDateLastModified());
			}
		});
	}

	/**
	 * Get ModelMapper instance and add Scale related mapping to it
	 */
	private static void addScaleMappers(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<Scale, ScaleDetails>() {

			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setMinValue(this.source.getMinValue());
				this.map().setMaxValue(this.source.getMaxValue());
				this.map().getMetadata().setDeletable(false);
				this.map().getMetadata().setDateCreated(this.source.getDateCreated());
				this.map().getMetadata().setDateLastModified(this.source.getDateLastModified());
			}
		});
	}

	/**
	 * Get ModelMapper instance and add Variable related mapping to it
	 */
	private static void addVariableMappers(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<Variable, VariableDetails>() {

			@Override
			protected void configure() {
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setFavourite(this.source.getIsFavorite());
				this.map().setAlias(this.source.getAlias());
				this.map().setObservations(this.source.getObservations());
				this.map().setStudies(this.source.getStudies());
				this.map().setExpectedMin(this.source.getMinValue());
				this.map().setExpectedMax(this.source.getMaxValue());
				this.map().getMetadata().setDateCreated(this.source.getDateCreated());
				this.map().getMetadata().setDateLastModified(this.source.getDateLastModified());
				this.map().getMetadata().setDeletable(false);
				this.map().setFormula(this.source.getFormula());
				this.map().setAllowsFormula(this.source.isAllowsFormula());
				// Mapping datasets this through metadata instead of creating a new set as it is in studies and observations
				// since it was failing in some environments
				this.map().getMetadata().setDatasets(this.source.getDatasets());
				this.map().getMetadata().setGermplasm(this.source.getGermplasm());
				this.map().getMetadata().setBreedingMethods(this.source.getBreedingMethods());
				this.map().getMetadata().setLists(this.source.getLists());

			}
		});
	}

	/**
	 * This will map middleware TermRelationship(SubjectTerm) to bmsapi TermSummary
	 *
	 * @param mapper ModelMapper instance
	 */
	private static void addTermRelationShipMapper(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<TermRelationship, TermSummary>() {

			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getSubjectTerm().getId()));
				this.map().setName(this.source.getSubjectTerm().getName());
				this.map().setDescription(this.source.getSubjectTerm().getDefinition());
			}
		});
	}

	public static void addVariableTypeMapper(final ModelMapper mapper) {
		// Note: This will type map middleware VariableType which is enum to BMSAPI VariableType (Both are collections element)
		mapper.createTypeMap(VariableType.class, org.ibp.api.domain.ontology.VariableType.class).setConverter(
				new Converter<VariableType, org.ibp.api.domain.ontology.VariableType>() {

					@Override public org.ibp.api.domain.ontology.VariableType convert(
						final MappingContext<VariableType, org.ibp.api.domain.ontology.VariableType> context) {
						if (context.getSource() == null) {
							return null;
						}
						final VariableType variableEnum = context.getSource();

						final org.ibp.api.domain.ontology.VariableType variableType = new org.ibp.api.domain.ontology.VariableType();
						variableType.setId(String.valueOf(variableEnum.getId()));
						variableType.setName(variableEnum.getName());
						variableType.setDescription(variableEnum.getDescription());
						return variableType;
					}
				});
	}

	public static void addTermSummaryMapper(final ModelMapper mapper) {
		// Note: This will type map middleware TermSummary to BMSAPI TermSummary for scale categorical values.
		mapper.createTypeMap(org.generationcp.middleware.domain.oms.TermSummary.class, TermSummary.class).setConverter(
				new Converter<org.generationcp.middleware.domain.oms.TermSummary, TermSummary>() {

					@Override
					public TermSummary convert(
						final MappingContext<org.generationcp.middleware.domain.oms.TermSummary, TermSummary> context) {
						if (context.getSource() == null) {
							return null;
						}

						final org.generationcp.middleware.domain.oms.TermSummary termSummary = context.getSource();

						final TermSummary term = new TermSummary();
						term.setId(String.valueOf(termSummary.getId()));
						term.setName(termSummary.getName());
						term.setDescription(termSummary.getDefinition());
						return term;
					}
				});
	}

	public static void addDataTypeMapper(final ModelMapper mapper) {
		// Note: This will type map middleware DataType which is enum to BMSAPI DataType (Both are collections element)
		mapper.createTypeMap(DataType.class, org.ibp.api.domain.ontology.DataType.class).setConverter(
				new Converter<DataType, org.ibp.api.domain.ontology.DataType>() {

					@Override
					public org.ibp.api.domain.ontology.DataType convert(
						final MappingContext<DataType, org.ibp.api.domain.ontology.DataType> context) {
						if (context.getSource() == null) {
							return null;
						}
						final DataType dataTypeEnum = context.getSource();

						final org.ibp.api.domain.ontology.DataType dataType = new org.ibp.api.domain.ontology.DataType();
						dataType.setId(String.valueOf(dataTypeEnum.getId()));
						dataType.setName(dataTypeEnum.getName());
						return dataType;
					}
				});
	}

	public static void addCategoryMapper(final ModelMapper mapper) {
		// Note: This will type map middleware TermSummary to BMSAPI Category for scale categorical values.
		mapper.createTypeMap(org.generationcp.middleware.domain.oms.TermSummary.class, Category.class)
				.setConverter(new Converter<org.generationcp.middleware.domain.oms.TermSummary, Category>() {

							@Override
							public Category convert(
								final MappingContext<org.generationcp.middleware.domain.oms.TermSummary, Category> context) {
								if (context.getSource() == null) {
									return null;
								}

								final org.generationcp.middleware.domain.oms.TermSummary termSummary = context.getSource();

								final Category category = new Category();
								category.setId(String.valueOf(termSummary.getId()));
								category.setName(termSummary.getName());
								category.setDescription(termSummary.getDefinition());
								category.setEditable(Boolean.TRUE);
								return category;
							}
						});
	}

}
