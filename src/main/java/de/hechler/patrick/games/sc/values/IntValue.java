package de.hechler.patrick.games.sc.values;

import java.util.Objects;

public record IntValue(String name, int value) implements Value {
	
	public IntValue {
		Objects.requireNonNull(name, "name");
	}
	
}

