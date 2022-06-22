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
		final String entity = this.messageSource.getMessage(importResponse.getEntity(), null, LocaleContextHolder.getLocale());
		final Map<String, String> messageInfo = this.createInfoMessage(this.messageSource.getMessage("message.created.successfully",
			new Object[] {importResponse.getCreatedSize(), importResponse.getImportListSize(), entity},
			LocaleContextHolder.getLocale()));
		messagesList.add(messageInfo);
		this.addErrorMessageList(messagesList, importResponse.getErrors());
		return messagesList;
	}

	public List<Map<String, String>> getMessagesList(final BrapiUpdateResponse<T> variableUpdateResponse) {
		final List<Map<String, String>> messagesList = new ArrayList<>();
		if (CollectionUtils.isEmpty(variableUpdateResponse.getErrors())) {
			final String entity = this.messageSource.getMessage(variableUpdateResponse.getEntity(), null, LocaleContextHolder.getLocale());
			final Map<String, String> messageInfo = this.createInfoMessage(this.messageSource.getMessage("message.updated.successfully",
				new Object[] {variableUpdateResponse.getEntityName(), entity},
				LocaleContextHolder.getLocale()));
			messagesList.add(messageInfo);
		} else {
			this.addErrorMessageList(messagesList, variableUpdateResponse.getErrors());
		}
		return messagesList;
	}

	public List<Map<String, String>> getMessagesList(final BrapiBatchUpdateResponse<T> batchUpdateResponse) {
		final List<Map<String, String>> messagesList = new ArrayList<>();
		final String entity = this.messageSource.getMessage(batchUpdateResponse.getEntity(), null, LocaleContextHolder.getLocale());
		final Map<String, String> messageInfo = this.createInfoMessage(this.messageSource.getMessage("message.batch.updated.successfully",
			new Object[] {batchUpdateResponse.getUpdatedSize(), batchUpdateResponse.getUpdateListSize(), entity},
			LocaleContextHolder.getLocale()));
		messagesList.add(messageInfo);
		this.addErrorMessageList(messagesList, batchUpdateResponse.getErrors());
		return messagesList;
	}

	private Map<String, String> createInfoMessage(final String message) {
		final Map<String, String> messageInfo = new HashMap<>();
		final String messageHeader = this.messageSource.getMessage("message.header", null, LocaleContextHolder.getLocale());
		final String messageTypeHeader = this.messageSource.getMessage("message.type.header", null, LocaleContextHolder.getLocale());
		messageInfo.put(messageHeader, message);
		messageInfo.put(messageTypeHeader, this.messageSource.getMessage("message.type.info", null, LocaleContextHolder.getLocale()));
		return messageInfo;
	}

	private Map<String, String> createErrorMessage(final String errorHeader, final String message) {
		final Map<String, String> messageError = new HashMap<>();
		final String messageHeader = this.messageSource.getMessage("message.header", null, LocaleContextHolder.getLocale());
		final String messageTypeHeader = this.messageSource.getMessage("message.type.header", null, LocaleContextHolder.getLocale());
		messageError.put(messageHeader, message);
		messageError.put(messageTypeHeader, errorHeader);
		return messageError;
	}

	private void addErrorMessageList(final List<Map<String, String>> messagesList, final List<ObjectError> errors) {
		if (!CollectionUtils.isEmpty(errors)) {
			int index = 1;
			for (final ObjectError error : errors) {
				final String errorHeader = this.messageSource.getMessage("message.type.error", null, LocaleContextHolder.getLocale());
				messagesList.add(this.createErrorMessage(errorHeader, errorHeader + index++ + " " + this.messageSource
					.getMessage(error.getCode(), error.getArguments(), LocaleContextHolder.getLocale())));
			}
		}
	}

}
