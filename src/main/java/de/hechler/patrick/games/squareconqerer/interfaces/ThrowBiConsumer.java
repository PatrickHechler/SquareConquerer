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
package de.hechler.patrick.games.squareconqerer.interfaces;

/**
 * this interface represents an operation which accepts two values of type <code>A</code> and <code>B</code><br>
 * the operation is allowed to throw an error of type <code>E</code>
 * 
 * @author Patrick Hechler
 * @param <A> the first value type
 * @param <B> the second value type
 * @param <E> the error type which can be thrown
 */
@FunctionalInterface
public interface ThrowBiConsumer<A, B, E extends Throwable> {
	
	/**
	 * accepts the two values
	 * <p>
	 * this operation may throw an error (of type <code>E</code>)
	 * 
	 * @param a the first value
	 * @param b the second value
	 * @throws E the error which can be thrown
	 */
	void accept(A a, B b) throws E;
	
}
