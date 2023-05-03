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
package de.hechler.patrick.games.squareconqerer.world.tile;

import java.awt.image.BufferedImage;
import java.text.Format;

import de.hechler.patrick.games.squareconqerer.Messages;
import de.hechler.patrick.games.squareconqerer.world.stuff.ImageableObj;

/**
 * this class is used to describe what type of ground a tile has
 * 
 * @author Patrick Hechler
 */
public enum GroundType implements ImageableObj {
	
	/** this type is used for tiles which are not yet visible to the player */
	NOT_EXPLORED,
	
	/** deep water or ocean tiles */
	WATER_DEEP,
	/** normal water tiles */
	WATER_NORMAL,
	
	/** sand tiles */
	SAND,
	/** sand tiles with hills */
	SAND_HILL,
	
	/** grass tiles */
	GRASS,
	/** grass tiles with hills */
	GRASS_HILL,
	
	/** forest tiles */
	FOREST,
	/** forest tiles with hills */
	FOREST_HILL,
	
	/** swamp tiles */
	SWAMP,
	/** swamp tiles with hills */
	SWAMP_HILL,
	
	/** mountain tiles */
	MOUNTAIN,
	
	;
	
	private static final String STR_MOUNTAIN     = Messages.getString("GroundType.str-mountain");    //$NON-NLS-1$
	private static final String STR_SWAMP_HILL   = Messages.getString("GroundType.str-swamp-hill");  //$NON-NLS-1$
	private static final String STR_SWAMP        = Messages.getString("GroundType.str-swamp");       //$NON-NLS-1$
	private static final String STR_FOREST_HILL  = Messages.getString("GroundType.str-forest-hill"); //$NON-NLS-1$
	private static final String STR_FOREST       = Messages.getString("GroundType.str-forest");      //$NON-NLS-1$
	private static final String STR_GRASS_HILL   = Messages.getString("GroundType.str-grass-hill");  //$NON-NLS-1$
	private static final String STR_GRASS        = Messages.getString("GroundType.str-grass");       //$NON-NLS-1$
	private static final String STR_SAND_HILL    = Messages.getString("GroundType.str-sand-hill");   //$NON-NLS-1$
	private static final String STR_SAND         = Messages.getString("GroundType.str-sand");        //$NON-NLS-1$
	private static final String STR_WATER        = Messages.getString("GroundType.str-water");       //$NON-NLS-1$
	private static final String STR_WATER_DEEP   = Messages.getString("GroundType.str-water-deep");  //$NON-NLS-1$
	private static final String STR_NOT_EXPLORED = Messages.getString("GroundType.str-not-explred"); //$NON-NLS-1$
	private static final Format NOT_ADD_HILLS    = Messages.getFormat("GroundType.not-add-hill");    //$NON-NLS-1$
	private static final Format NOT_ADD_DEEP     = Messages.getFormat("GroundType.not-add-deep");    //$NON-NLS-1$
	private static final Format NOT_ADD_NORMAL   = Messages.getFormat("GroundType.not-add-normal");  //$NON-NLS-1$
	
	private static final GroundType[] VALS = values();
	
	/**
	 * returns the ground type of the given ordinal
	 * 
	 * @param ordinal the ordinal value
	 * 
	 * @return the ground type with the given ordinal
	 */
	public static GroundType of(int ordinal) {
		return VALS[ordinal];
	}
	
