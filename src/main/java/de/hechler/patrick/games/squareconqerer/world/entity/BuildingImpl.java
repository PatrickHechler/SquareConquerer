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
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;
import de.hechler.patrick.games.squareconqerer.stuff.IntMap;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

/**
 * this class is used to help implementing the {@link Building} interface.<br>
 * if you want to use this class for your own {@link Building}, see {@link MyBuild}
 * 
 * @author Patrick Hechler
 */
public abstract sealed class BuildingImpl extends EntityImpl implements Building permits Storage, MyBuild {
	
	/**
	 * the number of intern subclasses (not including {@link MyBuild})
	 */
	public static final int MY_COUNT_NO_NULL = 1;
	
	private IntMap<ProducableResourceType> neededResources;
	private int                            neededBuildTurns;
	
	/**
	 * creates a new {@link BuildingImpl} with the given coordinates, owner, max-lives and needed resources
	 * 
	 * @param x               the {@link Entity#x()} coordinate
	 * @param y               the {@link Entity#y()} coordinate
	 * @param usr             the {@link Entity#owner()}
	 * @param maxlives        the {@link Entity#maxLives()} and the {@link Entity#lives()}
	 * @param neededResources the {@link Building#neededResources()}
	 */
	public BuildingImpl(int x, int y, User usr, int maxlives, IntMap<ProducableResourceType> neededResources) {
		super(x, y, usr, maxlives, maxlives, 0);
		this.neededResources = neededResources;
		for (int val : neededResources.array()) {
			this.neededBuildTurns += val;
		}
		this.neededBuildTurns = (this.neededBuildTurns >>> 1) + 1;
	}
	
	/**
	 * create a new {@link BuildingImpl} with the given values
	 * 
	 * @param x                   the {@link Entity#x()} coordinate
	 * @param y                   the {@link Entity#y()} coordinate
	 * @param usr                 the {@link Entity#owner()}
	 * @param maxlives            the {@link Entity#maxLives()}
	 * @param lives               the {@link Entity#lives()}
	 * @param neededResources     the {@link Building#neededResources()}
	 * @param remainingBuildTurns the {@link Building#remainingBuildTurns()}
	 */
	public BuildingImpl(int x, int y, User usr, int maxlives, int lives, IntMap<ProducableResourceType> neededResources, int remainingBuildTurns) {
		super(x, y, usr, maxlives, lives, 0);
		this.neededResources  = neededResources;
		this.neededBuildTurns = remainingBuildTurns;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * this implementation returns <code>true</code>, if {@link #neededResources()} is <code>null</code> and {@link #remainingBuildTurns()} is (lower or) equal to
	 * <code>0</code>
	 */
	@Override // check both if there is no build time
	public boolean isFinishedBuild() {
		if (this.neededBuildTurns > 0) return false;
		if (this.neededResources == null) return true;
		for (int val : this.neededResources.array()) {
			if (val > 0) return false;
		}
		this.neededResources = null;
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public int remainingBuildTurns() { return this.neededBuildTurns <= 0 ? 0 : this.neededBuildTurns; }
	
	/** {@inheritDoc} */
	@Override
	public IntMap<ProducableResourceType> neededResources() { return this.neededResources == null ? null : this.neededResources.copy(); }
	
	/** {@inheritDoc} */
	@Override
	public void store(Unit u, Resource res, int amount) throws TurnExecutionException {
		checkOwner(u);
		if (u.carryAmount() < amount || u.carryRes() != res) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (amount <= 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (this.neededResources == null) {
			finishedBuildStore(u, res, amount);
			return;
		}
		if (!(res instanceof ProducableResourceType prt)) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (this.neededResources.subBy(prt, amount) < 0) {
			this.neededResources.addBy(prt, amount);
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
	}
	
	/**
	 * stores the given amount of the resource type from the unit
	 * 
	 * @param u      the unit which carries the given resource
	 * @param r      the resource type
	 * @param amount the number to store
	 * 
	 * @throws TurnExecutionException if this building can not store the given additional resources with the given amount
	 */
	protected void finishedBuildStore(Unit u, Resource r, int amount) throws TurnExecutionException {
		throw new TurnExecutionException(ErrorType.INVALID_TURN);
	}
	
	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unused")
	public void giveRes(Unit u, Resource res, int amount) throws TurnExecutionException {
		throw new TurnExecutionException(ErrorType.INVALID_TURN);
	}
	
	/** {@inheritDoc} */
	@Override
	public void build(Unit u) throws TurnExecutionException {
		checkOwner(u);
		if (this.neededResources != null) {
			for (int val : this.neededResources.array()) {
				if (val > 0) {
					throw new TurnExecutionException(ErrorType.INVALID_TURN);
				}
			}
			this.neededResources = null;
		}
		if (this.neededBuildTurns <= 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		this.neededBuildTurns--;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean canBuild(Unit u) {
		if (u.owner() != owner()) return false;
		if (this.neededResources != null) {
			for (int val : this.neededResources.array()) {
				if (val > 0) return false;
			}
			this.neededResources = null;
		}
		return this.neededBuildTurns > 0;
	}
	
	/** {@inheritDoc} */
	@Override
	public String type() {
		return "Building"; //$NON-NLS-1$
	}
	
	/** {@inheritDoc} */
	@Override
	public String name() {
		return super.type();
	}
	
}
