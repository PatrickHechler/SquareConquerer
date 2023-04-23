package de.hechler.patrick.games.squareconqerer.ui;

import de.hechler.patrick.games.squareconqerer.world.entity.Building;
import de.hechler.patrick.games.squareconqerer.world.entity.Unit;
import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;
import de.hechler.patrick.games.squareconqerer.world.tile.Tile;
import de.hechler.patrick.games.squareconqerer.world.tile.TileType;

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
	
	public boolean isActive() { return state != STATE_INACTIVE; }
	
	public Tile modify(Tile t) {
		return switch (state) {
		case STATE_INACTIVE -> throw new IllegalStateException("I am inactive");
		case STATE_DEEP -> new Tile(t.type.addDeep(false), t.resource, t.visible());
		case STATE_HILL -> new Tile(t.type.addHill(false), t.resource, t.visible());
		case STATE_NORMAL -> new Tile(t.type.addNormal(false), t.resource, t.visible());
		case STATE_SET_GROUND -> new Tile((TileType) obj, t.resource, t.visible());
		case STATE_SET_ORE -> new Tile(t.type, (OreResourceType) obj, t.visible());
		case STATE_SET_UNIT -> {
			t.unit((Unit) obj);
			yield t;
		}
		case STATE_SET_BUILD -> {
			t.build((Building) obj);
			yield t;
		}
		default -> throw new AssertionError("illegal state: " + state);
		};
	}
	
	public void makeInactive() { state = STATE_INACTIVE; }
	
	public void makePlusDeep() { state = STATE_DEEP; }
	
	public void makePlusHill() { state = STATE_HILL; }
	
	public void makePlusNormal() { state = STATE_NORMAL; }
	
	public void makeSetResource(OreResourceType res) {
		if (res == null) throw new NullPointerException("resource is null");
		state = STATE_SET_ORE;
		obj   = res;
	}
	
	public void makeSetGround(TileType grd) {
		if (grd == null) throw new NullPointerException("ground is null");
		state = STATE_SET_GROUND;
		obj   = grd;
	}
	
	public void makeSetUnit(Unit u) {
		if (u == null) throw new NullPointerException("unit is null");
		state = STATE_SET_UNIT;
		obj   = u;
	}
	
	public void makeSetBuilding(Building b) {
		if (b == null) throw new NullPointerException("building is null");
		state = STATE_SET_UNIT;
		obj   = b;
	}
	
	@Override
	public String toString() {
		return switch (state) {
		case STATE_INACTIVE -> "none";
		case STATE_DEEP -> "add +deep suffix";
		case STATE_HILL -> "add +hill suffix";
		case STATE_NORMAL -> "add +normal suffix";
		case STATE_SET_ORE -> "set ore type: " + /* ((OreResourceType) */obj/* ) */.toString();
		case STATE_SET_GROUND -> "set ground type: " + /* ((TileType) */obj/* ) */.toString();
		case STATE_SET_UNIT -> "set the unit: " + /* ((Unit) */obj/* ) */.toString();
		case STATE_SET_BUILD -> "set the building: " + /* ((Build) */obj/* ) */.toString();
		default -> throw new AssertionError("illegal state: " + state);
		};
	}
	
}
