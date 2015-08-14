package com.whaka.util.reflection.properties;

import java.lang.reflect.ParameterizedType;

import com.whaka.util.reflection.Visibility;

public interface ClassProperty<Type, TargetType> {

	ClassPropertyKey getKey();

	Class<Type> getType();
	
	default ParameterizedType getGenericType() {
		return null;
	}
	
	Visibility getVisibility();

	boolean isStatic();
	
	/**
	 * If {@link #isStatic()} returns true - specified target is ignored and may be null
	 * @throws IllegalArgumentException if target is not an instance of the declaring class from the property key
	 */
	Type getValue(TargetType target) throws Exception;
	
	/**
	 * If {@link #isStatic()} returns true - specified target is ignored and may be null
	 * @throws UnsupportedOperationException if {@link #isMutable()} returns <code>false</code>
	 * @throws IllegalArgumentException if target is not an instance of the declaring class from the property key
	 * @throws IllegalArgumentException if value of an illegal type is specified
	 */
	default void setValue(TargetType target, Type value) throws Exception {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * If this method returns <code>true</code> - {@link #setValue(Object, Object)} can be called
	 */
	default boolean isMutable() {
		return false;
	}
}