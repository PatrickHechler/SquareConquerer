package de.hechler.patrick.sc.objects;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.exeptions.InvalidDestinationException;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.MovableEntity;
import de.hechler.patrick.sc.interfaces.Position;

public class HouseBuilding extends Building {
	
	protected int capacity;
	
	protected Set <MovableEntity> inside;
	
	
	
	public HouseBuilding(Position pos, Set <Grounds> canExsistOn, Type type, int totalActions, int capacity) {
		this(new UnchangeablePosition(pos), canExsistOn, type, totalActions, capacity);
	}
	
	public HouseBuilding(UnchangeablePosition pos, Set <Grounds> canExsistOn, Type type, int totalActions, int capacity) {
		this(pos, canExsistOn, type, totalActions, 0, capacity);
	}
	
	public HouseBuilding(Position pos, Set <Grounds> canExsistOn, Type type, int totalActions, int remainingActions, int capacity) {
		this(new UnchangeablePosition(pos), canExsistOn, type, totalActions, remainingActions, capacity);
	}
	
	public HouseBuilding(UnchangeablePosition pos, Set <Grounds> canExsistOn, Type type, int totalActions, int remainingActions, int capacity) {
		super(pos, canExsistOn, type, totalActions, remainingActions);
		this.capacity = capacity;
		this.inside = new HashSet <MovableEntity>();
	}
	
	
	
	public Set <MovableEntity> inside() {
		return Collections.unmodifiableSet(inside);
	}
	
	public void goIn(MovableEntity entity, World world) throws IllegalArgumentException, IllegalStateException {
		Position p = entity.position();
		Field f = world.getField(p);
		if (capacity >= inside.size()) {
			throw new IllegalStateException("full");
		} else if (f.getEntity() != entity) {
			throw new IllegalArgumentException("entity is not on my field: expected:'" + entity + "', but on my field is:'" + f.getEntity() + "'");
		} else if (pos.distance(p) != 1) {
			throw new IllegalStateException("not my neighbour: distance=" + pos.distance(p));
		}
		f.setEntity(null);
		entity.useAction();
		entity.setPosition(pos);
		if ( !inside.add(entity)) {// if entity is inside, {entity == f.getEntity()} must return false, if it returns true, it is now deleted
			throw new IllegalStateException("already inside!");
		}
	}
	
	public void goOut(MovableEntity entity, World world, Direction dir) {
		UnchangeablePosition newPos = pos.newCreateMove(dir);
		Field nf = world.getField(newPos);
		if (!entity.canExsitOn().contains(nf.ground())) {
			throw new InvalidDestinationException(nf, dir, nf.ground(), entity.canExsitOn());
		}
		entity.useAction();
		entity.setPosition(newPos);
	}
	
}
