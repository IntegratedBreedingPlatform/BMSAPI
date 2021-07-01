
package org.ibp.api.java.impl.middleware.ontology;

import com.google.common.collect.Sets;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermRelationship;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.TermRelationshipId;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.domain.ontology.Category;
import org.ibp.api.domain.ontology.MetadataDetails;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.java.impl.middleware.ontology.validator.MiddlewareIdFormatValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ScaleServiceImplTest {

	private static final String DATA_TYPE = "dataType";

	private static final String NAME = "name";

	private static final String VALID_VALUES = "validValues";

	private static final String DESCRIPTION = "description";

	private static final Integer NUMERIC_SCALE = 8267;

	private static final Integer UNUSED_SCALE = 8000;

	private static final Integer CATEGORICAL_SCALE = 50965;

	@Mock
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private TermDataManager termDataManager;

	@Mock
	private MiddlewareIdFormatValidator idFormatValidator;

	@Mock
	private TermValidator termValidator;

	@InjectMocks
	private final ScaleServiceImpl scaleServiceImpl = new ScaleServiceImpl();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetScaleByIdForScaleUsedInVariableButNotInStudies() {
		Mockito.doReturn(this.createNumericScale()).when(this.ontologyScaleDataManager).getScaleById(ScaleServiceImplTest.NUMERIC_SCALE,
				true);
		Mockito.doReturn(this.getTermRelationships()).when(this.termDataManager)
				.getRelationshipsWithObjectAndType(ScaleServiceImplTest.NUMERIC_SCALE, TermRelationshipId.HAS_SCALE);
		Mockito.doReturn(true).when(this.termDataManager).isTermReferred(ScaleServiceImplTest.NUMERIC_SCALE);

		// Method to test
		final ScaleDetails scaleDetails = this.scaleServiceImpl.getScaleById(ScaleServiceImplTest.NUMERIC_SCALE.toString());
		Assert.assertNotNull(scaleDetails);
		Assert.assertEquals(ScaleServiceImplTest.NUMERIC_SCALE.toString(), scaleDetails.getId());

		// Check Variable Usage metadata
		final MetadataDetails metadata = scaleDetails.getMetadata();
		Assert.assertFalse(metadata.getUsage().getVariables().isEmpty());

		// Check that scale is not deletable since it's used by a variable
		Assert.assertFalse(metadata.isDeletable());

		// Check that scale is editable since the variable that uses it is not used in any study
		Assert.assertTrue(metadata.isEditable());
		// "Description" is editable since scale is used by variable and "Valid Values" is editable because scale is numeric
		Assert.assertEquals(Arrays.asList(ScaleServiceImplTest.DESCRIPTION, ScaleServiceImplTest.VALID_VALUES),
				metadata.getEditableFields());
	}

	@Test
	public void testGetScaleByIdForScaleUsedInVariablesAndStudies() {
		Mockito.doReturn(this.createNumericScale()).when(this.ontologyScaleDataManager).getScaleById(ScaleServiceImplTest.NUMERIC_SCALE,
				true);
		Mockito.doReturn(this.getTermRelationships()).when(this.termDataManager)
				.getRelationshipsWithObjectAndType(ScaleServiceImplTest.NUMERIC_SCALE, TermRelationshipId.HAS_SCALE);
		Mockito.doReturn(true).when(this.termDataManager).isTermReferred(ScaleServiceImplTest.NUMERIC_SCALE);
		Mockito.doReturn(true).when(this.ontologyVariableDataManager).areVariablesUsedInStudy(Matchers.anyListOf(Integer.class));

		// Method to test
		final ScaleDetails scaleDetails = this.scaleServiceImpl.getScaleById(ScaleServiceImplTest.NUMERIC_SCALE.toString());
		Assert.assertNotNull(scaleDetails);
		Assert.assertEquals(ScaleServiceImplTest.NUMERIC_SCALE.toString(), scaleDetails.getId());

		// Check Variable Usage metadata
		final MetadataDetails metadata = scaleDetails.getMetadata();
		Assert.assertFalse(metadata.getUsage().getVariables().isEmpty());

		// Check that scale is not deletable since it's used by a variable
		Assert.assertFalse(metadata.isDeletable());

		// Check that scale is not editable since the variable that uses it is used in a study
		Assert.assertFalse(metadata.isEditable());
		// Only "Description" is editable since scale is used by variable in a study
		Assert.assertEquals(Arrays.asList(ScaleServiceImplTest.DESCRIPTION), metadata.getEditableFields());
	}

	@Test
	public void testGetScaleByIdForCategoricalScaleUsedInStudy() {
		Mockito.doReturn(this.createCategoricalScale()).when(this.ontologyScaleDataManager)
				.getScaleById(ScaleServiceImplTest.CATEGORICAL_SCALE, true);
		Mockito.doReturn(this.getTermRelationships()).when(this.termDataManager)
				.getRelationshipsWithObjectAndType(ScaleServiceImplTest.CATEGORICAL_SCALE, TermRelationshipId.HAS_SCALE);
		Mockito.doReturn(true).when(this.termDataManager).isTermReferred(ScaleServiceImplTest.CATEGORICAL_SCALE);
		Mockito.doReturn(true).when(this.ontologyVariableDataManager).areVariablesUsedInStudy(Matchers.anyListOf(Integer.class));
		// Only 3 out of 5 valid values are being used in non-deleted studies
		final Set<String> usedCategories = Sets.newHashSet("1", "2", "3");
		Mockito.doReturn(usedCategories).when(this.termDataManager)
				.getCategoriesInUse(ScaleServiceImplTest.CATEGORICAL_SCALE);

		// Method to test
		final ScaleDetails scaleDetails = this.scaleServiceImpl.getScaleById(ScaleServiceImplTest.CATEGORICAL_SCALE.toString());
		Assert.assertNotNull(scaleDetails);

		// Check the categorical values and which ones are editable (not used in studies)
		Assert.assertEquals(ScaleServiceImplTest.CATEGORICAL_SCALE.toString(), scaleDetails.getId());
		final List<Category> categories = scaleDetails.getValidValues().getCategories();
		Assert.assertEquals(5, categories.size());
		for (final Category category : categories) {
			Assert.assertEquals(!usedCategories.contains(category.getName()), category.isEditable());
		}

		// Check Variable Usage metadata
		final MetadataDetails metadata = scaleDetails.getMetadata();
		Assert.assertFalse(metadata.getUsage().getVariables().isEmpty());

		// Check that scale is not deletable since it's used by a variable
		Assert.assertFalse(metadata.isDeletable());

		// Check that scale is not editable since the variable that uses it is used in a study
		Assert.assertFalse(metadata.isEditable());
		// "Description" is editable since scale is used by variable in a study and "Valid Values" since scale has categorical data type
		Assert.assertEquals(Arrays.asList(ScaleServiceImplTest.DESCRIPTION, ScaleServiceImplTest.VALID_VALUES),
				metadata.getEditableFields());
	}

	@Test
	public void testGetScaleByIdForUnusedScale() {
		Mockito.doReturn(this.createCharacterScale()).when(this.ontologyScaleDataManager).getScaleById(ScaleServiceImplTest.UNUSED_SCALE,
				true);

		// Method to test
		final ScaleDetails scaleDetails = this.scaleServiceImpl.getScaleById(ScaleServiceImplTest.UNUSED_SCALE.toString());
		Assert.assertNotNull(scaleDetails);
		Assert.assertEquals(ScaleServiceImplTest.UNUSED_SCALE.toString(), scaleDetails.getId());

		// Check that scale is editable and deletable since it's unused in any variable or study
		final MetadataDetails metadata = scaleDetails.getMetadata();
		Assert.assertTrue(metadata.isDeletable());
		Assert.assertTrue(metadata.isEditable());
		Assert.assertEquals(Arrays.asList(ScaleServiceImplTest.NAME, ScaleServiceImplTest.DESCRIPTION, ScaleServiceImplTest.DATA_TYPE),
				metadata.getEditableFields());
	}

	private Scale createNumericScale() {
		final Scale scale = new Scale(new Term());
		scale.setName("SEED_AMOUNT_kg");
		scale.setDefinition("for kg - Weighed");
		scale.setId(ScaleServiceImplTest.NUMERIC_SCALE);
		scale.setDataType(DataType.NUMERIC_VARIABLE);
		return scale;
	}

	private Scale createCharacterScale() {
		final Scale scale = new Scale(new Term());
		scale.setName("NOTEZ");
		scale.setDefinition("Notez taken");
		scale.setId(ScaleServiceImplTest.UNUSED_SCALE);
		scale.setDataType(DataType.CHARACTER_VARIABLE);
		return scale;
	}

	private Scale createCategoricalScale() {
		final Scale scale = new Scale(new Term());
		scale.setName("1-5 damage scoring scale");
		scale.setDefinition("1-5 damage scoring scale");
		scale.setId(ScaleServiceImplTest.CATEGORICAL_SCALE);
		scale.setDataType(DataType.CATEGORICAL_VARIABLE);
		scale.addCategory(new TermSummary(1, "1", "clean, no damage"));
		scale.addCategory(new TermSummary(1, "2", "slightly (20%) damaged"));
		scale.addCategory(new TermSummary(1, "3", "moderately (50% ) damaged"));
		scale.addCategory(new TermSummary(1, "4", "Significantly (70%) damaged"));
		scale.addCategory(new TermSummary(1, "5", "highly (>80%) damaged"));
		return scale;
	}

	private List<TermRelationship> getTermRelationships() {
		final TermRelationship r1 = new TermRelationship();
		r1.setSubjectTerm(new Term(20422, "BactSRInc_Cmp_pct", "Bacterial stalk rot incidence BY BactSRInc - Computation IN %"));
		return Arrays.asList(r1);
	}

}