	/**
	 * returns the number of different ground types
	 * 
	 * @return the number of different ground types
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
	 * returns <code>true</code> if this is a deep tile
	 * <p>
	 * only the following tile is an deep tile:
	 * <ul>
	 * <li>{@link #WATER_DEEP}</li>
	 * </ul>
	 * 
	 * @return <code>true</code> if this is a water tile
	 */
	public boolean isDeep() {
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
	 * a flat tile is a tile, which is <b>no</b> {@link #isHill() hill}, <b>no</b> {@link #isMountain() mountain} and <b>no</b> {@link #isWater() water}
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
	 * a not-flat tile is a tile, which is a {@link #isHill() hill} or a {@link #isMountain() mountain} tile
	 * <p>
	 * note that {@link #isWater() water} tiles are <b>no</b> {@link #isFlat() flat} tiles and also <b>no</b> {@link #isNotFlat() not-flat}
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
	
	/**
	 * returns the normalized version of this ground
	 * <ul>
	 * <li>if this ground is deep ({@link #isDeep()}), the non deep version will be returned</li>
	 * <li>if this ground has hills ({@link #isHill()}), the version without hills will be returned ({@link #isFlat()})</li>
	 * <li>if <code>failIfAlreadyNormal</code> is <code>false</code> and this ground is already normal no action is taken and this ground is returned</li>
	 * <li>if <code>failIfAlreadyNormal</code> is <code>true</code> and this ground is already normal an {@link IllegalStateException} is thrown</li>
	 * <li>if <code>failIfNoNormal</code> is <code>false</code> and this ground does not support normal no action is taken and this ground is returned</li>
	 * <li>if <code>failIfNoNormal</code> is <code>true</code> and this ground does not support normal an {@link IllegalStateException} is thrown</li>
	 * </ul>
	 * 
	 * @param failIfAlreadyNormal if this operation should fail if the ground is already normalized
	 * @param failIfNoNormal      if this operation should fail if the ground does not have a normal version
	 * 
	 * @return the normalized type
	 */
	public GroundType addNormal(boolean failIfAlreadyNormal, boolean failIfNoNormal) {
		return switch (this) {
		case WATER_DEEP -> WATER_NORMAL;
		case FOREST_HILL -> FOREST;
		case GRASS_HILL -> GRASS;
		case SAND_HILL -> SAND;
		case SWAMP_HILL -> SWAMP;
		case FOREST, MOUNTAIN, SAND, SWAMP, WATER_NORMAL, GRASS -> {
			if (failIfAlreadyNormal) throw new IllegalStateException(Messages.format(NOT_ADD_NORMAL, toString()));
			yield this;
		}
		case NOT_EXPLORED -> {
			if (failIfNoNormal) throw new IllegalStateException(Messages.format(NOT_ADD_NORMAL, toString()));
			yield this;
		}
		};
	}
	
	/**
	 * returns the {@link #isDeep() deep} version of this ground
	 * <ul>
	 * <li>if <code>failAlreadyDeep</code> is <code>false</code> and this ground is already {@link #isDeep() deep} no action is taken and this ground is
	 * returned</li>
	 * <li>if <code>failAlreadyDeep</code> is <code>true</code> and this ground is already {@link #isDeep() deep} an {@link IllegalStateException} is thrown</li>
	 * <li>if <code>failNoDeep</code> is <code>false</code> and this ground does not support {@link #isDeep() deep} no action is taken and this ground is
	 * returned</li>
	 * <li>if <code>failNoDeep</code> is <code>true</code> and this ground does not support {@link #isDeep() deep} an {@link IllegalStateException} is thrown</li>
	 * </ul>
	 * 
	 * @param failAlreadyDeep if this operation should fail if the ground is already deep
	 * @param failNoDeep      if this operation should fail if the ground has no deep version
	 * 
	 * @return the normalized type
	 */
	public GroundType addDeep(boolean failAlreadyDeep, boolean failNoDeep) {
		return switch (this) {
		case WATER_NORMAL -> WATER_DEEP;
		case WATER_DEEP -> {
			if (failAlreadyDeep) throw new IllegalStateException(Messages.format(NOT_ADD_DEEP, toString()));
			yield this;
		}
		case GRASS, FOREST, SWAMP, FOREST_HILL, GRASS_HILL, MOUNTAIN, NOT_EXPLORED, SAND_HILL, SWAMP_HILL, SAND -> {
			if (failNoDeep) throw new IllegalStateException(Messages.format(NOT_ADD_DEEP, toString()));
			yield this;
		}
		};
	}
	
	/**
	 * returns the {@link #isHill() hill} version of this ground
	 * <ul>
	 * <li>if <code>failAlreadyHill</code> is <code>false</code> and this ground is already {@link #isHill() hill} no action is taken and this ground is
	 * returned</li>
	 * <li>if <code>failAlreadyHill</code> is <code>true</code> and this ground is already {@link #isHill() hill} an {@link IllegalStateException} is thrown</li>
	 * <li>if <code>failNoHill</code> is <code>false</code> and this ground does not support {@link #isHill() hill} no action is taken and this ground is
	 * returned</li>
	 * <li>if <code>failNoHill</code> is <code>true</code> and this ground does not support {@link #isHill() hill} an {@link IllegalStateException} is thrown</li>
	 * </ul>
	 * 
	 * @param failAlreadyHill if this operation should fail if the ground is already with {@link #isHill() hills}
	 * @param failNoHill      if this operation should fail if the ground has no {@link #isHill() hill} version
	 * 
	 * @return the normalized type
	 */
	public GroundType addHill(boolean failAlreadyHill, boolean failNoHill) {
		return switch (this) {
		case SAND -> SAND_HILL;
		case GRASS -> GRASS_HILL;
		case FOREST -> FOREST_HILL;
		case SWAMP -> SWAMP_HILL;
		case FOREST_HILL, GRASS_HILL, SAND_HILL, SWAMP_HILL -> {
			if (failAlreadyHill) throw new IllegalStateException(Messages.format(NOT_ADD_HILLS, toString()));
			yield this;
		}
		case MOUNTAIN, NOT_EXPLORED, WATER_DEEP, WATER_NORMAL -> {
			if (failNoHill) throw new IllegalStateException(Messages.format(NOT_ADD_HILLS, toString()));
			yield this;
		}
		};
	}
	
	/**
	 * returns a string describing this ground type<br>
	 * the returned string is already localized
	 */
	@Override
	public String toString() {
		return switch (this) {
		case NOT_EXPLORED -> STR_NOT_EXPLORED;
		case WATER_DEEP -> STR_WATER_DEEP;
		case WATER_NORMAL -> STR_WATER;
		case SAND -> STR_SAND;
		case SAND_HILL -> STR_SAND_HILL;
		case GRASS -> STR_GRASS;
		case GRASS_HILL -> STR_GRASS_HILL;
		case FOREST -> STR_FOREST;
		case FOREST_HILL -> STR_FOREST_HILL;
		case SWAMP -> STR_SWAMP;
		case SWAMP_HILL -> STR_SWAMP_HILL;
		case MOUNTAIN -> STR_MOUNTAIN;
		};
	}
	
}
