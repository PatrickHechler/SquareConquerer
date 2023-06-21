package de.hechler.patrick.games.sc.values;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

public record MapValue<K extends Value, V extends Value>(String name, Map<K, V> value) implements Value {
	
	@SuppressWarnings("cast")
	public MapValue(String name, Map<K, V> value) {
		this.name  = Objects.requireNonNull(name, "name");
		this.value = Collections.unmodifiableNavigableMap(new TreeMap<>(value));
		this.value.forEach((k, v) -> {
			Objects.requireNonNull(k, "entry.key");
			Objects.requireNonNull(v, "entry.value");
			if (!(k instanceof Value) || !(v instanceof Value)) {
				throw new AssertionError("key or value are from an illegal type");
			}
		});
	}
	
	public NavigableMap<K, V> navigatableMap() {
		return (NavigableMap<K, V>) this.value;
	}
	
}

