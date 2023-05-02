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
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;
import de.hechler.patrick.games.squareconqerer.stuff.IntMap;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public final class Storage extends BuildingImpl {
	
	public static final String NAME   = "Storage";
	public static final int    NUMBER = 0x5A1C58D0;
	
	private static final int ORIDINAL_BASE_VALUE = 1;
	private static int       oridinal;
	
	public static final int MAX_LIVES  = 5;
	public static final int VIEW_RANGE = 0;
	
	private final IntMap<OreResourceType>        ores       = IntMap.createIntIntMap(OreResourceType.class);
	private final IntMap<ProducableResourceType> producable = IntMap.createEnumIntMap(ProducableResourceType.class);
	
	public Storage(int x, int y, User usr) {
		super(x, y, usr, MAX_LIVES, neededRes());
	}
	
	public Storage(int x, int y, User usr, int lives, IntMap<ProducableResourceType> neededBuildResources, int remainBuildTurns, IntMap<OreResourceType> ores,
			IntMap<ProducableResourceType> producable) {
		super(x, y, usr, 5, lives, neededBuildResources, remainBuildTurns);
		this.ores.setAll(ores);
		this.producable.setAll(producable);
	}
	
	private static IntMap<ProducableResourceType> neededRes() {
		IntMap<ProducableResourceType> res = IntMap.createIntIntMap(ProducableResourceType.class);
		res.set(ProducableResourceType.WOOD, 6);
		res.set(ProducableResourceType.STONE, 3);
		return res;
	}
	
	@Override
	protected void finishedBuildStore(Unit u, Resource res, int amount) throws TurnExecutionException {
		if (res instanceof ProducableResourceType prt) {
			this.producable.addBy(prt, amount);
		} else if (res instanceof OreResourceType ort) {
			this.ores.addBy(ort.ordinal(), amount);
		} else {
			throw new AssertionError("unknown resource type: " + res.getClass());
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
			if (this.ores.subBy(ort.ordinal(), amount) < 0) {
				this.ores.addBy(ort.ordinal(), amount);
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
		IntMap<ProducableResourceType> res = IntMap.createEnumIntMap(ProducableResourceType.class);
		res.setAll(this.producable);
		return res;
	}
	
	public IntMap<OreResourceType> ores() {
		IntMap<OreResourceType> res = IntMap.createIntIntMap(OreResourceType.class);
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
