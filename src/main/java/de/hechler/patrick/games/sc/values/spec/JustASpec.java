package de.hechler.patrick.games.sc.values.spec;

import java.util.Objects;

import de.hechler.patrick.games.sc.values.JustAValue;

public record JustASpec(String name) implements ValueSpec {
	
	public JustASpec {
		Objects.requireNonNull(name, "name");
	}
	
	public JustAValue asValue() {
		return new JustAValue(this.name);
	}
	
}

