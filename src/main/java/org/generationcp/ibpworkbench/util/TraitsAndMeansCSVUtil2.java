package org.generationcp.ibpworkbench.util;

import au.com.bytecode.opencsv.CSVReader;
import com.Ostermiller.util.CSVParser;

import java.io.FileReader;
import java.net.URL;
import java.util.*;

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

public class TraitsAndMeansCSVUtil2 {
    public Map<String, ArrayList<String>> csvToMap(String fileName) throws Exception {
        CSVReader reader = new CSVReader(new FileReader(fileName));
        Map<String, ArrayList<String>> csvMap = new LinkedHashMap<String, ArrayList<String>>();
        String[] header = reader.readNext();
        String[] nextLine;
        int x = 0;
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine != null) {
            	if (x == 0){
        		  	if (isNumeric(nextLine[0])){
        		  		header[0] = "SITENO";
        		  	}else{
        		  		header[0] = "SITECODE";
        		  	}
        			for(String headerCol : header) {
        	            csvMap.put(headerCol.trim(), new ArrayList<String>());
        	        }
        			x++;
            	}
                for(int i = 0; i < header.length; i++) {
                    csvMap.get(header[i].toString().trim()).add(nextLine[i].trim());
                }
            }
        }
        return csvMap;
    }
    
    public boolean isNumeric(String val){
    	try{
    		Double.parseDouble(val);
    		return true;
    	}catch (NumberFormatException e){
    		return false;
    	}
    	
    	
    }
}
