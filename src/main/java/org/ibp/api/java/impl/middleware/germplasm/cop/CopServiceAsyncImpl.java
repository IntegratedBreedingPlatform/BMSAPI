package org.ibp.api.java.impl.middleware.germplasm.cop;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import org.generationcp.middleware.api.germplasm.pedigree.GermplasmTreeNode;
import org.generationcp.middleware.api.germplasm.pedigree.cop.CopCalculation;
import org.generationcp.middleware.api.germplasm.pedigree.cop.CopResponse;
import org.generationcp.middleware.api.germplasm.pedigree.cop.CopUtils;
import org.generationcp.middleware.exceptions.MiddlewareRequestException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.pojos.CopMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import static java.time.Duration.between;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;
import static org.generationcp.middleware.util.Debug.debug;
import static org.generationcp.middleware.util.Debug.info;

@Transactional
public class CopServiceAsyncImpl implements CopServiceAsync {

	private static final Logger LOG = LoggerFactory.getLogger(CopServiceAsyncImpl.class);

	public static final int COP_MAX_JOB_COUNT;
	private static final int COP_MAX_JOB_COUNT_DEFAULT = 1;

	public static final Integer LEVEL;
	private static final Integer LEVEL_DEFAULT = 40;

	static {
		final String envVar = System.getenv("COP_MAX_JOB_COUNT");
		COP_MAX_JOB_COUNT = !isBlank(envVar) ? Integer.parseInt(envVar) : COP_MAX_JOB_COUNT_DEFAULT;

		final String levelVar = System.getenv("COP_PEDIGREE_LEVEL");
		LEVEL = !isBlank(levelVar) ? Integer.parseInt(levelVar) : LEVEL_DEFAULT;
	}
	public static final Semaphore semaphore = new Semaphore(COP_MAX_JOB_COUNT);

	/**
	 * Map gid -> bool (finished, not finished).
	 * Tracks progress
	 */
	private static final Map<Integer, Boolean> gidProcessingQueue = new ConcurrentHashMap<>();
	/**
	 * Groups gids by job (used to group progress by batch)
	 */
	private static final Map<Integer, UUID> gidProcessingToQueueUUID = new ConcurrentHashMap<>();
	/**
	 * Used to cancel jobs
	 */
	private static final Map<Integer, Future<Boolean>> gidProcessingToFutureTask = new ConcurrentHashMap<>();


	private static final boolean INCLUDE_DERIVATIVE_LINES = true;

	private org.generationcp.middleware.api.germplasm.pedigree.cop.CopServiceAsync copServiceAsyncMiddleware;

	@Value("${cop.btype}")
	private int bType;

	public CopServiceAsyncImpl(final HibernateSessionProvider sessionProvider) {
		this.copServiceAsyncMiddleware = new org.generationcp.middleware.api.germplasm.pedigree.cop.CopServiceAsyncImpl(sessionProvider);
	}

