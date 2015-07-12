
package org.ibp.api.domain.study;

public class FieldMapPlantingDetails {

	private String fieldLocation;

	private String fieldName;

	private String blockCapacity;

	private Integer rowsPerPlot;

	private Integer columns;

	private String startingCoordinates;

	private String plotLayout;

	private Integer rowCapacityOfPlantingMachine;

	public String getFieldLocation() {
		return this.fieldLocation;
	}

	public void setFieldLocation(String fieldLocation) {
		this.fieldLocation = fieldLocation;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getBlockCapacity() {
		return this.blockCapacity;
	}

	public void setBlockCapacity(String blockCapacity) {
		this.blockCapacity = blockCapacity;
	}

	public Integer getRowsPerPlot() {
		return this.rowsPerPlot;
	}

	public void setRowsPerPlot(Integer rowsPerPlot) {
		this.rowsPerPlot = rowsPerPlot;
	}

	public Integer getColumns() {
		return this.columns;
	}

	public void setColumns(Integer columns) {
		this.columns = columns;
	}

	public String getStartingCoordinates() {
		return this.startingCoordinates;
	}

	public void setStartingCoordinates(String startingCoordinates) {
		this.startingCoordinates = startingCoordinates;
	}

	public String getPlotLayout() {
		return this.plotLayout;
	}

	public void setPlotLayout(String plotLayout) {
		this.plotLayout = plotLayout;
	}

	public Integer getRowCapacityOfPlantingMachine() {
		return this.rowCapacityOfPlantingMachine;
	}

	public void setRowCapacityOfPlantingMachine(Integer rowCapacityOfPlantingMachine) {
		this.rowCapacityOfPlantingMachine = rowCapacityOfPlantingMachine;
	}

}
