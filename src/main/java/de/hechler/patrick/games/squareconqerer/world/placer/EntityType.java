package de.hechler.patrick.games.squareconqerer.world.placer;


public enum EntityType {
	
	STORE_BUILD, CARRIER
	
	;
	
	private static final EntityType[] VALS = values();
	
	public static int count() {
		return VALS.length;
	}
	
	public static EntityType of(int ordinal) {
		return VALS[ordinal];
	}
	
}
