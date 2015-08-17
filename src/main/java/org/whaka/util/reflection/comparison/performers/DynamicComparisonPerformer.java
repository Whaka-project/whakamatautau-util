package org.whaka.util.reflection.comparison.performers;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonPerformers;
import org.whaka.util.reflection.comparison.ComparisonResult;

/**
 * <p>This performer allows you to register delegates that will be dynamically selected at the moment of execution.
 * 
 * <p>All registered delegates are mapped by a class. When {@link #compare(Object, Object)} is called -
 * {@link #getDelegate(Object, Object)} is called and result delegate is used to perform actual comparison;
 */
public class DynamicComparisonPerformer extends AbstractComparisonPerformer<Object> {

	private final Map<Class<?>, ComparisonPerformer<?>> registeredDelegates = new LinkedHashMap<>();
	private final Map<Class<? extends Collection<?>>, Map<Class<?>, Function<?, ?>>> collectionDelegateProviders = new LinkedHashMap<>();
	private final Map<Class<? extends Map<?,?>>, Map<Class<?>, Function<?, ?>>> mapDelegateProviders = new LinkedHashMap<>();
	private final Map<Class<?>, Function<?, ?>> arrayDelegateProviders = new LinkedHashMap<>();
	private ComparisonPerformer<Object> defaultDelegate = ComparisonPerformers.DEEP_EQUALS;
	
	public DynamicComparisonPerformer() {
		super("DynamicCompare");
	}
	
	/**
	 * This method calls {@link #findRegisteredDelegate(Object, Object)} and if its result is null -
	 * result of the {@link #getDefaultDelegate()} is returned.
	 */
	public ComparisonPerformer<?> getDelegate(Object actual, Object expected) {
		ComparisonPerformer<?> registered = findRegisteredDelegate(actual, expected);
		return registered == null ? getDefaultDelegate() : registered;
	}

	/**
	 * Default delegate to be used when no other delegate is found for a pair of objects.
	 * Cannot be <code>null</code> or <code>this</code> object.
	 */
	public void setDefaultDelegate(ComparisonPerformer<Object> defaultDelegate) {
		Objects.requireNonNull(defaultDelegate, "Default delegate cannot be null!");
		Preconditions.checkArgument(defaultDelegate != this, "Dynamic performer cannot be used as it's own default delegate!");
		this.defaultDelegate = defaultDelegate;
	}
	
	/**
	 * Default delegate to be used when no other delegate is found for a pair of objects.
	 * @see #setDefaultDelegate(ComparisonPerformer)
	 */
	public ComparisonPerformer<Object> getDefaultDelegate() {
		return defaultDelegate;
	}
	
	public <V> DynamicComparisonPerformer registerDelegate(Class<V> valueType, ComparisonPerformer<? super V> delegate) {
		registeredDelegates.put(valueType, delegate);
		return this;
	}
	
	public Map<Class<?>, ComparisonPerformer<?>> getRegisteredDelegates() {
		return registeredDelegates;
	}

	/**
	 * <p>Method allows you to register delegate <i>providers</i>, used to created delegates for arrays of different types.
	 * Delegate is matched if both compared values are instances of the key class.
	 * Delegates are matched in the order of registration, so first matched delegate will be used.
	 * 
	 * <p>Function will always receive <b>this</b> dynamic performer and should produce comparison performer able
	 * to accept two arrays of the specified type, and compare them properly.
	 * 
	 * <p>By default: {@link ArrayComparisonPerformer} is used for all arrays.
	 * 
	 * @see #getArrayDelegateProviders()
	 */
	public <V> DynamicComparisonPerformer registerArrayDelegateProvider(
			Class<V> arrayType, Function<ComparisonPerformer<? super V>, ComparisonPerformer<? super V[]>> provider) {
		Preconditions.checkArgument(Object[].class.isAssignableFrom(arrayType),
				"Array performer provider can be registered only for a type derived from Object[]!");
		arrayDelegateProviders.put(arrayType, provider);
		return this;
	}
	
	/**
	 * <p>Returns map of registered array delegates providers. Map is fully mutable, so you can do whatever you want
	 * with it, just remember that types should be thoroughly checked, or there's a big possibility of type cast error
	 * at the moment of performance. It is recommended to use {@link #registerArrayDelegateProvider(Class, Function)}
	 * method.
	 */
	public Map<Class<?>, Function<?, ?>> getArrayDelegateProviders() {
		return arrayDelegateProviders;
	}
	
