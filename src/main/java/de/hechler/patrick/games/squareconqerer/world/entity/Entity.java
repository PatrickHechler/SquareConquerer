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

import java.util.Collections;
import java.util.List;

import de.hechler.patrick.games.squareconqerer.User;

public sealed interface Entity permits Unit, Building, EntityImpl {
	
	int x();
	
	int y();
	
	User owner();
	
	int lives();
	
	int maxLives();
	
	int viewRange();
	
	Entity copy();
	
	/**
	 * returns an empty list
	 * <p>
	 * the returned list is sorted with the {@link Unit#compareTo(Unit)}
	 * 
	 * @return an empty list
	 */
	default List<Unit> units() {
		return Collections.emptyList();
	}
	
}
