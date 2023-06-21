package de.hechler.patrick.games.sc.values;

import java.util.Objects;

public record LongValue(String name, long value) implements Value {
	
	public LongValue {
		Objects.requireNonNull(name, "name");
	}
	
}

