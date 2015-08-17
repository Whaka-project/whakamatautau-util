package org.whaka.util.reflection.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import org.whaka.util.reflection.Visibility;

public class FieldClassProperty<Type, TargetType> implements ClassProperty<Type, TargetType> {

	private final Field field;
	private final ClassPropertyKey key;
	private final Visibility visibility;
	
	public FieldClassProperty(Field field) {
		this.field = assertField(field);
		key = new ClassPropertyKey(field.getName(), field.getDeclaringClass());
		visibility = Visibility.getFromModifiers(field.getModifiers());
	}
	
	private static Field assertField(Field field) {
		return Objects.requireNonNull(field, "Member field cannot be null!");
	}
	
	@Override
	public ClassPropertyKey getKey() {
		return key;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<Type> getType() {
		return (Class<Type>) field.getType();
	}
	
	@Override
	public ParameterizedType getGenericType() {
		return (ParameterizedType) field.getGenericType();
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(field.getModifiers());
	}
	
	@Override
	public Visibility getVisibility() {
		return visibility;
	}
	
	public Field getField() {
		return field;
	}

	/**
	 * @throws SecurityException if property field cannot be accessed
	 * @throws IllegalAccessException if property field is inaccessible.
     * @throws IllegalArgumentException if the specified target is not an
     *              instance of the class or interface declaring the underlying
     *              field (or a subclass or implementor thereof).
     * @throws NullPointerException if the specified target is null
     *              and the field is an instance field.
     * @throws ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
	 * @see Field#get(Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Type getValue(TargetType target) throws Exception {
		field.setAccessible(true);
		return (Type) field.get(target);
	}
	
	/**
	 * @throws UnsupportedOperationException if {@link #isMutable()} returns false.
	 * @throws SecurityException if property field cannot be accessed
	 * @throws IllegalAccessException if property field is inaccessible.
     * @throws IllegalArgumentException if the specified object is not an
     *              instance of the class or interface declaring the underlying
     *              field (or a subclass or implementor thereof),
     *              or if an unwrapping conversion fails.
     * @throws NullPointerException if the specified target is null
     *              and the field is an instance field.
     * @throws ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
	 * @see Field#set(Object, Object)
	 */
	@Override
	public void setValue(TargetType target, Type value) throws Exception {
		if (!isMutable())
			throw new UnsupportedOperationException("Cannot change value of a final field");
		field.setAccessible(true);
		field.set(target, value);
	}
	
	/**
	 * Returns <code>false</code> if underlying field is <b>final</b>.
	 */
	@Override
	public boolean isMutable() {
		return !Modifier.isFinal(field.getModifiers());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getField());
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			FieldClassProperty<?, ?> that = (FieldClassProperty<?, ?>) object;
			return getField().equals(that.getField());
		}
		return false;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.omitNullValues()
				.add("field", getField())
				.toString();
	}
}