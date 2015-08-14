package com.whaka.util.reflection.comparison.performers;

import java.util.function.Consumer;

import com.whaka.util.reflection.comparison.ComparisonPerformer;

/**
 * Each dynamic performer builder contains a single instance of the {@link DynamicComparisonPerformer} class.
 * This instance can be configured using methods:
 * <ul>
 * 	<li>{@link #getDynamicPerformer()}
 * 	<li>{@link #configureDynamicPerformer(Consumer)}
 * </ul>
 * 
 * <b>Note:</b> the same single instance of the dynamic performer will be used in all comparison performers
 * produced by the {@link #build(String)} method. So basically each instance of a builder meant to be used only
 * once, to produce only one performer.
 */
public abstract class AbstractDynamicPerformerBuilder<T, R extends ComparisonPerformer<?>> {

	private final Class<T> type;
	private final DynamicComparisonPerformer dynamicPerformer = new DynamicComparisonPerformer();

	public AbstractDynamicPerformerBuilder(Class<T> type) {
		this.type = type;
	}
	
	public Class<T> getType() {
		return type;
	}

	/**
	 * The same instance of the dynamic performer is returned throughout the lifetime of the builder.
	 */
	public DynamicComparisonPerformer getDynamicPerformer() {
		return dynamicPerformer;
	}
	
	/**
	 * Specified consumer receives result of the {@link #getDynamicPerformer()} method.
	 * May be used to perform configuration of the dynamic performer without losing chain-link to the builder.
	 */
	public AbstractDynamicPerformerBuilder<T, R> configureDynamicPerformer(Consumer<DynamicComparisonPerformer> consumer) {
		consumer.accept(getDynamicPerformer());
		return this;
	}
	
	public abstract R build(String name);
}
