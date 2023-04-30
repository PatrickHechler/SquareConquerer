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

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.addons.SquareConquererAddon;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;

public non-sealed interface Unit extends Entity, Comparable<Unit> {
	
	/**
	 * this value holds the amount of different unit types plus {@code 1} (for
	 * <code>null</code>/none)
	 */
	static final int COUNT = Intern.calcCount();
	
	static final int COUNT_NO_NULL = COUNT - 1;
	
	static class Intern {
		
		private Intern() {}
		
		private static int calcCount() {
			int         cnt   = 1;
			Set<String> names = new HashSet<>();
			for (SquareConquererAddon addon : SquareConquererAddon.addons()) {
				Map<Class<? extends Entity>, String> cls = addon.entities().entityClassses();
				for (Entry<Class<? extends Entity>, String> entry : cls.entrySet()) {
					if (Unit.class.isAssignableFrom(entry.getClass())) continue;
					if (names.add(entry.getValue())) cnt++;
				}
				names.clear();
			}
			return cnt;
		}
		
	}
	
	void changePos(int newx, int newy, Tile checkcanEnter) throws TurnExecutionException;
	
	Resource carryRes();
	
	int carryAmount();
	
	int carryMaxAmount();
	
	void carry(Resource res, int amount) throws TurnExecutionException;
	
	void uncarry(Resource res, int amount) throws TurnExecutionException;
	
	int moveRange();
	
	@Override
	Unit copy();
	
	static int ordinal(Unit u) {
		return u == null ? 0 : u.ordinal();
	}
	
	/**
	 * compares this Unit with the given other unit
	 * <p>
	 * {@code 0} is only allowed to be returned if the unit has on all
	 * fields an equal value to the fields of this unit
	 * <ol>
	 * <li>the {@link #owner() owners} {@link User#name() name}</li>
	 * <li>the units {@link Object#getClass() class} {@link Class#getName()
	 * name}</li>
	 * <li>the {@link #lives() health}</li>
	 * <li>the {@link #moveRange() move range}</li>
	 * <li>the {@link #maxLives() maximum health}</li>
	 * <li>their {@link #units()}
	 * <ol>
	 * <li>the {@link List#size()}</li>
	 * <li>the lists content starting from {@code 0}</li>
	 * </ol>
	 * </li>
	 * <li>the {@link Object#getClass() class} {@link Class#getName() name}</li>
	 * <li>a unit type specific compare</li>
	 * </ol>
	 * <p>
	 * <b>implNote</b> subclasses only have to implement the unit specific
	 * compare.<br>
	 * They should do something like:
	 * <code>int c = super.compareTo(0); if (c == 0) c = specificCompare((SpecificUnitType) o); return c;</code><br>
	 * also note that the non unit type specific compare also compares the
	 * {@link Object#getClass() class} {@link Class#getName() names}, so
	 * the other unit has the same type if the specific compare is needed
	 */
	@Override
	default int compareTo(Unit o) {
		User u  = owner();
		User ou = o.owner();
		if (u != ou) {
			int cmp = u.name().compareTo(ou.name());
			if (cmp == 0) throw new AssertionError("different users with same name");
			return cmp;
		}
		int l  = lives();
		int ol = o.lives();
		if (l != ol) return Integer.compare(l, ol);
		l  = moveRange();
		ol = o.moveRange();
		if (l != ol) return Integer.compare(l, ol);
		l  = maxLives();
		ol = o.maxLives();
		if (l != ol) return Integer.compare(l, ol);
		List<Unit> us  = units();
		List<Unit> ous = o.units();
		if (us.size() != ous.size()) return Integer.compare(us.size(), ous.size());
		if (!us.isEmpty()) {
			Iterator<Unit> iter  = us.iterator();
			Iterator<Unit> oiter = ous.iterator();
			while (iter.hasNext()) {
				Unit n  = iter.next();
				Unit on = oiter.next();
				int  c  = n.compareTo(on);
				if (c != 0) return c;
			}
			if (oiter.hasNext()) throw new ConcurrentModificationException();
		}
		return getClass().getName().compareTo(o.getClass().getName());
	}
	
}
