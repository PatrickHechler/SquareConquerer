package de.hechler.patrick.sc.objects;

import java.util.Objects;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.exeptions.InvalidDestinationException;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.MovableEntity;
import de.hechler.patrick.sc.interfaces.Position;

public class Map {
	
	private final Field[][] map;
	
	
	
	public Map(int xCnt, int yCnt) {
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
	
	public void setField(Position pos, Field field) throws NullPointerException {
		map[pos.getX()][pos.getY()] = Objects.requireNonNull(field, "a null field is forbidden!");
	}
	
}
