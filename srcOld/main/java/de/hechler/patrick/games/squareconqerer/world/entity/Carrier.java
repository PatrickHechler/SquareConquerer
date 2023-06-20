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
import de.hechler.patrick.games.squareconqerer.addons.SCAddon;
import de.hechler.patrick.games.squareconqerer.addons.TheGameAddon;
import de.hechler.patrick.games.squareconqerer.world.resource.Resource;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;

/**
 * this unit can carry resources
 * 
 * @author Patrick Hechler
 */
public final class Carrier extends UnitImpl {
	
	private static final String LOCAL_NAME = "Carrier";
	
	/**
	 * the {@link #name() name} of {@link ImageableObj} instances from this class
	 */
	public static final String NAME   = "Carrier"; //$NON-NLS-1$
	/**
	 * this number is used when {@link TheGameAddon} sends units from this class
	 */
	public static final int    NUMBER = 0x925D9B86;
	
	/**
	 * the maximum lives a carrier can have
	 */
	public static final int MAX_LIVES  = 3;
	/**
	 * the view range a carrier has
	 */
	public static final int VIEW_RANGE = 4;
	/**
	 * the maximum amount a carrier can carry
	 */
	public static final int MAX_CARRY  = 5;
	
	private static final int LOCAL_ORIDINAL = 0;
	private static final int ORIDINAL       = SCAddon.addon(Carrier.class).oridinalOffsetUnit() + LOCAL_ORIDINAL;
	
	/**
	 * creates a new carrier with the given values
	 * 
	 * @param x the {@link #x()} coordinate
	 * @param y the {@link #y()} coordinate
	 * @param usr the {@link #owner()} of this unit
	 */
	public Carrier(int x, int y, User usr) {
		super(x, y, usr, MAX_LIVES, VIEW_RANGE, MAX_CARRY);
	}
	
	/**
	 * creates a new carrier with the given values
	 * 
	 * @param x the {@link #x()} coordinate
	 * @param y the {@link #y()} coordinate
	 * @param usr the {@link #owner()} of this unit
	 * @param lives the {@link #lives()}
	 * @param res the {@link #carryRes()}
	 * @param carryAmount the {@link #carryAmount()}
	 */
	public Carrier(int x, int y, User usr, int lives, Resource res, int carryAmount) {
		super(x, y, usr, MAX_LIVES, lives, VIEW_RANGE, MAX_CARRY, carryAmount, res);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * when the unit carries more, it is slower
	 */
	@Override
	public int moveRange() {
		return 3 - (super.carryAmount >>> 2);
	}
	
	/** {@inheritDoc} */
	@Override
	public Carrier copy() {
		return new Carrier(super.x, super.y, owner(), lives(), super.carryResource, super.carryAmount);
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
