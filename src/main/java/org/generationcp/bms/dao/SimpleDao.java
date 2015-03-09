package org.generationcp.bms.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SimpleDao {

	private JdbcTemplate jdbcTemplate;

	@Autowired
    public SimpleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	public List<String> getAllCentralCropSchemaNames() {
		
		List<Map<String, Object>> queryResults = this.jdbcTemplate.queryForList(
				"SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA where SCHEMA_NAME like 'ibdbv2_%_central'");
		
		List<String> schemaNames = new ArrayList<String>();
		
		for (Map<String, Object> row : queryResults) {
			schemaNames.add((String) row.get("SCHEMA_NAME"));
		}
		
		return schemaNames;
	}
}