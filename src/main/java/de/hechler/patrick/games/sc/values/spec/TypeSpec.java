package de.hechler.patrick.games.sc.values.spec;

import java.util.Objects;

import de.hechler.patrick.games.sc.addons.addable.AddableType;
import de.hechler.patrick.games.sc.values.TypeValue;

public record TypeSpec<T extends AddableType<T, ?>>(String name, Class<T> cls) implements ValueSpec {
	
	public TypeSpec {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(cls, "value");
		cls.asSubclass(AddableType.class);
	}
	
	public TypeValue<T> withValue(T value) {
		return new TypeValue<>(this.name, this.cls.cast(value));
	}
	
}

