package de.hechler.patrick.games.sc.values;

import java.util.Objects;

public record BooleanValue(String name, boolean value) implements Value {
	
	public BooleanValue {
		Objects.requireNonNull(name, "name");
	}
	
}

