package org.whaka.util;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;

/**
 * <p>Abstract implementation of the {@link Destroyable}. Provides default implementations for all methods.
 * 
 * <p><b>Note:</b> this class also takes the stack snapshot when {@link #destroy()} method is called for
 * the first time. Stack will be added to error message from {@link #assertNotDestroyed()}
 * or may be accessed by {@link #getDestructionStackTrace()}
 * 
 * @see #doDestroy()
 * @see #getDestructionStackTrace()
 * @see #assertNotDestroyed()
 */
public abstract class AbstractDestroyable implements Destroyable {

	private final AtomicBoolean destroyed = new AtomicBoolean();
	private StackTraceElement[] destructionStackTrace = new StackTraceElement[0];
	
	@Override
	public synchronized void destroy() {
		if (destroyed.compareAndSet(false, true)) {
			destructionStackTrace = Thread.currentThread().getStackTrace();
			doDestroy();
		}
	}
	
	/**
	 * Returns snapshot of the stack from the moment {@link #destroy()} method was called for the first time.
	 * Or empty array if {@link #destroy()} wasn't yet called.
	 */
	public StackTraceElement[] getDestructionStackTrace() {
		return destructionStackTrace;
	}
	
	/**
	 * This method will be called only when {@link #destroy()} is called <b>for the first time</b>.
	 */
	protected void doDestroy() {}
	
	@Override
	public boolean isDestroyed() {
		return destroyed.get();
	}
	
	protected void assertNotDestroyed() throws IllegalStateException {
		Preconditions.checkState(!isDestroyed(), "Instance cannot be used after 'destroy()' method is called!");
	}
}
