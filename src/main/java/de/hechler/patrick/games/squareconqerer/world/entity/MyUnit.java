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
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

/**
 * this class provides an abstract implementation of the {@link Building} interface for extern use
 * 
 * @author Patrick Hechler
 */
public abstract non-sealed class MyUnit extends UnitImpl {
	
	/**
	 * create a new unit with the given values
	 * 
	 * @param x              the {@link #x()} coordinate
	 * @param y              the {@link #y()} coordinate
	 * @param usr            the {@link #owner()}
	 * @param maxlives       the {@link #maxLives()} and {@link #lives()}
	 * @param viewRange      the {@link #viewRange()}
	 * @param carryMaxAmount the {@link #carryMaxAmount()}
	 */
	public MyUnit(int x, int y, User usr, int maxlives, int viewRange, int carryMaxAmount) {
		super(x, y, usr, maxlives, viewRange, carryMaxAmount);
	}
	
	/**
	 * create a new unit with the given values
	 * 
	 * @param x              the {@link #x()} coordinate
	 * @param y              the {@link #y()} coordinate
	 * @param usr            the {@link #owner()}
	 * @param maxlives       the {@link #maxLives()}
	 * @param lives          the {@link #lives()}
	 * @param viewRange      the {@link #viewRange()}
	 * @param carryMaxAmount the {@link #carryMaxAmount()}
	 * @param carryAmount    the {@link #carryAmount()}
	 * @param res            the {@link #carryRes()}
	 */
	public MyUnit(int x, int y, User usr, int maxlives, int lives, int viewRange, int carryMaxAmount, int carryAmount, Resource res) {
		super(x, y, usr, maxlives, lives, viewRange, carryMaxAmount, carryAmount, res);
	}
	
}
