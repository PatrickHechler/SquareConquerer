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
package de.hechler.patrick.games.squareconqerer.world.resource;

import java.awt.image.BufferedImage;

import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;

public enum OreResourceType implements ImageableObj, Resource {
	
	NONE,
	
	GOLD_ORE,
	
	IRON_ORE,
	
	COAL_ORE,
	
	;
	
	public static final int NUMBER = 0x6A58EEA4;
	
	private static final OreResourceType[] VALS = values();
	
	public static OreResourceType of(int oridinal) {
		return VALS[oridinal];
	}
	
	public static int count() {
		return VALS.length;
	}
	
	private volatile BufferedImage resource;
	
	@Override public BufferedImage resource() { return resource; }
	
	@Override public void resource(BufferedImage nval) { this.resource = nval; }
	
	@Override
	public String toString() {
		return switch (this) {
		case NONE -> "none";
		case GOLD_ORE -> "Gold Ore";
		case IRON_ORE -> "Iron Ore";
		case COAL_ORE -> "Coal Ore";
		default -> throw new AssertionError(name());
		};
	}
	
}
