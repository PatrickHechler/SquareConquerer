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

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.User;
import de.hechler.patrick.games.squareconqerer.exceptions.TurnExecutionException;
import de.hechler.patrick.games.squareconqerer.exceptions.enums.ErrorType;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;

/**
 * this class is used to help implementing the {@link Entity} interface.<br>
 * if you want to use this class for your own {@link EntityImpl}, see {@link MyUnit} and {@link MyBuild}
 * 
 * @author Patrick Hechler
 */
public abstract sealed class EntityImpl implements ImageableObj permits BuildingImpl, UnitImpl {
	// do not implement Entity, so that a enhanced switch does not need a default case
	
	private static final String EVERY_ENTITY_HAS_AN_OWNER = Messages.get("EntityImpl.no-owner");                        //$NON-NLS-1$
	private static final String NEGATIVE_VIEW_RANGE       = Messages.get("EntityImpl.negative-view-range");             //$NON-NLS-1$
	private static final String LIVES_IS                  = Messages.get("EntityImpl.lives-is");                        //$NON-NLS-1$
	private static final String MAXLIVES_LESS_LIVES       = Messages.get("EntityImpl.maxlives-less-lives-maxlives-is"); //$NON-NLS-1$
	private static final String LIVES_NOT_STRICT_POSITIVE = Messages.get("EntityImpl.lives-not-strict-posibive");       //$NON-NLS-1$
	
	/** the {@link #owner()} of this entity */
	protected final User usr;
	/** the {@link #maxLives() maximum lives} amount of this entity */
	protected final int  maxlives;
	/** the current {@link #lives()} of this entity */
	protected int        lives;
	/** the current {@link #viewRange()} of this entity */
	protected int        viewRange;
	/** the {@link Entity#x()} coordinate of the entity */
	protected int        x;
	/** the {@link Entity#y()} coordinate of the entity */
	protected int        y;
	
	/**
	 * creates a new {@link EntityImpl} with the given values
	 * 
	 * @param x         the {@link #x} coordinate
	 * @param y         the {@link #y} coordinate
	 * @param usr       the {@link #owner()}
	 * @param maxlives  the {@link #maxLives()}
	 * @param lives     the {@link #lives}
	 * @param viewRange the {@link #viewRange}
	 */
	public EntityImpl(int x, int y, User usr, int maxlives, int lives, int viewRange) {
		if (lives <= 0) {
			throw new IllegalArgumentException(LIVES_NOT_STRICT_POSITIVE + maxlives);
		}
		if (maxlives < lives) {
			throw new IllegalArgumentException(MAXLIVES_LESS_LIVES + maxlives + LIVES_IS + lives);
		}
		if (viewRange < 0) {
			throw new IllegalArgumentException(NEGATIVE_VIEW_RANGE);
		}
		if (usr == null) {
			throw new NullPointerException(EVERY_ENTITY_HAS_AN_OWNER);
		}
		this.usr       = usr;
		this.maxlives  = maxlives;
		this.lives     = lives;
		this.viewRange = viewRange;
		this.x         = x;
		this.y         = y;
	}
	
	/**
	 * returns the {@link #x} value
	 * 
	 * @return the {@link #x} value
	 * 
	 * @see Entity#x()
	 */
	public int x() { return this.x; }
	
	/**
	 * returns the {@link #y} value
	 * 
	 * @return the {@link #y} value
	 * 
	 * @see Entity#y()
	 */
	public int y() { return this.y; }
	
	/**
	 * returns the {@link #usr}
	 * 
	 * @return the {@link #usr}
	 */
	public User owner() { return this.usr; }
	
	/**
	 * returns the {@link #lives}
	 * 
	 * @return the {@link #lives}
	 */
	public int lives() { return this.lives; }
	
	/**
	 * returns the {@link #maxlives}
	 * 
	 * @return the {@link #maxlives}
	 */
	public int maxLives() { return this.maxlives; }
	
	/**
	 * returns the {@link #viewRange}
	 * 
	 * @return the {@link #viewRange}
	 */
	public int viewRange() { return this.viewRange; }
	
	/**
	 * checks that the given entity has the same {@link Entity#owner()} as this entity
	 * 
	 * @param e the other entity
	 * 
	 * @throws TurnExecutionException if the given entity has a different owner
	 */
	protected void checkOwner(Entity e) throws TurnExecutionException {
		if (e.owner() != this.usr) {
			throw new TurnExecutionException(ErrorType.INVALID_TURN);
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
