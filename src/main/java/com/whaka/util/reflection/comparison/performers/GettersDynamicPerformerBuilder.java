package com.whaka.util.reflection.comparison.performers;

import static com.whaka.util.UberPredicates.*;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.MoreObjects;
import com.whaka.util.UberPredicates;
import com.whaka.util.reflection.comparison.ComparisonPerformer;
import com.whaka.util.reflection.properties.ClassProperty;
import com.whaka.util.reflection.properties.ClassPropertyExtractor;
import com.whaka.util.reflection.properties.ClassPropertyKey;
import com.whaka.util.reflection.properties.GetterClassProperty;
import com.whaka.util.reflection.properties.GettersExtractor;

/**
 * <p>Class provides functionality to build {@link CompositeComparisonPerformer} by streaming all the getter methods
 * in the specified class and filtering out the ones you want to include or exclude. All getter properties will
 * be automatically mapper by property keys. Result performer will contain delegating performers for each getter.
 *
 * <p>Getters are retrieved by the instance of the {@link ClassPropertyExtractor} with type {@link GetterClassProperty}.
 * By default - {@link GettersExtractor} class is used. But you can specify your own extractor - if necessary.
 *
 * <p><b>Note:</b> that {@link GettersExtractor} by default treats as getters only methods with non-void return type
 * and no arguments, so any other mathods will be ignored by default. Also all static methods are ignored at the very
 * beginning, so you don't have to filter them out manually.
 *
 * <p><b>Note:</b> if you specified in the constructor actual class (not an interface) - it will also contain all the
 * 'getters' from the ancestor Object class, including #hashCode(), #getClass(), and #clone(). They are not filtered
 * out by default, to support rare occasions when they might be required. But cuz most of the time they are not wanted
 * to be used - you can used constant field {@link #DEFAULT_METHODS}. You can use method
 * {@link #addExcludingFilter(String)} with this filter to exclude all getters from Object class.
 *
 * <p>This builder doesn't allow to specify a delegate performer specifically for a property, so default
 * {@link DynamicComparisonPerformer} will be used for all the created properties.
 *
 * <p>Method is included in the result performer if it matched by at least one including filter (if any present)
 * and NOT matched by any excluding filter. This means that if there are including filters. and none of them
 * returned <code>true</code> for a method - it is ignored. If one of the filters returned true - method tested by
 * all the excluding filters. If one of excluding filters returned true - method ignored. If none of excluding
 * filters returned true - methdo is included. If no filter were specified - all extracted getters will be used.
 *
 * <p>This builder implements {@link AbstractDynamicPerformerBuilder}, so it provides all the
 * {@link DynamicComparisonPerformer} functionality. Check out documentation for the parent class.
 *
 * <p><b>Note:</b> all created performers are <b>recursive</b>. Meaning that each created performer will delegate
 * to <b>itself</b> for any field of the same type (or an extended type). Unless (!) any other delegate is registered
 * for the type specified to the constructor.
 *
 * @see #addFilter(String)
 * @see #addFilter(Predicate)
 * @see #addExcludingFilter(String)
 * @see #addExcludingFilter(Predicate)
 */
public class GettersDynamicPerformerBuilder<T> extends AbstractDynamicPerformerBuilder<T, CompositeComparisonPerformer<T>> {

	/**
	 * Regexp string that matches four methods:
	 * <ul>
	 * 	<li>clone
	 * 	<li>getClass
	 * 	<li>hashCode
	 * 	<li>toString
	 * </ul>
	 *
	 * <p>Field is implemented as a String so any of these four methods will be matched
	 * even if overriden in a subclass.
	 *
	 * @see #addExcludingFilter(String)
	 */
	public static final String DEFAULT_METHODS = "clone|getClass|hashCode|toString";
	
	private final AtomicBoolean buildFinished = new AtomicBoolean();
	private final ClassPropertyExtractor<GetterClassProperty<?, ?>> gettersExtractor;
	private final Set<Predicate<Method>> includingFilters = new LinkedHashSet<>();
	private final Set<Predicate<Method>> excludingFilters = new LinkedHashSet<>();
	
	public GettersDynamicPerformerBuilder(Class<T> type) {
		this(type, new GettersExtractor());
	}
	
	public GettersDynamicPerformerBuilder(Class<T> type, ClassPropertyExtractor<GetterClassProperty<?, ?>> gettersExtractor) {
		super(type);
		this.gettersExtractor = Objects.requireNonNull(gettersExtractor, "Getters extractor cannot be null!");
	}

	public ClassPropertyExtractor<GetterClassProperty<?, ?>> getGettersExtractor() {
		return gettersExtractor;
	}
	
	/**
	 * Returns set of all including filters. Collection is fully mutable,
	 * so you are free to do whatever you want with it.
	 */
	public Set<Predicate<Method>> getIncludingFilters() {
		return includingFilters;
	}
	
