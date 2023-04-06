package de.hechler.patrick.games.squareconqerer.world.resource;

public enum ProducableResourceType implements Resource {
	
	GOLD, IRON, STEEL, WOOD, STONE, GLASS
	
	;
	
	public static final int NUMBER = 0x461D8706;
	
	private static final ProducableResourceType[] VALS = values();
	
	public static ProducableResourceType of(int oridinal) {
		return VALS[oridinal];
	}
	
	public static int count() {
		return VALS.length;
	}
	
}
