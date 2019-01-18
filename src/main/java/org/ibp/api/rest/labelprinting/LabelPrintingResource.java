package org.ibp.api.rest.labelprinting;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Label Printing Services")
@RestController
public class LabelPrintingResource {

	@RequestMapping(value = "/crops/{cropname}/labelPrinting/labels/summary", method = RequestMethod.GET)
	@ApiOperation(value = "Get Inventory Information A",
		notes = "Returns information about all inventory lots available for the given germplasm id (gid).")
	public Table<String, String, String> getLabelsNeededSummary () {
		Table<String, String, String> table = HashBasedTable.create();
		table.put("1","1","A");
		table.put("1","2","B");
		table.put("2","1","C");
		table.put("2","1","D");
		return table;
	}



}
