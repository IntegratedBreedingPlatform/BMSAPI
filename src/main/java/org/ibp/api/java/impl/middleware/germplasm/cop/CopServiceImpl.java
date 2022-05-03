package org.ibp.api.java.impl.middleware.germplasm.cop;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.generationcp.middleware.api.cropparameter.CropParameterEnum;
import org.generationcp.middleware.api.germplasm.pedigree.cop.BTypeEnum;
import org.generationcp.middleware.api.germplasm.pedigree.cop.CopResponse;
import org.generationcp.middleware.api.germplasm.pedigree.cop.CopUtils;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataService;
import org.generationcp.middleware.exceptions.MiddlewareRequestException;
import org.generationcp.middleware.pojos.CropParameter;
import org.ibp.api.exception.ApiRuntime2Exception;
import org.ibp.api.java.file.FileStorageService;
import org.ibp.api.java.impl.middleware.cropparameter.CropParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;

import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkArgument;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Service
@Transactional
public class CopServiceImpl implements CopService {

	@Autowired
	private CopServiceAsync copServiceAsync;

	@Autowired
	private org.generationcp.middleware.api.germplasm.pedigree.cop.CopService copServiceMiddleware;
	
	@Autowired
	private GermplasmListDataService germplasmListDataService;

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private CropParameterService cropParameterService;

	@Override
	public CopResponse viewCoefficientOfParentage(Set<Integer> gids, final Integer listId,
		final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		if (listId != null) {
			gids = new LinkedHashSet<>(this.germplasmListDataService.getGidsByListId(listId));
		}

		if (this.copServiceAsync.threadExists(gids)) {
			return new CopResponse(this.copServiceAsync.getProgress(gids));
		}

		if (listId != null) {
			final byte[] csv = this.downloadFile(listId);
			if (csv.length > 0) {
				return new CopResponse(true);
			} else {
				throw new MiddlewareRequestException("", "cop.csv.not.exists");
			}
		}

		final Table<Integer, Integer, Double> matrix = this.copServiceMiddleware.getCopMatrixByGids(gids);

		// if all cop values are calculated, return them
		boolean someCopValuesExists = false;
		for (final Integer gid1 : gids) {
			for (final Integer gid2 : gids) {
				if (matrix.contains(gid1, gid2) || matrix.contains(gid2, gid1)) {
					someCopValuesExists = true;
				}
			}
		}
		if (someCopValuesExists) {
			return new CopResponse(matrix);
		}

		// no thread nor matrix for gids
		throw new MiddlewareRequestException("", "cop.no.queue.error");
	}

	@Override
	public CopResponse calculateCoefficientOfParentage(final Set<Integer> gids, final Integer listId, final boolean reset) {

		final Optional<CropParameter> cropParameter = this.cropParameterService.getCropParameter(CropParameterEnum.BTYPE);
		checkArgument(cropParameter.isPresent(), "crop.parameter.not.exists", new String[] {CropParameterEnum.BTYPE.getKey()});
		final Optional<BTypeEnum> btype = BTypeEnum.parse(cropParameter.get().getValue());
		checkArgument(btype.isPresent(), "cop.btype.not.configured");

		Table<Integer, Integer, Double> matrix = HashBasedTable.create();
		// if all cop values are calculated, return them
		boolean requiresProcessing = false;

		if (reset) {
			requiresProcessing = true;
		} else {
			matrix = this.copServiceMiddleware.getCopMatrixByGids(gids);
			for (final Integer gid1 : gids) {
				for (final Integer gid2 : gids) {
					if (!(matrix.contains(gid1, gid2) || matrix.contains(gid2, gid1))) {
						requiresProcessing = true;
					}
				}
			}
		}

		if (requiresProcessing) {
			if (this.copServiceAsync.threadExists(gids)) {
				throw new MiddlewareRequestException("", "cop.gids.in.queue", this.copServiceAsync.getProgress(gids));
			}

			this.copServiceAsync.prepareExecution(gids);
			final Future<Boolean> booleanFuture = this.copServiceAsync.calculateAsync(gids, matrix, listId, btype.get());
			this.copServiceAsync.trackFutureTask(gids, booleanFuture);
			return new CopResponse(this.copServiceAsync.getProgress(gids));
		}

		return new CopResponse(matrix);
	}

	@Override
	public CopResponse calculateCoefficientOfParentage(final Integer listId) {
		final Set<Integer> gids = new LinkedHashSet<>(this.germplasmListDataService.getGidsByListId(listId));
		return this.calculateCoefficientOfParentage(gids, listId, false);
	}

	@Override
	public void cancelJobs(Set<Integer> gids, final Integer listId) {
		if (listId != null) {
			gids = new LinkedHashSet<>(this.germplasmListDataService.getGidsByListId(listId));
		}
		this.copServiceAsync.cancelJobs(gids);
	}

	@Override
	public byte[] downloadFile(final Integer listId) throws IOException {
		if (this.fileStorageService.isConfigured()) {
			try {
				return this.fileStorageService.getFile(CopUtils.getStorageFilePath(listId));
			} catch (final RuntimeException ex) {
				throw new ApiRuntime2Exception("", "cop.file.download.exception");
			}
		} else {
			throw new ApiRuntime2Exception("", "cop.file.storage.not.configured");
		}
	}

}
