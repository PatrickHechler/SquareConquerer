package de.hechler.patrick.sc.objects;

import de.hechler.patrick.sc.enums.Direction;
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
