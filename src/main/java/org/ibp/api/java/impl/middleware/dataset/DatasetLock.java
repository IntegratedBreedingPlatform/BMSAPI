package org.ibp.api.java.impl.middleware.dataset;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatasetLock {

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	public void lockWrite() {
		this.lock.writeLock().lock();
	}

	public void unlockWrite() {
		this.lock.writeLock().unlock();
	}

	public void lockRead() {
		this.lock.readLock().lock();
	}

	public void unlockRead() {
		this.lock.readLock().unlock();
	}
}
