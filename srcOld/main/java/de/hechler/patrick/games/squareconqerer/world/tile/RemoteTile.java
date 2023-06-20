//This file is part of the Square Conquerer Project
//DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//Copyright (C) 2023  Patrick Hechler
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published
//by the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with this program.  If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.squareconqerer.world.tile;

import de.hechler.patrick.games.squareconqerer.world.resource.OreResourceType;

/**
 * this class adds an {@link #created} time-stamp to the tile
 * 
 * @author Patrick Hechler
 */
public final class RemoteTile extends Tile {
	
	/**
	 * the time when this {@link RemoteTile} was created<br>
	 * 
	 * @see System#currentTimeMillis()
	 * @see #RemoteTile(GroundType,OreResourceType,boolean)
	 */
	public final long created;
	
	/**
	 * creates a new remote tile with the given ground, resource and initial visibility
	 * 
	 * @param ground   the ground
	 * @param resource the resource
	 * @param visible  the initial visibility
	 */
	public RemoteTile(GroundType ground, OreResourceType resource, boolean visible) {
		this(System.currentTimeMillis(), ground, resource, visible);
	}
	
	/**
	 * creates a new remote tile with the given ground, resource, initial visibility and time-stamp
	 * 
	 * @param time     the time-stamp when this tile thinks it creation was
	 * @param ground   the ground
	 * @param resource the resource
	 * @param visible  the initial visibility
	 */
	public RemoteTile(long time, GroundType ground, OreResourceType resource, boolean visible) {
		super(ground, resource, visible);
		this.created = time;
	}
	
}
