/**
 * <p>Package provides all the freaky implementations for the interface
 * {@link org.whaka.util.reflection.comparison.ComparisonPerformer ComparisonPerformer} and different
 * tools to build or combine them.
 * 
 * <ul>Key members:
 * 	<li>{@link org.whaka.util.reflection.comparison.performers.AbstractComparisonPerformer AbstractComparisonPerformer}
 * 		<p>Can be used to build the most obvious and plane implementation of a custom comparison performer.
 * 		No restrictions. You just implements a method and do what you want to produce a result.
 * 		May be successfully combined with the {@link org.whaka.util.reflection.comparison.ComplexComparisonResultBuilder ComplexComparisonResultBuilder}
 * 
 * 	<br/><br/>
 * 	<li>{@link org.whaka.util.reflection.comparison.performers.DynamicComparisonPerformer DynamicComparisonPerformer}
 * 		<p>Can be used to register custom delegates for specific types and then select them at the moment of execution.
 * 		Provides in-built functionality to resolve arrays, collections, and maps. Quite complex functionality.
 * 		Please read documentation.
 * 
 * 	<br/><br/>
 * 	<li>{@link org.whaka.util.reflection.comparison.performers.CompositeComparisonPerformer CompositeComparisonPerformer}
 * 		<p>Combines multiple custom performers into map, to produce the
 * 		{@link org.whaka.util.reflection.comparison.ComplexComparisonResult ComplexComparisonResult}
 * 		with a mirroring map of results.
 * 
 * 	<br/><br/>
 * 	<li>{@link org.whaka.util.reflection.comparison.performers.PropertyDynamicPerformerBuilder PropertyDynamicPerformerBuilder}
 * 		<p>More complex (or more easy) way to build a custom comparison performer. Allows to add specific class properties,
 * 		or to build these properties right there, and then produce composite performer with a corresponding map of
 * 		delegate performers. Allows specify direct delegates for some properties.
 * 		<p>This builder incorporate {@link org.whaka.util.reflection.comparison.performers.DynamicComparisonPerformer DynamicComparisonPerformer}
 * 		into its functionality. So be sure you read the documentation.
 * 
 * 	<br/><br/>
 * 	<li>{@link org.whaka.util.reflection.comparison.performers.GettersDynamicPerformerBuilder GettersDynamicPerformerBuilder}
 * 		<p>Next step in building custom performers. This one allows you to automatically build performer from all the
 * 		getter methods in a specific class (and its ancestors). You can filter getters in or out. But you cannot specify
 * 		direct delegates for separate getters (only by result type), so if you need more detailed or complex functionality
 * 		use {@link org.whaka.util.reflection.comparison.performers.PropertyDynamicPerformerBuilder PropertyDynamicPerformerBuilder}
 * 		<p>This builder also incorporate {@link org.whaka.util.reflection.comparison.performers.DynamicComparisonPerformer DynamicComparisonPerformer}
 * 		into its functionality. So be sure you read the documentation.
 * </ul>
 * 
 * <p>Also provides bunch of specific performers or wrappers. You should check out them by yourself.
 */
package org.whaka.util.reflection.comparison.performers;