	/*
	 * TODO
	 *  - email on finish/error
	 *  - progress considering tree height
	 *  - API to list jobs (cancel by admin)
	 */
	@Override
	@Async
	public Future<Boolean> calculateAsync(
		final Set<Integer> gids,
		final Table<Integer, Integer, Double> matrix,
		final Integer listId) {

		try {
			final TreeBasedTable<Integer, Integer, Double> matrixNew = TreeBasedTable.create();

			// Avoid query multiple times
			final Map<Integer, GermplasmTreeNode> nodes = new HashMap<>();

			// matrix copy because CopCalculation also stores intermediate results
			final CopCalculation copCalculation = new CopCalculation(HashBasedTable.create(matrix), this.bType);

			outer: for (final Integer gid1 : gids) {
				inner: for (final Integer gid2 : gids) {
					if (!(matrix.contains(gid1, gid2) || matrix.contains(gid2, gid1))) {

						if (Thread.currentThread().isInterrupted()) {
							return new AsyncResult<>(Boolean.FALSE);
						}

						final GermplasmTreeNode gid1Tree;
						if (!nodes.containsKey(gid1)) {
							debug("retrieving pedigree: gid=%d", gid1);
							final Instant start = Instant.now();
							try {
								gid1Tree = this.copServiceAsyncMiddleware.getGermplasmPedigreeTree(gid1, LEVEL, INCLUDE_DERIVATIVE_LINES);
							} catch (final MiddlewareRequestException ex) {
								continue outer;
							}
							final Instant end = Instant.now();
							debug("pedigree retrieved: gid=%d, Duration: %s", gid1, formatDurationHMS(between(start, end).toMillis()));
							copCalculation.populateOrder(gid1Tree, 0);
							trackNodes(gid1Tree, nodes);
						} else {
							gid1Tree = nodes.get(gid1);
						}

						final GermplasmTreeNode gid2Tree;
						if (!nodes.containsKey(gid2)) {
							debug("retrieving pedigree: gid=%d", gid2);
							final Instant start = Instant.now();
							try {
								gid2Tree = this.copServiceAsyncMiddleware.getGermplasmPedigreeTree(gid2, LEVEL, INCLUDE_DERIVATIVE_LINES);
							} catch (final MiddlewareRequestException ex) {
								continue inner;
							}
							final Instant end = Instant.now();
							debug("pedigree retrieved: gid=%d, Duration: %s", gid2, formatDurationHMS(between(start, end).toMillis()));
							copCalculation.populateOrder(gid2Tree, 0);
							trackNodes(gid2Tree, nodes);
						} else {
							gid2Tree = nodes.get(gid2);
						}

						final double cop = copCalculation.coefficientOfParentage(gid1Tree, gid2Tree);
						matrixNew.put(gid1, gid2, cop);
						matrix.put(gid1, gid2, cop);

						/*
						 * Note:
						 * Saving intermediate results has been tested here (calling a separate bean/method
						 * with @Transactional(propagation = Propagation.REQUIRES_NEW)) but resulted in a significant
						 * decrease of performance.
						 */
					}
				}
				// track progress
				gidProcessingQueue.put(gid1, Boolean.TRUE);
			}

			final int recordCount;

			if (listId == null) {
				/*
				 * Case 1: cop for some gids: store in db
				 */

				recordCount = matrixNew.size();
				info("cop: saving %s records", recordCount);

				for (final Map.Entry<Integer, Map<Integer, Double>> rowEntrySet : matrixNew.rowMap().entrySet()) {
					for (final Integer column : rowEntrySet.getValue().keySet()) {
						final Integer row = rowEntrySet.getKey();
						final CopMatrix copMatrix = new CopMatrix(row, column, matrixNew.get(row, column));
						this.copServiceAsyncMiddleware.save(copMatrix);
					}
				}
			} else {

				final TreeBasedTable<Integer, Integer, Double> matrixRequest = TreeBasedTable.create();

				recordCount = matrixRequest.size();
				info("cop: saving %s records", recordCount);

				// csv storage saves all gids from request, not just the ones that are new
				for (final Map.Entry<Integer, Map<Integer, Double>> rowEntrySet : matrix.rowMap().entrySet()) {
					for (final Integer column : rowEntrySet.getValue().keySet()) {
						if (gids.contains(column)) {
							matrixRequest.put(rowEntrySet.getKey(), column, rowEntrySet.getValue().get(column));
						}
					}
				}

				final String fileFullPath = CopUtils.getFileFullPath(listId);
				try (final CSVWriter csvWriter = new CSVWriter(
					new OutputStreamWriter(new FileOutputStream(fileFullPath), StandardCharsets.UTF_8), ',')
				) {
					// TODO option to write as gid1,gid2,value (some spreadsheets has max column)
					csvWriter.writeAll(new CopResponse(matrixRequest).getArray());
				}
			}

			info("cop: finish saving %s records", recordCount);

			return new AsyncResult<>(Boolean.TRUE);
		} catch (final RuntimeException | IOException ex) {
			LOG.error("Error in CopServiceAsyncImpl.calculateAsync(), gids=" + gids + ", message: " + ex.getMessage(), ex);
			return new AsyncResult<>(Boolean.FALSE);
		} finally {
			cleanup(gids);
		}
	}

