package de.hechler.patrick.games.sc.values.spec;

import java.util.Objects;

import de.hechler.patrick.games.sc.values.DoubleValue;

public record DoubleSpec(String name, double min, double max) implements ValueSpec {
	
	public DoubleSpec {
		Objects.requireNonNull(name, "name");
	}
	
	public DoubleValue withValue(double val) {
		if (val < this.min || val > this.max) {
			throw new IllegalArgumentException("val=" + val + " min=" + this.min + " max=" + this.max);
		}
		return new DoubleValue(this.name, val);
	}
	
}

