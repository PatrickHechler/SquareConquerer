package de.hechler.patrick.games.sc.values.spec;

import java.util.Objects;
import java.util.function.Consumer;

import de.hechler.patrick.games.sc.values.WorldThingValue;
import de.hechler.patrick.games.sc.world.WorldThing;

public record WorldThingSpec(String name, Consumer<WorldThing<?, ?>> validator) implements ValueSpec {
	
	public WorldThingSpec {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(validator, "validator");
	}
	
	public WorldThingValue withValue(WorldThing<?, ?> val) {
		this.validator.accept(val);
		return new WorldThingValue(this.name, val);
	}
	
}

