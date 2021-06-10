
package org.ibp.api.java.impl.middleware.ontology;

import static org.generationcp.middleware.domain.ontology.DataType.CATEGORICAL_VARIABLE;
import static org.generationcp.middleware.domain.ontology.DataType.NUMERIC_VARIABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

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
import com.google.common.collect.Lists;

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
			final List<Scale> scales = this.ontologyScaleDataManager.getAllScales();
			final List<ScaleDetails> scaleSummaries = new ArrayList<>();

			final ModelMapper mapper = OntologyMapper.getInstance();

			for (final Scale scale : scales) {
				final ScaleDetails scaleDetail = mapper.map(scale, ScaleDetails.class);
				scaleSummaries.add(scaleDetail);
			}
			return scaleSummaries;
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException(ScaleServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public ScaleDetails getScaleById(final String id) {
		this.validateId(id, ScaleServiceImpl.SCALE);
		// Note: Validate Scale Id for valid format and scale exists or not
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ScaleServiceImpl.SCALE);
		final TermRequest term = new TermRequest(id, ScaleServiceImpl.SCALE, CvId.SCALES.getId());
		this.termValidator.validate(term, errors);

		// If any error occurs then throws Exception with error messages
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		final Integer scaleId = StringUtil.parseInt(id, null);
		final Scale scale = this.ontologyScaleDataManager.getScaleById(scaleId, true);
		if (scale == null) {
			return null;
		}

		final ModelMapper mapper = OntologyMapper.getInstance();
		final ScaleDetails scaleDetails = mapper.map(scale, ScaleDetails.class);

		final DataType dataType = DataType.getById(scale.getDataType().getId());

		// Get list of variables using the scale
		final List<TermRelationship> relationships =
				this.termDataManager.getRelationshipsWithObjectAndType(scaleId, TermRelationshipId.HAS_SCALE);

		Collections.sort(relationships, new Comparator<TermRelationship>() {

			@Override
			public int compare(final TermRelationship l, final TermRelationship r) {
				return l.getSubjectTerm().getName().compareToIgnoreCase(r.getSubjectTerm().getName());
			}
		});

		// Add variables of scale in Scale's Metadata
		for (final TermRelationship relationship : relationships) {
			final org.ibp.api.domain.ontology.TermSummary termSummary =
					mapper.map(relationship, org.ibp.api.domain.ontology.TermSummary.class);
			scaleDetails.getMetadata().getUsage().addUsage(termSummary);
		}

		Boolean deletable = Boolean.TRUE;
		Boolean editable = Boolean.TRUE;

		if (this.termDataManager.isTermReferred(scaleId)) {
			// Scale will only be deletable if it's not used by any variable
			deletable = false;

			final List<Integer> variablesIds = this.getVariablesIds(relationships);

			editable = !this.ontologyVariableDataManager.areVariablesUsedInStudy(variablesIds);

			// If scale is categorical, determine which categories could be edited (ie. those not used in existing studies)
			if (Objects.equals(scale.getDataType().getId(), CATEGORICAL_VARIABLE.getId()) && !editable) {
				final List<String> categories = this.termDataManager.getCategoriesInUse(scaleId);

				for (final Category category : scaleDetails.getValidValues().getCategories()) {
					if (categories.contains(category.getName())) {
						category.setEditable(Boolean.FALSE);
					}
				}
			}

		}

		if (!dataType.isSystemDataType()) {
			if (!deletable) {
				scaleDetails.getMetadata().addEditableField(ScaleServiceImpl.FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED);

			} else {
				scaleDetails.getMetadata().addEditableField("name");
				scaleDetails.getMetadata().addEditableField(ScaleServiceImpl.FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED);
				scaleDetails.getMetadata().addEditableField("dataType");

			}

			if (editable && Objects.equals(scale.getDataType().getId(), NUMERIC_VARIABLE.getId())
					|| Objects.equals(scale.getDataType().getId(), CATEGORICAL_VARIABLE.getId())) {
				scaleDetails.getMetadata().addEditableField("validValues");
			}

			scaleDetails.getMetadata().setDeletable(deletable);
		} else {
			scaleDetails.getMetadata().setDeletable(false);
		}
		scaleDetails.getMetadata().setEditable(editable);

		return scaleDetails;
	}

	private List<Integer> getVariablesIds(final List<TermRelationship> relationships) {
		return Lists.transform(relationships, new Function<TermRelationship, Integer>() {

			@Nullable
			@Override
			public Integer apply(final TermRelationship termRelationship) {
				return termRelationship.getSubjectTerm().getId();
			}
		});
	}

	@Override
	public GenericResponse addScale(final ScaleDetails scaleDetail) {
		// Note: Set id to null because add scale does not need id
		scaleDetail.setId(null);
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ScaleServiceImpl.SCALE);
		this.scaleValidator.validate(scaleDetail, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			final Scale scale = new Scale();
			scale.setName(scaleDetail.getName().trim());
			scale.setDefinition(scaleDetail.getDescription().trim());

			final Integer dataTypeId = StringUtil.parseInt(scaleDetail.getDataType().getId(), null);
			scale.setDataType(DataType.getById(dataTypeId));

			if (Objects.equals(dataTypeId, CATEGORICAL_VARIABLE.getId())) {
				for (final org.ibp.api.domain.ontology.TermSummary category : scaleDetail.getValidValues().getCategories()) {
					scale.addCategory(new TermSummary(null, category.getName().trim(), category.getDescription().trim()));
				}
			}
			if (Objects.equals(dataTypeId, NUMERIC_VARIABLE.getId())) {
				final String min = scaleDetail.getValidValues().getMin() == null ? null : scaleDetail.getValidValues().getMin();
				final String max = scaleDetail.getValidValues().getMax() == null ? null : scaleDetail.getValidValues().getMax();
				scale.setMinValue(min);
				scale.setMaxValue(max);
			}

			this.ontologyScaleDataManager.addScale(scale);
			return new GenericResponse(String.valueOf(scale.getId()));
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException(ScaleServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public void updateScale(final String id, final ScaleDetails scaleDetails) {
		this.validateId(id, ScaleServiceImpl.SCALE);
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ScaleServiceImpl.SCALE);
		final TermRequest term = new TermRequest(id, ScaleServiceImpl.SCALE, CvId.SCALES.getId());
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
			final Scale scale = new Scale(new Term(StringUtil.parseInt(scaleDetails.getId(), null), scaleDetails.getName().trim(),
					scaleDetails.getDescription().trim()));

			final Integer dataTypeId = StringUtil.parseInt(scaleDetails.getDataType().getId(), null);

			scale.setDataType(DataType.getById(dataTypeId));

			final ValidValues validValues =
					Objects.equals(scaleDetails.getValidValues(), null) ? new ValidValues() : scaleDetails.getValidValues();

			if (Objects.equals(dataTypeId, CATEGORICAL_VARIABLE.getId())) {
				for (final org.ibp.api.domain.ontology.TermSummary category : validValues.getCategories()) {
					scale.addCategory(new TermSummary(null, category.getName().trim(), category.getDescription().trim()));
				}
			}
			if (Objects.equals(dataTypeId, NUMERIC_VARIABLE.getId())) {
				// if scale is numerical

				final String min = scaleDetails.getValidValues().getMin() == null ? null : scaleDetails.getValidValues().getMin();
				final String max = scaleDetails.getValidValues().getMax() == null ? null : scaleDetails.getValidValues().getMax();
				scale.setMinValue(min);
				scale.setMaxValue(max);
			}

			this.ontologyScaleDataManager.updateScale(scale);

		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException(ScaleServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public void deleteScale(final String id) {
		// Note: Validate Id for valid format and check if scale exists or not
		this.validateId(id, ScaleServiceImpl.SCALE);
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ScaleServiceImpl.SCALE);

		// Note: Check if scale is deletable or not by checking its usage in variable
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(id), ScaleServiceImpl.SCALE, CvId.SCALES.getId()), errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			this.ontologyScaleDataManager.deleteScale(StringUtil.parseInt(id, null));
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException(ScaleServiceImpl.ERROR_MESSAGE, e);
		}
	}

}
