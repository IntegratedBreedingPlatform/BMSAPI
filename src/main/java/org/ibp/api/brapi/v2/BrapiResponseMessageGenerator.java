package org.ibp.api.brapi.v2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BrapiResponseMessageGenerator<T> {

	@Autowired
	private ResourceBundleMessageSource messageSource;

	public List<Map<String, String>> getMessagesList(final BrapiImportResponse<T> importResponse) {
		final List<Map<String, String>> messagesList = new ArrayList<>();
		final Map<String, String> messageInfo = new HashMap<>();
		final String messageHeader = this.messageSource.getMessage("message.header", null, LocaleContextHolder.getLocale());
		final String entity = this.messageSource.getMessage(importResponse.getEntity(), null, LocaleContextHolder.getLocale());
		messageInfo.put(messageHeader, this.messageSource.getMessage("message.created.successfully",
			new Object[] {importResponse.getCreatedSize(), importResponse.getImportListSize(), entity},
			LocaleContextHolder.getLocale()));
		final String messageTypeHeader = this.messageSource.getMessage("message.type.header", null, LocaleContextHolder.getLocale());
		messageInfo.put(messageTypeHeader, this.messageSource.getMessage("message.type.info", null, LocaleContextHolder.getLocale()));
		messagesList.add(messageInfo);
		if (!CollectionUtils.isEmpty(importResponse.getErrors())) {
			int index = 1;
			for (final ObjectError error : importResponse.getErrors()) {
				final Map<String, String> messageError = new HashMap<>();
				final String errorHeader = this.messageSource.getMessage("message.type.error", null, LocaleContextHolder.getLocale());
				messageError.put(messageHeader, errorHeader + index++ + " " + this.messageSource
					.getMessage(error.getCode(), error.getArguments(), LocaleContextHolder.getLocale()));
				messageError.put(messageTypeHeader, errorHeader);
				messagesList.add(messageError);
			}
		}
		return messagesList;
	}

}
