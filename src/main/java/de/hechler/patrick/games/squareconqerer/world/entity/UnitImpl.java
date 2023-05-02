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
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;

/**
 * this class is used to help implementing the {@link Unit} interface.<br>
 * if you want to use this class for your own {@link Unit}, see {@link MyUnit}
 * 
 * @author Patrick Hechler
 */
public abstract sealed class UnitImpl extends EntityImpl implements Unit permits Carrier, MyUnit {
	
	/**
	 * the number of intern subclasses (not including {@link MyUnit})
	 */
	public static final int MY_COUNT_NO_NULL = 1;
	
	/**
	 * the resource which is currently being carried
	 */
	protected Resource  carryResource;
	/**
	 * the number of carried resources
	 */
	protected int       carryAmount;
	/**
	 * the maximum number of carried resources
	 */
	protected final int carryMaxAmount;
	
	/**
	 * creates a new {@link UnitImpl} with the given values
	 * 
	 * @param x              the {@link #x()} coordinate
	 * @param y              the {@link #y()} coordinate
	 * @param usr            the {@link #owner()}
	 * @param maxlives       the {@link #maxLives()} and the {@link #lives()}
	 * @param viewRange      the {@link #viewRange()}
	 * @param carryMaxAmount the {@link #carryMaxAmount()}
	 */
	public UnitImpl(int x, int y, User usr, int maxlives, int viewRange, int carryMaxAmount) {
		super(x, y, usr, maxlives, maxlives, viewRange);
		this.carryMaxAmount = carryMaxAmount;
	}
	
	/**
	 * creates a new {@link UnitImpl} with the given values
	 * 
	 * @param x              the {@link #x()} coordinate
	 * @param y              the {@link #y()} coordinate
	 * @param usr            the {@link #owner()}
	 * @param maxlives       the {@link #maxLives()}
	 * @param lives          the {@link #lives()}
	 * @param viewRange      the {@link #viewRange()}
	 * @param carryMaxAmount the {@link #carryMaxAmount()}
	 * @param carryAmount    the {@link #carryAmount()}
	 * @param res            the {@link #carryRes()}
	 */
	public UnitImpl(int x, int y, User usr, int maxlives, int lives, int viewRange, int carryMaxAmount, int carryAmount, Resource res) {
		super(x, y, usr, maxlives, lives, viewRange);
		this.carryMaxAmount = carryMaxAmount;
		this.carryAmount    = carryAmount;
		this.carryResource  = res;
	}
	
	/** {@inheritDoc} */
	@Override
	public void changePos(int newx, int newy, Tile checkCanTile) throws TurnExecutionException {
		checkValid(checkCanTile);
		super.x = newx;
		super.y = newy;
	}
	
	/**
	 * checks that this unit can enter the given tile
	 * 
	 * @param checkCanTile the tile to check
	 * @throws TurnExecutionException if the unit can not enter the given tile
	 */
	protected void checkValid(Tile checkCanTile) throws TurnExecutionException {
		if (!checkCanTile.ground.isLand()) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public Resource carryRes() {
		return this.carryResource;
	}
	
	/** {@inheritDoc} */
	@Override
	public int carryAmount() {
		return this.carryAmount;
	}
	
	/** {@inheritDoc} */
	@Override
	public int carryMaxAmount() {
		return this.carryMaxAmount;
	}
	
	/** {@inheritDoc} */
	@Override
	public void carry(Resource res, int amount) throws TurnExecutionException {
		if (amount < 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (res == null) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (res != this.carryResource && this.carryResource != null) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		this.carryResource  = res;
		this.carryAmount   += amount;
	}
	
	/** {@inheritDoc} */
	@Override
	public void uncarry(Resource res, int amount) throws TurnExecutionException {
		if (amount <= 0) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (res != this.carryResource) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		if (this.carryAmount < amount) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
		}
		this.carryAmount -= amount;
		if (this.carryAmount == 0) {
			this.carryResource = null;
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public String type() {
		return "Unit"; //$NON-NLS-1$
	}
	
	/** {@inheritDoc} */
	@Override
	public String name() {
		return super.type();
	}
	
}
