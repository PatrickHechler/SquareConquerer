package de.hechler.patrick.games.squareconqerer.world.stuff;

import java.util.Random;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.World;

public interface UserPlacer {
	
	void initilize(World world, User[] usrs, Random rnd);
	
}
