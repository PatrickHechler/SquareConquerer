package de.hechler.patrick.games.squareconqerer.world.enums;

import java.awt.image.BufferedImage;

import de.hechler.patrick.games.squareconqerer.world.interfaces.Resource;

public enum OreResourceType implements ImageableEnum, Resource {
	
	NONE,
	
	GOLD_ORE,
	
	IRON_ORE,
	
	COAL_ORE,
	
	;
	
	private static final OreResourceType[] VALS = values();
	
	public static OreResourceType of(int oridinal) {
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
