package org.generationcp.bms.util;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

public class Init {

    private static final Locale getLocale(){
        return LocaleContextHolder.getLocale();
    }

    public static String formatErrorMessage(ResourceBundleMessageSource messageSource, String errorCode, Object[] parameters){
        return messageSource.getMessage(errorCode, parameters, Init.getLocale());
    }
}
