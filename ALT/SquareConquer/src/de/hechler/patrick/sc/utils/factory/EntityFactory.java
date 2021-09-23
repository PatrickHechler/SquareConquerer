package de.hechler.patrick.sc.utils.factory;

import de.hechler.patrick.sc.enums.Resources;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.exeptions.CreationException;
import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.MovableEntity;
import de.hechler.patrick.sc.interfaces.Position;
import de.hechler.patrick.sc.objects.BuildPlace;
import de.hechler.patrick.sc.objects.CarriearUnit;
import de.hechler.patrick.sc.objects.HouseBuilding;
import de.hechler.patrick.sc.objects.ProducingBuilding;
import de.hechler.patrick.sc.objects.StorageBuilding;
import de.hechler.patrick.sc.objects.Unit;
import de.hechler.patrick.sc.objects.World;
import de.hechler.patrick.sc.utils.Units;

public class EntityFactory {
	
	private static final int FIGHT_BOAT_ACTIONS = 5;
	private static final int BOAT_ACTIONS       = 3;
	private static final int SIMPLE_U_ACTIONS   = 4;
	private static final int BOW_ACTIONS        = 3;
	private static final int MEELE_ACTIONS      = 3;
	private static final int BUILDER_ACTIONS    = 2;
	private static final int CARRIER_ACTIONS    = 2;
	
	private static final int BOAT_SIGHT       = 4;
	private static final int FIGHT_BOAT_SIGHT = 6;
	private static final int SIMPLE_U_SIGHT   = 1;
	private static final int BOW_SIGHT        = 4;
	private static final int MEELE_SIGHT      = 2;
	private static final int BUILDER_SIGHT    = 1;
	private static final int CARRIER_SIGHT    = 1;
	private static final int MAX_CARRIING     = 2;
	
	private static final int       FARM_ACTIONS       = 0;
	private static final int       FARM_INTERVAL      = 10;
	private static final int       FARM_CAPACITY      = 50;
	private static final int       FARM_PRODUCING_CNT = 5;
	private static final Resources FARM_PRODUCING     = Resources.eat;
	
	private static final int HOUSE_ACTIONS       = 0;
	private static final int HOUSE_CAPACITY      = 8;
	private static final int UNIT_HOUSE_CAPACITY = 4;
	
	private static final int       MINE_ACTIONS       = 0;
	private static final int       MINE_INTERVAL      = 2;
	private static final int       MINE_CAPACITY      = 10;
	private static final int       MINE_PRODUCING_CNT = 1;
	private static final Resources MINE_PRODUCING     = Resources.metal;
	
	private static final int       SPRING_ACTIONS       = 0;
	private static final int       SPRING_INTERVAL      = 1;
	private static final int       SPRING_PRODUCING_CNT = 5;
	private static final int       SPRING_CAPACITY      = 5;
	private static final Resources SPRING_PRODUCING     = Resources.drink;
	
	private static final int       WOOD_FARM_ACTIONS       = 0;
	private static final int       WOOD_FARM_INTERVAL      = 3;
	private static final int       WOOD_FARM_PRODUCING_CNT = 2;
	private static final int       WOOD_FARM_CAPACITY      = 10;
	private static final Resources WOOD_FARM_PRODUCING     = Resources.wood;
	
	private static final int STORAGE_ACTIONS = 0;
	
	
	
	public static void create(int owner, World world, Position pos, Type type) throws CreationException {
		create(owner, world, pos, type, null);
	}
	
	public static void create(int owner, World world, Position pos, Type type, StorageCreateParam storage) throws CreationException {
		Field f = world.getField(pos);
		if ( !Units.canExistOn(type, f.ground())) {
			throw new CreationException(type + " can't exist on the ground " + f.ground() + " pos=" + pos);
		} else if (f.getEntity() != null) {
			throw new CreationException("field'" + f + "' on pos'" + pos + "' is not empty: " + f.getEntity());
		}
		Entity e = create(owner, pos, type, storage);
		f.setEntity(e);
	}
	
