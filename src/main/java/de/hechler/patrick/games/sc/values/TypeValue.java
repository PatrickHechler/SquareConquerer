package de.hechler.patrick.games.sc.values;

import java.util.Objects;

import de.hechler.patrick.games.sc.addons.addable.AddableType;

public record TypeValue<T extends AddableType<T, ?>>(String name, T value) implements Value {
	
	public TypeValue {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(value, "value");
	}
	
}

