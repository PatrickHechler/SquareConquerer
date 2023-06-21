package de.hechler.patrick.games.sc.values.spec;

import java.util.Objects;

import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.values.UserValue;

public record UserSpec(String name) implements ValueSpec {
	
	public UserSpec {
		Objects.requireNonNull(name, "name");
	}
	
	public UserValue withValue(User val) {
		return new UserValue(this.name, val);
	}
	
}

