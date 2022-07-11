
package org.ibp.api.java.impl.middleware.ontology;

import com.google.common.base.Function;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.util.StringUtil;
import org.generationcp.middleware.util.Util;
import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.ontology.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ModelServiceImpl implements ModelService {

	@Autowired
	private TermDataManager termDataManager;

	@Override
	public List<DataType> getAllDataTypes() {
		return Util.convertAll(Arrays.asList(org.generationcp.middleware.domain.ontology.DataType.values()),
				new Function<org.generationcp.middleware.domain.ontology.DataType, DataType>() {

					@Override
					public DataType apply(org.generationcp.middleware.domain.ontology.DataType dataType) {
						return new DataType(String.valueOf(dataType.getId()), dataType.getName(), dataType.isSystemDataType());
					}
				});
	}

	@Override
	public boolean isNumericDataType(String dataTypeId) {
		Integer id = StringUtil.parseInt(dataTypeId, null);
		return Objects.equals(id, org.generationcp.middleware.domain.ontology.DataType.NUMERIC_VARIABLE.getId());
	}

	@Override
	public boolean isCategoricalDataType(String dataTypeId) {
		Integer id = StringUtil.parseInt(dataTypeId, null);
		return Objects.equals(id, org.generationcp.middleware.domain.ontology.DataType.CATEGORICAL_VARIABLE.getId());
	}

	@Override
	public List<String> getAllClasses() {
		try {
			List<Term> classes = this.termDataManager.getTermByCvId(CvId.TRAIT_CLASS.getId());
			List<String> classList = new ArrayList<>();

			for (Term term : classes) {
				classList.add(term.getName());
			}

			Comparator<String> alphabeticalOrder = new Comparator<String>() {

				@Override
				public int compare(String str1, String str2) {
					int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
					if (res == 0) {
						res = str1.compareTo(str2);
					}
					return res;
				}
			};

			Collections.sort(classList, alphabeticalOrder);

			return classList;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public List<VariableType> getAllVariableTypes(final Boolean excludeRestrictedTypes) {

		List<VariableType> variableTypes =
				Util.convertAll(Arrays.asList(org.generationcp.middleware.domain.ontology.VariableType.values()),
						new Function<org.generationcp.middleware.domain.ontology.VariableType, VariableType>() {

							@Override
							public VariableType apply(org.generationcp.middleware.domain.ontology.VariableType variableType) {
								return new VariableType(String.valueOf(variableType.getId()), variableType.getName(), variableType.getDescription());
							}
						});

		if (Boolean.TRUE.equals(excludeRestrictedTypes)) {
			Optional<VariableType> germplasmDescriptorType = variableTypes.stream().
				filter(variableType ->
					variableType.getId()
						.equals(org.generationcp.middleware.domain.ontology.VariableType.GERMPLASM_DESCRIPTOR.getId().toString())
				).findFirst();
			variableTypes.remove(germplasmDescriptorType.get());
		}

		Collections.sort(variableTypes, new Comparator<VariableType>() {

			@Override
			public int compare(VariableType o1, VariableType o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return variableTypes;
	}
}
