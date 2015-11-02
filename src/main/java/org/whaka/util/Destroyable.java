package org.whaka.util;

/**
 * <p>This interface adds "destruction" context to an implementing class.
 * Any instance of the class may now be in "pre-destroyed" (working) state or "destroyed" state.
 * And it may be called to destroy itself to clean up after performing some work.
 * 
 * @see #destroy()
 * @see #isDestroyed()
 * @see AbstractDestroyable
 */
public interface Destroyable {

	/**
	 * <p>This method may be called to ask instance to release any locked resources
	 * and to prepare itself to be properly garbage collected.
	 * 
	 * <p><b>Note:</b> this method should not throw an exception for being called for the second (or more) time.
	 * If instance is already destroyed - method should exit without performing any actual functionality.
	 */
	void destroy();

	/**
	 * <p>This method returns <code>true</code> if this instance was already destroyed and <b>cannot be used anymore</b>.
	 * If this method returns <code>true</code> - it is a signal for other parts of the program to not perform any
	 * additional calls on it.
	 * 
	 * <p>If this method returns <code>false</code> - {@link #destroy()} should be called when instance is not planned
	 * to be used anymore.
	 */
	boolean isDestroyed();
}
