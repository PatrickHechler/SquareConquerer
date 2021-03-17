package de.hechler.patrick.sc.objects;

import java.util.Set;

import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.enums.Type;
import de.hechler.patrick.sc.interfaces.Position;
import de.hechler.patrick.sc.interfaces.UnmovableEntity;

public class Building implements UnmovableEntity {
	
	public final UnchangeablePosition pos;
	public final Type                 type;
	private final Set <Grounds>       ceo;
	
	protected int totalActions;
	protected int remainingActions;
	
	
	
	public Building(Position pos, Set <Grounds> canExsistOn, Type type, int totalActions) {
		this(new UnchangeablePosition(pos), canExsistOn, type, totalActions);
	}
	
	public Building(UnchangeablePosition pos, Set <Grounds> canExsistOn, Type type, int totalActions) {
		this(pos, canExsistOn, type, totalActions, 0);
	}
	
	public Building(Position pos, Set <Grounds> canExsistOn, Type type, int totalActions, int remainingActions) {
		this(new UnchangeablePosition(pos), canExsistOn, type, totalActions, remainingActions);
	}
	
	public Building(UnchangeablePosition pos, Set <Grounds> canExsistOn, Type type, int totalActions, int remainingActions) {
		this.pos = pos;
		this.ceo = canExsistOn;
		this.type = type;
		this.totalActions = totalActions;
		this.remainingActions = remainingActions;
	}
	
	
	
	@Override
	public Position position() {
		return pos;
	}
	
	@Override
	public Set <Grounds> canExsitOn() {
		return ceo;
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
	public void useAction() throws IllegalStateException {
		if (remainingActions <= 0) throw new IllegalStateException("no more actions left!");
		remainingActions -- ;
	}
	
	@Override
	public void newTurn() {
		remainingActions = totalActions;
	}
	
	@Override
	public Type type() {
		return type;
	}
	
	@Override
	public final boolean isMovable() {
		return false;
	}
	
}
