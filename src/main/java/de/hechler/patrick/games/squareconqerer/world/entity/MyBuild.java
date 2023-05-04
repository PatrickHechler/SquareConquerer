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
package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.stuff.IntMap;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;

/**
 * this class provides an abstract implementation of the {@link Building} interface for extern use
 * 
 * @author Patrick Hechler
 */
public abstract non-sealed class MyBuild extends BuildingImpl {
	
	/**
	 * create a new building with the given values
	 * 
	 * @param x               the {@link #x()} coordinate
	 * @param y               the {@link #y()} coordinate
	 * @param usr             the {@link #owner()}
	 * @param maxlives        the {@link #maxLives()} and {@link #lives()}
	 * @param neededResources the {@link #neededResources()}
	 */
	public MyBuild(int x, int y, User usr, int maxlives, IntMap<ProducableResourceType> neededResources) {
		super(x, y, usr, maxlives, neededResources);
	}
	
	/**
	 * create a new building with the given values
	 * 
	 * @param x                   the {@link #x()} coordinate
	 * @param y                   the {@link #y()} coordinate
	 * @param usr                 the {@link #owner()}
	 * @param maxlives            the {@link #maxLives()}
	 * @param lives               the {@link #lives()}
	 * @param neededResources     the {@link #neededResources()}
	 * @param remainingBuildTurns the {@link #remainingBuildTurns()}
	 */
	public MyBuild(int x, int y, User usr, int maxlives, int lives, IntMap<ProducableResourceType> neededResources, int remainingBuildTurns) {
		super(x, y, usr, maxlives, lives, neededResources, remainingBuildTurns);
	}
	
}
