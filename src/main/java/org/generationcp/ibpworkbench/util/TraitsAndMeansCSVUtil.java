package org.generationcp.ibpworkbench.util;

import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import org.generationcp.ibpworkbench.model.TraitsAndMeans;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

@Configurable
public class TraitsAndMeansCSVUtil {
    private Map<String, String> columnMapping;

    public Map<String, String> getColumnMapping() {
        return columnMapping;
    }

    public void setColumnMapping(Map<String, String> columnMapping) {
        this.columnMapping = columnMapping;
    }

    /*
         * @param fileName name of the file located in the classpath
         * @return List<TraitsAndMeans> list of rows in csv, list is empty if no data
         * List contains a map with column header as key
         */
    public List<TraitsAndMeans> csvToList(String fileName)throws Exception {
        URL url = TraitsAndMeansCSVUtil.class.getClassLoader().getResource(fileName);
        CsvToBean<TraitsAndMeans> bean = new CsvToBean<TraitsAndMeans>();

        //Define strategy
        GCPHeaderColumnNameTranslateMappingStrategy<TraitsAndMeans> strategy =
                new GCPHeaderColumnNameTranslateMappingStrategy<TraitsAndMeans>();
        strategy.setType(TraitsAndMeans.class);
        strategy.setColumnMapping(columnMapping);

        //Parse the CSV
        List<TraitsAndMeans> traitsAndMeans = bean.parse(strategy, new FileReader(url.getFile()));
        return traitsAndMeans;
    }
}

class GCPHeaderColumnNameTranslateMappingStrategy<T> extends HeaderColumnNameTranslateMappingStrategy<T> {
    protected String getColumnName(int col) {
        return col < header.length ? getColumnMapping().get(header[col].trim().toUpperCase()) : null;
    }
}
