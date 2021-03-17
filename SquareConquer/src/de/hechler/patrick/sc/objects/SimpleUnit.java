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


public class SimpleUnit implements MovableEntity {
	
	private final Set <Grounds> canExsist;
	
	private Position  pos;
	
	private int totalActions;
	private int remainingActions;
	
	
	public SimpleUnit(Position pos, Collection <Grounds> canExsistOn, int totalActions) {
		this.pos = new NicePosition(pos);
		EnumSet <Grounds> ceo = new EnumSet <>(Grounds.class);
		ceo.addAll(canExsistOn);
		this.canExsist = Collections.unmodifiableSet(ceo);
		this.totalActions = totalActions;
	}
	
	public SimpleUnit(int x, int y, Collection <Grounds> canExsistOn, int totalActions, int remainingActions) {
		this.pos = new NicePosition(x, y);
		EnumSet <Grounds> ceo = new EnumSet <>(Grounds.class);
		ceo.addAll(canExsistOn);
		this.canExsist = Collections.unmodifiableSet(ceo);
		this.totalActions = totalActions;
		this.remainingActions = remainingActions;
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
	public Type type() {
		return Type.simple;
	}
	
}
