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
import java.util.ArrayList;
import java.util.List;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.addons.TheGameAddon;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;

/**
 * this resource type is used for ores which can be found in the world
 * 
 * @author Patrick Hechler
 */
public class OreResourceType implements ImageableObj, Resource {
	
	private static final String CLASS_ALREADY_INITILIZED_NO_NEW_SUPPORTED             = Messages.getString("OreResourceType.constants-already-initilized"); //$NON-NLS-1$
	private static final String STR_COAL_ORE                                          = Messages.getString("OreResourceType.coal");                         //$NON-NLS-1$
	private static final String STR_IRON_ORE                                          = Messages.getString("OreResourceType.iron");                         //$NON-NLS-1$
	private static final String STR_GOLD_ORE                                          = Messages.getString("OreResourceType.gold");                         //$NON-NLS-1$
	private static final String STR_NONE                                              = Messages.getString("OreResourceType.none");                         //$NON-NLS-1$
	
	private static List<OreResourceType> values = new ArrayList<>();
	
	/** no ore resource */
	public static final OreResourceType NONE     = new OreResourceType("NONE", STR_NONE);         //$NON-NLS-1$
	/** gold ore */
	public static final OreResourceType GOLD_ORE = new OreResourceType("GOLD_ORE", STR_GOLD_ORE); //$NON-NLS-1$
	/** iron ore */
	public static final OreResourceType IRON_ORE = new OreResourceType("IRON_ORE", STR_IRON_ORE); //$NON-NLS-1$
	/** coal ore */
	public static final OreResourceType COAL_ORE = new OreResourceType("COAL_ORE", STR_COAL_ORE); //$NON-NLS-1$
	
	
	/**
	 * this number is used to identify ore resources, when the {@link TheGameAddon} sends/receives them
	 */
	public static final int NUMBER = 0x6A58EEA4;
	
	private final String name;
	private final String localName;
	private final int    ordinal;
	
	/**
	 * creates a new ore resource with the given name and localized name
	 * 
	 * @param name      the name
	 * @param localName the localized name
	 */
	public OreResourceType(String name, String localName) {
		if (VALS != null) throw new AssertionError(CLASS_ALREADY_INITILIZED_NO_NEW_SUPPORTED);
		this.name      = name;
		this.localName = localName;
		this.ordinal   = values.size();
		values.add(this);
	}
	
	private static final OreResourceType[] VALS;
	
	static {
		VALS   = values.toArray(new OreResourceType[values.size()]);
		values = null;
	}
	
	/**
	 * Returns the elements of this class
	 * 
	 * @return an array containing the values comprising this class represented in the order they're created (the {@link #ordinal()} values)
	 */
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
	
	/** {@inheritDoc} */
	@Override
	public String name() {
		return this.name;
	}
	
	/** {@inheritDoc} */
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
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.localName;
	}
	
}
