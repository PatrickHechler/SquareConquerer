package de.hechler.patrick.games.sc.values.spec;

import java.util.List;
import java.util.Objects;

import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.values.UserListValue;

public record UserListSpec(String name, int minSize, int maxSize) implements ValueSpec {
	
	public UserListSpec {
		Objects.requireNonNull(name, "name");
		if (minSize > maxSize || minSize < 0) {
			throw new IllegalArgumentException("min=" + minSize + " max=" + maxSize);
		}
	}
	
	public UserListValue withValue(List<User> val) {
		int s = val.size();
		if (s < this.minSize || s > this.maxSize) {
			throw new IllegalArgumentException("min=" + this.minSize + " max=" + this.maxSize + " size=" + s);
		}
		return new UserListValue(this.name, val);
	}
	
	public UserListValue withValue(User... val) {
		int s = val.length;
		if (s < this.minSize || s > this.maxSize) {
			throw new IllegalArgumentException("min=" + this.minSize + " max=" + this.maxSize + " size=" + s);
		}
		return new UserListValue(this.name, List.of(val));
	}
	
}

