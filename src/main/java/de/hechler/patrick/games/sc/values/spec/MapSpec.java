package de.hechler.patrick.games.sc.values.spec;

import java.util.Map;
import java.util.Objects;

import de.hechler.patrick.games.sc.values.MapValue;
import de.hechler.patrick.games.sc.values.Value;

public record MapSpec(String name) implements ValueSpec {
	
	public MapSpec {
		Objects.requireNonNull(name, "name");
	}
	
	public <K extends Value, V extends Value> MapValue<K, V> withValue(Map<K,V> value) {
		return new MapValue<>(this.name, value);
	}
	
}

