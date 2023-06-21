package de.hechler.patrick.games.sc.values;

import java.util.Objects;

public record EnumValue<T extends Enum<T>>(String name, T value) implements Value {
	
	public EnumValue {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(value, "value");
	}
	
}

