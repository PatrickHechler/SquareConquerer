package de.hechler.patrick.games.squareconqerer.enums;

public enum Direction {
	
	xup, xdown,
	yup, ydown
	;
	public static Direction forName(String str) {
		switch(str.toLowerCase()) {
			case "xup":
				return xup;
			case "xdown":
				return xdown;
			case "yup":
				return yup;
			case "ydown":
				return ydown;
			default:
				throw new IllegalArgumentException("unknown Direction: '"+str+"'" );
		}
	}
}
