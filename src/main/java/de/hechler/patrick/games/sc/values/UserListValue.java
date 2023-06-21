package de.hechler.patrick.games.sc.values;

import java.util.List;
import java.util.Objects;

import de.hechler.patrick.games.sc.ui.players.User;

public record UserListValue(String name, List<User> value) implements Value {
	
	public UserListValue(String name, List<User> value) {
		this.name  = Objects.requireNonNull(name, "name");
		this.value = List.copyOf(value);
	}
	
}

