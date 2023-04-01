package de.hechler.patrick.games.squareconqerer.world.enums;

import java.awt.image.BufferedImage;

import de.hechler.patrick.games.squareconqerer.world.interfaces.Resource;

public enum ProducableResourceType implements ImageableEnum, Resource {
	
	GOLD, IRON, STEEL, WOOD, STONE, GLASS
	
	;
	
	private static final ProducableResourceType[] VALS = values();
	
	public static ProducableResourceType of(int oridinal) {
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
	
}
