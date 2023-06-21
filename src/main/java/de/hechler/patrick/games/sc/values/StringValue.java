package de.hechler.patrick.games.sc.values;

import java.util.Objects;

public record StringValue(String name, String value) implements Value {
	
	public StringValue {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(value, "value");
	}
	
}

