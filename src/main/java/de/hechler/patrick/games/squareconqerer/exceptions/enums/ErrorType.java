package de.hechler.patrick.games.squareconqerer.exceptions.enums;


public enum ErrorType {
	BLOCKED_WAY(0xDD41CD00),
	INVALID_TURN(0x6E826655),
	UNKNOWN(0x9922EA84)
	;
	
	public final int identifier;
	
	
	private ErrorType(int identifier) {
		this.identifier = identifier;
	}
	
}