	public static Entity create(int owner, Position pos, Type type, StorageCreateParam storage) {
		Entity e;
		switch (type) {
		case simple:
			e = new Unit(owner, pos, Units.canExistOn(type), SIMPLE_U_ACTIONS, type, SIMPLE_U_SIGHT);
			break;
		case fightingBoat:
			e = new Unit(owner, pos, Units.canExistOn(type), FIGHT_BOAT_ACTIONS, type, FIGHT_BOAT_SIGHT);
			break;
		case boat:
			e = new Unit(owner, pos, Units.canExistOn(type), BOAT_ACTIONS, type, BOAT_SIGHT);
			break;
		case bow:
			e = new Unit(owner, pos, Units.canExistOn(type), BOW_ACTIONS, type, BOW_SIGHT);
			break;
		case carrier:
			e = new CarriearUnit(owner, MAX_CARRIING, pos, Units.canExistOn(type), CARRIER_ACTIONS, type, CARRIER_SIGHT);
			break;
		case meele:
			e = new Unit(owner, pos, Units.canExistOn(type), MEELE_ACTIONS, type, MEELE_SIGHT);
			break;
		case builder:
			e = new Unit(owner, pos, Units.canExistOn(type), BUILDER_ACTIONS, type, BUILDER_SIGHT);
			break;
		case farm:
			e = new ProducingBuilding(pos, Units.canExistOn(type), type, FARM_ACTIONS, FARM_INTERVAL, FARM_PRODUCING, FARM_CAPACITY, FARM_PRODUCING_CNT);
			break;
		case house:
			e = new HouseBuilding(pos, Units.canExistOn(type), type, HOUSE_ACTIONS, HOUSE_CAPACITY);
			break;
		case houseBow:
		case houseBuilder:
		case houseMelee:
			e = new HouseBuilding(pos, Units.canExistOn(type), type, HOUSE_ACTIONS, UNIT_HOUSE_CAPACITY);
			break;
		case mine:
			e = new ProducingBuilding(pos, Units.canExistOn(type), type, MINE_ACTIONS, MINE_INTERVAL, MINE_PRODUCING, MINE_CAPACITY, MINE_PRODUCING_CNT);
			break;
		case spring:
			e = new ProducingBuilding(pos, Units.canExistOn(type), type, SPRING_ACTIONS, SPRING_INTERVAL, SPRING_PRODUCING, SPRING_PRODUCING_CNT, SPRING_CAPACITY);
			break;
		case storage:
			if (storage == null) {
				throw new CreationException("can't create an storage with a null starable");
			}
			e = new StorageBuilding(pos, Units.canExistOn(type), type, STORAGE_ACTIONS, storage.capacity, storage.storable);
		case woodFarm:
			e = new ProducingBuilding(pos, Units.canExistOn(type), type, WOOD_FARM_ACTIONS, WOOD_FARM_INTERVAL, WOOD_FARM_PRODUCING, WOOD_FARM_PRODUCING_CNT, WOOD_FARM_CAPACITY);
			break;
		default:
			throw new CreationException("unknown type: " + type);
		}
		return e;
	}
	
	public static void createBuildPlace(World world, Position pos, Type evolveTo, StorageCreateParam storage) throws CreationException {
		Field f = world.getField(pos);
		if ( !Units.canExistOn(evolveTo, f.ground())) {
			throw new CreationException(evolveTo + " can't exist on the ground " + f.ground() + " pos=" + pos);
		} else if (f.getEntity() != null) {
			throw new CreationException("field'" + f + "' on pos'" + pos + "' is not empty: " + f.getEntity());
		}
		Entity e = new BuildPlace(pos, evolveTo, storage);
		f.setEntity(e);
	}
	
	public static class StorageCreateParam {
		
		public Resources[] storable;
		public int         capacity;
		
		public StorageCreateParam(Resources[] storable, int capacity) {
			this.storable = storable;
			this.capacity = capacity;
		}
		
		public StorageCreateParam(int capacity, Resources... storable) {
			this.storable = storable;
			this.capacity = capacity;
		}
		
	}
	
	public static Unit create(MovableEntity me, Type type) {
		return new Unit(me, type);
	}
	
}
