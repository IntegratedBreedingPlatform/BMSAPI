package org.generationcp.bms.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.bms.domain.Trait;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariableSummary;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class SimpleDao {

	private JdbcTemplate jdbcTemplate;

	@Autowired
    public SimpleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	/**
	 * Reads summary of a standard variable from a database view <strong>standard_variable_summary</strong>.
	 * 
	 *  Note: view definition is in src/main/resources/sql/View_Standard_Variable_Summary.sql, make sure the view exists before using this method. 
	 * 
	 * @param standardVariableId
	 * @return
	 * @throws MiddlewareQueryException
	 */
	public StandardVariableSummary getStandardVariableSummary(Integer standardVariableId) throws MiddlewareQueryException {
        
		StandardVariableSummary result = this.jdbcTemplate.queryForObject(
				"SELECT * FROM standard_variable_summary where id = ?",
				new Object[] { standardVariableId },
				new RowMapper<StandardVariableSummary>() {

			@Override
			public StandardVariableSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
				
				StandardVariableSummary variable = new StandardVariableSummary(rs.getInt("id"), rs.getString("name"), rs.getString("definition"));
				
				variable.setProperty(new TermSummary(rs.getInt("property_id"), rs.getString("property_name"), rs.getString("property_def")));
				variable.setMethod(new TermSummary(rs.getInt("method_id"), rs.getString("method_name"), rs.getString("method_def")));
				variable.setScale(new TermSummary(rs.getInt("scale_id"), rs.getString("scale_name"), rs.getString("scale_def")));
				
				variable.setDataType(new TermSummary(rs.getInt("data_type_id"), rs.getString("data_type_name"), rs.getString("data_type_def")));
				variable.setStoredIn(new TermSummary(rs.getInt("stored_in_id"), rs.getString("stored_in_name"), rs.getString("stored_in_def")));
				variable.setIsA(new TermSummary(rs.getInt("is_a_id"), rs.getString("is_a_name"), rs.getString("is_a_def")));
				
				variable.setPhenotypicType(PhenotypicType.valueOf(rs.getString("phenotypic_type")));
											
				return variable;
			}
		});
		
		return result;
		
    }

	/**
	 * Returns a list of traits for the given study (identified by the {@code studyId})
	 * where measurements are avaibale. If study design intended for traits but
	 * no measurements are avaibale for those traits, they are not included.
	 * 
	 * Relies on the database views <strong>germplasm_trial_details</strong> and <strong>standard_variable_details</strong>.
	 * 
	 * @param studyId the identifier of the study
	 * @return matching traits with measurement count for each. Empty list if no such match is found. Never returns {@code null}
	 */
	public List<Trait> getMeasuredTraits(int studyId) {

		List<Trait> measuredTraits = new ArrayList<Trait>();
		List<Map<String, Object>> queryResults = this.jdbcTemplate
				.queryForList(""
						+ "select gtd.stdvar_id, svd.stdvar_name, svd.stdvar_definition, svd.property, svd.method, svd.scale, svd.type, count(gtd.observed_value) as total_observations "
						+ " from germplasm_trial_details gtd "
						+ " inner join standard_variable_details svd on svd.cvterm_id =  gtd.stdvar_id"
						+ " where study_id = " + studyId
						+ " group by stdvar_id;");

		for (Map<String, Object> row : queryResults) {
			Trait trait = new Trait((Integer) row.get("stdvar_id"));
			trait.setName((String) row.get("stdvar_name"));
			trait.setDescription((String) row.get("stdvar_definition"));
			trait.setProperty((String) row.get("property"));
			trait.setMethod((String) row.get("method"));
			trait.setScale((String) row.get("scale"));
			trait.setType((String) row.get("type"));
			trait.setNumberOfMeasurements((Long) row.get("total_observations"));
			measuredTraits.add(trait);
		}
		return measuredTraits;
	}

	public List<Observation> getTraitObservationsForTrial(Integer trialEnvironmentId) {
		return new ArrayList<Observation>();
	}
}