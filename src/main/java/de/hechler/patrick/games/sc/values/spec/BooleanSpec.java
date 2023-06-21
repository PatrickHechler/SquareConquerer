package de.hechler.patrick.games.sc.values.spec;

import java.util.Objects;

import de.hechler.patrick.games.sc.values.BooleanValue;

public record BooleanSpec(String name) implements ValueSpec {
	
	public BooleanSpec {
		Objects.requireNonNull(name, "name");
	}
	
	public BooleanValue withValue(boolean val) {
		return new BooleanValue(this.name, val);
	}
	
}

