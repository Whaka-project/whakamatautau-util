package org.whaka.util.function;

public interface DangerousBiConsumer<A, B, E extends Exception> {

	public void accept(A a, B b) throws E;
}
