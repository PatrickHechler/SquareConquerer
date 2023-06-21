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
package de.hechler.patrick.games.sc.turn;

import de.hechler.patrick.games.sc.world.entity.Unit;
import de.hechler.patrick.games.sc.world.resource.Resource;

/**
 * this {@link EntityTurn} class is used to let an unit store resources in a building
 * 
 * @author Patrick Hechler
 * @param entity   the unit which should make the store operation
 * @param resource the resource which should be stored
 * @param amount   how much should be stored
 */
public record StoreTurn(Unit entity, Resource resource, int amount) implements EntityTurn {
	
}
