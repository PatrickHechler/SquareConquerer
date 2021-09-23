package de.hechler.patrick.sc.objects;

import java.util.ArrayList;
import java.util.List;

import de.hechler.patrick.sc.enums.Resources;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.Position;
import de.hechler.patrick.sc.utils.Units;
import de.hechler.patrick.sc.utils.factory.EntityFactory;
import de.hechler.patrick.sc.utils.factory.EntityFactory.StorageCreateParam;

public class BuildPlace extends StorageBuilding {
	
	public final Type          evolveTo;
	private StorageCreateParam scp;
	
	public BuildPlace(Position pos, Type evolveTo, StorageCreateParam scp) {
		super(pos, Units.canExistOn(evolveTo), Type.buildplace, 0, capacity(evolveTo), storable(evolveTo));
		this.evolveTo = evolveTo;
		this.scp = scp;
	}
	
	@Override
	public void store(Resources resource, int cnt) throws IllegalStateException, IllegalArgumentException {
		int stored = contains(resource);
		checkBuildPlaceStore(resource, stored + cnt, type);
		super.store(resource, cnt);
	}
	
	public int capacity(Resources r) {
		return capacity(evolveTo, r);
	}
	
	public int freeSpace(Resources r) {
		return capacity(evolveTo, r) - contains(r);
	}
	
	public Type evolveTo() {
		return evolveTo;
	}
	
	private final static Resources[] storable(Type t) {
		List <Resources> s = new ArrayList <>();
		for (Resources r : Resources.values()) {
			if (0 < capacity(t, r)) s.add(r);
		}
		return s.toArray(new Resources[s.size()]);
	}
	
	private final static int capacity(Type t, Resources r) {
		switch (t) {
		case house:
		case houseBow:
		case houseBuilder:
		case houseMelee:
			switch (r) {
			case drink:
				return 0;
			case eat:
				return 0;
			case metal:
				return 0;
			case wood:
				return 10;
			default:
				throw new RuntimeException("unknown ressource: " + r.name());
			}
		case farm:
			switch (r) {
			case drink:
				return 0;
			case eat:
				return 0;
			case metal:
				return 0;
			case wood:
				return 25;
			default:
				throw new RuntimeException("unknown ressource: " + r.name());
			}
		case mine:
			switch (r) {
			case drink:
				return 0;
			case eat:
				return 0;
			case metal:
				return 0;
			case wood:
				return 10;
			default:
				throw new RuntimeException("unknown ressource: " + r.name());
			}
		case spring:
			switch (r) {
			case drink:
				return 0;
			case eat:
				return 0;
			case metal:
				return 2;
			case wood:
				return 5;
			default:
				throw new RuntimeException("unknown ressource: " + r.name());
			}
		case storage:
			switch (r) {
			case drink:
				return 0;
			case eat:
				return 0;
			case metal:
				return 5;
			case wood:
				return 10;
			default:
				throw new RuntimeException("unknown ressource: " + r.name());
			}
		case woodFarm:
			switch (r) {
			case drink:
				return 0;
			case eat:
				return 0;
			case metal:
				return 0;
			case wood:
				return 0;
			default:
				throw new RuntimeException("unknown ressource: " + r.name());
			}
		default:
			throw new AssertionError("illegal type=" + t.name() + " of buildplace");
		}
	}
	
	private final static int capacity(Type t) {
		int c = 0;
		for (Resources r : Resources.values()) {
			c += capacity(t, r);
		}
		return c;
	}
	
	private void checkBuildPlaceStore(Resources resource, int store, Type type) throws IllegalStateException {
		int cap = capacity(type, resource);
		if (store > cap) throw new IllegalStateException("from the resource (" + resource.name() + ") this buildplace can only store " + cap + ", but it would store " + store + " after that operation!");
	}
	
	
	
	public void finishBuild(World world) throws IllegalStateException {
		if (freeSpace() != 0) throw new IllegalStateException("not all needed resources are here!");
		Entity replacement = EntityFactory.create( -1, pos, evolveTo, scp);
		Field myField = world.getField(pos);
		assert myField.getEntity() == this : "the entity of the field is on the world is not me";
		myField.setEntity(replacement);
	}
	
}
