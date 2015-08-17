package org.whaka.util.reflection.properties;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * <p>Class represents stack of property keys (or property calls). The same way how objects are recursivly
 * contain each other - their properties can be represented as stacks, where property mapped by a key, contains
 * another property which can be mapped by another key.
 * 
 * <p>For example, class property stack may be represented as chained method call:
 */
public class ClassPropertyStack {

	private final ClassPropertyStack parent;
	private final ClassPropertyKey value;
	
	public ClassPropertyStack(ClassPropertyKey value) {
		this(null, value);
	}
	
	public ClassPropertyStack(ClassPropertyStack parent, ClassPropertyKey value) {
		this.parent = parent;
		this.value = Objects.requireNonNull(value, "Value property key cannot be null!");
	}
	

	public ClassPropertyStack getParent() {
		return parent;
	}
	
	public ClassPropertyKey getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(toLongCallString())
				.toString();
	}
	
	public String toLongCallString() {
		String parentStr = "";
		if (getParent() != null)
			parentStr = getParent().toLongCallString() + "->";
		return parentStr + getValue().toString();
	}
	
	public String toCallString() {
		ClassPropertyStack parent = getParent();
		if (parent == null)
			return getValue().toString();
		return parent.toCallString() + "." + getValue().getId();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getParent(), getValue());
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			ClassPropertyStack that = (ClassPropertyStack) object;
			return Objects.equals(getParent(), that.getParent())
					&& Objects.equals(getValue(), that.getValue());
		}
		return false;
	}
	
	public static ClassPropertyStack createStack(ClassPropertyKey... keys) {
		ClassPropertyStack parent = null;
		for (ClassPropertyKey key : keys)
			parent = new ClassPropertyStack(parent, key);
		return parent;
	}
}
