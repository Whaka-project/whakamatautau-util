package org.whaka

import org.whaka.asserts.AssertResult
import org.whaka.asserts.AssertResultTest

class TestData {

	/*
	 * NOTE: #variousObjects, #variousMessages, and #variousCauses
	 * might return arrays of equal size! For they are used as data providers.
	 */

	public static Object[] variousObjects() {
		return [
			null,
			true,
			42,
			999999L,
			"En Taro Adun!",
			Double.POSITIVE_INFINITY,
			1f,
			(short) 100,
		]
	}

	public static Object[] variousMessages() {
		return [
			null,
			"",
			"Do you expect me to talk?",
			"979876542313213156654",
			"file://<>&*()!@#\$%^&*[{]\\%s%d%q%_%%//[}{}[]()(**/*/*/**..,.,.,.<><><",
			AssertResultTest.metaClass.methods.get(0).toString(),
			"*",
			"Supercalifragilisticexpialidocious"
		]
	}

	public static Throwable[] variousCauses() {
		return [
			null,
			new RuntimeException(),
			new IllegalArgumentException("sup"),
			new OutOfMemoryError("fake"),
			new Error("ah oh"),
			new IndexOutOfBoundsException(),
			new IOException(),
			new Throwable(new RuntimeException("levels!"))
		]
	}

	public static def createAssertResultsList(Object actual, Object expected, String message, Throwable cause) {
		return [new AssertResult(actual, expected, message, cause), new AssertResult(actual, expected, message),
			new AssertResult(message, cause), new AssertResult(message), new AssertResult()]
	}
}
