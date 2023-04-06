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
