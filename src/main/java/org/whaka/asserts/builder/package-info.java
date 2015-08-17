/**
 * <p>Package represents the basic part of the "Assertion framework" that performs assertions and provides
 * functionality to manage results.
 * 
 * <p>The only first class citizen in the package and main point of access to its functionality is the
 * {@link org.whaka.asserts.builder.AssertBuilder AssertBuilder} class. Builder provides functionality
 * to collect assertion results and throw an assertion error if results are present. Also it provides "syntactic help"
 * kind of methods to create specific corresponding "assertion performers" for different types of arguments.
 * 
 * <p>Assertion performers (which are specific for different types) provide functionality to perform direct asserts
 * and redirect results (if any) to the builder.
 * 
 * <p>Also class {@link org.whaka.asserts.builder.AssertResultConstructor AssertResultConstructor}
 * is present. It's a helper, and it's not functionally "required", but it's a part of the "responsibility chain".
 * 
 * The basic trick of the package functionality is the "chain of the responsibility" among all classes present. It
 * works something like this:
 * <ol>
 * 	<li>Assert builder creates specific assert performer with specified argument
 * 	<li>Assert performer executes assertion itself (on specific method call), sends assert fail (if any) back to
 * the builder, and creates instance of the "message constructor". <b>Note:</b> if no "assertion" method was called
 * on a performer - no results created.
 * 	<li>Result constructor might be ignored completely, for assert result (if any) is already added to the builder
 * at this point. But it provides functionality to set a message or a cause to the created result (if any) in a convenient way.
 * If no result was created by the performer - result constructor will ignore any interactions.
 * </ol>
 * 
 * <p><b>Pseudo code</b> example:
 * <pre>
 * 	1) performer = builder.assertValue(value)
 *		Performer is created.
 *		No real assertion performed.
 *		No results created yet.
 *
 * 	2) resultConstructor = performer.check()
 * 		Assertion is performed.
 * 		If assertion failed - result is created and sent to the builder.
 * 		Either way - instance of the result constructor is returned.
 * 
 * 	3) resultConstructor = resultConstructor.withMessage("message!")
 * 		Message set to the created result ONLY if it was created in the step 2
 * 		If no result is available - nothing happens.
 * 		Either way - the same instance of the result constructor is returned.
 * 
 * 	4) resultConstructor = resultConstructor.withCause(throwable)
 * 		The same as with message constructor.
 * 		If result is available - cause is set to it.
 * 		If no result is available - nothing happens.
 * 
 * 	P.S. The same functionality can be used in a method chain:
 * 
 * 		performer.assertValue(value);
 * 		performer.assertValue(value).withMessage("message!");
 * 		performer.assertValue(value).withMessage("message!").withCause(throwable);
 * </pre>
 */
package org.whaka.asserts.builder;