package org.whaka.util.reflection.properties;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.whaka.util.reflection.Visibility;

public class FunctionalClassProperty<Type, TargetType> implements ClassProperty<Type, TargetType> {

	private final ClassPropertyKey key;
	private final Class<Type> type;
	private final Visibility visibility;
	private final boolean _static;
	private final Function<TargetType, Type> getter;
	private final BiConsumer<TargetType, Type> setter;

	public FunctionalClassProperty(ClassPropertyKey key, Class<Type> type, Visibility visibility,
			Function<TargetType, Type> getter) {
		this(key, type, visibility, getter, null);
	}
	
	public FunctionalClassProperty(ClassPropertyKey key, Class<Type> type, Visibility visibility,
			Function<TargetType, Type> getter, BiConsumer<TargetType, Type> setter) {
		this(key, type, visibility, getter, setter, false);
	}
	
	public FunctionalClassProperty(ClassPropertyKey key, Class<Type> type, Visibility visibility,
			Function<TargetType, Type> getter, BiConsumer<TargetType, Type> setter, boolean _static) {
		
		this.key = key;
		this.type = type;
		this.visibility = visibility;
		this._static = _static;
		this.getter = getter;
		this.setter = setter;
	}
	
	@Override
	public ClassPropertyKey getKey() {
		return key;
	}

	@Override
	public Class<Type> getType() {
		return type;
	}

	@Override
	public Visibility getVisibility() {
		return visibility;
	}

	@Override
	public boolean isStatic() {
		return _static;
	}

	public Function<TargetType, Type> getGetter() {
		return getter;
	}
	
	public BiConsumer<TargetType, Type> getSetter() {
		return setter;
	}
	
	@Override
	public Type getValue(TargetType target) throws Exception {
		return getter.apply(target);
	}
	
	/**
	 * throws UnsupportedOperationException if {@link #isMutable()} returns false.
	 */
	@Override
	public void setValue(TargetType target, Type value) throws Exception {
		if (!isMutable())
			throw new UnsupportedOperationException("No setter function specified!");
		setter.accept(target, value);
	}
	
	/**
	 * Returns <code>false</code> is no setter function was specified for this property.
	 */
	@Override
	public boolean isMutable() {
		return setter != null;
	}
	
	public static <TargetType, Type> FunctionalClassProperty<Type,TargetType> newPublic(ClassPropertyKey key,
			Class<Type> type, Function<TargetType, Type> getter) {
		return new FunctionalClassProperty<>(key, type, Visibility.PUBLIC, getter);
	}
	
	public static <TargetType, Type> FunctionalClassProperty<Type,TargetType> newPublic(ClassPropertyKey key,
			Class<Type> type, Function<TargetType, Type> getter, BiConsumer<TargetType, Type> setter) {
		return new FunctionalClassProperty<>(key, type, Visibility.PUBLIC, getter, setter);
	}
	
	public static <TargetType, Type> FunctionalClassProperty<Type,TargetType> newPublic(ClassPropertyKey key,
			Class<Type> type, Function<TargetType, Type> getter, BiConsumer<TargetType, Type> setter, boolean _static) {
		return new FunctionalClassProperty<>(key, type, Visibility.PUBLIC, getter, setter, _static);
	}
}
