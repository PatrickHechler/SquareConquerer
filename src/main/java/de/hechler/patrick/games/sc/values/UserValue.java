package de.hechler.patrick.games.sc.values;

import java.util.Objects;
import java.util.Optional;

import de.hechler.patrick.games.sc.ui.players.User;

public record UserValue(String name, User value) implements Value {
	
	public UserValue {
		Objects.requireNonNull(name, "name");
	}
	
	public Optional<User> asOptional() {
		return Optional.ofNullable(this.value);
	}
	
	public boolean isEmpty() {
		return this.value == null;
	}
	
	public boolean hasValue() {
		return this.value != null;
	}
	
}

