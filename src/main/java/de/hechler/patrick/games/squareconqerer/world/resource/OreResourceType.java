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
public abstract class OreResourceType implements ImageableObj, Resource {
	
	private static final String STR_COAL_ORE = Messages.get("OreResourceType.coal"); //$NON-NLS-1$
	private static final String STR_IRON_ORE = Messages.get("OreResourceType.iron"); //$NON-NLS-1$
	private static final String STR_GOLD_ORE = Messages.get("OreResourceType.gold"); //$NON-NLS-1$
	private static final String STR_NONE     = Messages.get("OreResourceType.none"); //$NON-NLS-1$
	
	/** no ore resource */
	public static final OreResourceType NONE = new OreResourceType("NONE") { //$NON-NLS-1$
		
		@Override
		public String toString() { return STR_NONE; }
		
	};
	
	/** gold ore */
	public static final OreResourceType GOLD_ORE = new OreResourceType("GOLD_ORE") { //$NON-NLS-1$
		
		@Override
		public String toString() { return STR_GOLD_ORE; }
		
	};
	
	/** iron ore */
	public static final OreResourceType IRON_ORE = new OreResourceType("IRON_ORE") { //$NON-NLS-1$
		
		@Override
		public String toString() { return STR_IRON_ORE; }
		
	};
	
	/** coal ore */
	public static final OreResourceType COAL_ORE = new OreResourceType("COAL_ORE") { //$NON-NLS-1$
		
		@Override
		public String toString() { return STR_COAL_ORE; }
		
	};
	
	
	/**
	 * this number is used to identify ore resources, when the {@link TheGameAddon} sends/receives them
	 */
	public static final int NUMBER = 0x6A58EEA4;
	
	private static int ordinalCnt;
	
	private final String name;
	private final int    ordinal;
	
	public OreResourceType(String name) {
		if (VALS != null) throw new AssertionError("the class is already initilized! no new instances are supported");
		this.name    = name;
		this.ordinal = ordinalCnt++;
	}
	
	private static final OreResourceType[] VALS = { NONE, GOLD_ORE, IRON_ORE, COAL_ORE };
	
	public static OreResourceType[] values() {
		return VALS.clone();
	}
	
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
	
	@Override
	public String name() {
		return this.name;
	}
	
	@Override
	public int ordinal() {
		return this.ordinal;
	}
	
	private volatile BufferedImage resource;
	
	/** {@inheritDoc} */
	@Override
	public BufferedImage image() { return this.resource; }
	
	/** {@inheritDoc} */
	@Override
	public void image(BufferedImage nval) { this.resource = nval; }
	
	@Override
	public abstract String toString();
	
}
