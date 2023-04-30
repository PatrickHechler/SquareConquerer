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
package de.hechler.patrick.games.squareconqerer.world.resource;

import java.awt.image.BufferedImage;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.addons.TheGameAddon;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;

/**
 * this resource type is used for ores which can be found in the world
 * 
 * @author Patrick Hechler
 */
public enum OreResourceType implements ImageableObj, Resource {
	
	/** no ore resource */
	NONE,
	/** gold ore */
	GOLD_ORE,
	/** iron ore */
	IRON_ORE,
	/** coal ore */
	COAL_ORE,
	
	;
	
	private static final String STR_COAL_ORE = Messages.get("OreResourceType.coal"); //$NON-NLS-1$
	private static final String STR_IRON_ORE = Messages.get("OreResourceType.iron"); //$NON-NLS-1$
	private static final String STR_GOLD_ORE = Messages.get("OreResourceType.gold"); //$NON-NLS-1$
	private static final String STR_NONE     = Messages.get("OreResourceType.none"); //$NON-NLS-1$
	
	/**
	 * this number is used to identify ore resources, when the {@link TheGameAddon} sends/receives them
	 */
	public static final int NUMBER = 0x6A58EEA4;
	
	private static final OreResourceType[] VALS = values();
	
	/**
	 * returns the resource with the given ordinal
	 * 
	 * @param ordinal the ordinal of the ore resource
	 * 
	 * @return the resource with the given ordinal
	 */
	public static OreResourceType of(int ordinal) {
		return VALS[ordinal];
	}
	
	/**
	 * returns the number of different ore resources (<code>null</code> not included)
	 * 
	 * @return the number of different ore resources (<code>null</code> not included)
	 */
	public static int count() {
		return VALS.length;
	}
	
	private volatile BufferedImage resource;
	
	/** {@inheritDoc} */
	@Override
	public BufferedImage image() { return this.resource; }
	
	/** {@inheritDoc} */
	@Override
	public void image(BufferedImage nval) { this.resource = nval; }
	
	/**
	 * returns a localized description of this ore resource
	 */
	@Override
	public String toString() {
		return switch (this) {
		case NONE -> STR_NONE;
		case GOLD_ORE -> STR_GOLD_ORE;
		case IRON_ORE -> STR_IRON_ORE;
		case COAL_ORE -> STR_COAL_ORE;
		default -> throw new AssertionError(name());
		};
	}
	
}
