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
 * this interface describes code that can be executed and sometimes (or always/never) throws a <code>T</code> (or {@link RuntimeException})
 * 
 * @author Patrick Hechler
 * @param <T> the {@link Throwable} which can be thrown when executing the {@link Executable}
 */
@FunctionalInterface
public interface Executable<T extends Throwable> {
	
	/**
	 * executes this executable, which may result in an error of type <code>T</code> (or {@link RuntimeException})
	 * 
	 * @throws T sometimes
	 */
	void execute() throws T;
	
}
