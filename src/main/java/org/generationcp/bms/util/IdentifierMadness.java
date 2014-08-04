package org.generationcp.bms.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IdentifierMadness {
	
	public static final Set<Integer> FILTER_NURSERY_FIELDS = new HashSet<Integer>(Arrays.asList(8070, 8040, 8170, 8020, 8006));
	public static final Set<Integer> HIDE_ID_VARIABLES = new HashSet<Integer>(Arrays.asList(8257, 8262, 8110, 8190, 8372, 8261));
	public static final Set<Integer> HIDE_PLOT_FIELDS = new HashSet<Integer>(Arrays.asList(8150, 8155, 8160, 8170));
	
	public static final List<Integer> SELECTION_VARIATES_PROPERTIES = Arrays.asList(2620, 2670, 2660);

}
