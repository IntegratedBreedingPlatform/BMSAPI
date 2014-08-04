package org.generationcp.bms.util;

public enum VariableCategory {
    
	 STUDY(1), 
	 PLOT(2),
	 TRAITS(3),
	 TRIAL_ENVIRONMENT(4),
	 TREATMENT_FACTORS(5),
	 SELECTION_VARIATES(6),
	 NURSERY_CONDITIONS(7),
	 GERMPLASM(8);
	 
	 private final int id;
	  
	 VariableCategory(int id) {
		 this.id = id;
	 }

	public int getId() {
		return id;
	}
}
