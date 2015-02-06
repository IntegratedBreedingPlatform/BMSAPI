package org.generationcp.bms.dao;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MongoDao {
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	public List<StandardVariable> getAllStandardVariables() {
		return mongoTemplate.findAll(StandardVariable.class, "ontology");
	}

}
