package de.hechler.patrick.games.squareconqerer.world.enums;


public enum Direction {
	
	X_ADD(1, 0), X_DEC(-1, 0), Y_ADD(0, 1), Y_DEC(0, -1);
	
	private static final Direction[] VALS = values();
	
	public static Direction of(int ordinal) {
		return VALS[ordinal];
	}
	
	public static int count() {
		return VALS.length;
	}
	
	public final int xadd;
	public final int yadd;
	
	private Direction(int xadd, int yadd) { this.xadd = xadd; this.yadd = yadd; }
	
}