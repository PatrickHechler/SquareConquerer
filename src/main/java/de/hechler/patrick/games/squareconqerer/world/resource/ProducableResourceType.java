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

import de.hechler.patrick.games.squareconqerer.addons.TheGameAddon;

/**
 * this resource type can be produced
 * 
 * @author Patrick Hechler
 */
public enum ProducableResourceType implements Resource {
	
	/** produced/usable gold */
	GOLD,
	/** produced/usable iron */
	IRON,
	/** steel */
	STEEL,
	/** wood */
	WOOD,
	/** stone */
	STONE,
	/** glass */
	GLASS
	
	;
	
	/**
	 * this number is used to identify producable resources, when the {@link TheGameAddon} sends/receives them
	 */
	public static final int NUMBER = 0x461D8706;
	
	private static final ProducableResourceType[] VALS = values();
	
	/**
	 * returns the resource with the given ordinal
	 * 
	 * @param ordinal the ordinal of the producable resource
	 * 
	 * @return the resource with the given ordinal
	 */
	public static ProducableResourceType of(int ordinal) {
		return VALS[ordinal];
	}
	
	/**
	 * returns the number of different producable resources (<code>null</code> not included)
	 * 
	 * @return the number of different producable resources (<code>null</code> not included)
	 */
	public static int count() {
		return VALS.length;
	}
	
}
