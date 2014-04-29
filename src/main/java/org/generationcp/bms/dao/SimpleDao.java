package org.generationcp.bms.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariableSummary;
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
	 * Reads summay of a standard variable from a database view standard_variable_summary.
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


}