package com.whaka.util.reflection.properties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Primitives;
import com.whaka.util.reflection.Visibility;

/**
 * @see #isTypeCompatible(Method, Method)
 */
public class GetterClassProperty<Type, TargetType> implements ClassProperty<Type, TargetType> {

	private final Method getter;
	private final Method setter;
	private final ClassPropertyKey key;
	private final Visibility visibility;
	
	public GetterClassProperty(Method getter) {
		this(getter, null);
	}
	
	public GetterClassProperty(Method getter, Method setter) {
		this.getter = assertGetter(getter);
		this.setter = assertSetter(getter, setter);
		key = new ClassPropertyKey(getter.getName() + "()", getter.getDeclaringClass());
		visibility = Visibility.getFromModifiers(getter.getModifiers());
	}
	
	private static Method assertGetter(Method getter) {
		Objects.requireNonNull(getter, "Getter method cannot be null!");
		Preconditions.checkArgument(getter.getReturnType() != void.class, "Getter cannot have return type 'void'!");
		Preconditions.checkArgument(getter.getParameterCount() == 0, "Getter cannot have any arguments!");
		return getter;
	}
	
	private static Method assertSetter(Method getter, Method setter) {
		if (setter != null) {
			assertConsistentlyStatic(getter, setter);
			assertSetterArguments(setter);
			assertSetterTypeCompatible(getter, setter);
		}
		return setter;
	}
	
	private static void assertConsistentlyStatic(Method getter, Method setter) {
		boolean getterStatic = Modifier.isStatic(getter.getModifiers());
		boolean setterStatic = Modifier.isStatic(setter.getModifiers());
		Preconditions.checkArgument(getterStatic == setterStatic,
				"Getter and setter should be either both static or both NOT static!");
	}
	
	private static void assertSetterArguments(Method setter) {
		Preconditions.checkArgument(setter.getParameterCount() == 1, "Setter method should have exactly one argument");
	}
	
	private static void assertSetterTypeCompatible(Method getter, Method setter) {
		Preconditions.checkArgument(isTypeCompatible(getter, setter), "Setter and getter are not type compatible!");
	}

	/**
	 * Checks if return type of the specified getter is compatible with the type of the <b>first</b> argument of the
	 * specified setter. Types compatible if next conditions are satisfied:
	 * <ul>
	 * 	<li>If setter type is a primitive - then getter type is equal
	 * 	<li>If getter type is a primitive - then setter type is the corresponding wrapper or its ancestor
	 * 	<li>Setter type is assignable from getter type
	 * </ul>
	 * @throws IndexOutOfBoundsException if specified setter has no arguments
	 */
	public static boolean isTypeCompatible(Method getter, Method setter) {
		Class<?> getterType = getter.getReturnType();
		Class<?> setterType = setter.getParameterTypes()[0];
		return isTypesCompatible(getterType, setterType);
	}
	
	private static boolean isTypesCompatible(Class<?> getterType, Class<?> setterType) {
		if (setterType.isPrimitive())
			return getterType == setterType;
		if (getterType.isPrimitive())
			getterType = Primitives.wrap(getterType);
		return setterType.isAssignableFrom(getterType);
	}
	
	@Override
	public ClassPropertyKey getKey() {
		return key;
		
	}
	@Override
	@SuppressWarnings("unchecked")
	public Class<Type> getType() {
		return (Class<Type>) getter.getReturnType();
	}
	
	@Override
	public ParameterizedType getGenericType() {
		return (ParameterizedType) getter.getGenericReturnType();
	}
	
	@Override
	public boolean isStatic() {
		return Modifier.isStatic(getter.getModifiers());
	}
	
	@Override
	public Visibility getVisibility() {
		return visibility;
	}
	
	public Method getGetter() {
		return getter;
	}
	
	public Method getSetter() {
		return setter;
	}

	/**
	 * @throws SecurityException if property getter cannot be accessed
	 * @throws IllegalAccessException if property getter is inaccessible.
     * @throws IllegalArgumentException  if property getter is an
     *              instance method and the specified object argument
     *              is not an instance of the class or interface
     *              declaring the underlying method (or of a subclass
     *              or implementor thereof);
     * @throws InvocationTargetException if the underlying method throws an exception.
     * @throws NullPointerException if the specified object is null and the method is an instance method.
     * @throws ExceptionInInitializerError if the initialization provoked by this method fails.
     * @see Method#invoke(Object, Object...)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Type getValue(TargetType target) throws Exception {
		getter.setAccessible(true);
		return (Type) getter.invoke(target);
	}
	
	/**
	 * @throws UnsupportedOperationException is {@link #isMutable()} returns false.
	 * @throws SecurityException if property getter cannot be accessed
	 * @throws IllegalAccessException if property getter is inaccessible.
     * @throws IllegalArgumentException  if property getter is an
     *              instance method and the specified object argument
     *              is not an instance of the class or interface
     *              declaring the underlying method (or of a subclass
     *              or implementor thereof);
     * @throws InvocationTargetException if the underlying method throws an exception.
     * @throws NullPointerException if the specified object is null and the method is an instance method.
     * @throws ExceptionInInitializerError if the initialization provoked by this method fails.
     * @see Method#invoke(Object, Object...)
	 */
	@Override
	public void setValue(TargetType target, Type value)
			throws Exception, UnsupportedOperationException, IllegalArgumentException {
		if (!isMutable())
			throw new UnsupportedOperationException("Cannot set new value! Setter is not present!");
		setter.setAccessible(true);
		try {
			setter.invoke(target, value);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Illegal argument: " + value + "! Expected type: " + getType(), e);
		}
	}
	
	/**
	 * Returns <code>false</code> if setter wasn't specified for this property.
	 */
	@Override
	public boolean isMutable() {
		return setter != null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getGetter(), getSetter());
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			GetterClassProperty<?,?> that = (GetterClassProperty<?,?>) object;
			return Objects.equals(getGetter(), that.getGetter())
					&& Objects.equals(getSetter(), that.getSetter());
		}
		return false;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.omitNullValues()
				.add("getter", getGetter())
				.add("setter", getSetter())
				.toString();
	}
}
