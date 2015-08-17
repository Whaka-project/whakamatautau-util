package org.whaka.util;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.MoreObjects;
import org.whaka.asserts.Assert;
import org.whaka.asserts.AssertError;
import org.whaka.asserts.ThrowableAssert;
import org.whaka.util.function.DangerousBiFunction;
import org.whaka.util.function.DangerousConsumer;
import org.whaka.util.function.DangerousFunction;
import org.whaka.util.function.DangerousRunnable;
import org.whaka.util.function.DangerousSupplier;

/**
 * <p>Use methods: {@link #run(DangerousRunnable)}, {@link #perform(DangerousSupplier)},
 * or {@link #withResource(DangerousSupplier, DangerousFunction)} to create instance of the class that will contain
 * either result of the code execution, or the exception thrown in the process of execution.
 * 
 * <p>Methods #perform and #withResource intercept instances of {@link Exception} and created instance of the Try
 * allows you to perform operations on either outcome in a functional matter.
 * 
 * <p><b>Note:</b> only {@link Exception} instances are caught. Any {@link Error}s will fall thru.
 */
public class Try<R> {

	private final R result;
	private final Exception cause;
	private final AtomicBoolean caught;
	private final Throwable suppressed;
	
	private Try(R result, Exception cause) {
		this(result, cause, null);
	}
	
	private Try(R result, Exception cause, Throwable suppressed) {
		this(result, cause, suppressed, false);
	}
	
	private Try(R result, Exception cause, Throwable suppressed, boolean caught) {
		this.result = result;
		this.cause = cause;
		this.suppressed = suppressed;
		this.caught = new AtomicBoolean(caught);
	}
	
	/**
	 * Perform a chunk of code in a safe manner. Result will have type of {@link Void} and alwaus
	 * will have <code>null</code> as result.
	 */
	public static Try<Void> run(DangerousRunnable<Exception> code) {
		try {
			code.run();
			return new Try<>(null, null);
		} catch (Exception e) {
			return new Try<>(null, e);
		}
	}
	
	/**
	 * Perform a chunk of code that returns some value and store this value if execution finished successfully.
	 * Result instance will contain either result of the supplier, or thrown exception.
	 */
	public static <R> Try<R> perform(DangerousSupplier<R, Exception> code) {
		try {
			return new Try<>(code.get(), null);
		} catch (Exception e) {
			return new Try<>(null, e);
		}
	}
	
	/**
	 * <p>Simulate try-with-resource functionality. Specified resource supplier can produce any resource of the type
	 * {@link AutoCloseable}. If resource was supplied successfully - specified function is applied to it.
	 * Result instance will contain either result of the function, or thrown exception.
	 * 
	 * <p><b>Note:</b> any throwable thrown from the {@link AutoCloseable#close()} method of the resource will
	 * be stored as 'suppressed', and might be accessed by {@link #getSuppressed()} method. If function finished
	 * successfully, but resource failed to close - result instance will contain <b>both</b> result and 'suppressed'
	 * error - and will be treated as successful.
	 */
	public static <Res extends AutoCloseable, R> Try<R> withResource(
			DangerousSupplier<Res, Exception> resourceSupplier,
			DangerousFunction<Res, R, Exception> code) {
		R result = null;
		Exception cause = null;
		Throwable suppressed = null;
		boolean success = false;
		try (Res resource = resourceSupplier.get()) {
			result = code.apply(resource);
			success = true;
		} catch (Exception e) {
			if (success) {
				suppressed = e;
			}
			else {
				cause = e;
				if (e.getSuppressed().length > 0)
					suppressed = e.getSuppressed()[0];
			}
		}
		return new Try<R>(result, cause, suppressed);
	}
	
	/**
	 * <p>Get result of the safe execution.
	 * 
	 * <p><b>Note:</b> result might be null in case of successful execution. Either if runnable were executed
	 * (then type of this instance is {@link Void}, and nothing but <code>null</code> is possible as result), or
	 * executed code finished successfully, but returned <code>null</code> as result;
	 * 
	 * <p>Only {@link #isSuccess()} method provides definite indication whether try was performed successfully.
	 */
	public R getResult() {
		return result;
	}
	
	/**
	 * Similar to using {@link #getResult()} with {@link Optional#ofNullable(Object)}.
	 * Created for better usability in functional style.
	 */
	public Optional<R> getOptionalResult() {
		return Optional.ofNullable(getResult());
	}

	/**
	 * <p>Method returns result of the try, if perform were successful, or specified value.
	 * 
	 * <p><b>Note:</b> seemingly similar to {@link Optional#orElse(Object)} this method <b>might return <code>null</code></b>
	 * if execution finished successfully, but returned <code>null</code>. For example, if runnable were executed successfully
	 * result will always be <code>null</code>.
	 * 
	 * <p>If you want to achieve functionality similar to {@link Optional} - use {@link #getOptionalResult()}
	 */
	public R getOrElse(R inCaseOfFail) {
		return isSuccess() ? getResult() : inCaseOfFail;
	}
	
