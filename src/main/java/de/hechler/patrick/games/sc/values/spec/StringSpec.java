package de.hechler.patrick.games.sc.values.spec;

import java.util.Objects;

import de.hechler.patrick.games.sc.values.StringValue;

public record StringSpec(String name) implements ValueSpec {
	
	public StringSpec {
		Objects.requireNonNull(name, "name");
	}
	
	public StringValue withValue(String val) {
		return new StringValue(this.name, val);
	}
	
}

