package org.whaka.util;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;

/**
 * <p>Abstract implementation of the {@link Destroyable}. Provides default implementations for all methods.
 * 
 * @see #doDestroy()
 * @see #assertNotDestroyed()
 */
public abstract class AbstractDestroyable implements Destroyable {

	private final AtomicBoolean destroyed = new AtomicBoolean();
	
	@Override
	public final synchronized void destroy() {
		if (destroyed.compareAndSet(false, true)) {
			doDestroy();
		}
	}
	
	/**
	 * This method will be called only when {@link #destroy()} is called <b>for the first time</b>.
	 */
	protected void doDestroy() {}
	
	@Override
	public final boolean isDestroyed() {
		return destroyed.get();
	}
	
	protected final void assertNotDestroyed() throws IllegalStateException {
		Preconditions.checkState(!isDestroyed(), "Instance cannot be used after 'destroy()' method is called!");
	}
}
