package de.hechler.patrick.games.sc.values.spec;

import java.util.Objects;

import de.hechler.patrick.games.sc.values.IntValue;

public record IntSpec(String name, long min, long max) implements ValueSpec {
	
	public IntSpec {
		Objects.requireNonNull(name, "name");
	}
	
	public IntValue withValue(int val) {
		if (val < this.min || val > this.max) {
			throw new IllegalArgumentException("val=" + val + " min=" + this.min + " max=" + this.max);
		}
		return new IntValue(this.name, val);
	}
	
}

