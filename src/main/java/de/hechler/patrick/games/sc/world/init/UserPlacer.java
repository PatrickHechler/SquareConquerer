package de.hechler.patrick.games.sc.world.init;

import java.io.IOException;

import de.hechler.patrick.games.sc.connect.Connection;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.utils.objects.Random2;

public interface UserPlacer {
	
	void initilize(World w, User[] users, Random2 rnd) throws TurnExecutionException;

	void writePlacer(Connection conn) throws IOException;
	
	// static UserPlacer readPlacer(Connection conn) throws IOException;
	
}
