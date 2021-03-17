package de.hechler.patrick.sc.objects;

import de.hechler.patrick.sc.enums.Grounds;
import de.hechler.patrick.sc.interfaces.Entity;
import de.hechler.patrick.sc.interfaces.Field;
import de.hechler.patrick.sc.interfaces.Position;


public class FieldImpl implements Field {
	
	public final UnchangeablePosition pos;
	public final Grounds              ground;
	
	private Entity entity;
	
	
	
	public FieldImpl(int x, int y, Grounds ground) {
		this.pos = new UnchangeablePosition(x, y);
		this.ground = ground;
	}
	
	public FieldImpl(UnchangeablePosition pos, Grounds ground) {
		this.pos = pos;
		this.ground = ground;
	}
	
	public FieldImpl(Position pos, Grounds ground) {
		this.pos = new UnchangeablePosition(pos);
		this.ground = ground;
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
		this.entity = entity;
	}
	
}
