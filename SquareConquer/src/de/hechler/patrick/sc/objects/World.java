package de.hechler.patrick.sc.objects;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.enums.Resources;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.exeptions.InvalidDestinationException;
import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.MovableEntity;
import de.hechler.patrick.sc.interfaces.Position;

public class World {
	
	private final Field[][] map;
	
	
	
	public World(int xCnt, int yCnt) {
		map = new Field[xCnt][yCnt];
	}
	
	
	
	public void fillCarrier(CarriearUnit carriear, StorageBuilding store, Resources resource) throws IllegalStateException {
		if (carriear.isFull()) throw new IllegalStateException("carrier is full!");
		if ( !carriear.isEmpty() && carriear.carries() != resource) throw new IllegalStateException("carrier carries wrong ressource!");
		int cMaxCnt = carriear.freeCount(), sMaxCnt = store.contains(resource);
		if (sMaxCnt < 0) throw new IllegalStateException("store does not contain this resource!");
		int cnt = Math.min(cMaxCnt, sMaxCnt);
		if (carriear.isEmpty()) {
			carriear.carrie(resource);
		}
		carriear.add(cnt);
		store.remove(resource, cnt);
	}
	
	public void fillStore(StorageBuilding store, CarriearUnit carriear) {
		if (carriear.isEmpty()) throw new IllegalStateException("carrier is empty!");
		if (store.freeSpace() <= 0) throw new IllegalStateException("store is full!");
		int cMaxCnt = carriear.count(), sMaxCnt = store.freeSpace();
		int cnt = Math.min(cMaxCnt, sMaxCnt);
		store.store(carriear.carries(), cnt);
		carriear.remove(cnt);
	}
	
	/**
	 * Attacks the {@code defender} with the {@code attacker}.<br>
	 * Only one of the will get {@link Entity#getDamage(int)}, so even the attacker can get destroyed as cause of this call.
	 * 
	 * @param attacker
	 *            the attacking {@link MovableEntity}
	 * @param defender
	 *            the defending {@link Entity}
	 */
	public void attack(MovableEntity attacker, Entity defender) {
		if (defender.isMovable()) {
			if ( ((MovableEntity) defender).owner() == attacker.owner()) {
				System.err.println("ID[" + attacker.owner() + "] is attacking its own units");
			}
		}
		double am;
		int dist = attacker.position().distance(defender.position());
		switch (attacker.type()) {
		case meele:
			if (dist > 1) throw new IllegalStateException("too much distance");
			am = 1.5;
			break;
		case boat:
			if (dist > 3) throw new IllegalStateException("too much distance");
			if (dist != 1) am = 1.125;
			else am = 1.5;
			break;
		case bow:
			if (dist > 3) throw new IllegalStateException("too much distance");
			am = 1;
			break;
		case builder:
			if (dist > 1) throw new IllegalStateException("too much distance");
			am = 0.375;
			break;
		case simple:
			if (dist > 1) throw new IllegalStateException("too much distance");
			am = 0.125;
			break;
		default:
			throw new RuntimeException("unknown type of the attacker: " + attacker.type());
		}
		double dm;
		double zusatz = 0;
		switch (defender.type()) {
		case boat:
			if (dist != 1) dm = 0.75;
			else dm = 0.875;
			break;
		case bow:
			dm = 0.5;
			break;
		case builder:
			dm = 0.125;
			break;
		case house:
		case houseBow:
		case houseBuilder:
		case houseMelee: {
			dm = 0;
			for (MovableEntity me : ((HouseBuilding) defender).inside) {
				if (me.type() == Type.meele) zusatz += me.health() * 0.5;
			}
			break;
		}
		case meele:
			if (dist == 1) dm = 1;
			else dm = 0;
			break;
		case simple:
			dm = 0.125;
			break;
		case farm:
		case spring:
		case storage:
		case woodFarm:
		case mine:
			dm = 0;
			break;
		default:
			throw new RuntimeException("unknown type of the defender: " + defender.type());
		}
		double attackPoints = (am * attacker.health()) - ( (dm * defender.health()) + zusatz);
		Entity victim;
		if (attackPoints > 0) {
			victim = defender.getDamage((int) attackPoints) ? defender : null;
		} else {
			victim = attacker.getDamage((int) -attackPoints) ? attacker : null;
		}
		if (victim != null) {
			Field f = getField(victim.position());
			f.setEntity(null);
		}
	}
	
	public void move(MovableEntity entity, Direction dir) throws InvalidDestinationException {
		Position pos = entity.position();
		final int x = pos.getX(), y = pos.getY();
		Position dest = pos.newCreateMove(dir);
		final int destX = dest.getX(), destY = dest.getY();
		if (x >= map.length) {
			throw new InvalidDestinationException("out of map: X_LEN=" + map.length + " pos: '" + pos + "'");
		}
		if (y >= map[0].length) {
			throw new InvalidDestinationException("out of map: Y_LEN=" + map[0].length + " pos: '" + pos + "'");
		}
		if ( !entity.canExsitOn().contains(map[destX][destY].ground())) {
			throw new InvalidDestinationException("'" + entity + "' can't exist on field: '" + map[destX][destY] + "'");
		}
		if (map[destX][destY].getEntity() != null) {
			throw new InvalidDestinationException("'" + entity + "' can't move to field '" + map[destX][destY] + "', because there is already an entity: '" + map[destX][destY].getEntity() + "'");
		}
		map[x][y].setEntity(null);
		map[destX][destY].setEntity(entity);
		entity.move(dest);
	}
	
	public Field getField(Position pos) {
		return map[pos.getX()][pos.getY()];
	}
	
	/**
	 * adds the {@link Field} to this {@link World} on the {@link Position} of the {@link Field} {@code field}
	 * 
	 * @param field
	 *            the {@link Field} to be added
	 */
	public void overrideField(Field field) {
		map[field.getXPos()][field.getYPos()] = field;
	}
	
	public int getXCnt() {
		return map.length;
	}
	
	public int getYCnt() {
		return map[0].length;
	}
	
	public void newTurn() {
		for (Field[] fields : map) {
			for (Field f : fields) {
				Entity e = f.getEntity();
				if (e != null) e.newTurn();
			}
		}
	}
	
}
