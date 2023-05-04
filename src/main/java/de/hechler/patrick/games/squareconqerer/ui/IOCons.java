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

import java.io.PrintStream;
import java.util.Scanner;

/**
 * this implementation of the {@link Cons} interface delegates to a {@link Scanner} and {@link PrintStream}
 * 
 * @author Patrick Hechler
 */
public class IOCons implements Cons {
	
	private final Scanner     in;
	private final PrintStream out;
	
	/**
	 * create a new {@link IOCons} with the given {@link Scanner} and {@link PrintStream}
	 * @param in the {@link Scanner} of the IO-cons
	 * @param out the {@link PrintStream} of the IO-cons
	 */
	public IOCons(Scanner in, PrintStream out) { this.in = in; this.out = out; }
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * this is the same as <code>{@link #readLine()}.{@link String#toCharArray() toCharArray()}</code>
	 */
	@Override
	public char[] readPassword() {
		return readLine().toCharArray();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * this is the same as <code>{@link #readLine(String) readLine(prompt)}.{@link String#toCharArray() toCharArray()}</code>
	 */
	@Override
	public char[] readPassword(String prompt) {
		return readLine(prompt).toCharArray();
	}
	
	/** {@inheritDoc} */
	@Override
	public String readLine() {
		return this.in.nextLine();
	}
	
	/** {@inheritDoc} */
	@Override
	public String readLine(String prompt) {
		this.out.print(prompt);
		return this.in.nextLine();
	}
	
	/** {@inheritDoc} */
	@Override
	public void writeLine(String line) {
		this.out.println(line);
	}
	
}