	@Override
	public void prepareExecution(final Set<Integer> gids) {
		debug("%s", semaphore.availablePermits());
		if (!semaphore.tryAcquire()) {
			throw new MiddlewareRequestException("", "cop.max.thread.error", COP_MAX_JOB_COUNT);
		}
		final UUID batchUUID = UUID.randomUUID();
		for (final Integer gid : gids) {
			if (null != gidProcessingQueue.putIfAbsent(gid, Boolean.FALSE)) {
				cleanup(gids);
				throw new MiddlewareRequestException("", "cop.gids.in.queue", this.getProgress(gids));
			}
			gidProcessingToQueueUUID.put(gid, batchUUID);
		}
	}

	@Override
	public boolean threadExists(final Set<Integer> gids) {
		for (final Integer gid : gids) {
			if (null != gidProcessingQueue.get(gid)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public double getProgress(final Set<Integer> gids) {
		// From gids param, which job/queue they belong to
		final Set<UUID> queueUUIDs = gidProcessingToQueueUUID.entrySet().stream()
			.filter(e -> gids.contains(e.getKey()))
			.map(Map.Entry::getValue)
			.collect(toSet());
		// all the gids from the filtered queues
		final Set<Integer> gidsInQueue = gidProcessingToQueueUUID.entrySet().stream()
			.filter(e -> queueUUIDs.contains(e.getValue()))
			.map(Map.Entry::getKey)
			.collect(toSet());

		return gidProcessingQueue.entrySet().stream()
			.filter(e -> gidsInQueue.contains(e.getKey()))
			.map(Map.Entry::getValue)
			.mapToInt(isFinished -> Boolean.TRUE.equals(isFinished) ? 1 : 0)
			.summaryStatistics()
			.getAverage() * 100;
	}

	@Override
	public void trackFutureTask(final Set<Integer> gids, final Future<Boolean> future) {
		for (final Integer gid : gids) {
			if (null != gidProcessingToFutureTask.putIfAbsent(gid, future)) {
				cleanup(gids);
				throw new MiddlewareRequestException("", "cop.runtime.error");
			}
		}
	}

	@Override
	public void cancelJobs(final Set<Integer> gids) {
		for (final Integer gid : gids) {
			if (gidProcessingToFutureTask.containsKey(gid) && !gidProcessingToFutureTask.get(gid).isCancelled()) {
				gidProcessingToFutureTask.get(gid).cancel(true);
			}
		}
	}

	private static void trackNodes(final GermplasmTreeNode gid1Tree, final Map<Integer, GermplasmTreeNode> nodes) {
		nodes.put(gid1Tree.getGid(), gid1Tree);
		final GermplasmTreeNode femaleParentNode = gid1Tree.getFemaleParentNode();
		if (femaleParentNode != null) {
			nodes.put(femaleParentNode.getGid(), femaleParentNode);
			trackNodes(femaleParentNode, nodes);
		}
		final GermplasmTreeNode maleParentNode = gid1Tree.getMaleParentNode();
		if (maleParentNode != null) {
			nodes.put(maleParentNode.getGid(), maleParentNode);
			trackNodes(maleParentNode, nodes);
		}
	}

	private static void cleanup(final Set<Integer> gids) {
		gids.forEach(gidProcessingQueue::remove);
		gids.forEach(gidProcessingToQueueUUID::remove);
		gids.forEach(gidProcessingToFutureTask::remove);
		semaphore.release();
	}
}
