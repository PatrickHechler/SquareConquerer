// This file is part of the Square Conquerer Project
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// Copyright (C) 2023 Patrick Hechler
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.world.entity;

import java.awt.image.BufferedImage;

import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;

public abstract sealed class EntityImpl implements ImageableObj permits BuildingImpl, UnitImpl {
	// do not implement Entity, so that a enhanced switch does not need a default case
	
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
	
	/**
	 * returns the x value
	 * 
	 * @return the x value
	 * 
	 * @see Entity#x()
	 */
	public int x() { return this.x; }
	
	/**
	 * returns the y value
	 * 
	 * @return the y value
	 * 
	 * @see Entity#y()
	 */
	public int y() { return this.y; }
	
	public User owner() { return this.usr; }
	
	public int lives() { return this.lives; }
	
	public int maxLives() { return this.maxlives; }
	
	public int viewRange() { return this.viewRange; }
	
	protected void checkOwner(Entity e) {
		if (e.owner() != this.usr) {
			throw new IllegalStateException("the entity does not belong to my owner");
		}
	}
	
	private volatile BufferedImage resource;
	
	/** {@inheritDoc} */
	@Override
	public BufferedImage image() { return this.resource; }
	
	/** {@inheritDoc} */
	@Override
	public void image(BufferedImage nval) { this.resource = nval; }
	
	/** {@inheritDoc} */
	@Override
	public String name() { return getClass().getSimpleName(); }
	
}
