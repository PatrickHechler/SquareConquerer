package de.hechler.patrick.games.squareconqerer.world.enums;

import java.awt.image.BufferedImage;

public enum ResourceType {
	
	NONE,
	
	GOLD,
	
	IRON,
	
	COAL,
	
	;
	
	private static final ResourceType[] VALS = values();
	
	public static ResourceType of(int oridinal) {
		return VALS[oridinal];
	}
	
	public static int count() {
		return VALS.length;
	}
	
	volatile BufferedImage resource;
	volatile boolean       resolution;
	
}
