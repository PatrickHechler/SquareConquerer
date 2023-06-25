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
import de.hechler.patrick.games.sc.error.ErrorType;
import de.hechler.patrick.games.sc.error.TurnExecutionException;
import de.hechler.patrick.games.sc.values.IntValue;
import de.hechler.patrick.games.sc.values.Value;
import de.hechler.patrick.games.sc.values.WorldThingValue;
import de.hechler.patrick.games.sc.values.spec.IntSpec;
import de.hechler.patrick.games.sc.world.resource.Resource;
import de.hechler.patrick.games.sc.world.tile.Tile;

public abstract non-sealed class Unit extends Entity<UnitType, Unit> {
	
	public Unit(UUID uuid) {
		super(uuid);
	}
	
	public static final String WORK_EFFICIENCY     = "work:efficiency";
	public static final String WORK_EFFICIENCY_LOC = "work efficiency";
	public static final String MOVE_RANGE          = "move:range";
	public static final String MOVE_RANGE_LOC      = "move range";
	public static final String CARRY               = "carry";
	public static final String CARRY_LOC           = "carry resources";
	
	public int moveRange() {
		return intValue(MOVE_RANGE).value();
	}
	
	public void changePos(int x, int y, Tile newTile, Tile oldTile) throws TurnExecutionException {
		value(new IntValue(X, x));
		value(new IntValue(Y, y));
	}
	
	public Resource addResource(Resource add) {
		Map<String, Value> map    = new TreeMap<>(mapValue(CARRY).navigatableMap());
		String             tvname = add.type().name;
		Value              val    = map.get(tvname);
		if (val == null) {
			map.put(tvname, new WorldThingValue(tvname, add));
		} else {
			Resource old = ((Resource) ((WorldThingValue) val).value());
			old.add(add);
		}
		return null;
	}
	
	public int workEfficency() {
		return intValue(WORK_EFFICIENCY).value();
	}
	
	public void attack(Entity<?, ?> enemy) throws TurnExecutionException {
		int x    = x();
		int y    = y();
		int ex   = enemy.x();
		int ey   = enemy.y();
		int diff = Math.abs(x - ex) + Math.abs(y - ey);
		if (diff > 1) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		int    oldLives = lives();
		double l        = oldLives;
		double maxl     = ((IntSpec) type().values.get(LIVES)).max();
		double el       = enemy.lives();
		double emaxl    = ((IntSpec) enemy.type().values.get(LIVES)).max();
		double val      = (l * l * l) / maxl;
		val /= el * el * emaxl;
		int d = enemy.defend(this, (int) val);
		if (d < 0) {
			throw new AssertionError("defend result is negative");
		}
		int newLives = oldLives - d;
		value(new IntValue(LIVES, Math.max(0, newLives)));
	}
	
	/** {@inheritDoc} */
	@Override
	public int defend(Unit enemy, int attackStrength) {
		if (attackStrength < 0) throw new AssertionError();
		int    oldLives = lives();
		int    newLives = Math.max(0, oldLives - attackStrength);
		double l        = newLives + ((oldLives - newLives) / 2D);
		double maxl     = ((IntSpec) type().values.get(LIVES)).max();
		double el       = enemy.lives();
		double emaxl    = ((IntSpec) enemy.type().values.get(LIVES)).max();
		value(new IntValue(LIVES, newLives));
		double result = (l * l) / maxl;
		result /= el * emaxl; // by default only do a little defense
		return (int) result;
	}
	
}
