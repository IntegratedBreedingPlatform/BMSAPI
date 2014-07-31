package org.generationcp.ibpworkbench.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


















import javassist.bytecode.Descriptor.Iterator;

import org.generationcp.middleware.domain.dms.ExperimentValues;
import org.generationcp.middleware.domain.dms.VariableTypeList;

import au.com.bytecode.opencsv.CSVReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class SummaryStatsCSV {
	
    
    private final static Logger LOG = LoggerFactory.getLogger(SummaryStatsCSV.class); 
    private String fileName;
    private Map<String, Map<String, ArrayList<String> >> data;
    private HashMap<String, String> nameToAliasMapping;
    private String[] header;
    
    public SummaryStatsCSV(String fileName,
			HashMap<String, String> nameToAliasMapping) {
    	this.fileName = fileName;
    	this.nameToAliasMapping = nameToAliasMapping;
	}

	public List<String> getHeader() throws Exception{
    	
    	data = getData();
    	
    	return Arrays.asList(header);
    }
    
    public List<String> getHeaderStats() throws Exception{
    	
    	data = getData();
    	
    	ArrayList<String> list = new ArrayList<String>(Arrays.asList(header));
    	list.remove(0);
    	list.remove(0);
    	return list;
    }
    
    public String getTrialHeader() throws Exception{
    	
    	return nameToAliasMapping.get(getHeader().get(0));
    }
    
    public Map<String, Map<String, ArrayList<String> >> getData() throws Exception {
    	
    	if (data != null) return data;
    	
    	CSVReader reader = new CSVReader(new FileReader(fileName));
        data = new LinkedHashMap<String, Map<String, ArrayList<String>>>();
        this.header = reader.readNext();
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            
        	String environment = nextLine[0].trim();
            String trait = null;
			
			trait = nameToAliasMapping.get(nextLine[1]).trim();
			if (trait == null) trait = nextLine[1].trim();
			
        	if(!data.containsKey(environment)) {
        		data.put(environment, new LinkedHashMap<String, ArrayList<String>>());
        	}
        	
        	if(!data.get(environment).containsKey(trait)){
    			data.get(environment).put(trait,  new ArrayList<String>());
    		}
        	 for(int i = 2; i < header.length; i++) {
        		 data.get(environment).get(trait).add(nextLine[i].trim());
             }
            
        }
        
        reader.close();
        return data;
    }
    
}
