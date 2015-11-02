package org.whaka.util;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;

/**
 * <p>Abstract implementation of the {@link Initializable}. Provides default implementations for all methods.
 * 
 * <p><b>Note:</b> {@link #isInitialized()} returns <code>false</code> by default, until {@link #initialize()}
 * won't be called for the first time! Unlike default {@link Initializable} implementation, where
 * {@link #isInitialized()} returns <code>true</code>. So extending this class automatically means that your
 * class does <b>need</b> the "initialization" context and will require to call {@link #initialize()} method.
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
