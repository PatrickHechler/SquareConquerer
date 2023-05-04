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
package de.hechler.patrick.games.squareconqerer.ui;

/**
 * this interface is used by the {@link SquareConquererCUI} to communicate with the user
 * 
 * @author Patrick Hechler
 */
public interface Cons {
	
	/**
	 * reads a password (if possible without echo)
	 * 
	 * @return the password wich was read
	 */
	char[] readPassword();
	
	/**
	 * reads a password (if possible without echo) after prompting
	 * 
	 * @param prompt the prompt to print before reading the password
	 * 
	 * @return the password wich was read
	 */
	char[] readPassword(String prompt);
	
	/**
	 * reads a line
	 * 
	 * @return the line wich was read
	 */
	String readLine();
	
	/**
	 * reads a line after prompting
	 * 
	 * @param prompt the prompt to print before reading the line
	 * 
	 * @return the line wich was read
	 */
	String readLine(String prompt);
	
	/**
	 * writes the given line
	 * 
	 * @param line the line to print
	 */
	void writeLine(String line);
	
	/**
	 * writes the given lines
	 * <p>
	 * passing an {@link String#isEmpty() empty} string as argument will result in nothing
	 * 
	 * @param lines the lines to write
	 */
	default void writeLines(String lines) {
		if (lines.indexOf('\n') != -1 || lines.indexOf('\r') != -1) lines.lines().sequential().forEach(this::writeLine);
		else if (!lines.isEmpty()) writeLine(lines);
	}
	
}
