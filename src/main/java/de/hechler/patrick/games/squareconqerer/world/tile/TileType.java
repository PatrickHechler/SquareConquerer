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

import java.awt.image.BufferedImage;

import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;

public enum TileType implements ImageableObj {
	
	/**
	 * this type is used for tiles which are not yet visible to the player
	 */
	NOT_EXPLORED,
	
	WATER_DEEP, WATER_NORMAL,
	
	SAND, SAND_HILL,
	
	GRASS, GRASS_HILL,
	
	FOREST, FOREST_HILL,
	
	SWAMP, SWAMP_HILL,
	
	MOUNTAIN,
	
	;
	
	private static final TileType[] VALS = values();
	
	public static TileType of(int oridinal) {
		return VALS[oridinal];
	}
	
	public static int count() {
		return VALS.length;
	}
	
	private volatile BufferedImage resource;
	
	@Override
	public BufferedImage resource() { return resource; }
	
	@Override
	public void resource(BufferedImage nval) { this.resource = nval; }
	
	/**
	 * returns <code>true</code> if this is a ocean/deep water tile
	 * <p>
	 * only the following tile is an ocean tile:
	 * <ul>
	 * <li>{@link #WATER_DEEP}</li>
	 * </ul>
	 * 
	 * @return <code>true</code> if this is a water tile
	 */
	public boolean isOcean() {
		return switch (this) {
		case WATER_DEEP -> true;
		case FOREST, FOREST_HILL, GRASS, GRASS_HILL, MOUNTAIN, NOT_EXPLORED, SAND, SAND_HILL, SWAMP, SWAMP_HILL, WATER_NORMAL -> false;
		};
	}
	
	/**
	 * returns <code>true</code> if this is a water tile
	 * <p>
	 * only the following tiles are water tiles:
	 * <ul>
	 * <li>{@link #WATER_DEEP}</li>
	 * <li>{@link #WATER_NORMAL}</li>
	 * </ul>
	 * 
	 * @return <code>true</code> if this is a water tile
	 */
	public boolean isWater() {
		return switch (this) {
		case WATER_DEEP, WATER_NORMAL -> true;
		case FOREST, FOREST_HILL, GRASS, GRASS_HILL, MOUNTAIN, NOT_EXPLORED, SAND, SAND_HILL, SWAMP, SWAMP_HILL -> false;
		};
	}
	
	/**
	 * returns <code>true</code> if this is a land tile
	 * <p>
	 * a land tile is a tile, which is no {@link #isWater() water} tile
	 * 
	 * @return <code>true</code> if this is a land tile
	 */
	public boolean isLand() {
		return switch (this) {
		case WATER_DEEP, WATER_NORMAL, NOT_EXPLORED -> false;
		case FOREST, FOREST_HILL, GRASS, GRASS_HILL, MOUNTAIN, SAND, SAND_HILL, SWAMP, SWAMP_HILL -> true;
		};
	}
	
	/**
	 * returns <code>true</code> if this is a flat tile
	 * <p>
	 * a flat tile is a tile, which is <b>no</b> {@link #isHill() hill}, <b>no</b>
	 * {@link #isMountain() mountain} and
	 * <b>no</b> {@link #isWater() water}
	 * 
	 * @return <code>true</code> if this is a flat tile
	 */
	public boolean isFlat() {
		return switch (this) {
		case SAND, GRASS, FOREST, SWAMP -> true;
		case FOREST_HILL, GRASS_HILL, MOUNTAIN, NOT_EXPLORED, SAND_HILL, SWAMP_HILL, WATER_DEEP, WATER_NORMAL -> false;
		};
	}
	
	/**
	 * returns <code>true</code> if this is a hill tile
	 * <p>
	 * only the following tiles are hill tiles:
	 * <ul>
	 * <li>{@link #SAND_HILL}</li>
	 * <li>{@link #GRASS_HILL}</li>
	 * <li>{@link #FOREST_HILL}</li>
	 * <li>{@link #SWAMP_HILL}</li>
	 * </ul>
	 * 
	 * @return <code>true</code> if this is a hill tile
	 */
	public boolean isHill() {
		return switch (this) {
		case SAND_HILL, GRASS_HILL, FOREST_HILL, SWAMP_HILL -> true;
		case FOREST, GRASS, MOUNTAIN, NOT_EXPLORED, SAND, SWAMP, WATER_DEEP, WATER_NORMAL -> false;
		};
	}
	
	/**
	 * returns <code>true</code> if this is a mountain tile
	 * <p>
	 * only {@link #MOUNTAIN} is a mountain tile
	 * 
	 * @return <code>true</code> if this is a mountain tile
	 */
	public boolean isMountain() { return this == MOUNTAIN; }
	
	/**
	 * returns <code>true</code> if this is a not-flat tile
	 * <p>
	 * a not-flat tile is a tile, which is a {@link #isHill() hill} or a
	 * {@link #isMountain() mountain} tile
	 * <p>
	 * note that {@link #isWater() water} tiles are <b>no</b> {@link #isFlat() flat}
	 * tiles and also <b>no</b>
	 * {@link #isNotFlat() not-flat}
	 * 
	 * @return <code>true</code> if this is a mountain tile
	 */
	public boolean isNotFlat() { return isHill() || isMountain(); }
	
