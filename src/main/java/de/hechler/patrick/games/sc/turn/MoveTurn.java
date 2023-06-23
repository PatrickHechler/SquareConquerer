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
package de.hechler.patrick.games.sc.turn;

import java.util.List;

import de.hechler.patrick.games.sc.world.entity.Unit;

/**
 * this {@link EntityTurn} is used to let an unit move in the world
 * 
 * @author Patrick Hechler
 * 
 * @param entity the unit which should be moved
 * @param acts   the directions in which the entity should be moved
 */
public record MoveTurn(Unit entity, List<MoveAct> acts) implements EntityTurn {
	
	/**
	 * create a new {@link MoveTurn} for the given unit with the direction list
	 * 
	 * @param entity the unit to be moved
	 * @param acts   the directions to which the unit should move
	 */
	public MoveTurn(Unit entity, List<MoveAct> acts) {
		this.entity = entity;
		this.acts   = List.copyOf(acts);
	}
	
	/**
	 * create a new {@link MoveTurn} for the given unit with the direction array
	 * 
	 * @param entity the unit to be moved
	 * @param dirs   the directions to which the unit should move
	 */
	public MoveTurn(Unit entity, MoveAct... dirs) {
		this(entity, List.of(dirs));
	}
	
}
