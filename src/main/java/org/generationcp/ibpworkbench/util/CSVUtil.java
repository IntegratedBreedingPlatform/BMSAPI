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
package org.generationcp.ibpworkbench.util;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileReader;
import java.util.*;

public class CSVUtil {

	private HashMap<String, String> nameToAliasMapping;

	public CSVUtil(HashMap<String, String> nameToAliasMapping) {
		this.nameToAliasMapping = nameToAliasMapping;
	}

	public Map<String, ArrayList<String>> csvToMap(String fileName) throws Exception {

		CSVReader reader = new CSVReader(new FileReader(fileName));
		Map<String, ArrayList<String>> csvMap = new LinkedHashMap<String, ArrayList<String>>();
		String[] header = reader.readNext();
		for(String headerCol : header) {
			String aliasLocalName = headerCol.trim().replace("_Means", "").replace("_UnitErrors", "");
			String actualLocalName = nameToAliasMapping.get(aliasLocalName);
			if (actualLocalName != null){
				csvMap.put(headerCol.trim().replace(aliasLocalName, actualLocalName), new ArrayList<String>());
			}
		}
		String[] trimHeader = csvMap.keySet().toArray(new String[0]);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine != null) {
				for(int i = 0; i < header.length; i++) {
					csvMap.get(trimHeader[i]).add(nextLine[i].trim());
				}
			}
		}
		
		reader.close();
		
		return csvMap;
	}
}
