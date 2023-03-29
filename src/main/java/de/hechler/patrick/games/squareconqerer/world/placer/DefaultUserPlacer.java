package de.hechler.patrick.games.squareconqerer.world.placer;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.UserWorld;
import de.hechler.patrick.games.squareconqerer.world.World;
import de.hechler.patrick.games.squareconqerer.world.interfaces.UserPlacer;


public class DefaultUserPlacer implements UserPlacer {
	
	private final World world;
	
	public DefaultUserPlacer(World world) {
		this.world = world;
	}
	
	@Override
	public UserWorld apply(User t, int usrModCnt) {
		return new UserWorld(world, t, usrModCnt);
	}

}
