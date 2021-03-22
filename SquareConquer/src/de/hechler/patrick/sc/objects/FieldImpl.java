package de.hechler.patrick.sc.objects;

import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.Position;


public class FieldImpl implements Field {
	
	public final UnchangeablePosition pos;
	public final Grounds              ground;
	public final boolean              isMemory;
	
	private Entity entity;
	
	
	
	public FieldImpl(int x, int y, Grounds ground, boolean isMemory) {
		this(new UnchangeablePosition(x, y), ground, isMemory);
	}
	
	public FieldImpl(UnchangeablePosition pos, Grounds ground, boolean isMemory) {
		this.pos = pos;
		this.ground = ground;
		this.isMemory = isMemory;
	}
	
	public FieldImpl(Position pos, Grounds ground, boolean isMemory) {
		this(new UnchangeablePosition(pos), ground, isMemory);
	}
	
	public FieldImpl(int x, int y, Grounds ground) {
		this(new UnchangeablePosition(x, y), ground);
	}
	
	public FieldImpl(UnchangeablePosition pos, Grounds ground) {
		this(pos, ground, false);
	}
	
	public FieldImpl(Position pos, Grounds ground) {
		this(new UnchangeablePosition(pos), ground);
	}
	
	
	@Override
	public boolean isMemory() {
		return isMemory;
	}
	
	@Override
	public Position position() {
		return pos;
	}
	
	@Override
	public int getXPos() {
		return pos.x;
	}
	
	@Override
	public int getYPos() {
		return pos.y;
	}
	
	@Override
	public Grounds ground() {
		return ground;
	}
	
	@Override
	public boolean hasEntity() {
		return entity != null;
	}
	
	@Override
	public Entity getEntity() {
		return entity;
	}
	
	@Override
	public void setEntity(Entity entity) {
		if (isMemory) throw new IllegalStateException("I am an memory!");
		this.entity = entity;
	}
	
}
