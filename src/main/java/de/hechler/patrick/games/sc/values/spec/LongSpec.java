package de.hechler.patrick.games.sc.values.spec;

import java.util.Objects;

import de.hechler.patrick.games.sc.values.LongValue;

public record LongSpec(String name, long min, long max) implements ValueSpec {
	
	public LongSpec {
		Objects.requireNonNull(name, "name");
	}
	
	public LongValue withValue(long val) {
		if (val < this.min || val > this.max) {
			throw new IllegalArgumentException("val=" + val + " min=" + this.min + " max=" + this.max);
		}
		return new LongValue(this.name, val);
	}
	
}

