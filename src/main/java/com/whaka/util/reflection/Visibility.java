package com.whaka.util.reflection;

import java.lang.reflect.Modifier;

public enum Visibility {
	
	PRIVATE,
	DEFAULT,
	PROTECTED,
	PUBLIC;
	
	public boolean isPrivate() {
		return this == PRIVATE;
	}
	
	public boolean isDefault() {
		return this == DEFAULT;
	}
	
	public boolean isProtected() {
		return this == PROTECTED;
	}
	
	public boolean isPublic() {
		return this == PUBLIC;
	}
	
	public static Visibility getFromModifiers(int modifiers) {
		if (Modifier.isPrivate(modifiers))
			return PRIVATE;
		if (Modifier.isProtected(modifiers))
			return PROTECTED;
		if (Modifier.isPublic(modifiers))
			return PUBLIC;
		return DEFAULT;
	}
}