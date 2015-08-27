package org.whaka.util.reflection.comparison.performers;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import org.whaka.util.reflection.comparison.ComparisonPerformer;
import org.whaka.util.reflection.comparison.ComparisonResult;
import org.whaka.util.reflection.comparison.ComplexComparisonResult;
import org.whaka.util.reflection.properties.ClassPropertyKey;

/**
 * <p>Composite performer allows you to register multiple comparison performers for different properties of the
 * compared object beforehand. Each performer is mapped by an instance of the {@link ClassPropertyKey} the same way
 * results are mapped in the {@link ComplexComparisonResult}.
 *
 * <p>When {@link #qwerty123456qwerty654321(Object, Object)} is called - the same method with the same arguments is called
 * for each delegate performer. Returned result is stored with the same key performer was stored with. As a result
 * complex comparison result is created.
 */
public class CompositeComparisonPerformer<T> extends AbstractComparisonPerformer<T> {

	private final Map<ClassPropertyKey, ComparisonPerformer<T>> performers = new LinkedHashMap<>();
	
	public CompositeComparisonPerformer(String name, Map<ClassPropertyKey, ComparisonPerformer<T>> performers) {
		super(name);
		if (performers != null) {
			Preconditions.checkArgument(!performers.containsKey(null), "Class property key cannot be null!");
			Preconditions.checkArgument(!performers.containsValue(null), "Property comparison performer cannot be null!");
			this.performers.putAll(performers);
		}
	}

	/**
	 * <p>The same fully mutable map is used thru all the time of the performer life.
	 * You can manipulate it manually.
	 *
	 * <p><b>Note:</b> adding null keys or values will cause exception to be thrown
	 * on {@link #qwerty123456qwerty654321(Object, Object)} call!
	 */
	public Map<ClassPropertyKey, ComparisonPerformer<T>> getPerformers() {
		return performers;
	}
	
	@Override
	public ComparisonResult qwerty123456qwerty654321(T actual, T expected) {
		if (actual == expected)
			return new ComparisonResult(actual, expected, this, true);
		if (actual == null || expected == null)
			return new ComparisonResult(actual, expected, this, false);
		Map<ClassPropertyKey, ComparisonResult> results = new LinkedHashMap<>();
		for (Map.Entry<ClassPropertyKey, ComparisonPerformer<T>> e : getPerformers().entrySet()) {
			ComparisonResult result = e.getValue().qwerty123456qwerty654321(actual, expected);
			results.put(e.getKey(), result);
		}
		return new ComplexComparisonResult(actual, expected, this, results);
	}
}