	public Exception getCause() {
		return cause;
	}
	
	/**
	 * <p>Any exception thrown from {@link AutoCloseable#close()} method of a resource used in
	 * the {@link #withResource(DangerousSupplier, DangerousFunction)} method.
	 * 
	 * <p><b>Note:</b> presence if this throwable does not affect success status of the try.
	 */
	public Throwable getSuppressed() {
		return suppressed;
	}
	
	public boolean isSuccess() {
		return getCause() == null;
	}
	
	public boolean isCaught() {
		return caught.get();
	}

	/**
	 * Perform some operation in any case of success or fail. Specified consumer receives result of the execution
	 * and exception thrown on execution fail. One of the specified arguments will always be <code>null</code>.
	 * In some cases both arguments might be <code>null</code> (if execution finished successfully, but returned null)
	 */
	public Try<R> onAnyResult(BiConsumer<R, Exception> consumer) {
		consumer.accept(getResult(), getCause());
		return this;
	}
	
	/**
	 * <p>Specified consumer is called only if this try is successful.
	 * 
	 *  <p><b>Note:</b> result accepted by the consumer might be <code>null</code>, if execution finished successfully,
	 *  but returned <code>null</code>.
	 */
	public Try<R> onPerformSuccess(Consumer<R> consumer) {
		if (isSuccess())
			consumer.accept(getResult());
		return this;
	}

	/**
	 * <p>If execution of this try has finished successfully - applies specified dangerous mapper function to the result
	 * and return new instance of a Try with result of the function. If this Try is not successful - new instance contains
	 * the same cause and suppressed error.
	 * 
	 * <p>Equivalent of:
	 * <pre>
	 * 	Try t = Try.perform(code);
	 * 	if (t.isSuccess())
	 * 		t = Try.perform(() -> mapper.apply(t.getResult()));
	 * </pre>
	 */
	public <V> Try<V> onPerformSuccessTry(DangerousFunction<R, V, Exception> mapper) {
		if (isSuccess())
			return perform(() -> mapper.apply(getResult()));
		return new Try<V>(null, getCause(), getSuppressed(), isCaught());
	}

	/**
	 * <p>If execution of this try has finished successfully - calls specified resource supplier to receive resource,
	 * and then applies specified mapper bifunction to the resource and the result of this try. Then new instance
	 * of a Try returned with the result of this mapper bifunction. If this Try is not successful - new instance
	 * contains the same cause and suppressed error.
	 * 
	 * <p>Equivalent of:
	 * <pre>
	 * 	Try t = try.perform(code);
	 * 	if (t.isSuccess())
	 * 		t = Try.withResource(resourceSupplier, res -> mapper.apply(res, t.getResult()));
	 * </pre>
	 */
	public <Res extends AutoCloseable, V> Try<V> onPerformSuccessTryWithResource(
			DangerousSupplier<Res, Exception> resourceSupplier,
			DangerousBiFunction<Res, R, V, Exception> mapper) {
		if (isSuccess())
			return withResource(resourceSupplier, resource -> mapper.apply(resource, getResult()));
		return new Try<V>(null, getCause(), getSuppressed(), isCaught());
	}
	
	public Try<R> onPerformFail(Consumer<Exception> consumer) {
		if (!isSuccess())
			consumer.accept(getCause());
		return this;
	}
	
	public Try<R> onPerformFailDangerous(DangerousConsumer<Exception, Exception> consumer) throws Exception {
		if (!isSuccess())
			consumer.accept(getCause());
		return this;
	}
	
	/**
	 * <p>If this Try is not successful - applies specified mapper to the result of {@link #getCause()} method and then
	 * throws result of the function.
	 */
	public <T extends Exception> Try<R> onPerformFailThrow(Function<Exception, T> function) throws T {
		if (!isSuccess())
			throw function.apply(getCause());
		return this;
	}
	
	/**
	 * <p>If this try is not successful - throws result of the {@link #getCause()} method.
	 */
	public Try<R> onPerformFailRethrow() throws Exception {
		if (!isSuccess())
			throw getCause();
		return this;
	}

