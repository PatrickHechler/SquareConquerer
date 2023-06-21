package de.hechler.patrick.games.sc.values;

import java.util.Objects;
import java.util.Optional;

import de.hechler.patrick.games.sc.world.WorldThing;

public record WorldThingValue(String name, WorldThing<?, ?> value) implements Value {
	
	public WorldThingValue {
		Objects.requireNonNull(name, "name");
	}
	
	public Optional<WorldThing<?, ?>> asOptional() {
		return Optional.ofNullable(this.value);
	}
	
	public boolean isEmpty() {
		return this.value == null;
	}
	
	public boolean hasValue() {
		return this.value != null;
	}
	
}

