//This file is part of the Square Conquerer Project
//DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//Copyright (C) 2023  Patrick Hechler
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published
//by the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with this program.  If not, see <https://www.gnu.org/licenses/>.
package de.hechler.patrick.games.sc.error;

import java.util.function.Function;

/**
 * this class is used to tell why something went wrong
 * 
 * @author Patrick Hechler
 */
public enum ErrorType {
	/**
	 * a unit could not move because the was is blocked
	 */
	BLOCKED_WAY(0xDD41CD00),
	/**
	 * a turn was invalid
	 */
	INVALID_TURN(0x6E826655),
	/**
	 * the game did not start yet
	 */
	NOT_STARTED(0x70098916),
	/**
	 * some unknown/unspecified reason
	 */
	UNKNOWN(0x9922EA84);
	
	private static final String STR_UNKNOWN      = "unknown";
	private static final String STR_NOT_STARTED  = "not yet started";
	private static final String STR_INVALID_TURN = "invalid turn";
	private static final String STR_BLOCKED_WAY  = "blocked way";
	
	/**
	 * the identifier used to send this error type
	 */
	public final int identifier;
	
	
	private ErrorType(int identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * returns the {@link ErrorType} with the given identifier
	 * 
	 * @param <T>        the error thrown when an illegal identifier was passed
	 * @param identifier the identifier
	 * @param err        the error function
	 * @return the {@link ErrorType} with the given identifier
	 * @throws T when an illegal identifier was passed
	 */
	public static <T extends Throwable> ErrorType ofIdentifier(int identifier, Function<int[], T> err) throws T {
		return switch (identifier) {
		case ErrorIdentifiers.BLOCKED_WAY -> BLOCKED_WAY;
		case ErrorIdentifiers.INVALID_TURN -> INVALID_TURN;
		case ErrorIdentifiers.NOT_STARTED -> NOT_STARTED;
		case ErrorIdentifiers.UNKNOWN -> UNKNOWN;
		default ->
			throw err.apply(new int[] { ErrorIdentifiers.BLOCKED_WAY, ErrorIdentifiers.INVALID_TURN, ErrorIdentifiers.NOT_STARTED, ErrorIdentifiers.UNKNOWN, });
		};
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return switch (this) {
		case BLOCKED_WAY -> STR_BLOCKED_WAY;
		case INVALID_TURN -> STR_INVALID_TURN;
		case NOT_STARTED -> STR_NOT_STARTED;
		case UNKNOWN -> STR_UNKNOWN;
		};
	}
	
}

class ErrorIdentifiers {
	
	static final int BLOCKED_WAY  = 0xDD41CD00;
	static final int INVALID_TURN = 0x6E826655;
	static final int NOT_STARTED  = 0x70098916;
	static final int UNKNOWN      = 0x9922EA84;
	
	private ErrorIdentifiers() {}
	
}
