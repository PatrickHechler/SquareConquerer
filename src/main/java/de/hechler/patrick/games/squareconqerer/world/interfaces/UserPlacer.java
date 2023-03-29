package de.hechler.patrick.games.squareconqerer.world.interfaces;

import java.util.function.Function;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.UserWorld;
import de.hechler.patrick.games.squareconqerer.world.World;

public interface UserPlacer extends Function<User, UserWorld> {
	
	/**
	 * creates a new {@link UserWorld} for the given {@link User}
	 * 
	 * @param the {@link User} which just joined the {@link World}
	 */
	@Override
	UserWorld apply(User usr);
	
}
