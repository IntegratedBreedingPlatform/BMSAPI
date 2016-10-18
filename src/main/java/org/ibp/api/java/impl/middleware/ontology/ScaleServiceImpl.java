
package org.ibp.api.java.impl.middleware.ontology;

import static org.generationcp.middleware.domain.ontology.DataType.CATEGORICAL_VARIABLE;
import static org.generationcp.middleware.domain.ontology.DataType.NUMERIC_VARIABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermRelationship;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.TermRelationshipId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.oms.VariableOverrides;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.Category;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.ValidValues;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.ServiceBaseImpl;
import org.ibp.api.java.impl.middleware.ontology.validator.ScaleValidator;
import org.ibp.api.java.ontology.ScaleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * Validate data of API Services and pass data to middleware services
 */

@Service
@Transactional
public class ScaleServiceImpl extends ServiceBaseImpl implements ScaleService {

	private static final String ERROR_MESSAGE = "Error!";
	private static final String SCALE = "Scale";
	private static final String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";

	@Autowired
	private OntologyScaleDataManager ontologyScaleDataManager;
	
	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private ScaleValidator scaleValidator;

	@Override
	public List<ScaleDetails> getAllScales() {
		try {
			List<Scale> scales = this.ontologyScaleDataManager.getAllScales();
			List<ScaleDetails> scaleSummaries = new ArrayList<>();

			ModelMapper mapper = OntologyMapper.getInstance();

			for (Scale scale : scales) {
				ScaleDetails scaleDetail = mapper.map(scale, ScaleDetails.class);
				scaleSummaries.add(scaleDetail);
			}
			return scaleSummaries;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(ScaleServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ScaleDetails getScaleById(final String id) {
		this.validateId(id, ScaleServiceImpl.SCALE);
		// Note: Validate Scale Id for valid format and scale exists or not
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ScaleServiceImpl.SCALE);
		final TermRequest term = new TermRequest(id, ScaleServiceImpl.SCALE, CvId.SCALES.getId());
		this.termValidator.validate(term, errors);

		// Note: If any error occurs then throws Exception with error messages
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			final Scale scale = this.ontologyScaleDataManager.getScaleById(StringUtil.parseInt(id, null), true);
			if (scale == null) {
				return null;
			}

			final ModelMapper mapper = OntologyMapper.getInstance();
			final ScaleDetails scaleDetails = mapper.map(scale, ScaleDetails.class);

			final DataType dataType = DataType.getById(scale.getDataType().getId());

			// Note : Get list of relationships related to scale Id
			final List<TermRelationship> relationships =
					this.termDataManager.getRelationshipsWithObjectAndType(StringUtil.parseInt(id, null), TermRelationshipId.HAS_SCALE);

			Collections.sort(relationships, new Comparator<TermRelationship>() {

				@Override
				public int compare(final TermRelationship l, final TermRelationship r) {
					return l.getSubjectTerm().getName().compareToIgnoreCase(r.getSubjectTerm().getName());
				}
			});

			/* variables of scale */
			for (final TermRelationship relationship : relationships) {
				final org.ibp.api.domain.ontology.TermSummary termSummary =
						mapper.map(relationship, org.ibp.api.domain.ontology.TermSummary.class);
				scaleDetails.getMetadata().getUsage().addUsage(termSummary);
			}

			Boolean deletable = Boolean.TRUE;
			Boolean editable = Boolean.TRUE;

			if (this.termDataManager.isTermReferred(StringUtil.parseInt(id, null))) {
				// scale is not used in any variable
				// Given the scale is used in one or more variables
				deletable = false;

				final List<Integer> variablesIds = (List<Integer>) CollectionUtils.collect(relationships, new Transformer() {

					@Override
					public Integer transform(final Object input) {
						final TermRelationship termRelationship = (TermRelationship) input;
						return Integer.valueOf(termRelationship.getSubjectTerm().getId());
					}

				});

				if (Objects.equals(scale.getDataType().getId(), CATEGORICAL_VARIABLE.getId())) {
					// if scale is categorical

					editable = !this.ontologyVariableDataManager.areVariablesUsedInStudy(variablesIds);

					if (!editable) {

						final List<String> categories =
								this.termDataManager.getCategoriesReferredInPhenotype(StringUtil.parseInt(id, null));
						final Map<String, String> mappedCategories = Maps.uniqueIndex(categories, new Function<String, String>() {

							@Override
							public String apply(final String from) {
								return from;
							}
						});
						for (final Category category : scaleDetails.getValidValues().getCategories()) {
							if (mappedCategories.containsKey(category.getName())) {
								category.setEditable(Boolean.FALSE);
							}
						}
					}
				}

				else if (Objects.equals(scale.getDataType().getId(), NUMERIC_VARIABLE.getId())) {
					// if scale is numerical
					final List<VariableOverrides> overrides =
							this.ontologyVariableDataManager.getVariableOverridesByVariableIds(variablesIds);

					final Iterator<VariableOverrides> it = overrides.iterator();
					while (it.hasNext() && editable) {
						final VariableOverrides override = it.next();
						final Float scaleMinValue = Float.valueOf(scale.getMinValue());
						final Float scaleMaxValue = Float.valueOf(scale.getMaxValue());
						final Float overrideMinValue = Float.valueOf(override.getExpectedMin());
						final Float overrideMaxValue = Float.valueOf(override.getExpectedMax());
						if (!(scaleMinValue.compareTo(overrideMinValue) <= 0 && scaleMaxValue.compareTo(overrideMaxValue) >= 0)) {
							// variable expected range not included in scale range
							editable = false;
						}

					}
				}
			}

			if (!dataType.isSystemDataType()) {
				if (!deletable) {
					scaleDetails.getMetadata().addEditableField(ScaleServiceImpl.FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED);
					scaleDetails.getMetadata().addEditableField("validValues");
				} else {
					scaleDetails.getMetadata().addEditableField("name");
					scaleDetails.getMetadata().addEditableField(ScaleServiceImpl.FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED);
					scaleDetails.getMetadata().addEditableField("dataType");
					scaleDetails.getMetadata().addEditableField("validValues");
				}
				scaleDetails.getMetadata().setDeletable(deletable);
			} else {
				scaleDetails.getMetadata().setDeletable(false);
			}
			scaleDetails.getMetadata().setEditable(editable);

			return scaleDetails;
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException(ScaleServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public GenericResponse addScale(ScaleDetails scaleDetail) {
		// Note: Set id to null because add scale does not need id
		scaleDetail.setId(null);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ScaleServiceImpl.SCALE);
		this.scaleValidator.validate(scaleDetail, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			Scale scale = new Scale();
			scale.setName(scaleDetail.getName().trim());
			scale.setDefinition(scaleDetail.getDescription().trim());

			Integer dataTypeId = StringUtil.parseInt(scaleDetail.getDataType().getId(), null);
			scale.setDataType(DataType.getById(dataTypeId));

			if (Objects.equals(dataTypeId, CATEGORICAL_VARIABLE.getId())) {
				for (org.ibp.api.domain.ontology.TermSummary category : scaleDetail.getValidValues().getCategories()) {
					scale.addCategory(new TermSummary(null, category.getName().trim(), category.getDescription().trim()));
				}
			}
			if (Objects.equals(dataTypeId, NUMERIC_VARIABLE.getId())) {
				String min = scaleDetail.getValidValues().getMin() == null ? null : scaleDetail.getValidValues().getMin();
				String max = scaleDetail.getValidValues().getMax() == null ? null : scaleDetail.getValidValues().getMax();
				scale.setMinValue(min);
				scale.setMaxValue(max);
			}

			this.ontologyScaleDataManager.addScale(scale);
			return new GenericResponse(String.valueOf(scale.getId()));
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(ScaleServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public void updateScale(String id, ScaleDetails scaleDetails) {
		this.validateId(id, ScaleServiceImpl.SCALE);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ScaleServiceImpl.SCALE);
		TermRequest term = new TermRequest(id, ScaleServiceImpl.SCALE, CvId.SCALES.getId());
		this.termValidator.validate(term, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		scaleDetails.setId(id);

		// Note: Validate scale data
		this.scaleValidator.validate(scaleDetails, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try {
			Scale scale = new Scale(new Term(StringUtil.parseInt(scaleDetails.getId(), null), scaleDetails.getName().trim(), scaleDetails.getDescription().trim()));

			Integer dataTypeId = StringUtil.parseInt(scaleDetails.getDataType().getId(), null);

			scale.setDataType(DataType.getById(dataTypeId));

			ValidValues validValues =
					Objects.equals(scaleDetails.getValidValues(), null) ? new ValidValues() : scaleDetails.getValidValues();

			if (Objects.equals(dataTypeId, CATEGORICAL_VARIABLE.getId())) {
				for (org.ibp.api.domain.ontology.TermSummary category : validValues.getCategories()) {
					scale.addCategory(new TermSummary(null, category.getName().trim(), category.getDescription().trim()));
				}
			}
			if (Objects.equals(dataTypeId, NUMERIC_VARIABLE.getId())) {
				String min = scaleDetails.getValidValues().getMin() == null ? null : scaleDetails.getValidValues().getMin();
				String max = scaleDetails.getValidValues().getMax() == null ? null : scaleDetails.getValidValues().getMax();
				scale.setMinValue(min);
				scale.setMaxValue(max);
			}

			this.ontologyScaleDataManager.updateScale(scale);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(ScaleServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public void deleteScale(String id) {
		// Note: Validate Id for valid format and check if scale exists or not
		this.validateId(id, ScaleServiceImpl.SCALE);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ScaleServiceImpl.SCALE);

		// Note: Check if scale is deletable or not by checking its usage in variable
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(id), ScaleServiceImpl.SCALE, CvId.SCALES.getId()), errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			this.ontologyScaleDataManager.deleteScale(StringUtil.parseInt(id, null));
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(ScaleServiceImpl.ERROR_MESSAGE, e);
		}
	}

}
