package org.whaka.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whaka.util.function.DangerousConsumer;

/**
 * <p>Util class provides usability static methods to close instances of {@link AutoCloseable} or {@link Destroyable},
 * or to perform any kind of dangerous close operations on any target.
 * 
 * <p>There are two groups of methods, duplicating each other in arguments:
 * <ul>
 * 	<li>close - if any exception was caught - it is rethrown as {@link CloseException}
 * 	<li>closeQuietly - if any exception was caught - it is logged using SLF4J logger with level 'WARN'
 * </ul>
 * 
 * <p><b>Note:</b> even though methods taking {@link DangerousConsumer} as operation to be performed
 * may be used to perform <i>any</i> kinds of operation - it is discouraged, since names of methods
 * and logged messages or thrown exceptions are explicitly say about 'closing' something. Performing any other
 * operation except actual closing or destroying may cause confusing code or error messages. Use {@link Try} to
 * perform any other kind of operation.
 * 
 * @see #close(AutoCloseable, String)
 * @see #close(Destroyable, String)
 * @see #close(Object, DangerousConsumer, String)
 * 
 * @see #closeQuietly(AutoCloseable, String)
 * @see #closeQuietly(Destroyable, String)
 * @see #closeQuietly(Object, DangerousConsumer, String)
 */
public final class UberCloser {

	private static final Logger log = LoggerFactory.getLogger(UberCloser.class);
	
	private UberCloser() {
	}
	
	/**
	 * Equal to {@link #close(AutoCloseable, String)} with name produced from target object.
	 * 
	 * @see #close(AutoCloseable, String)
	 * 
	 * @throws CloseException
	 */
	public static void close(AutoCloseable closeable) {
		close(closeable, String.valueOf(closeable));
	}
	
	/**
	 * Equal to {@link #close(Object, DangerousConsumer, String)}
	 * where dangerous operation is {@link AutoCloseable#close()}
	 * 
	 * @see #close(AutoCloseable)
	 * 
	 * @throws CloseException
	 */
	public static void close(AutoCloseable closeable, String name) {
		close(closeable, AutoCloseable::close, name);
	}
	
	/**
	 * Equal to {@link #close(Destroyable, String)} with name produced from target object.
	 * 
	 * @see #close(Destroyable, String)
	 * 
	 * @throws CloseException
	 */
	public static void close(Destroyable destroyable) {
		close(destroyable, String.valueOf(destroyable));
	}
	
	/**
	 * Equal to {@link #close(Object, DangerousConsumer, String)}
	 * where dangerous operation is {@link Destroyable#destroy()}
	 * 
	 * @see #close(Destroyable)
	 * 
	 * @throws CloseException
	 */
	public static void close(Destroyable destroyable, String name) {
		close(destroyable, Destroyable::destroy, name);
	}
	
	/**
	 * Equal to {@link #close(Object, DangerousConsumer, String)} with name produced from target object.
	 * 
	 * @see #close(Object, DangerousConsumer, String)
	 * 
	 * @throws CloseException
	 */
	public static <T> void close(T target, DangerousConsumer<T, ?> operation) {
		close(target, operation, String.valueOf(target));
	}
	
	/**
	 * If specified target is <code>null</code> - nothing happens. Otherwise specified dangerous operation
	 * is performed on the specified target. If any exception was raised - new instance of the {@link CloseException}
	 * is thrown with message containing specified name and with raised cause exception.
	 * 
	 * @see #close(Object, DangerousConsumer)
	 * 
	 * @throws CloseException
	 */
	public static <T> void close(T target, DangerousConsumer<T, ?> operation, String name) {
		if (target == null) {
			log.trace("Target '{}' is null. Ignoring.");
			return;
		}
		try {
			log.trace("Closing '{}'");
			operation.accept(target);
		} catch (Exception e) {
			throw new CloseException(String.format("Failed to close '%s'!", name), e);
		}
	}
	
	/**
	 * Equal to {@link #closeQuietly(AutoCloseable, String)} with name produced from target object.
	 * 
	 * @see #closeQuietly(AutoCloseable, String)
	 */
	public static void closeQuietly(AutoCloseable closeable) {
		closeQuietly(closeable, String.valueOf(closeable));
	}
	
	/**
	 * Equal to {@link #closeQuietly(Object, DangerousConsumer, String)}
	 * where dangerous operation is {@link AutoCloseable#close()}
	 * 
	 * @see #closeQuietly(AutoCloseable)
	 */
	public static void closeQuietly(AutoCloseable closeable, String name) {
		closeQuietly(closeable, AutoCloseable::close, name);
	}
	
	/**
	 * Equal to {@link #closeQuietly(Destroyable, String)} with name produced from target object.
	 * 
	 * @see #closeQuietly(Destroyable, String)
	 */
	public static void closeQuietly(Destroyable destroyable) {
		closeQuietly(destroyable, String.valueOf(destroyable));
	}
	
	/**
	 * Equal to {@link #closeQuietly(Object, DangerousConsumer, String)}
	 * where dangerous operation is {@link Destroyable#destroy()}
	 * 
	 * @see #closeQuietly(Destroyable)
	 */
	public static void closeQuietly(Destroyable destroyable, String name) {
		closeQuietly(destroyable, Destroyable::destroy, name);
	}
	
	/**
	 * Equal to {@link #closeQuietly(Object, DangerousConsumer, String)} with name produced from target object.
	 * 
	 * @see #closeQuietly(Object, DangerousConsumer, String)
	 */
	public static <T> void closeQuietly(T target, DangerousConsumer<T, ?> operation) {
		closeQuietly(target, operation, String.valueOf(target));
	}
	
	/**
	 * If specified target is <code>null</code> - nothing happens. Otherwise specified
	 * dangerous operation is performed on the specified target. If any exception
	 * was raised - it is logged thru SLF4J logger with 'WARN' level.
	 * 
	 * @see #closeQuietly(Object, DangerousConsumer)
	 */
	public static <T> void closeQuietly(T target, DangerousConsumer<T, ?> operation, String name) {
		if (target == null) {
			log.trace("Target '{}' is null. Ignoring.");
			return;
		}
		try {
			log.trace("Closing '{}'");
			operation.accept(target);
		} catch (Exception e) {
			log.warn("Failed to close '{}'!", name, e);
		}
	}
	
	
	/**
	 * Exception indicating an error happened during the closing process.
	 */
	public static class CloseException extends RuntimeException {
		public CloseException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
