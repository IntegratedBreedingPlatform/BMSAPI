package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.domain.fieldbook.FieldmapBlockInfo;
import org.generationcp.middleware.service.api.FieldbookService;
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class DatasetCollectionOrderServiceImpl implements DatasetCollectionOrderService {

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private DataCollectionSorter dataCollectionSorter;

	@Override
	public List<ObservationUnitRow> reorder(
		final CollectionOrder collectionOrder,
		final int trialDatasetId, final String instanceNumber, final List<ObservationUnitRow> observationUnitRows) {

		final String blockId =
			this.fieldbookMiddlewareService.getBlockId(trialDatasetId, instanceNumber);

		FieldmapBlockInfo fieldmapBlockInfo = null;
		if (blockId != null) {
			fieldmapBlockInfo = this.fieldbookMiddlewareService.getBlockInformation(Integer.valueOf(blockId));
		}

		if (collectionOrder == CollectionOrder.PLOT_ORDER || fieldmapBlockInfo == null) {
			// meaning no fieldmap
			// we just return the normal observations
			return observationUnitRows;
		} else if (collectionOrder == CollectionOrder.SERPENTINE_ALONG_ROWS) {
			return this.dataCollectionSorter.orderByRange(fieldmapBlockInfo, observationUnitRows);
		} else if (collectionOrder == CollectionOrder.SERPENTINE_ALONG_COLUMNS) {
			return this.dataCollectionSorter.orderByColumn(fieldmapBlockInfo, observationUnitRows);
		} else {
			return observationUnitRows;
		}

	}

	public enum CollectionOrder {

		PLOT_ORDER(1),
		SERPENTINE_ALONG_ROWS(2),
		SERPENTINE_ALONG_COLUMNS(3);

		private int id;

		private CollectionOrder(int id) {
			this.id = id;
		}

		public static CollectionOrder findById(int id) {
			for (CollectionOrder order : CollectionOrder.values()) {
				if (order.getId() == id) {
					return order;
				}
			}
			return null;
		}

		public int getId() {
			return this.id;
		}

	}

}
