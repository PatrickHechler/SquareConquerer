package de.hechler.patrick.sc.enums;


public enum Direction {
	
	up, down,
	
	rigth, left
	
	;
	
	public Direction reverse() {
		switch (this) {
		case up:
			return down;
		case down:
			return up;
		case rigth:
			return left;
		case left:
			return rigth;
		}
		throw new RuntimeException("unknown direction: " + this);
	}
	
}
