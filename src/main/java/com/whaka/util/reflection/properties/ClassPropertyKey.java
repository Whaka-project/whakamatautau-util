package com.whaka.util.reflection.properties;

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * <p>Class describes the "point of access" to an object property. It means that this class itself doesn't have
 * any restrictions, it just contains the link to the class and the "id of a property". Which exactly property is
 * described is decided by the specific performed. <b>Note again:</b> the same key might describe different "properties"
 * depending on the specific performer!
 * 
 * <p>Class is created to underline the fact that all "properties" of an object cannot be directly pointed
 * only by an id (or name), the class declared the "property" is also required.
 * 
 * <p><b>Note:</b> The keys have single resctriction: declaring class cannot be primitive. Primitives cannot declare
 * any properties, so it is completely pointless to have a key for one.
 */
public class ClassPropertyKey {
	
	private final Object id;
	private final Class<?> declaringClass;
	
	/**
	 * Declaring class set as <code>null</code>. Basically this is means: "any property of this name".
	 */
	public ClassPropertyKey(String name) {
		this(name, null);
	}
	
	public ClassPropertyKey(String name, Class<?> declaringClass) {
		this((Object) name, declaringClass);
	}
	
	public ClassPropertyKey(Object id, Class<?> declaringClass) {
		this.id = assertId(id);
		this.declaringClass = assertDeclaringClass(declaringClass);
	}
	
	private static Object assertId(Object id) {
		Preconditions.checkArgument(id != null && !id.toString().trim().isEmpty(), "Property id cannot be null or empty!");
		return id;
	}
	
	private static Class<?> assertDeclaringClass(Class<?> declaringClass) {
		if (declaringClass != null)
			Preconditions.checkArgument(!declaringClass.isPrimitive(), "Primitive types cannot have any members!");
		return declaringClass;
	}
	
	public Object getId() {
		return id;
	}
	
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getDeclaringClass(), getId());
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			ClassPropertyKey that = (ClassPropertyKey) object;
			return Objects.equals(getDeclaringClass(), that.getDeclaringClass())
					&& Objects.equals(getId(), that.getId());
		}
		return false;
	}

	@Override
	public String toString() {
		String classStr = Optional.ofNullable(getDeclaringClass()).map(Class::getSimpleName).orElse("?");
		return String.format("%s#%s", classStr, getId());
	}
}