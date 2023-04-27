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
package de.hechler.patrick.games.squareconqerer.world;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.User.RootUser;
import de.hechler.patrick.games.squareconqerer.world.entity.Entity;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.turn.Turn;

/**
 * this interface defines what a Square Conquerer World has to be able to do
 * 
 * @author Patrick Hechler
 */
public sealed interface World permits RootWorld, RootWorld.Builder, RemoteWorld, UserWorld {
	
	/**
	 * returns the {@link User} of the world
	 * 
	 * @return the {@link User} of the world
	 */
	User user();
	
	/**
	 * returns the x-len (width) of the world
	 * 
	 * @return the x-len (width) of the world
	 */
	int xlen();
	
	/**
	 * returns the y-len (height) of the world
	 * 
	 * @return the y-len (height) of the world
	 */
	int ylen();
	
	/**
	 * returns the tile at the given position
	 * <p>
	 * if the world supports changes:<br>
	 * until the next turn/(next) game start changes to the world are visible to the tile and changes to the tile are also made in the world<br>
	 * after the next turn/(next) game start the behavior is undefined if the tile is still used<br>
	 * if the world does not supports changes:<br>
	 * changes to the returned tile lead to unspecified behavior
	 * 
	 * @param x the x coordinate of the tile
	 * @param y the y coordinate of the tile
	 * @return the tile at the given position
	 */
	Tile tile(int x, int y);
	
	/**
	 * adds a new next turn listener. every time the world changes all next turn listeners are executed
	 * <p>
	 * the listener:
	 * <ol>
	 * <li>the first argument will be the root world hash value</li>
	 * <li>the second argument will be the hash value of all turns</li>
	 * </ol>
	 * note that both arguments are allowed to be <code>null</code><br>
	 * when the game starts the turn hash will be <code>null</code> and the world hash will have a non <code>null</code> value<br>
	 * note that no listener is allowed to modify the passed values
	 * 
	 * @param listener the next turn listener to add
	 */
	void addNextTurnListener(BiConsumer<byte[], byte[]> listener);
	
	/**
	 * removes the given next turn listener from this world<br>
	 * if the listener was added multiple times, only the first one will be removed
	 * 
	 * @param listener the listener to be removed
	 */
	void removeNextTurnListener(BiConsumer<byte[], byte[]> listener);
	
	/**
	 * returns a map containing all entities visible to this world with their {@link Entity#owner() owners} as keys
	 * 
	 * @return a map containing all entities visible to this world with their {@link Entity#owner() owners} as keys
	 */
	Map<User, List<Entity>> entities();
	
	/**
	 * finish the given turn
	 * <p>
	 * if the turns {@link Turn#usr user} is not the {@link #user() user} of this world, the world is allowed to throw an {@link IllegalStateException}<br>
	 * if the turns {@link Turn#usr user} is a {@link RootUser root user}, the world is allowed to throw an {@link IllegalStateException}
	 * 
	 * @param t the turn to finish
	 * @throws IllegalStateException if the turn has a different user than this world and the world does not support finishing the turn of other users
	 */
	void finish(Turn t) throws IllegalStateException;
	
}