	/**
	 * Returns set of all excluding filters. Collection is fully mutable,
	 * so you are free to do whatever you want with it.
	 */
	public Set<Predicate<Method>> getExcludingFilters() {
		return excludingFilters;
	}
	
	/**
	 * {@link PatternPredicate} is created from the specified string and
	 * registered thru {@link #addFilter(Predicate)} method.
	 */
	public GettersDynamicPerformerBuilder<T> addFilter(String pattern) {
		return addFilter(new PatternPredicate(pattern));
	}
	
	/**
	 * {@link PatternPredicate} is created from the specified string and
	 * registered thru {@link #addExcludingFilter(Predicate)} method.
	 *
	 * @see #DEFAULT_METHODS
	 */
	public GettersDynamicPerformerBuilder<T> addExcludingFilter(String pattern) {
		return addExcludingFilter(new PatternPredicate(pattern));
	}
	
	/**
	 * Specified filter will be stored in the set of including filters.
	 * Any method matched by at least one including filter is sent to
	 * be test by excluding filters (if any), or included.
	 */
	public GettersDynamicPerformerBuilder<T> addFilter(Predicate<Method> filter) {
		Objects.requireNonNull(filter, "Property filter cannot be null!");
		getIncludingFilters().add(filter);
		return this;
	}
	
	/**
	 * Specified filter will be stored in the set of excluding filters.
	 * Any method matched by an including filter but ALSO matched by an excluding filter is excluded.
	 */
	public GettersDynamicPerformerBuilder<T> addExcludingFilter(Predicate<Method> filter) {
		Objects.requireNonNull(filter, "Property filter cannot be null!");
		getExcludingFilters().add(filter);
		return this;
	}
	
	@Override
	public CompositeComparisonPerformer<T> build(String name) {
		if (!buildFinished.compareAndSet(false, true))
			throw new IllegalStateException("Due to the dynamic delegate nature builder cannot be used twice!");
		CompositeComparisonPerformer<T> performer = new CompositeComparisonPerformer<>(name, buildPerformers());
		if (!getDynamicPerformer().getRegisteredDelegates().containsKey(getType()))
			getDynamicPerformer().registerDelegate(getType(), performer);
		return performer;
	}
	
	private Map<ClassPropertyKey, ComparisonPerformer<T>> buildPerformers() {
		return streamFilteredProperties()
				.collect(Collectors.toMap(ClassProperty::getKey, this::createPerformer));
	}
	
	private Stream<GetterClassProperty<?, ?>> streamFilteredProperties() {
		return getGettersExtractor().extractAll(getType())
				.values().stream()
				.filter(not(ClassProperty::isStatic))
				.filter(createTotalPredicate());
	}
	
	private Predicate<GetterClassProperty<?, ?>> createTotalPredicate() {
		Predicate<Method> totalPositive = createTotalPositivePredicate();
		Predicate<Method> totalNegative = createTotalNegativePredicate();
		Predicate<Method> total = totalPositive.and(totalNegative);
		return (GetterClassProperty<?, ?> prop) -> total.test(prop.getGetter());
	}
	
	private Predicate<Method> createTotalPositivePredicate() {
		Set<Predicate<Method>> filters = getIncludingFilters();
		return filters.isEmpty() ? m -> true : UberPredicates.anyOf(filters);
	}
	
	private Predicate<Method> createTotalNegativePredicate() {
		Set<Predicate<Method>> filters = getExcludingFilters();
		return UberPredicates.noneOf(filters);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ComparisonPerformer<T> createPerformer(GetterClassProperty property) {
		return new PropertyDelegatePerformer<>(property, getDynamicPerformer());
	}
	
	/**
	 * <p>Predicates created from a {@link Pattern} instance or a string regex pattern, and used to match {@link Method}
	 * instances.
	 *
	 * <p>Matches if name of a method is matched by the pattern.
	 */
	public static class PatternPredicate implements Predicate<Method> {
		
		private final Pattern pattern;

		public PatternPredicate(String pattern) {
			this(Pattern.compile(pattern));
		}
		
		public PatternPredicate(Pattern pattern) {
			this.pattern = Objects.requireNonNull(pattern, "Pattern cannot be null!");
		}
		
		public Pattern getPattern() {
			return pattern;
		}
		
		@Override
		public boolean test(Method t) {
			return getPattern().matcher(t.getName()).matches();
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(pattern.flags(), pattern.pattern());
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj.getClass() == getClass()) {
				PatternPredicate that = (PatternPredicate) obj;
				return Objects.equals(getPattern().pattern(), that.getPattern().pattern())
						&& getPattern().flags() == that.getPattern().flags();
			}
			return false;
		}
		
		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("pattern", getPattern().pattern())
					.add("flags", getPattern().flags())
					.toString();
		}
	}
}
