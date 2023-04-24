package de.hechler.patrick.games.squareconqerer.world.stuff;


import java.io.IOException;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.objects.Random2;
import de.hechler.patrick.games.squareconqerer.world.World;

public interface UserPlacer {
	
	void initilize(World world, User[] usrs, Random2 rnd);
	
	void writePlacer(Connection conn) throws IOException;
	
	/*
	 * note that also the static readPlacer method needs to be implemented
	 * signature: public static UserPlacer readPlacer(Connection) throws IOException
	 */
	
}
