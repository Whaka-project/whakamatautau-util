package org.whaka.util;

/**
 * <p>This interface adds "initialization" context to an implementing class.
 * Any instance of the class may now be in "pre-initialized" state or "initialized" (working) state.
 * And it may be called to initialize itself to be ready for using.
 * 
 * <p>Methods have default ("empty") implementation, so users can extend their own interfaces from it
 * without forced requiring to implement these methods.
 * 
 * @see #initialize()
 * @see #isInitialized()
 * @see AbstractInitializable
 */
public interface Initializable {

	/**
	 * <p>This instance should perform all the required operations that will put it into the working state,
	 * so it can properly perform any other functionality.
	 * 
	 * <p><b>Note:</b> if some implementation overrides this method
	 * it should also provide proper implementation for the {@link #isInitialized()} method!
	 * 
	 * <p>General contract is that if {@link #isInitialized()} returns <code>false</code> - this method
	 * may and should be called at least once. If {@link #isInitialized()} returns <code>true</code> - this
	 * method may be safely called - but not necessary perform any actual functionality.
	 */
	default void initialize() {
	}
	
	/**
	 * <p>Returns <code>true</code> if this instance is already initialized and may perform any functionality.
	 * If this method return <code>false</code> - {@link #initialize()} should be called.
	 */
	default boolean isInitialized() {
		return true;
	}
}
