package com.whaka.util.reflection.comparison;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.whaka.util.UberObjects;
import com.whaka.util.reflection.properties.ClassPropertyKey;
import com.whaka.util.reflection.properties.ClassPropertyStack;

/**
 * <p>Instance of this class represents 'complex' recursive result of performed comparison for two objects.
 * This result may contain other comparison results mapped by instances of the {@link ClassPropertyKey}
 * pointing to the specific (if possible) properties of the objects.
 * 
 * <p>Example:
 * <pre>
 * 	class SimplePerson {
 * 		public String name;
 * 		public int age;
 * 		public boolean male;
 * 	}</pre>
 * Now imagine we have two instances of this class:
 * <ul>
 * 	<li>SimplePerson{name="Martin", age=30, male=true}
 * 	<li>SimplePerson{name="Martina", age=30, male=false}
 * </ul>
 * If you want to compare these two object you probably would like to get some more informative result,
 * than single <code>false</code>. This way you could create instance of this complex comparison result, with
 * fields like this:
 * <ul>
 * 	<li><b>Actual:</b> SimplePerson{name="Martin", age=30, male=true}
 * 	<li><b>Expected:</b> SimplePerson{name="Martina", age=30, male=false}
 * 	<li><b>Success:</b> <code>false</code>
 * 	<li><b>Property results:</b>
 * 		<ul>
 * 			<li>SimplePerson#name: ComparisonResult{actual="Martin", expected="Martina"}
 * 			<li>SimplePerson#age: ComparisonResult{success}
 * 			<li>SimplePerson#male: ComparisonResult{actual=true, expected=false}
 * 		</ul>
 * 	</li>
 * </ul>
 * The same recursive way if some other object contains SimplePerson as a field - you can compare them and return
 * complex result that will <i>contain</i> this result as one of the "property results". And we can go deeper!
 * 
 * <p>Of course you always can use {@link #isSuccess()} to get simple "yes/no" answer if there's no need
 * for such a depth. Complex comparison result is assumed to be successful, if all property results
 * from {@link #getPropertyResults()} are successful.
 * 
 * <p><b>Note:</b> due to specifics of the {@link ClassPropertyKey} class single complex result may contain
 * property results mapped by keys pointing to any class - no validation is performed. But main recommendation is
 * for a single complex result to contain results for properties 'declared' by the class of compared objects, or its
 * superclasses. So if class JobPosition declares fields "String title" and "SimplePerson employee", then complex result
 * for this class should contain only properties declared in JobPosition or it's ancestors.
 */
public class ComplexComparisonResult extends ComparisonResult {

	private final Map<ClassPropertyKey, ComparisonResult> propertyResults = new LinkedHashMap<>();
	
	/**
	 * Construct complex result using specified map of sub results. Specified values represent compared objects.
	 * Specified performer represents the one, performer comparison.
	 * 
	 * @see ComparisonResult#ComparisonResult(Object, Object, ComparisonPerformer, boolean)
	 */
	public ComplexComparisonResult(Object actual, Object expected, ComparisonPerformer<?> comparisonPerformer,
			Map<ClassPropertyKey, ComparisonResult> unequalProperties) {
		super(actual, expected, comparisonPerformer, true);
		if (unequalProperties != null) {
			Preconditions.checkArgument(!unequalProperties.containsKey(null), "Property key cannot be null!");
			propertyResults.putAll(unequalProperties);
		}
	}
	
	/**
	 * <p>Returns map that describes results of comparison performed for properties of objects from this result.
	 * Instances of {@link ClassPropertyKey} are used as keys of the map pointing to the specific comparison result
	 * for specific property.
	 * 
	 * <p>This result is assumed to be successful if all the results in the map are successful.
	 */
	public Map<ClassPropertyKey, ComparisonResult> getPropertyResults() {
		return Collections.unmodifiableMap(propertyResults);
	}
	
	/**
	 * <p>Method creates flat representation of the property results.
	 * 
	 * <p>Result map will contain only simple comparison results, mapped by {@link ClassPropertyStack} instances.
	 * All complex subresults will be also flattened.
	 * 
	 * <p>Property stacks are built this way: if this result contains simple sub-result with key <code>String#length()</code>
	 * the result map will contain this sub-result with key <code>{String#length()}</code> (it's a stack of one element)
	 * or <code>{length()}</code> (it's a call-string stack representation). And if this result contains complex sub-result
	 * with key <code>String#getBytes()</code> and it contains simple sub-result with key <code>byte[]#length</code>
	 * then result map will contain this simple sub-sub-result with key <code>{String#getBytes()->byte[]#length}</code>
	 * or <code>{getBytes().length}</code>
	 * 
	 * <p><b>Note:</b> all the keys in the result map will contain <code>null</code> as parent, for calling of this
	 * method is considered to be the beginning of a stack.
	 * 
	 * @see #flatten(ClassPropertyStack)
	 */
	public Map<ClassPropertyStack, ComparisonResult> flatten() {
		return flatten(null);
	}
	
	/**
	 * Analogue of the {@link #flatten()} method, but all the keys in the result map will contains specified stack
	 * as parent.
	 */
	public Map<ClassPropertyStack, ComparisonResult> flatten(ClassPropertyStack parent) {
		Map<ClassPropertyStack, ComparisonResult> map = new LinkedHashMap<>();
		for (Map.Entry<ClassPropertyKey, ComparisonResult> e : getPropertyResults().entrySet()) {
			ClassPropertyStack stack = new ClassPropertyStack(parent, e.getKey());
			ComparisonResult result = e.getValue();
			if (result instanceof ComplexComparisonResult)
				map.putAll(((ComplexComparisonResult) result).flatten(stack));
			else
				map.put(stack, result);
		}
		return map;
	}
	
	/**
	 * Complex comparison result assumed to be successful if all the property comparison result
	 * from {@link #getPropertyResults()} returns <code>true</code> from {@link #isSuccess()}.
	 */
	@Override
	public boolean isSuccess() {
		return getPropertyResults().values().stream()
				.filter(Objects::nonNull)
				.allMatch(ComparisonResult::isSuccess);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(ComplexComparisonResult.class)
				.add("success", isSuccess())
				.add("property-results", getPropertyResults().keySet())
				.add("actual", UberObjects.toString(getActual()))
				.add("expected", UberObjects.toString(getExpected()))
				.add("performer", getComparisonPerformer())
				.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getActual(), getExpected(), getComparisonPerformer(), getPropertyResults());
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			ComplexComparisonResult that = (ComplexComparisonResult) object;
			return getActual() == that.getActual()
				&& getExpected() == that.getExpected()
				&& Objects.equals(getComparisonPerformer(), that.getComparisonPerformer())
				&& Objects.equals(getPropertyResults(), that.getPropertyResults());
		}
		return false;
	}
	
	
}
