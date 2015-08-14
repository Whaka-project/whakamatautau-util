package com.whaka.util.reflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class UberMethods {

	public static Visibility getVisibility(Method m) {
		return Visibility.getFromModifiers(m.getModifiers());
	}
	
	public static boolean isFinal(Method m) {
		return Modifier.isFinal(m.getModifiers());
	}
	
	public static boolean isStatic(Method m) {
		return Modifier.isStatic(m.getModifiers());
	}
	
	/**
	 * <p>Returns <code>true</code> if <code>a</code> overrides <code>b</code>.
	 * <p><b>Note:</b> returns <code>false</code> for equal methods! For method doesn't override itself.
	 */
	public static boolean overrides(Method a, Method b) {
		if (isFinal(b) || getVisibility(b).isPrivate())
			return false;
		if (!isEqualPackage(a, b) && getVisibility(b).isDefault())
			return false;
		if (!isEqualSignature(a, b))
			return false;
		if (!UberClasses.descends(a.getDeclaringClass(), b.getDeclaringClass()))
			return false;
		return true;
	}
	
	/**
	 * Returns <code>true</code> if both methods are declared in the same package.
	 */
	public static boolean isEqualPackage(Method a, Method b) {
		return a.getDeclaringClass().getPackage() == b.getDeclaringClass().getPackage();
	}
	
	/**
	 * Returns <code>true</code> if methods have equal names and equal argument lists.
	 */
	public static boolean isEqualSignature(Method a, Method b) {
		return a.getName().equals(b.getName()) && Arrays.equals(a.getParameterTypes(), b.getParameterTypes());
	}
}
