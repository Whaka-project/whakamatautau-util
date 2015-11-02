package org.whaka.util;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

import org.whaka.util.function.DangerousBiConsumer;
import org.whaka.util.function.DangerousBiFunction;
import org.whaka.util.function.DangerousConsumer;
import org.whaka.util.function.DangerousFunction;

import com.google.common.base.Preconditions;

/**
 * <p>Interface represents a "storable" timeout that you can create with required milliseconds value and then reuse
 * it in different parts of your program.
 * 
 * <p>This is a {@link FunctionalInterface} so it may be created with a simple lambda, like: {@code () -> 5000}
 * <br/>This code will create a 5 second timeout.
 * 
 * <p>Other way to get an instance is to use {@link #create(long)} method that will create new timeout
 * from the specified number of milliseconds.
 * 
 * <p>Note for each simple operation there's also a 'dangerous' method that takes one of the "dangerous functional"
 * interfaces and a string name that is required to make fail message more informative.
 * 
 * @see #getMillis()
 * @see #sleep()
 * @see #await(LongConsumer)
 * @see #await(BiConsumer)
 * @see #awaitAndGet(LongFunction)
 * @see #awaitAndGet(BiFunction)
 * @see #awaitDangerous(DangerousConsumer, String)
 * @see #awaitDangerous(DangerousBiConsumer, String)
 * @see #awaitAndGetDangerous(DangerousFunction, String)
 * @see #awaitAndGetDangerous(DangerousBiFunction, String)
 */
@FunctionalInterface
public interface Timeout {

	/**
	 * New instance of the {@link Timeout} is created with the specified milliseconds value.
	 */
	static Timeout create(long millis) {
		Preconditions.checkArgument(millis >= 0, "Milliseconds cannot be negative!");
		return () -> millis;
	}
	
	/**
	 * @return timeout in milliseconds
	 */
	long getMillis();
	
	/**
	 * Call {@link Thread#sleep(long)} for the number of milliseconds returned from {@link #getMillis()}
	 * 
	 * @return <code>true</code> if no {@link Exception} was thrown during sleep
	 */
	default boolean sleep() {
		return Try.run(() -> Thread.sleep(getMillis())).isSuccess();
	}
	
	/**
	 * Specified operation is called with the value received from the {@link #getMillis()}
	 */
	default void await(LongConsumer operation) {
		operation.accept(getMillis());
	}
	
	/**
	 * <p>Specified dangerous operation is called with the value received from the {@link #getMillis()}
	 * 
	 * @throws IllegalStateException thrown if calling of the specified operation has failed with exception.
	 * Contains cause exception and message with the specified name.
	 */
	default void awaitDangerous(DangerousConsumer<Long, ?> operation, String name) {
		try {
			operation.accept(getMillis());
		} catch (Exception e) {
			throw new IllegalStateException(String.format("Failed to await '%s'!", name), e);
		}
	}
	
	/**
	 * Specified operation is called with the value received from the {@link #getMillis()}
	 * and the time unit: {@link TimeUnit#MILLISECONDS}
	 */
	default void await(BiConsumer<Long, TimeUnit> operation) {
		operation.accept(getMillis(), TimeUnit.MILLISECONDS);
	}
	
	/**
	 * <p>Specified dangerous operation is called with the value received from the {@link #getMillis()}
	 * and the time unit: {@link TimeUnit#MILLISECONDS}
	 * 
	 * @throws IllegalStateException thrown if calling of the specified operation has failed with exception.
	 * Contains cause exception and message with the specified name.
	 */
	default void awaitDangerous(DangerousBiConsumer<Long, TimeUnit, ?> operation, String name) {
		try {
			operation.accept(getMillis(), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new IllegalStateException(String.format("Failed to await '%s'!", name), e);
		}
	}
	
	/**
	 * Specified function is applied to the value received from the {@link #getMillis()}.
	 * Result of the function is returned.
	 */
	default <T> T awaitAndGet(LongFunction<T> operation) {
		return operation.apply(getMillis());
	}
	
	/**
	 * <p>Specified dangerous function is applied to the value received from the {@link #getMillis()}.
	 * Result of the function is returned.
	 * 
	 * @throws IllegalStateException thrown if calling of the specified operation has failed with exception.
	 * Contains cause exception and message with the specified name.
	 */
	default <T> T awaitAndGetDangerous(DangerousFunction<Long, T, ?> operation, String name) {
		try {
			return operation.apply(getMillis());
		} catch (Exception e) {
			throw new IllegalStateException(String.format("Failed to await '%s'!", name), e);
		}
	}
	
	/**
	 * Specified function is applied to the value received from the {@link #getMillis()}
	 * and time unit: {@link TimeUnit#MILLISECONDS}. Result of the function is returned.
	 */
	default <T> T awaitAndGet(BiFunction<Long, TimeUnit, T> operation) {
		return operation.apply(getMillis(), TimeUnit.MILLISECONDS);
	}
	
	/**
	 * <p>Specified dangerous function is applied to the value received from the {@link #getMillis()}
	 * and time unit: {@link TimeUnit#MILLISECONDS}. Result of the function is returned.
	 * 
	 * @throws IllegalStateException thrown if calling of the specified operation has failed with exception.
	 * Contains cause exception and message with the specified name.
	 */
	default <T> T awaitAndGetDangerous(DangerousBiFunction<Long, TimeUnit, T, ?> operation, String name) {
		try {
			return operation.apply(getMillis(), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new IllegalStateException(String.format("Failed to await '%s'!", name), e);
		}
	}
}
