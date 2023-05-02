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

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.addons.SCAddon;
import de.hechler.patrick.games.squareconqerer.addons.TheGameAddon;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;
import de.hechler.patrick.games.squareconqerer.stuff.IntMap;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

/**
 * this building type can be used to store resources
 * 
 * @author Patrick Hechler
 */
public final class Storage extends BuildingImpl {
	
	/**
	 * the number used when {@link TheGameAddon} sends instances of this class
	 */
	public static final int    NUMBER = 0x5A1C58D0;
	/**
	 * the name of this building type
	 */
	public static final String NAME   = "Storage";
	
	private static final int ORIDINAL_BASE_VALUE = 1;
	private static int       oridinal;
	
	/**
	 * the maximum amount of lives for {@link Storage} instances
	 */
	public static final int MAX_LIVES  = 5;
	/**
	 * a storage can not see anything itself
	 */
	public static final int VIEW_RANGE = 0;
	
	private final IntMap<OreResourceType>        ores       = IntMap.create(OreResourceType.class);
	private final IntMap<ProducableResourceType> producable = IntMap.create(ProducableResourceType.class);
	
	/**
	 * creates a new storage with the given values
	 * 
	 * @param x the x coordinate of the storage
	 * @param y the y coordinate of the storage
	 * @param usr the owner of the storage
	 */
	public Storage(int x, int y, User usr) {
		super(x, y, usr, MAX_LIVES, neededRes());
	}
	
	/**
	 * creates a new storage with the given values
	 * 
	 * @param x the x coordinate of the storage
	 * @param y the y coordinate of the storage
	 * @param usr the owner of the storage
	 * @param lives the current lives of the storage
	 * @param neededBuildResources the remaining needed resources to build this storage
	 * @param remainBuildTurns the remaining build turns needed to build this storage
	 * @param ores the stored ore resources
	 * @param producable the stored producable resources
	 */
	public Storage(int x, int y, User usr, int lives, IntMap<ProducableResourceType> neededBuildResources, int remainBuildTurns, IntMap<OreResourceType> ores,
		IntMap<ProducableResourceType> producable) {
		super(x, y, usr, 5, lives, neededBuildResources, remainBuildTurns);
		this.ores.setAll(ores);
		this.producable.setAll(producable);
	}
	
	private static IntMap<ProducableResourceType> neededRes() {
		IntMap<ProducableResourceType> res = IntMap.create(ProducableResourceType.class);
		res.set(ProducableResourceType.WOOD, 6);
		res.set(ProducableResourceType.STONE, 3);
		return res;
	}
	
	@Override
	protected void finishedBuildStore(Unit u, Resource res, int amount) throws TurnExecutionException {
		switch (res) {
		case @SuppressWarnings("preview") ProducableResourceType prt -> this.producable.addBy(prt, amount);
		case @SuppressWarnings("preview") OreResourceType prt -> this.ores.addBy(prt, amount);
		default -> throw new AssertionError("unknown resource type: " + res.getClass());
		}
		u.uncarry(res, amount);
	}
	
	@Override
	public void giveRes(Unit u, Resource res, int amount) throws TurnExecutionException {
		if (amount <= 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (!isFinishedBuild()) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (res instanceof ProducableResourceType prt) {
			if (this.producable.subBy(prt, amount) < 0) {
				this.producable.addBy(prt, amount);
				throw new TurnExecutionException(ErrorType.INVALID_TURN);
			}
		} else if (res instanceof OreResourceType ort) {
			if (this.ores.subBy(ort, amount) < 0) {
				this.ores.addBy(ort, amount);
				throw new TurnExecutionException(ErrorType.INVALID_TURN);
			}
		} else {
			throw new AssertionError("unknown resource type: " + res.getClass());
		}
		u.carry(res, amount);
	}
	
	@Override
	public Storage copy() {
		return new Storage(super.x, super.y, owner(), lives(), neededResources(), remainingBuildTurns(), this.ores, this.producable);
	}
	
	public IntMap<ProducableResourceType> producable() {
		IntMap<ProducableResourceType> res = IntMap.create(ProducableResourceType.class);
		res.setAll(this.producable);
		return res;
	}
	
	public IntMap<OreResourceType> ores() {
		IntMap<OreResourceType> res = IntMap.create(OreResourceType.class);
		res.setAll(this.ores);
		return res;
	}
	
	@Override
	public int ordinal() {
		if (oridinal == 0) {
			oridinal = ORIDINAL_BASE_VALUE + SCAddon.theGame().oridinalOffsetBuilding();
		}
		return oridinal;
	}
	
}
