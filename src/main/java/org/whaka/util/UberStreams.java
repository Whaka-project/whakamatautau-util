package org.whaka.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.whaka.util.function.MapStream;
import org.whaka.util.function.UberStream;

public class UberStreams {

	private UberStreams() {
	}
	
	public static <T> UberStream<T> iterate(T seed, Function<T, T> next, Predicate<T> validator) {
		return stream(StreamSupport.stream(UberIterators.iterate(seed, next, validator).spliterator(), false));
	}
	
	@SafeVarargs
	public static <T> UberStream<T> stream(T... array) {
		return stream(Stream.of(array));
	}
	
	public static <T> UberStream<T> stream(Iterable<T> iterable) {
		return stream(StreamSupport.stream(iterable.spliterator(), false));
	}
	
	public static <T> UberStream<T> stream(Collection<T> collection) {
		return stream(collection.stream());
	}
	
	public static <T> UberStream<T> stream(Stream<T> stream) {
		return new UberStream<>(stream);
	}
	
	public static <K,V> MapStream<K, V> stream(Map<K, V> map) {
		return new MapStream<>(map);
	}
}