	/**
	 * <p>Method allows you to register delegate <i>providers</i>, used to created delegates for collections
	 * of different types and different contents. Provider is matched if both compared values are instances of the
	 * specified collection type, and all of its elements are instances of the specified value type. So you
	 * can register specific provider for types: List of Object, to match ALL lists; or you can register
	 * provider for types: Collection of String, to cover any collections of strings.
	 * 
	 * <p>Both collection type and element types are checked in the order of registration, so if you register
	 * provider for types Collection of Object, and then register provider for types: List of Object - second
	 * one will never be used. The same if you register second provider for types: Collection of String - it will
	 * never be used.
	 * 
	 * <p>Function will always receive <b>this</b> dynamic performer and should produce comparison performer able
	 * to accept two collections of the specified type, and compare them propertly.
	 * 
	 * <p>By default: {@link SetComparisonPerformer} is used for all collections.
	 * 
	 * @see #getCollectionDelegateProviders()
	 */
	public <V, C extends Collection<?>> DynamicComparisonPerformer registerCollectionDelegateProvider(
			Class<C> collectionType, Class<V> valueType, Function<ComparisonPerformer<V>, ComparisonPerformer<? super C>> provider) {
		collectionDelegateProviders
			.computeIfAbsent(collectionType, c-> new LinkedHashMap<>())
			.put(valueType, provider);
		return this;
	}

	/**
	 * <p>Returns map of registered collection delegate providers. Map is fully mutable, so you can do whatever you want
	 * with it, just remember that types should be thoroughly checked, or there's a big possibility of type cast error
	 * at the moment of performance. It is recommended to use {@link #registerCollectionDelegateProvider(Class, Class, Function)}
	 * method.
	 */
	public Map<Class<? extends Collection<?>>, Map<Class<?>, Function<?, ?>>> getCollectionDelegateProviders() {
		return collectionDelegateProviders;
	}
	
	/**
	 * <p>Method allows you to register delegate <i>providers</i>, used to created delegates for maps of different
	 * types and different contents. Provider is matched if both compared values are instances of the
	 * specified map type, and all of its <b>values</b> are instances of the specified value type. So you
	 * can register specific provider for types: HashMap of Object, to match ALL hashmaps; or you can register
	 * provider for types: Map of String, to cover any map with string values.
	 * 
	 * <p><b>Note:</b> keys are ignored - they can be of any type.
	 * 
	 * <p>Both map type and value type are checked in the order of registration, so if you register
	 * provider for types Map of Object, and then register provider for types: HashMap of Object - second
	 * one will never be used. The same if you register second provider for types: Map of String - it will
	 * never be used.
	 * 
	 * <p>Function will always receive <b>this</b> dynamic performer and should produce comparison performer able
	 * to accept two maps of the specified type, and compare them propertly.
	 * 
	 * <p>By default: {@link MapComparisonPerformer} is used for all maps.
	 * 
	 * @see #getMapDelegateProviders()
	 */
	public <V, M extends Map<?,V>> DynamicComparisonPerformer registerMapDelegateProvider(
			Class<M> mapType, Class<V> valueType, Function<ComparisonPerformer<V>, ComparisonPerformer<? super M>> provider) {
		mapDelegateProviders
			.computeIfAbsent(mapType, c-> new LinkedHashMap<>())
			.put(valueType, provider);
		return this;
	}

	/**
	 * <p>Returns map of registered map delegate providers. Map is fully mutable, so you can do whatever you want
	 * with it, just remember that types should be thoroughly checked, or there's a big possibility of type cast error
	 * at the moment of performance. It is recommended to use {@link #registerMapDelegateProvider(Class, Class, Function)}
	 * method.
	 */
	public Map<Class<? extends Map<?, ?>>, Map<Class<?>, Function<?, ?>>> getMapDelegateProviders() {
		return mapDelegateProviders;
	}

