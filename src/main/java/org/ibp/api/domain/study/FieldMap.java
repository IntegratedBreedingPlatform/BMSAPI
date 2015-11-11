
package org.ibp.api.domain.study;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldMap {

	private Integer blockId;

	private String blockName;

	private FieldMapMetaData fieldMapMetaData = new FieldMapMetaData();

	private Map<Integer, List<FieldPlot>> range = new HashMap<>();

	private Map<Integer, List<FieldPlot>> columns = new HashMap<>();

	private FieldPlot[][] plots;

	public FieldMap() {
	}

	public FieldPlot[][] getPlots() {
		return plots;
	}

	public void setPlots(FieldPlot[][] fieldPlots) {
		this.plots = fieldPlots;
	}

	public String getBlockName() {
		return this.blockName;
	}

	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}

	public Integer getBlockId() {
		return this.blockId;
	}

	public void setBlockId(Integer blockId) {
		this.blockId = blockId;
	}

	public FieldMapMetaData getFieldMapMetaData() {
		return this.fieldMapMetaData;
	}

	public void setFieldMapMetaData(FieldMapMetaData fieldMapMetaData) {
		this.fieldMapMetaData = fieldMapMetaData;
	}

	public Map<Integer, List<FieldPlot>> getRange() {
		return this.range;
	}

	public void setRange(Map<Integer, List<FieldPlot>> range) {
		this.range = range;
	}

	public Map<Integer, List<FieldPlot>> getColumns() {
		return this.columns;
	}

	public void setColumns(Map<Integer, List<FieldPlot>> columns) {
		this.columns = columns;
	}

}
