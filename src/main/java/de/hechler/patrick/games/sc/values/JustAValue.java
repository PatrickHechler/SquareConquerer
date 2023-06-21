package de.hechler.patrick.games.sc.values;

import java.util.Objects;

public record JustAValue(String name) implements Value {
	
	public JustAValue {
		Objects.requireNonNull(name, "name");
	}
	
}

