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
package de.hechler.patrick.games.squareconqerer.ui;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.tile.GroundType;

/**
 * this class is used to modify tiles
 * 
 * @author Patrick Hechler
 */
public class TileChanger {
	
	private static final String STR_SET_BUILDING    = Messages.getString("TileChanger.set-building");  //$NON-NLS-1$
	private static final String STR_SET_UNIT        = Messages.getString("TileChanger.set-unit");      //$NON-NLS-1$
	private static final String STR_SET_GROUND_TYPE = Messages.getString("TileChanger.set-ground");    //$NON-NLS-1$
	private static final String STR_SET_ORE_TYPE    = Messages.getString("TileChanger.set-ore");       //$NON-NLS-1$
	private static final String STR_ADD_NORMAL      = Messages.getString("TileChanger.add-normal");    //$NON-NLS-1$
	private static final String STR_ADD_HILL        = Messages.getString("TileChanger.add-hill");      //$NON-NLS-1$
	private static final String STR_ADD_DEEP        = Messages.getString("TileChanger.add-deep");      //$NON-NLS-1$
	private static final String STR_NONE            = Messages.getString("TileChanger.inactive");      //$NON-NLS-1$
	private static final String I_AM_INACTIVE       = Messages.getString("TileChanger.i-am-inactive"); //$NON-NLS-1$
	
	private static final int STATE_INACTIVE   = 0;
	private static final int STATE_DEEP       = 1;
	private static final int STATE_HILL       = 2;
	private static final int STATE_NORMAL     = 3;
	private static final int STATE_SET_ORE    = 4;
	private static final int STATE_SET_GROUND = 5;
	private static final int STATE_SET_UNIT   = 6;
	private static final int STATE_SET_BUILD  = 7;
	
	private int    state = STATE_INACTIVE;
	private Object obj;
	
	/**
	 * returns <code>true</code> if the tile changer is active and <code>false</code> if it is inactive
	 * 
	 * @return <code>true</code> if the tile changer is active and <code>false</code> if it is inactive
	 */
	public boolean isActive() { return this.state != STATE_INACTIVE; }
	
	/**
	 * modifies the given tile
	 * <p>
	 * note that if the current mode is incompatible with the given tile, a tile equal to the given tile is returned (possibly <code>t</code>)
	 * 
	 * @param t the tile to be modified
	 * @return the modified tile (possible <code>t</code>)
	 * @throws IllegalStateException if the tile change is currently inactive
	 */
	public Tile modify(Tile t) throws IllegalStateException {
		return switch (this.state) {
		case STATE_INACTIVE -> throw new IllegalStateException(I_AM_INACTIVE);
		case STATE_DEEP -> new Tile(t.ground.addDeep(false, false), t.resource, t.visible());
		case STATE_HILL -> new Tile(t.ground.addHill(false, false), t.resource, t.visible());
		case STATE_NORMAL -> new Tile(t.ground.addNormal(false, false), t.resource, t.visible());
		case STATE_SET_GROUND -> new Tile((GroundType) this.obj, t.resource, t.visible());
		case STATE_SET_ORE -> new Tile(t.ground, (OreResourceType) this.obj, t.visible());
		case STATE_SET_UNIT -> {
			t.unit(((Unit) this.obj).copy());
			yield t;
		}
		case STATE_SET_BUILD -> {
			t.build(((Building) this.obj).copy());
			yield t;
		}
		default -> throw new AssertionError("illegal state: " + this.state); //$NON-NLS-1$ this should never happen
		};
	}
	
	/**
	 * makes this tile changer inactive
	 */
	public void makeInactive() { this.state = STATE_INACTIVE; }
	
	/**
	 * let this tile changer add deep to the tiles
	 * 
	 * @see GroundType#addDeep(boolean, boolean)
	 */
	public void makePlusDeep() { this.state = STATE_DEEP; }
	
	/**
	 * let this tile changer add hill to the tiles
	 * 
	 * @see GroundType#addHill(boolean, boolean)
	 */
	public void makePlusHill() { this.state = STATE_HILL; }
	
	/**
	 * let this tile changer normalize the tiles
	 * 
	 * @see GroundType#addNormal(boolean, boolean)
	 */
	public void makePlusNormal() { this.state = STATE_NORMAL; }
	
	/**
	 * let this tile changer set the tiles resources to <code>res</code>
	 * 
	 * @param res the resource to be used
	 */
	public void makeSetResource(OreResourceType res) {
		if (res == null) throw new NullPointerException(Messages.getString("TileChanger.no-resource")); //$NON-NLS-1$
		this.state = STATE_SET_ORE;
		this.obj   = res;
	}
	
	/**
	 * let this tile changer set the tiles ground to <code>grd</code>
	 * 
	 * @param grd the ground to be used
	 */
	public void makeSetGround(GroundType grd) {
		if (grd == null) throw new NullPointerException(Messages.getString("TileChanger.no-ground")); //$NON-NLS-1$
		this.state = STATE_SET_GROUND;
		this.obj   = grd;
	}
	
	/**
	 * let the tile changer set the tiles unit to a {@link Unit#copy() copy} of the given unit
	 * 
	 * @param u the unit to be set
	 */
	public void makeSetUnit(Unit u) {
		if (u == null) throw new NullPointerException(Messages.getString("TileChanger.no-unit")); //$NON-NLS-1$
		this.state = STATE_SET_UNIT;
		this.obj   = u;
	}
	
	/**
	 * let the tile changer set the tiles building to a {@link Building#copy() copy} of the given building
	 * 
	 * @param b the building to be set
	 */
	public void makeSetBuilding(Building b) {
		if (b == null) throw new NullPointerException(Messages.getString("TileChanger.no-building")); //$NON-NLS-1$
		this.state = STATE_SET_UNIT;
		this.obj   = b;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return switch (this.state) {
		case STATE_INACTIVE -> STR_NONE;
		case STATE_DEEP -> STR_ADD_DEEP;
		case STATE_HILL -> STR_ADD_HILL;
		case STATE_NORMAL -> STR_ADD_NORMAL;
		case STATE_SET_ORE -> STR_SET_ORE_TYPE + /* ((OreResourceType) */this.obj/* ) */.toString();
		case STATE_SET_GROUND -> STR_SET_GROUND_TYPE + /* ((TileType) */this.obj/* ) */.toString();
		case STATE_SET_UNIT -> STR_SET_UNIT + /* ((Unit) */this.obj/* ) */.toString();
		case STATE_SET_BUILD -> STR_SET_BUILDING + /* ((Build) */this.obj/* ) */.toString();
		default -> throw new AssertionError("illegal state: " + this.state); //$NON-NLS-1$ this should never happen
		};
	}
	
}