	/**
	 * returns <code>true</code> if this tile is a sand tile
	 * <p>
	 * only {@link #SAND} and {@link #SAND_HILL} tiles are sand tiles
	 * 
	 * @return <code>true</code> if this tile is a sand tile
	 */
	public boolean isSand() {
		return switch (this) {
		case SAND, SAND_HILL -> true;
		case FOREST, FOREST_HILL, GRASS, GRASS_HILL, MOUNTAIN, NOT_EXPLORED, SWAMP, SWAMP_HILL, WATER_DEEP, WATER_NORMAL -> false;
		};
	}
	
	/**
	 * returns <code>true</code> if this tile is a grass tile
	 * <p>
	 * only {@link #GRASS} and {@link #GRASS_HILL} tiles are grass tiles
	 * 
	 * @return <code>true</code> if this tile is a grass tile
	 */
	public boolean isGrass() {
		return switch (this) {
		case GRASS, GRASS_HILL -> true;
		case FOREST, FOREST_HILL, MOUNTAIN, NOT_EXPLORED, SAND, SAND_HILL, SWAMP, SWAMP_HILL, WATER_DEEP, WATER_NORMAL -> false;
		};
	}
	
	/**
	 * returns <code>true</code> if this tile is a forest tile
	 * <p>
	 * only {@link #FOREST} and {@link #FOREST_HILL} tiles are forest tiles
	 * 
	 * @return <code>true</code> if this tile is a forest tile
	 */
	public boolean isForest() {
		return switch (this) {
		case FOREST, FOREST_HILL -> true;
		case GRASS, GRASS_HILL, MOUNTAIN, NOT_EXPLORED, SAND, SAND_HILL, SWAMP, SWAMP_HILL, WATER_DEEP, WATER_NORMAL -> false;
		};
	}
	
	/**
	 * returns <code>true</code> if this tile is a swamp tile
	 * <p>
	 * only {@link #SWAMP} and {@link #SWAMP_HILL} tiles are swamp tiles
	 * 
	 * @return <code>true</code> if this tile is a swamp tile
	 */
	public boolean isSwamp() {
		return switch (this) {
		case SWAMP, SWAMP_HILL -> true;
		case FOREST, FOREST_HILL, GRASS, GRASS_HILL, MOUNTAIN, NOT_EXPLORED, SAND, SAND_HILL, WATER_DEEP, WATER_NORMAL -> false;
		};
	}
	
	public TileType addNormal(boolean fail) {
		return switch (this) {
		case WATER_DEEP -> WATER_NORMAL;
		case FOREST_HILL -> FOREST;
		case GRASS_HILL -> GRASS;
		case SAND_HILL -> SAND;
		case SWAMP_HILL -> SWAMP;
		case NOT_EXPLORED, FOREST, MOUNTAIN, SAND, SWAMP, WATER_NORMAL, GRASS -> {
			if (fail) throw new IllegalStateException("can not add normal to this type (" + name() + ")");
			else yield this;
		}
		};
	}
	
	public TileType addDeep(boolean fail) {
		return switch (this) {
		case WATER_NORMAL -> WATER_DEEP;
		case GRASS, FOREST, SWAMP, FOREST_HILL, GRASS_HILL, MOUNTAIN, NOT_EXPLORED, SAND_HILL, SWAMP_HILL, WATER_DEEP, SAND -> {
			if (fail) throw new IllegalStateException("can not add deep to this type (" + name() + ")");
			else yield this;
		}
		};
	}
	
	public TileType addHill(boolean fail) {
		return switch (this) {
		case SAND -> SAND_HILL;
		case GRASS -> GRASS_HILL;
		case FOREST -> FOREST_HILL;
		case SWAMP -> SWAMP_HILL;
		case FOREST_HILL, GRASS_HILL, MOUNTAIN, NOT_EXPLORED, SAND_HILL, SWAMP_HILL, WATER_DEEP, WATER_NORMAL -> {
			if (fail) throw new IllegalStateException("can not add hills to this type (" + name() + ")");
			else yield this;
		}
		};
	}
	
	@Override
	public String toString() {
		return switch (this) {
		case NOT_EXPLORED -> "not yet explored";
		case WATER_DEEP -> "Deep Water/Ocean";
		case WATER_NORMAL -> "Water";
		case SAND -> "Sand";
		case SAND_HILL -> "Sand Hills";
		case GRASS -> "Grassland";
		case GRASS_HILL -> "Grassland with Hills";
		case FOREST -> "Forest";
		case FOREST_HILL -> "Forest Hills";
		case SWAMP -> "Swampland";
		case SWAMP_HILL -> "Swamp Hills";
		case MOUNTAIN -> "Mountains";
		default -> throw new AssertionError(name());
		};
	}
	
}
