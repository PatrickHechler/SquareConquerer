package de.hechler.patrick.games.sc.values;

import java.util.Objects;

public record DoubleValue(String name, double value) implements Value {
	
	public DoubleValue {
		Objects.requireNonNull(name, "name");
	}
	
}

