package com.whaka.util.reflection.comparison.performers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.whaka.util.reflection.comparison.ComparisonPerformer;
import com.whaka.util.reflection.properties.ClassProperty;
import com.whaka.util.reflection.properties.ClassPropertyKey;
import com.whaka.util.reflection.properties.FunctionalClassProperty;

/**
 * <p>Class provides functionality to build a {@link CompositeComparisonPerformer} by specifying actual properties
 * of a class to be compared. All specified properties are mapped by the specific property key. Result performer
 * contains delegating performers for each specified property.
 *
 * <p>Properties specified in the form of the {@link ClassProperty} instances. Each property should relate to the
 * class specified in the constructor.
 *
 * <p><b>Note:</b> registered properties are stored in a map, so you cannot register multiple instances for the
 * same property key.
 *
 * <p>This builder implements {@link AbstractDynamicPerformerBuilder}, so it provides all the
 * {@link DynamicComparisonPerformer} functionality. Check out documentation for the parent class.
 *
 * @see #addProperty(String, Function)
 * @see #addProperty(String, Function, ComparisonPerformer)
 * @see #addProperty(ClassProperty)
 * @see #addProperty(ClassProperty, ComparisonPerformer)
 */
public class PropertyDynamicPerformerBuilder<T> extends AbstractDynamicPerformerBuilder<T, CompositeComparisonPerformer<T>> {

	private final AtomicBoolean buildFinished = new AtomicBoolean();
	private final Map<ClassPropertyKey, ComparisonPerformer<T>> propertyPerformers = new LinkedHashMap<>();

	public PropertyDynamicPerformerBuilder(Class<T> type) {
		super(type);
	}
	
	public Map<ClassPropertyKey, ComparisonPerformer<T>> getPropertyPerformers() {
		return propertyPerformers;
	}
	
	/**
	 * <p>Instance of the {@link FunctionalClassProperty} will be created from the specified function, and stored
	 * in the builder. Property key will be created from the specified name and result of the {@link #getType()}
	 * method.
	 *
	 * <p>Default {@link DynamicComparisonPerformer} will be used to perform comparison for the property.
	 *
	 * @see #addProperty(String, Function, ComparisonPerformer)
	 * @see #addProperty(ClassProperty, ComparisonPerformer)
	 * @see #addProperty(ClassProperty)
	 */
	public <V> PropertyDynamicPerformerBuilder<T> addProperty(String name, Function<T, V> getter) {
		return addProperty(name, getter, getDynamicPerformer());
	}
	
	/**
	 * <p>Add specified property to the builder. {@link ClassProperty#getKey()} will be used to retrieve property
	 * key and map specified property.
	 *
	 * <p>Default {@link DynamicComparisonPerformer} will be used to perform comparison for the property.
	 *
	 * @see #addProperty(ClassProperty, ComparisonPerformer)
	 * @see #addProperty(String, Function, ComparisonPerformer)
	 * @see #addProperty(String, Function)
	 */
	public <V> PropertyDynamicPerformerBuilder<T> addProperty(ClassProperty<V,T> property) {
		return addProperty(property, getDynamicPerformer());
	}
	
	/**
	 * <p>Instance of the {@link FunctionalClassProperty} will be created from the specified function, and stored
	 * in the builder. Property key will be created from the specified name and result of the {@link #getType()}
	 * method.
	 *
	 * <p>Specified performer guaranteed to be used to perform comparison for the property.
	 *
	 * @see #addProperty(String, Function)
	 * @see #addProperty(ClassProperty, ComparisonPerformer)
	 * @see #addProperty(ClassProperty)
	 */
	public <V> PropertyDynamicPerformerBuilder<T> addProperty(String name, Function<T, V> getter,
			ComparisonPerformer<? super V> performer) {
		ClassPropertyKey key = new ClassPropertyKey(name, getType());
		FunctionalClassProperty<V, T> property = FunctionalClassProperty.newPublic(key, null, getter);
		return addProperty(property, performer);
	}
	
	/**
	 * <p>Add specified property to the builder. {@link ClassProperty#getKey()} will be used to retrieve property
	 * key and map specified property.
	 *
	 * <p>Specified performer guaranteed to be used to perform comparison for the property.
	 *
	 * @see #addProperty(ClassProperty)
	 * @see #addProperty(String, Function, ComparisonPerformer)
	 * @see #addProperty(String, Function)
	 */
	public <V> PropertyDynamicPerformerBuilder<T> addProperty(ClassProperty<V,T> property,
			ComparisonPerformer<? super V> delegate) {
		getPropertyPerformers().put(property.getKey(), new PropertyDelegatePerformer<V,T>(property, delegate));
		return this;
	}
	
	@Override
	public CompositeComparisonPerformer<T> build(String name) {
		if (!buildFinished.compareAndSet(false, true))
			throw new IllegalStateException("Due to the dynamic delegate nature builder cannot be used twice!");
		CompositeComparisonPerformer<T> performer = new CompositeComparisonPerformer<T>(name, getPropertyPerformers());
		if (!getDynamicPerformer().getRegisteredDelegates().containsKey(getType()))
			getDynamicPerformer().registerDelegate(getType(), performer);
		return performer;
	}
}
