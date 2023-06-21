package de.hechler.patrick.games.sc.turn;


public enum Direction {
	
	X_ADD(1, 0), Y_ADD(0, 1), X_SUB(-1, 0), Y_SUB(0, -1);
	
	public final int xadd;
	public final int yadd;
	
	private Direction(int xadd, int yadd) { this.xadd = xadd; this.yadd = yadd; }
	
	public static Direction of(int ordinal) {
		switch (ordinal) {
		case 0:
			return X_ADD;
		case 1:
			return Y_ADD;
		case 2:
			return X_SUB;
		case 3:
			return Y_SUB;
		}
		throw new IllegalArgumentException("there is no Direction with the ordinal " + ordinal);
	}
	
}
