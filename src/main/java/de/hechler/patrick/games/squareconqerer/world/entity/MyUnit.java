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

public abstract non-sealed class MyUnit extends UnitImpl {
	
	public MyUnit(int x, int y, User usr, int maxlives, int lives, int viewRange, int carryMaxAmount, int carryAmount, Resource res) {
		super(x, y, usr, maxlives, lives, viewRange, carryMaxAmount, carryAmount, res);
	}
	
	public MyUnit(int x, int y, User usr, int maxlives, int viewRange, int carryMaxAmount) {
		super(x, y, usr, maxlives, viewRange, carryMaxAmount);
	}
	
}
