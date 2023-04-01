package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.User;

public sealed class EntityImpl implements Entity permits BuildingImpl, UnitImpl {
	
	private final User usr;
	private int        maxlives;
	private int        lives;
	
	protected int x;
	protected int y;
	
	public EntityImpl(int x, int y, User usr, int maxlives) {
		if (maxlives <= 0) {
			throw new IllegalArgumentException("maxlives is not strict positive: " + maxlives);
		}
		this.usr      = usr;
		this.maxlives = maxlives;
		this.lives    = maxlives;
		this.x        = x;
		this.y        = y;
	}
	
	@Override
	public int x() { return x; }
	
	public int y() { return y; }
	
	@Override
	public User owner() {
		return usr;
	}
	
	@Override
	public int lives() {
		return lives;
	}
	
	@Override
	public int maxLives() {
		return maxlives;
	}
	
	protected void checkOwner(Entity e) {
		if (e.owner() != owner()) {
			throw new IllegalStateException("the entity does not belong to my owner");
		}
	}
	
}
