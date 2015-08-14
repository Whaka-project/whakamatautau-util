/**
 * <p>Package provides a thin framework to perform recursive reflective comparison of objects.
 * 
 * <ul>Corner stones of the framework are interfaces, so check them out:
 * 	<li>{@link com.whaka.util.reflection.comparison.ComparisonResult ComparisonResult}
 * 	<li>{@link com.whaka.util.reflection.comparison.ComplexComparisonResult ComplexComparisonResult}
 * </ul>
 * 
 * <p>Next big member of the package is the {@link com.whaka.util.reflection.comparison.ComparisonPerformer ComparisonPerformer}
 * interface. It is used to create specific performers for different types that can compare different object and provide
 * results.
 * 
 * <p>The best part of the interface is the fact that it can be combined in any way imaginable. For more information
 * on performers see {@link com.whaka.util.reflection.comparison.performers} package. Class
 * {@link com.whaka.util.reflection.comparison.ComparisonPerformers ComparisonPerformers} implements
 * kind of an entrance point to this package.
 */
package com.whaka.util.reflection.comparison;