package org.generationcp.ibpworkbench.util;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.middleware.domain.dms.ExperimentValues;
import org.generationcp.middleware.domain.dms.VariableTypeList;

import au.com.bytecode.opencsv.CSVReader;

/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

public class HeritabilityCSVUtil {
	/*
     * CSV Format: 
     * 
     *  <TRIAL NAME>   Trait             Heritability
	 *	1              Plant_Height      0.5
	 *	1              Yield             0.8
	 *
	 * Map format
	 * 
	 *  <site | trial > = list of <heritabilty traits map <key = trait name, value = value>>
     */
    public Map<String, ArrayList<Map<String,String>>> csvToMap(String fileName) throws Exception {
        CSVReader reader = new CSVReader(new FileReader(fileName));
        Map<String, ArrayList<Map<String,String>>> csvMap = new LinkedHashMap<String, ArrayList<Map<String,String>>>();
        String[] header = reader.readNext();
        String envName = header[0].trim();
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            String env = envName + "|" + nextLine[0].trim();
        	if(!csvMap.containsKey(env)) {
        		csvMap.put(env, new ArrayList<Map<String,String>>());
        	}
        	Map<String,String> traitHeritabilityMap = new HashMap<String, String>();
        	traitHeritabilityMap.put(nextLine[1].trim(), nextLine[2].trim());
            csvMap.get(env).add(traitHeritabilityMap);
        }
        System.out.println("Environment and Heritability: " + csvMap);
        reader.close();
        return csvMap;
    }
    
    public static void main(String[] args) {
    	String fileName = "C:/BV/IBWSSummary_6_1.csv";
    	try {
    		
    		Map<String, ArrayList<Map<String,String>>> environmentAndHeritability = new HeritabilityCSVUtil().csvToMap(fileName);			
			Set<String> environments = environmentAndHeritability.keySet();
	        for(String env : environments) {
	    		
	            String[] siteAndTrialInstance = env.split("\\|");
	        	String site = siteAndTrialInstance[0];
	        	String trial = siteAndTrialInstance[1];
	        	System.out.println(site+" = "+trial);
	    	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
