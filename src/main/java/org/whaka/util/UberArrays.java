package org.whaka.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

import com.google.common.base.Preconditions;

public class UberArrays {

	private UberArrays() {
	}
	
	/**
	 * This method will recursively convert any collection contained in the specified array into an object array
	 */
	public static Object[] eliminateCollections(Object[] array) {
		Object[] result = new Object[array.length];
		for (int i = 0; i < array.length; i++)
			result[i] = eliminateCollections(array[i]);
		return result;
	}
	
	private static Object eliminateCollections(Object o) {
		if (o instanceof Collection<?>)
			return eliminateCollections(((Collection<?>)o).toArray());
		else if (o instanceof Object[])
			return eliminateCollections(((Object[])o).clone());
		return o;
	}
	
	public static int getArrayDepth(Class<?> arrayClass) {
		int depth = 0;
		while(arrayClass.isArray()) {
			depth++;
			arrayClass = arrayClass.getComponentType();
		}
		return depth;
	}
	
	public static Class<?> createArrayClass(Class<?> componentType, int depth) {
		Preconditions.checkArgument(depth < 256, "Array depth cannot be greater than 255!");
		if (depth > 0) {
			for (int i = 0; i < depth; i++)
				componentType = Array.newInstance(componentType, 0).getClass();
		}
		else {
			for (int i = depth; i < 0 && componentType.isArray(); i++)
				componentType = componentType.getComponentType();
		}
		return componentType;
	}
	
	public static String toString(Object o) {
		if (o != null && o.getClass().isArray()) {
			if (o instanceof boolean[])
				return Arrays.toString((boolean[])o);
			if (o instanceof byte[])
				return Arrays.toString((byte[])o);
			if (o instanceof short[])
				return Arrays.toString((short[])o);
			if (o instanceof char[])
				return Arrays.toString((char[])o);
			if (o instanceof int[])
				return Arrays.toString((int[])o);
			if (o instanceof long[])
				return Arrays.toString((long[])o);
			if (o instanceof float[])
				return Arrays.toString((float[])o);
			if (o instanceof double[])
				return Arrays.toString((double[])o);
			if (o instanceof Object[])
				return Arrays.deepToString(eliminateCollections((Object[])o));
		}
		return String.valueOf(o);
	}
}
