package org.whaka.util;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;

/**
 * <p>Abstract implementation of the {@link Initializable}. Provides default implementations for all methods.
 * 
 * @see #doInitialize()
 * @see #assertInitialized()
 */
public abstract class AbstractInitializable implements Initializable {

	private final AtomicBoolean initialized = new AtomicBoolean();
	
	@Override
	public final void initialize() {
		if (initialized.compareAndSet(false, true))
			doInitialize();
	}
	
	/**
	 * This method will be called only when {@link #initialize()} is called <b>for the first time</b>.
	 */
	protected void doInitialize() {}
	
	@Override
	public final boolean isInitialized() {
		return initialized.get();
	}
	
	/**
	 * @throws IllegalStateException if {@link #isInitialized()} returns <code>false</code>
	 */
	protected final void assertInitialized() throws IllegalStateException {
		Preconditions.checkState(isInitialized(), "Instance cannot be used before 'initialize()' method is called!");
	}
}
