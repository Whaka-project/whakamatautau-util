package org.whaka.mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.whaka.mock.EventCollector.EventHandler;

/**
 * <p>Implementation of the {@link EventHandler} backed by an instance of the {@link CountDownLatch}.
 * Handler returns true from the {@link #test(Object)} method for any argument for as long a latch has positive count.
 * When {@link #eventCollected(Object)} is called with any argument - latch gets counted down.
 * 
 * @see #await(long, TimeUnit)
 * @see #awaitSeconds(int)
 * @see #awaitMillis(long)
 * @see #isOpen()
 * @see #close()
 */
public class LatchEventHandler implements EventHandler<Object> {

	private final Object lock = new Object();
	private final CountDownLatch latch;
	private final AtomicBoolean open = new AtomicBoolean(true);
	
	public LatchEventHandler(CountDownLatch latch) {
		this.latch = latch;
	}
	
	/**
	 * Returns <code>true</code> if this latch is open and underlying latch has positive count
	 */
	public boolean isOpen() {
		synchronized (lock) {
			return open.get() && getLatch().getCount() > 0;
		}
	}
	
	public CountDownLatch getLatch() {
		return latch;
	}

	/**
	 * This latch is closed. <b>Note:</b> underlying latch <b>IS NOT</b> counted down,
	 * so if you access it directly there still might be some positive counter.
	 */
	public void close() {
		synchronized (lock) {
			open.set(false);
		}
	}
	
	/**
	 * Equal to {@link #await(long, TimeUnit)} with specified timeout and {@link TimeUnit#SECONDS}
	 */
	public boolean awaitSeconds(int second) {
		return await(second, TimeUnit.SECONDS);
	}
	
	/**
	 * Equal to {@link #await(long, TimeUnit)} with specified timeout and {@link TimeUnit#MILLISECONDS}
	 */
	public boolean awaitMillis(long millis) {
		return await(millis, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * If this latch is open equal to calling {@link CountDownLatch#await(long, TimeUnit)} on the underlying latch
	 */
	public boolean await(long timeout, TimeUnit unit) {
		try {
			synchronized (lock) {
				if (!open.get())
					return false;
			}
			return getLatch().await(timeout, unit);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean awaitSecondsAndClose(int seconds) {
		return awaitAndClose(seconds, TimeUnit.SECONDS);
	}
	
	public boolean awaitAndClose(long timeout, TimeUnit unit) {
		try {
			return await(timeout, unit);
		} finally {
			close();
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
	 * If this latch is open this method counts underlying latch down for any argument
	 */
	@Override
	public void eventCollected(Object event) {
		synchronized (lock) {
			if (open.get())
				latch.countDown();
		}
	}
}