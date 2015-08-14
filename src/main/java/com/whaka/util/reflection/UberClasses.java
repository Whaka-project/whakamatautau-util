package com.whaka.util.reflection;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.whaka.util.UberStreams;

public class UberClasses {

	private UberClasses() {
	}
	
	/**
	 * Returns <code>true</code> if 'a' descends from 'b'.
	 * Meaning that 'b' is assignable from 'a' and 'b' is not 'a'.
	 */
	public static boolean descends(Class<?> a, Class<?> b) {
		return b.isAssignableFrom(a) && a != b;
	}
	
	/**
	 * <p>Stream of the type itself followed by all the ancestors
	 * <p>Use {@link Stream#skip(long)} if you need to skip the type itself.
	 */
	public static Stream<Class<?>> streamAncestors(Class<?> type) {
		return UberStreams.iterate(type, Class::getSuperclass, Objects::nonNull);
	}
	
	/**
	 * <p>Stream of the type itself followed by all the declared interfaces.
	 * <p>Use {@link Stream#skip(long)} if you need to skip the type itself.
	 */
	public static Stream<Class<?>> streamInterfaces(Class<?> type) {
		return Stream.concat(Stream.of(type), Stream.of(type.getInterfaces()));
	}
	
	/**
	 * <p>Stream of the type itself followed by interface-first linearization
	 * of all its supertypes in order of declaration.
	 * <p>Example:
	 * <pre>
	 * 	interface I {}
	 * 	interface I2 extends I {}
	 * 	interface I3 extends I2 {}
	 * 
	 * 	class A implements I {}
	 * 	class B extends A implements I3 {}
	 * 	class C extends B implements I2 {}
	 * 
	 * 	linearization of I3 = [I3, I2, I]
	 * 	linearization of A = [A, I]
	 * 	linearization of B = [B, I3, I2, I, A]
	 * 	linearization of C = [C, I2, I, B, I3, A]
	 * </pre>
	 */
	public static Stream<Class<?>> streamTypeLinearization(Class<?> type) {
		return linearization(type).stream();
	}
	
	private static Set<Class<?>> linearization(Class<?> type) {
		if (type == null)
			return Collections.emptySet();
		Set<Class<?>> set = new LinkedHashSet<>(Collections.singleton(type));
		for (Class<?> iface : type.getInterfaces())
			set.addAll(linearization(iface));
		set.addAll(linearization(type.getSuperclass()));
		return set;
	}
	
	/**
	 * Specified type is caster to requested type.
	 * No checks performer.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Class<T> cast(Class type) {
		return type;
	}
}