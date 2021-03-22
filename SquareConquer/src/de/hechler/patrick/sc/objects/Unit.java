package de.hechler.patrick.sc.objects;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import de.hechler.patrick.sc.enums.Direction;
import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.interfaces.MovableEntity;
import de.hechler.patrick.sc.interfaces.Position;
import de.hechler.patrick.sc.utils.objects.EnumSet;


public class Unit implements MovableEntity {
	
	public final Set <Grounds> canExsist;
	public final int           owner;
	
	public final Type type;
	
	protected Position pos;
	
	protected int health;
	protected int totalActions;
	protected int remainingActions;
	protected int sight;
	
	
	public Unit(int owner, Position pos, Collection <Grounds> canExsistOn, int totalActions, Type type, int sight) {
		this(owner, new NicePosition(pos), canExsistOn, totalActions, 0, type, sight);
	}
	
	public Unit(int owner, int x, int y, Collection <Grounds> canExsistOn, int totalActions, int remainingActions, Type type, int sight) {
		this(owner, new NicePosition(x, y), canExsistOn, totalActions, remainingActions, type, sight);
	}
	
	protected Unit(int owner, NicePosition pos, Collection <Grounds> canExsistOn, int totalActions, int remainingActions, Type type, int sight) {
		EnumSet <Grounds> ceo = new EnumSet <>(Grounds.class);
		ceo.addAll(canExsistOn);
		canExsist = Collections.unmodifiableSet(ceo);
		this.pos = pos;
		this.totalActions = totalActions;
		this.remainingActions = remainingActions;
		this.type = type;
		this.sight = sight;
		this.owner = owner;
		this.health = 20;
	}
	
	public Unit(MovableEntity copyExeptType, Type type) {
		this.type = type;
		this.pos = copyExeptType.position();
		this.totalActions = copyExeptType.totalActions();
		this.remainingActions = copyExeptType.remainingActions();
		EnumSet <Grounds> ceo = new EnumSet <>(Grounds.class);
		ceo.addAll(copyExeptType.canExsitOn());
		this.canExsist = Collections.unmodifiableSet(ceo);
		this.owner = copyExeptType.owner();
	}
	
	@Override
	public int owner() {
		return owner;
	}
	
	@Override
	public Position position() {
		return new PositionListener(pos);
	}
	
	@Override
	public Set <Grounds> canExsitOn() {
		return canExsist;
	}
	
	@Override
	public int remainingActions() {
		return remainingActions;
	}
	
	@Override
	public int totalActions() {
		return totalActions;
	}
	
	@Override
	public void newTurn() {
		remainingActions = totalActions;
	}
	
	@Override
	public void setPosition(Position pos) {
		this.pos = new NicePosition(pos);
	}
	
	@Override
	public void move(Position dest) {
		if (remainingActions <= 0) {
			throw new IllegalStateException("no more actions: total=" + totalActions + " remaining=" + remainingActions);
		}
		remainingActions -- ;
		pos = dest;
	}
	
	@Override
	public void move(Direction dir) {
		if (remainingActions <= 0) {
			throw new IllegalStateException("no more actions: total=" + totalActions + " remaining=" + remainingActions);
		}
		remainingActions -- ;
		pos.move(dir);
		
	}
	
	@Override
	public void useAction() throws IllegalStateException {
		if (remainingActions <= 0) throw new IllegalStateException("no more actions left!");
		remainingActions -- ;
	}
	
	@Override
	public Type type() {
		return type;
	}
	
	@Override
	public final boolean isMovable() {
		return true;
	}
	
	@Override
	public int sight() {
		return sight;
	}
	
	@Override
	public int health() {
		return health;
	}
	
	@Override
	public boolean getDamage(int damagePoints) {
		health -= damagePoints;
		return health > 0;
	}
	
	@Override
	public void getHealing(int healPoints) {
		health += healPoints;
	}
	
}
