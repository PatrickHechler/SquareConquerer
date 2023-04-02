package de.hechler.patrick.games.squareconqerer.world.enums;

import java.awt.image.BufferedImage;

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
	
	private static TileType[] VALS = values();
	
	public static TileType of(int oridinal) {
		return VALS[oridinal];
	}
	
	public static int count() {
		return VALS.length;
	}
	
	volatile BufferedImage resource;
	volatile boolean       resolution;
	
	@Override public BufferedImage resource() { return resource; }
	
	@Override public void resource(BufferedImage nval) { this.resource = nval; }
	
	@Override public boolean resolution() { return resolution; }
	
	@Override public void resolution(boolean nval) { this.resolution = nval; }
	
	@Override public boolean multipleResolutions() { return true; }
	
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
		default -> false;
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
		default -> true;
		};
	}
	
	/**
	 * returns <code>true</code> if this is a flat tile
	 * <p>
	 * a flat tile is a tile, which is <b>no</b> {@link #isHill() hill}, <b>no</b>
	 * {@link #isMountain() mountain} and <b>no</b> {@link #isWater() water}
	 * 
	 * @return <code>true</code> if this is a flat tile
	 */
	public boolean isFlat() {
		return switch (this) {
		case SAND, GRASS, FOREST, SWAMP -> true;
		default -> false;
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
		default -> false;
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
	 * a not-flat tile is a tile, which is a {@link #isHill() hill} or
	 * a {@link #isMountain() mountain} tile
	 * <p>
	 * note that {@link #isWater() water} tiles are <b>no</b> {@link #isFlat() flat}
	 * tiles and also <b>no</b> {@link #isNotFlat() not-flat}
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
		default -> false;
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
		default -> false;
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
		default -> false;
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
		default -> false;
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
