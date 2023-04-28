//This file is part of the Square Conquerer Project
//DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//Copyright (C) 2023  Patrick Hechler
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published
//by the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with this program.  If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.world.stuff;


import java.io.IOException;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.connect.Connection;
import de.hechler.patrick.games.squareconqerer.objects.Random2;
import de.hechler.patrick.games.squareconqerer.world.World;

/**
 * the user placer is used at the start of a game to place some initial entities for all users
 * 
 * @author Patrick Hechler
 */
public interface UserPlacer {
	
	/**
	 * Initialize the given world
	 * 
	 * @param world
	 * @param usrs
	 * @param rnd
	 */
	void initilize(World world, User[] usrs, Random2 rnd);
	
	void writePlacer(Connection conn) throws IOException;
	
	/*
	 * note that also the static readPlacer method needs to be implemented signature: public static UserPlacer readPlacer(Connection) throws IOException
	 */
	
}
