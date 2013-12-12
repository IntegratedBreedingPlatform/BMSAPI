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
     *  Site       Trial          Trait             Heritability
	 *	Site1      1              Pant_Height       0.5
	 *	Site1      1              Yield             0.8
	 *
	 * Map format
	 * 
	 *  <site | trial > = list of <heritabilty traits map <key = trait name, value = value>>
     */
    public Map<String, ArrayList<Map<String,String>>> csvToMap(String fileName) throws Exception {
        CSVReader reader = new CSVReader(new FileReader(fileName));
        Map<String, ArrayList<Map<String,String>>> csvMap = new LinkedHashMap<String, ArrayList<Map<String,String>>>();
        reader.readNext();//skip headers - not needed
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            String env = nextLine[0].trim() + "|" + nextLine[1].trim();
        	if(!csvMap.containsKey(env)) {
        		csvMap.put(env, new ArrayList<Map<String,String>>());
        	}
        	Map<String,String> traitHeritabilityMap = new HashMap<String, String>();
        	traitHeritabilityMap.put(nextLine[2].trim(), nextLine[3].trim());
            csvMap.get(env).add(traitHeritabilityMap);
        }
        System.out.println("Environment and Heritability: " + csvMap);
        return csvMap;
    }
    
    public static void main(String[] args) {
    	String fileName = "D:/gcp/workspace/3/breeding_view/output/heritability.csv";
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
