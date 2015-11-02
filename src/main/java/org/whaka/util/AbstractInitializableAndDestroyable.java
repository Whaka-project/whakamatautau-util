package org.whaka.util;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;

/**
 * <p>Abstract implementation of both {@link Initializable} and {@link Destroyable} interfaces.
 * 
 * <p>This class extends {@link AbstractDestroyable} and copies all the functionality of the {@link AbstractInitializable}.
 * 
 * @see #doInitialize()
 * @see #doDestroy()
 * @see #assertInitialized()
 * @see #assertNotDestroyed()
 */
public abstract class AbstractInitializableAndDestroyable extends AbstractDestroyable implements Initializable {

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
