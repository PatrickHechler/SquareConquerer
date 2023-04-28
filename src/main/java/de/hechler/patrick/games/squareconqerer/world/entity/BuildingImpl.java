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

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;
import de.hechler.patrick.games.squareconqerer.objects.EnumIntMap;
import de.hechler.patrick.games.squareconqerer.world.resource.ProducableResourceType;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;

public abstract sealed class BuildingImpl extends EntityImpl implements Building permits StoreBuild, MyBuild {
	
	public static final int MY_COUNT_NO_NULL = 1;
	
	private EnumIntMap<ProducableResourceType> neededResources;
	private int                                neededBuildTurns;
	
	protected BuildingImpl(int x, int y, User usr, int maxlives, EnumIntMap<ProducableResourceType> neededResources) {
		super(x, y, usr, maxlives, maxlives, 0);
		this.neededResources = neededResources;
		for (int val : neededResources.array()) {
			this.neededBuildTurns += val;
		}
		this.neededBuildTurns = (this.neededBuildTurns >>> 1) + 1;
	}
	
	protected BuildingImpl(int x, int y, User usr, int maxlives, int lives, EnumIntMap<ProducableResourceType> neededResources,
			int remainingBuildTurns) {
		super(x, y, usr, maxlives, lives, 0);
		this.neededResources  = neededResources;
		this.neededBuildTurns = remainingBuildTurns;
	}
	
	@Override // check both if there is no build time
	public boolean isFinishedBuild() { return this.neededBuildTurns <= 0 && this.neededResources != null; }
	
	@Override
	public int remainingBuildTurns() { return this.neededBuildTurns; }
	
	@Override
	public EnumIntMap<ProducableResourceType> neededResources() { return this.neededResources == null ? null : this.neededResources.copy(); }
	
	@Override
	public void store(Unit u, Resource res, int amount) throws TurnExecutionException {
		checkOwner(u);
		if (u.carryAmount() < amount) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (amount <= 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (this.neededResources == null) {
			finishedBuildStore(res, amount);
			u.uncarry(res, amount);
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
	
	protected void finishedBuildStore(Resource r, int amount) throws TurnExecutionException {
		throw new TurnExecutionException(ErrorType.INVALID_TURN);
	}
	
	@Override
	public void giveRes(Unit u, Resource res, int amount) throws TurnExecutionException {
		throw new TurnExecutionException(ErrorType.INVALID_TURN);
	}
	
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
	
	@Override
	public final String cls() { return "Building"; }
	
}
