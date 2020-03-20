package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.inventory.manager.TransactionDto;

import java.io.File;
import java.util.List;

public interface TransactionExportService {

	File export(final List<TransactionDto> transactionDtoList);

}
