package de.hechler.patrick.games.squareconqerer.world.entity;

import de.hechler.patrick.games.squareconqerer.User;

public abstract sealed class EntityImpl implements Entity permits BuildingImpl, UnitImpl {
	
	private final User usr;
	private int        maxlives;
	private int        lives;
	private int        viewRange;
	
	protected int x;
	protected int y;
	
	public EntityImpl(int x, int y, User usr, int maxlives, int lives, int viewRange) {
		if (maxlives <= 0) {
			throw new IllegalArgumentException("maxlives is not strict positive: " + maxlives);
		}
		if (maxlives < lives) {
			throw new IllegalArgumentException("maxlives is less than lives: maxlives=" + maxlives + " lives=" + lives);
		}
		if (viewRange < 0) {
			throw new IllegalArgumentException("the view range is negative");
		}
		if (usr == null) {
			throw new NullPointerException("every entity has an owner (even if it's the root)");
		}
		this.usr       = usr;
		this.maxlives  = maxlives;
		this.lives     = lives;
		this.viewRange = viewRange;
		this.x         = x;
		this.y         = y;
	}
	
	@Override
	public int x() { return x; }
	
	public int y() { return y; }
	
	@Override
	public User owner() { return usr; }
	
	@Override
	public int lives() { return lives; }
	
	@Override
	public int maxLives() { return maxlives; }
	
	@Override
	public int viewRange() { return viewRange; }
	
	protected void checkOwner(Entity e) {
		if (e.owner() != owner()) {
			throw new IllegalStateException("the entity does not belong to my owner");
		}
	}
	
}
