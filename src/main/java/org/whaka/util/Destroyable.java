package org.whaka.util;

/**
 * <p>This interface adds "destruction" context to an implementing class.
 * Any instance of the class may now be in "pre-destroyed" (working) state or "destroyed" state.
 * And it may be called to destroy itself to clean up after performing some work.
 * 
 * <p>Methods have default ("empty") implementation, so users can extend their own interfaces from it
 * without forced requiring to implement these methods.
 * 
 * @see #destroy()
 * @see #isDestroyed()
 * @see AbstractInitializable
 */
public interface Destroyable {

	default void destroy() {
	}
	
	default boolean isDestroyed() {
		return false;
	}
}
