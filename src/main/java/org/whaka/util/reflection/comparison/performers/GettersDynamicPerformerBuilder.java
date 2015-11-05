package org.whaka.util.reflection.comparison.performers;

import static org.whaka.util.UberPredicates.*;

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

import org.whaka.util.UberPredicates;
import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonPerformers;
import org.whaka.util.reflection.properties.ClassProperty;
import org.whaka.util.reflection.properties.ClassPropertyExtractor;
import org.whaka.util.reflection.properties.ClassPropertyKey;
import org.whaka.util.reflection.properties.GetterClassProperty;
import org.whaka.util.reflection.properties.GettersExtractor;

import com.google.common.base.MoreObjects;

/**
 * <p>Class provides functionality to build {@link CompositeComparisonPerformer} by streaming all the getter methods
 * in the specified class and filtering out the ones you want to include or exclude. All getter properties will
 * be automatically mapper by property keys. Result performer will contain delegating performers for each getter.
 *
 * <p>Getters are retrieved by the instance of the {@link ClassPropertyExtractor} with type {@link GetterClassProperty}.
 * By default - {@link GettersExtractor} class is used. But you can specify your own extractor - if necessary.
 *
 * <p><b>Note:</b> that {@link GettersExtractor} by default treats as getters only methods with non-void return type
 * and no arguments, so any other methods will be ignored by default. Also all static methods are ignored at the very
 * beginning, so you don't have to filter them out manually.
 *
 * <p><b>Note:</b> if you specified in the constructor actual class (not an interface) - it will also contain all the
 * 'getters' from the ancestor Object class, including #hashCode(), #getClass(), and #clone(). They are not filtered
 * out by default, to support rare occasions when they might be required. But cuz most of the time they are not wanted
 * to be used - you can use constant field {@link #DEFAULT_METHODS}. You can use method
 * {@link #addExcludingFilter(String)} with this filter to exclude all getters from Object class.
 * Or even better you can use {@link ComparisonPerformers#buildGetters(Class)} to create a builder with this filter
 * already added (but be careful, and read documentation).
 *
 * <p>This builder doesn't allow to specify a delegate performer specifically for a property, so default
 * {@link DynamicComparisonPerformer} will be used for all the created properties.
 *
 * <p>There're three groups of filters, used by this class to filter getters:
 * <ul>
 * 	<li>Requirement filters - <b>all</b> of them have to accept method, for it to be included.
 * 	<li>Including filters - <b>any</b> of them have to accept method (if any present), for it to be included.
 * 	<li>Excluding filters - <b>none</b> of them should accept method, for it to be included.
 * </ul>
 *
 * <p>Method is included in the result performer if it matched by all requirement filters, at least one including
 * filter (if any present) and NOT matched by any excluding filter.
 * 
 * <p>If there're any requirement filters, and at least one of them returned <code>false</code> - method is ignored.
 * If there're no requirement filters or all of them accepted method - it is sent to including filters.
 * If there're any including filters, and none of them returned <code>true</code> for a method - it is ignored.
 * If there're no including filters, or one of them returned <code>true</code> - it is sent to excluding filters.
 * If there're any excluding filters and any one of them returned <code>true</code> - method ignored.
 * If there're no excluding filters of none of them returned <code>true</code> - method is included.
 * 
 *  <p>So if no filters of any kind were specified - all extracted getters will be used.
 *
 * <p>This builder implements {@link AbstractDynamicPerformerBuilder}, so it provides all the
 * {@link DynamicComparisonPerformer} functionality. Check out documentation for the parent class.
 *
 * <p><b>Note:</b> all created performers are <b>recursive</b>. Meaning that each created performer will delegate
 * to <b>itself</b> for any field of the same type (or an extended type). Unless (!) any other delegate is registered
 * for the type specified to the constructor.
 *
 * @see #addRequirement(Predicate)
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
	private final Set<Predicate<Method>> requirementFilters = new LinkedHashSet<>();
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
	 * Returns set of all requirement filters. Collections is fully mutable
	 * so you are free to do whatever you want with it.
	 */
	public Set<Predicate<Method>> getRequirementFilters() {
		return requirementFilters;
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
	 * Specified filter will be stored in the set of requirement filters.
	 * Any method matched by all requirement filters is sent to be test by including filters.
	 */
	public GettersDynamicPerformerBuilder<T> addRequirement(Predicate<Method> filter) {
		Objects.requireNonNull(filter, "Property filter cannot be null!");
		getRequirementFilters().add(filter);
		return this;
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
		Predicate<Method> totalRequired = createTotalRequiredPredicate();
		Predicate<Method> totalIncluded = createTotalPositivePredicate();
		Predicate<Method> totalExcluded = createTotalNegativePredicate();
		Predicate<Method> total = UberPredicates.allOf(totalRequired, totalIncluded, totalExcluded);
		return (GetterClassProperty<?, ?> prop) -> total.test(prop.getGetter());
	}
	
	/**
	 * All of requirement filters should accept a method to be included
	 */
	private Predicate<Method> createTotalRequiredPredicate() {
		Set<Predicate<Method>> filters = getRequirementFilters();
		return filters.isEmpty() ? m -> true : UberPredicates.allOf(filters);
	}
	
	/**
	 * Any of including filters should accept a method to be included
	 */
	private Predicate<Method> createTotalPositivePredicate() {
		Set<Predicate<Method>> filters = getIncludingFilters();
		return filters.isEmpty() ? m -> true : UberPredicates.anyOf(filters);
	}
	
	/**
	 * None of excluding filters should accept a method to be included
	 */
	private Predicate<Method> createTotalNegativePredicate() {
		Set<Predicate<Method>> filters = getExcludingFilters();
		return filters.isEmpty() ? m -> true : UberPredicates.noneOf(filters);
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
