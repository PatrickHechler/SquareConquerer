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
package de.hechler.patrick.games.squareconqerer.world.entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.hechler.patrick.games.squareconqerer.addons.SCAddon;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.stuff.IntMap;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

/**
 * buildings are static entities which can not change their position and need to be built
 * 
 * @author Patrick Hechler
 */
public non-sealed interface Building extends Entity {
	
	/**
	 * this value holds the amount of different building types plus {@code 1} (for <code>null</code>/none)
	 */
	static final int COUNT = Intern.calcCount();
	
	/**
	 * this value holds the amount of different building types without <code>null</code>
	 */
	static final int COUNT_NO_NULL = COUNT - 1;
	
	/**
	 * this class is for intern stuff
	 * 
	 * @author Patrick Hechler
	 */
	static class Intern {
		
		private Intern() {}
		
		private static int calcCount() {
			int         cnt   = 1;
			Set<String> names = new HashSet<>();
			for (SCAddon addon : SCAddon.addons()) {
				Map<Class<? extends Entity>, String> cls = addon.entities().entityClassses();
				for (Entry<Class<? extends Entity>, String> entry : cls.entrySet()) {
					if (Building.class.isAssignableFrom(entry.getClass())) continue;
					if (names.add(entry.getValue())) cnt++;
				}
				names.clear();
			}
			return cnt;
		}
		
	}
	
	/**
	 * returns <code>true</code> if this building is completely builded
	 * 
	 * @return <code>true</code> if this building is completely builded
	 */
	boolean isFinishedBuild();
	
	/**
	 * returns the estimated remaining turns needed to build this building<br>
	 * returns <code>0</code> if {@link #isFinishedBuild()} returns <code>true</code>
	 * 
	 * @return the estimated remaining turns needed to build this building<br>
	 */
	int remainingBuildTurns();
	
	/**
	 * stores the given amount of the given resource type from the unit
	 * 
	 * @param u      the unit which carries the resources to store
	 * @param res    the resource type
	 * @param amount the amount to be stored
	 * 
	 * @throws TurnExecutionException if the operation fails
	 */
	void store(Unit u, Resource res, int amount) throws TurnExecutionException;
	
	/**
	 * returns an {@link IntMap} containing the resources needed to build this building or <code>null</code> if no more resources are needed to be
	 * {@link #store(Unit, Resource, int)} in order to build this building<br>
	 * returns <code>null</code> if {@link #isFinishedBuild()} returns <code>true</code>
	 * 
	 * @return an {@link IntMap} containing the resources needed to build this building or <code>null</code> if no more resources are needed to be
	 *         {@link #store(Unit, Resource, int)} in order to build this building
	 */
	IntMap<ProducableResourceType> neededResources();
	
	/**
	 * gives the unit the given amount from the given resource type
	 * 
	 * @param u      the unit which will carry the given resources
	 * @param res    the resource type
	 * @param amount the amount to be given
	 * 
	 * @throws TurnExecutionException if the operation fails
	 */
	void giveRes(Unit u, Resource res, int amount) throws TurnExecutionException;
	
	/**
	 * lets the given unit build this building
	 * <p>
	 * the building may disallow some units to build ad let some other units build faster
	 * 
	 * @param u the unit which should build
	 * 
	 * @throws TurnExecutionException if the turn could not be executed
	 */
	void build(Unit u) throws TurnExecutionException;
	
	/**
	 * returns <code>true</code> if the given unit could currently make a {@link #build(Unit) build} turn on this building
	 * 
	 * @param u the unit which (probably) wants to build this building
	 * @return <code>true</code> if the given unit could currently make a {@link #build(Unit) build} turn on this building
	 */
	boolean canBuild(Unit u);
	
	/** {@inheritDoc} */
	@Override
	Building copy();
	
	/**
	 * returns the global ordinal of the building
	 * 
	 * @param b the building
	 * @return the ordinal value of the building
	 */
	static int ordinal(Building b) {
		return b == null ? 0 : b.ordinal();
	}
	
	/**
	 * returns the addon local ordinal of the building plus <code>1</code><br>
	 * if <code>b</code> is <code>null</code> this method returns <code>0</code>
	 * 
	 * @param b the building
	 * @return the ordinal value of the building
	 */
	static int addonLocalOrdinal(Building b) {
		return b == null ? 0 : 1 + b.addonLocalOrdinal();
	}
	
}
