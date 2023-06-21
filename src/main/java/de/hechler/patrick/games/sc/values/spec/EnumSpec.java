package de.hechler.patrick.games.sc.values.spec;

import java.util.Objects;

import de.hechler.patrick.games.sc.values.EnumValue;

public record EnumSpec<T extends Enum<T>>(String name, Class<T> cls) implements ValueSpec {
	
	public EnumSpec {
		Objects.requireNonNull(name, "name");
	}
	
	public EnumValue<T> withValue(T val) {
		if (!this.cls.isInstance(val)) {
			throw new IllegalArgumentException("the value is no instance of the given class: " + this.cls + " def: " + (val != null ? val.getClass() : "null"));
		}
		return new EnumValue<>(this.name, val);
	}
	
}

