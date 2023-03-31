package de.hechler.patrick.games.squareconqerer.world.interfaces;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.UserWorld;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.connect.Connection;

public interface UserPlacer {
	
	/**
	 * creates a new {@link UserWorld} for the given {@link User}
	 * 
	 * @param usr the {@link User} which just joined the {@link World}
	 * @param usrModCnt the modify count of the user ({@link Connection#modCnt()})
	 */
	UserWorld apply(User usr, int usrModCnt);
	
}
