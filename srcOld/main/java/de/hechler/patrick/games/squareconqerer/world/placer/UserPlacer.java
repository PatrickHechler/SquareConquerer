// This file is part of the Square Conquerer Project
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// Copyright (C) 2023 Patrick Hechler
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.world.placer;


import java.io.IOException;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.stuff.ACORNRandom;
import de.hechler.patrick.games.squareconqerer.world.World;

/**
 * the user placer is used at the start of a game to place some initial entities for all users
 * 
 * @author Patrick Hechler
 */
public interface UserPlacer {
	
	/**
	 * Initialize the given world with the users and random
	 * <p>
	 * the users array is already in randomly reordered
	 * 
	 * @param world the world to initialize
	 * @param usrs  all users to be placed placed in a random order
	 * @param rnd   the worlds random object
	 */
	void initilize(World world, User[] usrs, ACORNRandom rnd);
	
	/**
	 * sends this {@link UserPlacer} over the given connection
	 * <p>
	 * this method is not allowed to use a read method from the connection
	 * 
	 * @param conn the connection
	 * 
	 * @throws IOException if an IO error occurs
	 */
	void writePlacer(Connection conn) throws IOException;
	
	/*
	 * note that also the static readPlacer method needs to be implemented signature:
	 * public static UserPlacer readPlacer(Connection) throws IOException
	 * the readPlacer is the reverse method of the writePlacer method
	 */
	
}
