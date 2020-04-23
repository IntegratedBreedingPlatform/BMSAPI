package org.ibp.api.java.impl.middleware.inventory.manager.common;

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class InventoryLock {

	private ReadWriteLock lock = new ReentrantReadWriteLock();

	public ReadWriteLock getLock() {
		return lock;
	}
}
