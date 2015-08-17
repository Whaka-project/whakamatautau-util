package org.whaka.util.function;

public interface DangerousBiFunction<A, B, R, E extends Exception> {

	public R apply(A a, B b) throws E;
}
