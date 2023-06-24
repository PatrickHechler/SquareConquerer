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
package de.hechler.patrick.games.sc.world.entity;

import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.EntityType;
import de.hechler.patrick.games.sc.ui.players.User;
import de.hechler.patrick.games.sc.world.World;
import de.hechler.patrick.games.sc.world.WorldThing;
import de.hechler.patrick.games.sc.world.tile.Tile;

public abstract sealed class Entity<T extends EntityType<T, M>, M extends Entity<T, M>> extends WorldThing<T, M> permits Unit, Build {
	
	public Entity(UUID uuid) {
		super(uuid);
	}
	
	public static final String X              = "x";
	public static final String X_LOC          = "x coordinate";
	public static final String Y              = "y";
	public static final String Y_LOC          = "y coordinate";
	public static final String OWNER          = "owner";
	public static final String OWNER_LOC      = "owner";
	public static final String VIEW_RANGE     = "view:range";
	public static final String VIEW_RANGE_LOC = "view range";
	public static final String LIVES          = "lives";
	public static final String LIVES_LOC      = "lives";
	
	public int x() {
		return intValue(X).value();
	}
	
	public int y() {
		return intValue(Y).value();
	}
	
	public User owner() {
		return userValue(OWNER).value();
	}
	
	public int viewRange() {
		return intValue(VIEW_RANGE).value();
	}
	
	public int lives() {
		return intValue(LIVES).value();
	}
	
	public int neededView(World w, int x, int y, Tile target, Tile neigbour) {
		int   result = target.ground().viewBlock();
		Build b      = target.build();
		if (b != null) result += b.viewBlock();
		result += target.resourcesStream().mapToInt(WorldThing::viewBlock).sum();
		result += target.unitsStream().mapToInt(WorldThing::viewBlock).sum();
		return result;
	}
	
	public abstract int defend(Unit unit, int attackStrength);
	
}
