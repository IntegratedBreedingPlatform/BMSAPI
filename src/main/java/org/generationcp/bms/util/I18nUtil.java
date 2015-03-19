package org.generationcp.bms.util;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

//TODO: Should remove this utility and resolve messages automatically via spring boot auto configuration.
public class I18nUtil {

    private static Locale getLocale(){
        return LocaleContextHolder.getLocale();
    }

    public static String formatErrorMessage(ResourceBundleMessageSource messageSource, String errorCode, Object[] parameters){
        return messageSource.getMessage(errorCode, parameters, I18nUtil.getLocale());
    }
}
