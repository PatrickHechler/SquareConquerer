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

import java.text.Format;

import de.hechler.patrick.games.squareconqerer.Messages;
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
	
	private static final Format UNKNOWN_RESOURCE_TYPE = Messages.getFormat("Storage.unknown-resource"); //$NON-NLS-1$
	private static final String LOCAL_NAME            = Messages.getString("Storage.local-name");       //$NON-NLS-1$
	
	/**
	 * the number used when {@link TheGameAddon} sends instances of this class
	 */
	public static final int    NUMBER = 0x5A1C58D0;
	/**
	 * the name of this building type
	 */
	public static final String NAME   = "Storage"; //$NON-NLS-1$
	
	private static final int LOCAL_ORIDINAL = 0;
	private static final int ORIDINAL       = SCAddon.addon(Storage.class).oridinalOffsetBuilding() + LOCAL_ORIDINAL;
	
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
	 * @param x   the x coordinate of the storage
	 * @param y   the y coordinate of the storage
	 * @param usr the owner of the storage
	 */
	public Storage(int x, int y, User usr) {
		super(x, y, usr, MAX_LIVES, neededRes());
	}
	
	/**
	 * creates a new storage with the given values
	 * 
	 * @param x                    the x coordinate of the storage
	 * @param y                    the y coordinate of the storage
	 * @param usr                  the owner of the storage
	 * @param lives                the current lives of the storage
	 * @param neededBuildResources the remaining needed resources to build this storage
	 * @param remainBuildTurns     the remaining build turns needed to build this storage
	 * @param ores                 the stored ore resources
	 * @param producable           the stored producable resources
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
	
	/** {@inheritDoc} */
	@Override
	protected void finishedBuildStore(Unit u, Resource res, int amount) throws TurnExecutionException {
		switch (res) {
		case @SuppressWarnings("preview") ProducableResourceType prt -> this.producable.addBy(prt, amount);
		case @SuppressWarnings("preview") OreResourceType prt -> this.ores.addBy(prt, amount);
		default -> throw new AssertionError(Messages.format(UNKNOWN_RESOURCE_TYPE, res.getClass()));
		}
		u.uncarry(res, amount);
	}
	
	/** {@inheritDoc} */
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
			throw new AssertionError(Messages.format(UNKNOWN_RESOURCE_TYPE, res.getClass()));
		}
		u.carry(res, amount);
	}
	
	/** {@inheritDoc} */
	@Override
	public Storage copy() {
		return new Storage(super.x, super.y, owner(), lives(), neededResources(), remainingBuildTurns(), this.ores, this.producable);
	}
	
	/**
	 * returns the current amount of stored producable resources
	 * 
	 * @return the current amount of stored producable resources
	 */
	public IntMap<ProducableResourceType> producable() {
		return this.producable.copy();
	}
	
	/**
	 * returns the current amount of stored ore resources
	 * 
	 * @return the current amount of stored ore resources
	 */
	public IntMap<OreResourceType> ores() {
		return this.ores.copy();
	}
	
	/** {@inheritDoc} */
	@Override
	public String localName() {
		return LOCAL_NAME;
	}
	
	/** {@inheritDoc} */
	@Override
	public int ordinal() {
		return ORIDINAL;
	}
	
	/** {@inheritDoc} */
	@Override
	public int addonLocalOrdinal() {
		return LOCAL_ORIDINAL;
	}
	
}
