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

import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.tile.GroundType;

public class BuildMode {
	
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
	
	public boolean isActive() { return this.state != STATE_INACTIVE; }
	
	public Tile modify(Tile t) {
		return switch (this.state) {
		case STATE_INACTIVE -> throw new IllegalStateException("I am inactive");
		case STATE_DEEP -> new Tile(t.ground.addDeep(false, false), t.resource, t.visible());
		case STATE_HILL -> new Tile(t.ground.addHill(false, false), t.resource, t.visible());
		case STATE_NORMAL -> new Tile(t.ground.addNormal(false, false), t.resource, t.visible());
		case STATE_SET_GROUND -> new Tile((GroundType) this.obj, t.resource, t.visible());
		case STATE_SET_ORE -> new Tile(t.ground, (OreResourceType) this.obj, t.visible());
		case STATE_SET_UNIT -> {
			t.unit((Unit) this.obj);
			yield t;
		}
		case STATE_SET_BUILD -> {
			t.build((Building) this.obj);
			yield t;
		}
		default -> throw new AssertionError("illegal state: " + this.state);
		};
	}
	
	public void makeInactive() { this.state = STATE_INACTIVE; }
	
	public void makePlusDeep() { this.state = STATE_DEEP; }
	
	public void makePlusHill() { this.state = STATE_HILL; }
	
	public void makePlusNormal() { this.state = STATE_NORMAL; }
	
	public void makeSetResource(OreResourceType res) {
		if (res == null) throw new NullPointerException("resource is null");
		this.state = STATE_SET_ORE;
		this.obj   = res;
	}
	
	public void makeSetGround(GroundType grd) {
		if (grd == null) throw new NullPointerException("ground is null");
		this.state = STATE_SET_GROUND;
		this.obj   = grd;
	}
	
	public void makeSetUnit(Unit u) {
		if (u == null) throw new NullPointerException("unit is null");
		this.state = STATE_SET_UNIT;
		this.obj   = u;
	}
	
	public void makeSetBuilding(Building b) {
		if (b == null) throw new NullPointerException("building is null");
		this.state = STATE_SET_UNIT;
		this.obj   = b;
	}
	
	@Override
	public String toString() {
		return switch (this.state) {
		case STATE_INACTIVE -> "none";
		case STATE_DEEP -> "add +deep suffix";
		case STATE_HILL -> "add +hill suffix";
		case STATE_NORMAL -> "add +normal suffix";
		case STATE_SET_ORE -> "set ore type: " + /* ((OreResourceType) */this.obj/* ) */.toString();
		case STATE_SET_GROUND -> "set ground type: " + /* ((TileType) */this.obj/* ) */.toString();
		case STATE_SET_UNIT -> "set the unit: " + /* ((Unit) */this.obj/* ) */.toString();
		case STATE_SET_BUILD -> "set the building: " + /* ((Build) */this.obj/* ) */.toString();
		default -> throw new AssertionError("illegal state: " + this.state);
		};
	}
	
}
