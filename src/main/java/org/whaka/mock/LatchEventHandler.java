package org.whaka.mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.whaka.mock.EventCollector.EventHandler;

/**
 * <p>Implementation of the {@link EventHandler} backed by an instance of the {@link CountDownLatch}.
 * Handler returns true from the {@link #test(Object)} method for any argument for as long a latch has positive count.
 * When {@link #eventCollected(Object)} is called with any argument - latch gets counted down.
 * 
 * @see #await(long, TimeUnit)
 * @see #awaitSecond(int)
 * @see #awaitMillis(long)
 * @see #isOpen()
 * @see #close()
 */
public class LatchEventHandler implements EventHandler<Object> {

	private final Object lock = new Object();
	private final CountDownLatch latch;
	
	public LatchEventHandler(CountDownLatch latch) {
		this.latch = latch;
	}
	
	/**
	 * Returns <code>true</code> if latch has positive count
	 */
	public boolean isOpen() {
		synchronized (lock) {
			return getLatch().getCount() > 0;
		}
	}
	
	public CountDownLatch getLatch() {
		return latch;
	}

	/**
	 * Counts down underlying latch until it has a zero count
	 */
	public void close() {
		synchronized (lock) {
			while(latch.getCount() > 0)
				latch.countDown();
		}
	}
	
	/**
	 * Equal to {@link #await(long, TimeUnit)} with specified timeout and {@link TimeUnit#SECONDS}
	 */
	public boolean awaitSecond(int second) {
		return await(second, TimeUnit.SECONDS);
	}
	
	/**
	 * Equal to {@link #await(long, TimeUnit)} with specified timeout and {@link TimeUnit#MILLISECONDS}
	 */
	public boolean awaitMillis(long millis) {
		return await(millis, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Equal to calling {@link CountDownLatch#await(long, TimeUnit)} on the underlying latch
	 */
	public boolean await(long timeout, TimeUnit unit) {
		try {
			return getLatch().await(timeout, unit);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * This method calls {@link #isOpen()} for any argument
	 */
	@Override
	public boolean test(Object t) {
		return isOpen();
	}

	/**
	 * This method counts underlying latch down for any argument
	 */
	@Override
	public void eventCollected(Object event) {
		synchronized (lock) {
			latch.countDown();
		}
	}
}