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

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import de.hechler.patrick.games.sc.addons.addable.UnitType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.values.IntValue;
import de.hechler.patrick.games.sc.values.TypeValue;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.WorldThingValue;
import de.hechler.patrick.games.sc.world.resource.Resource;
import de.hechler.patrick.games.sc.world.tile.Tile;

public abstract non-sealed class Unit extends Entity<UnitType, Unit> {
	
	public Unit(UUID uuid) {
		super(uuid);
	}
	
	public static final String MOVE_RANGE = "move:range";
	public static final String CARRY      = "carry";
	
	public int moveRange() {
		return intValue(MOVE_RANGE).value();
	}
	
	public void changePos(int x, int y, Tile newTile, Tile oldTile) throws TurnExecutionException {
		value(new IntValue(X, x));
		value(new IntValue(Y, y));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addResource(Resource add) {
		Map<Value, Value> map = new TreeMap<>(mapValue(CARRY).navigatableMap());
		TypeValue         tv  = new TypeValue(add.type().name, add.type());
		Value             val = map.get(tv);
		if (val == null) {
			map.put(tv, new WorldThingValue(tv.name(), add));
		} else {
			Resource old = ((Resource) ((WorldThingValue) val).value());
			old.add(add);
		}
	}
	
}
