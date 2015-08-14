/**
 * <p>Package represents a framework to perform assertion of certain conditions. All functionality is based
 * on the class {@link com.whaka.asserts.builder.AssertBuilder AssertBuilder} from the package
 * {@link com.whaka.asserts.builder asserts.builder}. Please check out its documentation for
 * complete understanding of functionality.
 * 
 * <p>First level classes in this package:
 * <ul>
 * 	<li>{@link com.whaka.asserts.AssertResult AssertResult}
 * 		<p>Represents single assertion <b>fail</b> result.
 * 		Created by different performers when conditions are not satisfied.
 * 		<br><br>
 * 	<li>{@link com.whaka.asserts.AssertError AssertError}
 * 		<p>Main throwable entity of the framework.
 * 		Contains 1+ assertion results.
 * 		<br><br>
 * 	<li>{@link com.whaka.asserts.Assert Assert}
 * 		<p>Kind of an entry point for all the functionality in the package.
 * </ul>
 */
package com.whaka.asserts;