package org.generationcp.bms.ontology.util;

import java.util.List;

public class Validator {

    public static boolean validateIsEmpty(String dataToValidate){
        if(dataToValidate.isEmpty() || dataToValidate == null) return true;
        return false;
    }

    public static boolean validateList(List<?> list){
        if(list.size() == 0 || list == null) return true;
        return false;
    }
}