	/**
	 * <p>If any registered delegate is matched - it will be returned first. Delegate is matched
	 * if both specified objects are instances of the key class. So if you will register a delegate
	 * for the Object class - it will match any specified objects (if both are not null).
	 * 
	 * <p><b>Note:</b> delegates are checked in the order of registration. So if you will register a delegate
	 * for the CharSequence class, and then register a delegate for the String class - second one will never be used.
	 * 
	 * <p>If no registered delegate is found - special cases are checked. Which are:
	 * <ul>
	 * 	<li>If both values are instances of the Object[] - array performer is created
	 * 	<li>If both values are instances of the Collection<?> - collection performer is created
	 * 	<li>If both values are instances of the Map<?,?> - map performer is created
	 * </ul>
	 * 
	 * <p><b>Note:</b> each specific performer receives <b>this</b> dynamic performer as delegate which means
	 * that elements are compared with the same registered delegates.
	 * 
	 * <p>You can configure special cases using methods:
	 * <ul>
	 * 	<li> {@link #registerArrayDelegateProvider(Class, Function)}
	 * 	<li> {@link #registerCollectionDelegateProvider(Class, Class, Function)}
	 * 	<li> {@link #registerMapDelegateProvider(Class, Class, Function)}
	 * </ul>
	 * 
	 * <p>If non of the special cases has matched - null is returned.
	 */
	public ComparisonPerformer<?> findRegisteredDelegate(Object actual, Object expected) {
		if (actual == null || expected == null)
			return null;
		for (Map.Entry<Class<?>, ComparisonPerformer<?>> e : getRegisteredDelegates().entrySet())
			if (e.getKey().isInstance(actual) && e.getKey().isInstance(expected))
				return e.getValue();
		if (actual instanceof Object[] && expected instanceof Object[])
			return createArrayDelegate((Object[]) actual, (Object[]) expected);
		if (actual instanceof Collection<?> && expected instanceof Collection<?>)
			return createCollectionDelegate((Collection<?>) actual, (Collection<?>) expected);
		if (actual instanceof Map<?,?> && expected instanceof Map<?,?>)
			return createMapDelegate((Map<?,?>) actual, (Map<?,?>) expected);
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	private ComparisonPerformer<?> createArrayDelegate(Object[] actual, Object[] expected) {
		Function<ComparisonPerformer, ComparisonPerformer> provider = findArrayDelegateProvider(actual, expected);
		return provider.apply(this);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Function<ComparisonPerformer, ComparisonPerformer> findArrayDelegateProvider(
			Object[] actual, Object[] expected) {
		Function provider = getArrayDelegateProviders().entrySet().stream()
			.filter(e -> e.getKey().isInstance(actual) && e.getKey().isInstance(expected))
			.map(Map.Entry::getValue)
			.findFirst().orElse(null);
		return provider == null ? ComparisonPerformers::array : provider;
	}
	
	@SuppressWarnings("rawtypes")
	private ComparisonPerformer<?> createCollectionDelegate(Collection<?> actual, Collection<?> expected) {
		Function<ComparisonPerformer, ComparisonPerformer> provider = findCollectionDelegateProvider(actual, expected);
		return provider.apply(this);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Function<ComparisonPerformer, ComparisonPerformer> findCollectionDelegateProvider(
			Collection<?> actual, Collection<?> expected) {
		Function provider = getProviderForCollectionElementsType(actual, expected);
		return provider == null ? ComparisonPerformers::set : provider;
	}
	
	private Function<?, ?> getProviderForCollectionElementsType(Collection<?> actual, Collection<?> expected) {
		BiPredicate<Collection<?>, Class<?>> allElementsMatch = (col, type) -> col.stream().allMatch(type::isInstance);
		Predicate<Class<?>> bothCollectionsMatch = c -> allElementsMatch.test(actual, c) && allElementsMatch.test(expected, c);
		return getProvidersForSpecificCollectionType(actual, expected).entrySet().stream()
			.filter(e -> bothCollectionsMatch.test(e.getKey()))
			.map(Map.Entry::getValue)
			.findFirst().orElse(null);
	}
	
	private Map<Class<?>, Function<?, ?>> getProvidersForSpecificCollectionType(Collection<?> actual, Collection<?> expected) {
		return getCollectionDelegateProviders().entrySet().stream()
			.filter(e -> e.getKey().isInstance(actual) && e.getKey().isInstance(expected))
			.map(Map.Entry::getValue)
			.findFirst()
			.orElseGet(Collections::emptyMap);
	}
	
	@SuppressWarnings("rawtypes")
	private ComparisonPerformer<?> createMapDelegate(Map<?,?> actual, Map<?,?> expected) {
		Function<ComparisonPerformer, ComparisonPerformer> provider = findMapDelegateProvider(actual, expected);
		return provider.apply(this);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Function<ComparisonPerformer, ComparisonPerformer> findMapDelegateProvider(
			Map<?,?> actual, Map<?,?> expected) {
		Function provider = getProviderForMapElementsType(actual, expected);
		return provider == null ? ComparisonPerformers::map : provider;
	}
	
	private Function<?, ?> getProviderForMapElementsType(Map<?,?> actual, Map<?,?> expected) {
		BiPredicate<Map<?,?>, Class<?>> allElementsMatch = (map, type) -> map.values().stream().allMatch(type::isInstance);
		Predicate<Class<?>> bothCollectionsMatch = c -> allElementsMatch.test(actual, c) && allElementsMatch.test(expected, c);
		return getProvidersForSpecificMapType(actual, expected).entrySet().stream()
			.filter(e -> bothCollectionsMatch.test(e.getKey()))
			.map(Map.Entry::getValue)
			.findFirst().orElse(null);
	}
	
	private Map<Class<?>, Function<?, ?>> getProvidersForSpecificMapType(Map<?,?> actual, Map<?,?> expected) {
		return getMapDelegateProviders().entrySet().stream()
			.filter(e -> e.getKey().isInstance(actual) && e.getKey().isInstance(expected))
			.map(Map.Entry::getValue)
			.findFirst()
			.orElseGet(Collections::emptyMap);
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ComparisonResult compare(Object actual, Object expected) {
		ComparisonPerformer delegate = getDelegate(actual, expected);
		return delegate.compare(actual, expected);
	}
}
