package com.whaka.util.function;

public interface DangerousRunnable<E extends Exception> {

	public void run() throws E;
}