	/**
	 * <p>If result of the {@link #getCause()} method is instance of the specified class - calls specified consumer
	 * with exception cast to the caught type.
	 * 
	 * <p><b>Note:</b> exception can be caught only once! If one of the blocks caught the exception - later blocks
	 * will not be called! Even though the other methods, like {@link #onPerformFail(Consumer)} still will be called.
	 * Example:
	 * <pre>
	 * 	Try.perform(() -> {throw new IOException()})
	 * 		.onPerformFailCatch(FileNotFoundException.class, (FileNotFoundException e) -> {
	 * 			System.out.println("This is not called");
	 * 		})
	 * 		.onPerformFailCatch(IOException.class, (IOException e) -> {
	 * 			System.out.println("This one is called");
	 * 		})
	 * 		.onPerformFailCatch(Exception.class, (Exception e) -> {
	 * 			System.out.println("This one is not called");
	 * 		})
	 * 		.onPerformFail((Exception e) -> {
	 * 			System.out.println("Though this one is called");
	 * 		});
	 * </pre>
	 * 
	 * <p><b>Note:</b> exception can be caught once even with retry. It means that if your initial try was not
	 * successful, and you've caught the cause - and then you performed {@link #onPerformSuccessTry(DangerousFunction)}
	 * or {@link #onPerformSuccessTryWithResource(DangerousSupplier, DangerousBiFunction)} - you have created new
	 * instance of a Try, but its cause still marked as caught. Example:
	 * <pre>
	 * 	Try t = Try.perform(() -> {throw new IOException()});
	 * 	t.onPerformFailCatch(Exception.class, (Exception e) -> {
	 * 		System.out.println("This one is called");
	 * 	});
	 * 	Try t2 = t.onPerformSuccessTry(x -> 42);
	 * 	assert t2 != t; // true
	 * 	assert t2.getCause() == t.getCause(); // true
	 * 	t2.onPerformFailCatch(Exception.class, (Exception e) -> {
	 * 		System.out.println("This one is NOT called");
	 * 	});
	 * </pre>
	 * <p>But NOT caught exceptions are marked as NOT caught even after retry. Example:
	 * <pre>
	 * 	Try.perform(() -> {throw new IOException()})
	 * 		.onPerformSuccessTry(x -> {throw new RuntimeException()})
	 * 		.onPerformFailCatch(IOException.class, (IOException e) -> {
	 * 			System.out.println("This one IS called");
	 * 		});
	 * </pre>
	 * <p><b>Note:</b> the same exception can be caught twice, if retry was created before catch, and then catch
	 * is performed on both instances. Example:
	 * <pre>
	 * 	Try t = Try.perform(() -> {throw new IOException()});
	 * 	Try t2 = t.onPerformSuccessTry(x -> 42);
	 * 	t.onPerformFailCatch(Exception.class, (Exception e) -> {
	 * 		System.out.println("This one IS called");
	 * 	});
	 * 	t2.onPerformFailCatch(Exception.class, (Exception e) -> {
	 * 		System.out.println("This one IS also called");
	 * 	});
	 * </pre>
	 * <p>So you can break 'functional' style, but preserve 'uncaught' state even after retry. This is specifically
	 * implemented for any rare situations, when you need to reuse first Try even after retry was created and caught.
	 * Though, such use is not recommended.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Exception> Try<R> onPerformFailCatch(Class<T> type, Consumer<T> consumer) {
		if (type.isInstance(getCause()) && caught.compareAndSet(false, true))
			consumer.accept((T) getCause());
		return this;
	}
	
	/**
	 * If this try is not successful - new instance of the {@link ThrowableAssert} is created and passed into
	 * specified consumer. Any errors evoked by the assert are rethrown directly.
	 */
	public Try<R> assertFail(Consumer<ThrowableAssert> consumer) {
		if (!isSuccess())
			consumer.accept(Assert.assertThrowable(getCause()));
		return this;
	}
	
	/**
	 * If this try is not successful - throw an {@link AssertError} about its cause. Equivalent to:
	 * <pre>
	 * 	#assertFail(a -> a.notExpected())
	 * </pre>
	 */
	public Try<R> assertFailNotExpected() {
		return assertFail(a -> a.notExpected());
	}
	
	/**
	 * If this try is not successful - throw an {@link AssertError} about its cause. Equivalent to:
	 * <pre>
	 * 	#assertFail(a -> a.notExpected(message))
	 * </pre>
	 */
	public Try<R> assertFailNotExpected(String message) {
		return assertFail(a -> a.notExpected(message));
	}
	
	/**
	 * If this try is not successful - check its cause is an instance of the specified class. Equivalent to:
	 * <pre>
	 * 	#assertFail(a -> a.isInstanceOf(type))
	 * </pre>
	 */
	public Try<R> assertFailIsInstanceOf(Class<? extends Exception> type) {
		return assertFail(a -> a.isInstanceOf(type));
	}
	
	/**
	 * If this try is not successful - check its cause is an instance of the specified class. Equivalent to:
	 * <pre>
	 * 	#assertFail(a -> a.isInstanceOf(type, message))
	 * </pre>
	 */
	public Try<R> assertFailIsInstanceOf(Class<? extends Exception> type, String message) {
		return assertFail(a -> a.isInstanceOf(type, message));
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.addValue(isSuccess() ? getResult() : getCause())
			.toString();
	}
}
